"""Packs classes.dex + icon into a v2-signed APK without the Android SDK build tools.

Writes the binary AndroidManifest.xml and resources.arsc by hand, zip-aligns
uncompressed entries and adds an APK Signature Scheme v2 block.
"""
import hashlib
import os
import struct
import sys
import zipfile

from cryptography import x509
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import padding, rsa
from cryptography.x509.oid import NameOID
import datetime

PKG = "de.kgoh.wackelpopo"
APP_NAME = "Wackel-Popo"
MIN_SDK, TARGET_SDK = 24, 35
# Version of the build; the in-app updater compares VERSION_CODE with version.txt on GitHub.
VERSION_CODE = int(os.environ.get("VERSION_CODE", "1"))
VERSION_NAME = os.environ.get("VERSION_NAME", "1.0")
ICON_PATH = "res/mipmap-xxxhdpi-v4/ic_launcher.png"
ID_ICON, ID_APP_NAME = 0x7F010000, 0x7F020000
THEME_MATERIAL_LIGHT_NOACTIONBAR = 16974401

ATTR = {
    "theme": 16842752, "label": 16842753, "icon": 16842754, "name": 16842755,
    "authorities": 16842776, "grantUriPermissions": 16842779, "exported": 16842768, "launchMode": 16842781, "screenOrientation": 16842782, "minSdkVersion": 16843276,
    "versionCode": 16843291, "versionName": 16843292, "targetSdkVersion": 16843376,
    "allowBackup": 16843392,
}
ANDROID_NS = "http://schemas.android.com/apk/res/android"

T_REF, T_STRING, T_INT_DEC, T_BOOL = 0x01, 0x03, 0x10, 0x12


def pad4(b):
    return b + b"\0" * (-len(b) % 4)


def string_pool(strings, utf8):
    data = b""
    offsets = []
    for s in strings:
        offsets.append(len(data))
        if utf8:
            raw = s.encode("utf-8")
            n = len(s)
            data += (bytes([n]) if n < 0x80 else struct.pack(">H", n | 0x8000))
            data += (bytes([len(raw)]) if len(raw) < 0x80 else struct.pack(">H", len(raw) | 0x8000))
            data += raw + b"\0"
        else:
            data += struct.pack("<H", len(s)) + s.encode("utf-16-le") + b"\0\0"
    data = pad4(data)
    header_size = 28
    strings_start = header_size + 4 * len(strings)
    body = b"".join(struct.pack("<I", o) for o in offsets) + data
    size = header_size + len(body)
    flags = 0x100 if utf8 else 0
    return struct.pack("<HHIIIIII", 0x0001, header_size, size, len(strings), 0, flags, strings_start, 0) + body


# ---------- binary XML (AndroidManifest.xml) ----------

def manifest_tree():
    A = lambda name, kind, value: (name, kind, value)
    return ("manifest", [
        (None, "package", "str", PKG),
        ("a", "versionCode", "int", VERSION_CODE),
        ("a", "versionName", "str", VERSION_NAME),
    ], [
        ("uses-sdk", [("a", "minSdkVersion", "int", MIN_SDK), ("a", "targetSdkVersion", "int", TARGET_SDK)], []),
        ("uses-permission", [("a", "name", "str", "android.permission.VIBRATE")], []),
        ("uses-permission", [("a", "name", "str", "android.permission.INTERNET")], []),
        ("uses-permission", [("a", "name", "str", "android.permission.REQUEST_INSTALL_PACKAGES")], []),
        ("application", [
            ("a", "theme", "ref", THEME_MATERIAL_LIGHT_NOACTIONBAR),
            ("a", "label", "ref", ID_APP_NAME),
            ("a", "icon", "ref", ID_ICON),
            ("a", "allowBackup", "bool", True),
        ], [
            ("activity", [
                ("a", "name", "str", PKG + ".MainActivity"),
                ("a", "exported", "bool", True),
                ("a", "launchMode", "int", 1),
                ("a", "screenOrientation", "int", 1),
            ], [
                ("intent-filter", [], [
                    ("action", [("a", "name", "str", "android.intent.action.MAIN")], []),
                    ("category", [("a", "name", "str", "android.intent.category.LAUNCHER")], []),
                ]),
            ]),
            ("provider", [
                ("a", "name", "str", PKG + ".ShareProvider"),
                ("a", "authorities", "str", PKG + ".share"),
                ("a", "exported", "bool", False),
                ("a", "grantUriPermissions", "bool", True),
            ], []),
        ]),
    ])


