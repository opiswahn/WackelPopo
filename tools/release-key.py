"""Leitet den Release-Signaturschlüssel deterministisch aus einem Passwort ab.

Gleiches Passwort -> gleicher Schlüssel und gleiches Zertifikat, deshalb muss der Schlüssel
nirgends gespeichert werden. In GitHub liegt nur das Passwort als Secret SIGNING_PASSPHRASE.

Aufruf: SIGNING_PASSPHRASE=... python3 tools/release-key.py <key-dir>
"""
import datetime
import hashlib
import os
import sys

from cryptography import x509
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.x509.oid import NameOID


class Drbg:
    def __init__(self, seed):
        self.seed, self.counter = seed, 0

    def bits(self, n):
        out = b""
        while len(out) * 8 < n:
            out += hashlib.sha256(self.seed + self.counter.to_bytes(8, "big")).digest()
            self.counter += 1
        return int.from_bytes(out, "big") >> (len(out) * 8 - n)


def is_probable_prime(n, rng):
    small = [2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37]
    if n < 2:
        return False
    for p in small:
        if n % p == 0:
            return n == p
    d, s = n - 1, 0
    while d % 2 == 0:
        d, s = d // 2, s + 1
    for _ in range(40):
        a = 2 + rng.bits(n.bit_length() - 1) % (n - 3)
        x = pow(a, d, n)
        if x in (1, n - 1):
            continue
        for _ in range(s - 1):
            x = pow(x, 2, n)
            if x == n - 1:
                break
        else:
            return False
    return True


def prime(rng, bits, e):
    while True:
        p = rng.bits(bits) | (3 << (bits - 2)) | 1
        if p % e != 1 and is_probable_prime(p, rng):
            return p


def derive(passphrase):
    seed = hashlib.pbkdf2_hmac("sha256", passphrase.encode(), b"wackel-popo-release", 200_000)
    rng, e = Drbg(seed), 65537
    p = prime(rng, 1024, e)
    q = prime(rng, 1024, e)
    while q == p:
        q = prime(rng, 1024, e)
    if p < q:
        p, q = q, p
    d = pow(e, -1, (p - 1) * (q - 1))
    pub = rsa.RSAPublicNumbers(e, p * q)
    key = rsa.RSAPrivateNumbers(p, q, d, d % (p - 1), d % (q - 1), pow(q, -1, p), pub).private_key()
    subject = x509.Name([x509.NameAttribute(NameOID.COMMON_NAME, "Wackel-Popo")])
    start = datetime.datetime(2026, 1, 1)
    # RSA-PKCS1v15 signiert deterministisch, das Zertifikat ist also jedes Mal byte-gleich.
    cert = (x509.CertificateBuilder().subject_name(subject).issuer_name(subject)
            .public_key(key.public_key()).serial_number(rng.bits(63) + 1)
            .not_valid_before(start).not_valid_after(start + datetime.timedelta(days=365 * 40))
            .sign(key, hashes.SHA256()))
    return key, cert.public_bytes(serialization.Encoding.DER)


if __name__ == "__main__":
    phrase = os.environ.get("SIGNING_PASSPHRASE", "")
    if len(phrase) < 8:
        sys.exit("SIGNING_PASSPHRASE fehlt oder ist kürzer als 8 Zeichen")
    key, der = derive(phrase)
    out = sys.argv[1]
    os.makedirs(out, exist_ok=True)
    # mkapk.py erwartet diese Dateinamen.
    with open(os.path.join(out, "debug-key.pem"), "wb") as f:
        f.write(key.private_bytes(serialization.Encoding.PEM, serialization.PrivateFormat.PKCS8,
                                  serialization.NoEncryption()))
    with open(os.path.join(out, "debug-cert.der"), "wb") as f:
        f.write(der)
    print("Fingerabdruck:", hashlib.sha256(der).hexdigest())
