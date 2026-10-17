"""Turns a portrait photo into the South-Park-style "Kanadier" head used by the app.

Cuts the head out with an ellipse, flattens it to a few colours with dark edges (cut-out look),
gives it a thick black outline and splits it at the mouth into a top and a bottom half, so the
top half can flap up like a lid when he talks.

    python3 tools/kanadier.py foto.jpg CX CY RX RY MOUTH_Y app/src/main/assets
"""
import sys

from PIL import Image, ImageChops, ImageDraw, ImageEnhance, ImageFilter, ImageOps

src, cx, cy, rx, ry, mouth, out = sys.argv[1], *map(int, sys.argv[2:7]), sys.argv[7]
photo = Image.open(src).convert("RGB")
box = (cx - rx, cy - ry, cx + rx, cy + ry)
head = photo.crop(box)
w, h = head.size
scale = 360 / w
head = head.resize((360, int(h * scale)), Image.LANCZOS)
w, h = head.size
mouth_y = int((mouth - box[1]) * scale)

# Flat colours plus dark edges: the cut-out cartoon look.
# The photo is lit by red Christmas lights: pull the red cast back and stretch the contrast.
r, g, b = head.split()
head = Image.merge("RGB", (r.point(lambda v: int(v * 0.88)), g, b.point(lambda v: min(255, int(v * 1.08)))))
head = ImageOps.autocontrast(head, cutoff=1)
smooth = head.filter(ImageFilter.MedianFilter(5)).filter(ImageFilter.MedianFilter(5))
flat = ImageEnhance.Color(smooth).enhance(1.15).quantize(colors=12, method=Image.Quantize.MEDIANCUT).convert("RGB")
# Only strong edges of the smoothed picture, cleaned of speckles, become ink lines.
edges = smooth.convert("L").filter(ImageFilter.FIND_EDGES).point(lambda v: 255 if v > 45 else 0)
edges = edges.filter(ImageFilter.MedianFilter(3)).filter(ImageFilter.MaxFilter(3))
edges = ImageOps.invert(edges)
cartoon = ImageChops.multiply(flat, Image.merge("RGB", [edges] * 3))

mask = Image.new("L", (w, h), 0)
ImageDraw.Draw(mask).ellipse((4, 4, w - 5, h - 5), fill=255)
outline = Image.new("L", (w, h), 0)
ImageDraw.Draw(outline).ellipse((0, 0, w - 1, h - 1), fill=255)
result = Image.new("RGBA", (w, h), (0, 0, 0, 0))
result.paste((20, 14, 12, 255), (0, 0), outline)
result.paste(cartoon, (0, 0), mask)

# Mouth line, so the split reads as a mouth when the top flaps open.
inner = Image.new("RGBA", (w, h), (0, 0, 0, 0))
d = ImageDraw.Draw(inner)
d.rectangle((0, mouth_y - 3, w, mouth_y + 3), fill=(20, 14, 12, 255))
result = Image.alpha_composite(result, Image.composite(inner, Image.new("RGBA", (w, h)), mask))

result.crop((0, 0, w, mouth_y + 3)).save(f"{out}/kanadier_oben.png", optimize=True)
result.crop((0, mouth_y - 3, w, h)).save(f"{out}/kanadier_unten.png", optimize=True)
print("head", w, h, "mouth at", mouth_y)