def build_axml():
    tree = manifest_tree()
    attr_names = []
    others = []

    def collect(node):
        tag, attrs, children = node
        for ns, name, kind, value in attrs:
            if ns and name not in attr_names:
                attr_names.append(name)
            elif not ns and name not in others:
                others.append(name)
            if kind == "str" and value not in others:
                others.append(value)
        if tag not in others:
            others.append(tag)
        for c in children:
            collect(c)

    collect(tree)
    attr_names.sort(key=lambda n: ATTR[n])
    for extra in ("android", ANDROID_NS):
        others.append(extra)
    strings = attr_names + [s for s in others if s not in attr_names]
    idx = {s: i for i, s in enumerate(strings)}

    chunks = [string_pool(strings, utf8=False)]
    resmap = b"".join(struct.pack("<I", ATTR[n]) for n in attr_names)
    chunks.append(struct.pack("<HHI", 0x0180, 8, 8 + len(resmap)) + resmap)

    def node_header(t, size):
        return struct.pack("<HHIII", t, 16, size, 1, 0xFFFFFFFF)

    ns_body = struct.pack("<II", idx["android"], idx[ANDROID_NS])
    chunks.append(node_header(0x0100, 24) + ns_body)

    def emit(node):
        tag, attrs, children = node
        attrs = sorted(attrs, key=lambda a: ATTR.get(a[1], 0) if a[0] else -1)
        abytes = b""
        for ns, name, kind, value in attrs:
            ns_i = idx[ANDROID_NS] if ns else 0xFFFFFFFF
            if kind == "str":
                raw, typ, data = idx[value], T_STRING, idx[value]
            elif kind == "int":
                raw, typ, data = 0xFFFFFFFF, T_INT_DEC, value
            elif kind == "bool":
                raw, typ, data = 0xFFFFFFFF, T_BOOL, 0xFFFFFFFF if value else 0
            else:
                raw, typ, data = 0xFFFFFFFF, T_REF, value
            abytes += struct.pack("<IIIHBBI", ns_i, idx[name], raw, 8, 0, typ, data)
        ext = struct.pack("<IIHHHHHH", 0xFFFFFFFF, idx[tag], 20, 20, len(attrs), 0, 0, 0)
        chunks.append(node_header(0x0102, 16 + len(ext) + len(abytes)) + ext + abytes)
        for c in children:
            emit(c)
        chunks.append(node_header(0x0103, 24) + struct.pack("<II", 0xFFFFFFFF, idx[tag]))

    emit(tree)
    chunks.append(node_header(0x0101, 24) + ns_body)
    body = b"".join(chunks)
    return struct.pack("<HHI", 0x0003, 8, 8 + len(body)) + body


# ---------- resources.arsc ----------

def config(density=0, sdk=0):
    # 52-byte ResTable_config with only density/sdkVersion set.
    c = struct.pack("<I", 52) + b"\0" * 48
    c = bytearray(c)
    struct.pack_into("<H", c, 14, density)
    struct.pack_into("<H", c, 24, sdk)
    return bytes(c)


def type_chunk(type_id, cfg, entries):
    # entries: list of (key_index, value_type, value_data)
    offsets = b""
    data = b""
    for key, vt, vd in entries:
        offsets += struct.pack("<I", len(data))
        data += struct.pack("<HHI", 8, 0, key) + struct.pack("<HBBI", 8, 0, vt, vd)
    header_size = 20 + len(cfg)
    entries_start = header_size + len(offsets)
    size = entries_start + len(data)
    return struct.pack("<HHIBBHII", 0x0201, header_size, size, type_id, 0, 0, len(entries), entries_start) + cfg + offsets + data


def type_spec(type_id, flags):
    body = b"".join(struct.pack("<I", f) for f in flags)
    return struct.pack("<HHIBBHI", 0x0202, 16, 16 + len(body), type_id, 0, 0, len(flags)) + body


def build_arsc():
    values = string_pool([ICON_PATH, APP_NAME], utf8=True)
    type_strings = string_pool(["mipmap", "string"], utf8=False)
    key_strings = string_pool(["ic_launcher", "app_name"], utf8=False)
    types = (
        type_spec(1, [0x0100])
        + type_chunk(1, config(density=640, sdk=4), [(0, T_STRING, 0)])
        + type_spec(2, [0])
        + type_chunk(2, config(), [(1, T_STRING, 1)])
    )
    header_size = 288
    name = PKG.encode("utf-16-le").ljust(256, b"\0")
    type_off = header_size
    key_off = type_off + len(type_strings)
    pkg_size = header_size + len(type_strings) + len(key_strings) + len(types)
    pkg = struct.pack("<HHII", 0x0200, header_size, pkg_size, 0x7F) + name
    pkg += struct.pack("<IIIII", type_off, 2, key_off, 2, 0)
    pkg += type_strings + key_strings + types
    body = values + pkg
    return struct.pack("<HHII", 0x0002, 12, 12 + len(body), 1) + body


