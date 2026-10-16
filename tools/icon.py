import sys
from PIL import Image, ImageDraw
S = 768
im = Image.new("RGBA", (S, S), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
ink = (43, 27, 23, 255); skin = (255, 193, 161, 255)
d.ellipse([16, 16, S - 16, S - 16], fill=(255, 213, 79, 255), outline=ink, width=24)
r = 175; cy = 400; lw = 26
for cx in (S / 2 - r * 0.86, S / 2 + r * 0.86):
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=skin)
left = Image.new("L", (S, S), 0); ImageDraw.Draw(left).rectangle([0, 0, S // 2, S], fill=255)
right = Image.new("L", (S, S), 0); ImageDraw.Draw(right).rectangle([S // 2, 0, S, S], fill=255)
for cx, mask in ((S / 2 - r * 0.86, left), (S / 2 + r * 0.86, right)):
    o = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    ImageDraw.Draw(o).ellipse([cx - r, cy - r, cx + r, cy + r], outline=ink, width=lw)
    im.paste(o, (0, 0), Image.composite(o.getchannel("A"), Image.new("L", (S, S), 0), mask))
h = (r * r - (r * 0.86) ** 2) ** 0.5
d.line([S / 2, cy - h, S / 2, cy + h], fill=ink, width=lw)
for cx in (S / 2 - r * 0.86 - 40, S / 2 + r * 0.86 + 40):
    d.ellipse([cx - 45, cy - 110, cx + 45, cy - 70], fill=(255, 255, 255, 220))
for size, out in [(int(a), b) for a, b in zip(sys.argv[1::2], sys.argv[2::2])]:
    im.resize((size, size), Image.LANCZOS).save(out)