# ---------- zip + v2 signature ----------

def write_zip(path, files):
    """files: list of (name, bytes, stored). Stored entries are 4-byte aligned."""
    with zipfile.ZipFile(path, "w") as z:
        for name, data, stored in files:
            info = zipfile.ZipInfo(name, date_time=(2026, 1, 1, 0, 0, 0))
            info.compress_type = zipfile.ZIP_STORED if stored else zipfile.ZIP_DEFLATED
            if stored:
                offset = z.fp.tell() + 30 + len(name.encode())
                info.extra = b"\0" * (-offset % 4)
            z.writestr(info, data)


def load_key(key_dir):
    os.makedirs(key_dir, exist_ok=True)
    kp, cp = os.path.join(key_dir, "debug-key.pem"), os.path.join(key_dir, "debug-cert.der")
    if os.path.exists(kp):
        key = serialization.load_pem_private_key(open(kp, "rb").read(), None)
        return key, open(cp, "rb").read()
    key = rsa.generate_private_key(public_exponent=65537, key_size=2048)
    subject = x509.Name([x509.NameAttribute(NameOID.COMMON_NAME, "Wackel-Popo Debug")])
    now = datetime.datetime(2026, 1, 1)
    cert = (x509.CertificateBuilder().subject_name(subject).issuer_name(subject)
            .public_key(key.public_key()).serial_number(x509.random_serial_number())
            .not_valid_before(now).not_valid_after(now + datetime.timedelta(days=365 * 30))
            .sign(key, hashes.SHA256()))
    open(kp, "wb").write(key.private_bytes(serialization.Encoding.PEM, serialization.PrivateFormat.PKCS8,
                                           serialization.NoEncryption()))
    der = cert.public_bytes(serialization.Encoding.DER)
    open(cp, "wb").write(der)
    return key, der


def lp(b):
    return struct.pack("<I", len(b)) + b


def chunked_digest(sections):
    digests = []
    for sec in sections:
        for i in range(0, len(sec), 1 << 20):
            chunk = sec[i:i + (1 << 20)]
            digests.append(hashlib.sha256(b"\xa5" + struct.pack("<I", len(chunk)) + chunk).digest())
    return hashlib.sha256(b"\x5a" + struct.pack("<I", len(digests)) + b"".join(digests)).digest()


def sign_v2(path, key, cert_der):
    apk = open(path, "rb").read()
    eocd = apk.rfind(b"PK\x05\x06")
    cd_size, cd_off = struct.unpack_from("<II", apk, eocd + 12)
    entries, cd, eocd_bytes = apk[:cd_off], apk[cd_off:eocd], apk[eocd:]
    # Digest uses the EOCD as if the central directory started right after the entries.
    eocd_for_digest = bytearray(eocd_bytes)
    struct.pack_into("<I", eocd_for_digest, 16, cd_off)
    digest = chunked_digest([entries, cd, bytes(eocd_for_digest)])

    algo = 0x0103  # RSASSA-PKCS1-v1_5 with SHA2-256
    signed_data = lp(lp(struct.pack("<I", algo) + lp(digest))) + lp(lp(cert_der)) + lp(b"")
    signature = key.sign(signed_data, padding.PKCS1v15(), hashes.SHA256())
    pub = key.public_key().public_bytes(serialization.Encoding.DER, serialization.PublicFormat.SubjectPublicKeyInfo)
    signer = lp(signed_data) + lp(lp(struct.pack("<I", algo) + lp(signature))) + lp(pub)
    value = lp(lp(signer))

    pair = struct.pack("<Q", 4 + len(value)) + struct.pack("<I", 0x7109871A) + value
    block_size = len(pair) + 8 + 16
    block = struct.pack("<Q", block_size) + pair + struct.pack("<Q", block_size) + b"APK Sig Block 42"

    new_eocd = bytearray(eocd_bytes)
    struct.pack_into("<I", new_eocd, 16, cd_off + len(block))
    open(path, "wb").write(entries + block + cd + bytes(new_eocd))


def main():
    dex, icon, out, key_dir = sys.argv[1:5]
    files = [
        ("AndroidManifest.xml", build_axml(), False),
        ("classes.dex", open(dex, "rb").read(), False),
        ("resources.arsc", build_arsc(), True),
        (ICON_PATH, open(icon, "rb").read(), True),
    ]
    write_zip(out, files)
    key, cert = load_key(key_dir)
    sign_v2(out, key, cert)
    print("wrote", out, os.path.getsize(out), "bytes")


if __name__ == "__main__":
    main()
