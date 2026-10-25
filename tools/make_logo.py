"""生成模组 logo：128x128 RGBA 像素画。

作画在 32x32 网格上，再 ×4 最近邻放大到 128，保持硬边像素感（无插值模糊）。
输出为标准 RGBA PNG（圆角外为透明）。

用法:
    python tools/make_logo.py
输出:
    src/main/resources/logo.png
"""
from __future__ import annotations

import math
import os

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src", "main", "resources", "logo.png")

S = 32
SCALE = 4

BG = (14, 22, 38, 255)
ACCENT = (52, 118, 168, 255)
ACCENT_HI = (96, 190, 235, 255)
RAY = (255, 209, 102, 255)
SUN_OUTER = (255, 138, 48, 255)
SUN_MID = (255, 200, 87, 255)
SUN_CORE = (255, 246, 208, 255)
GOLD = (217, 164, 65, 255)
GOLD_HI = (255, 226, 150, 255)
CELL_DARK = (14, 30, 60, 255)


def draw_icon() -> Image.Image:
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # 底板 + 四角 HUD 括线
    d.rounded_rectangle([0, 0, S - 1, S - 1], radius=5, fill=BG)
    for x, y, dx, dy in ((3, 3, 1, 1), (28, 3, -1, 1), (3, 28, 1, -1), (28, 28, -1, -1)):
        d.line([(x, y), (x + 4 * dx, y)], fill=ACCENT)
        d.line([(x, y), (x, y + 4 * dy)], fill=ACCENT)
    d.point((3, 3), fill=ACCENT_HI)
    d.point((28, 3), fill=ACCENT_HI)
    d.point((3, 28), fill=ACCENT_HI)
    d.point((28, 28), fill=ACCENT_HI)

    # 太阳与上射光芒
    cx, cy = 16, 11
    for deg in (0, 45, 90, 135, 180):
        a = math.radians(deg)
        for radius in (8, 9, 10):
            px = round(cx + math.cos(a) * radius)
            py = round(cy - math.sin(a) * radius)
            if 3 <= px <= S - 4 and 3 <= py <= S - 4:
                d.point((px, py), fill=RAY)

    d.ellipse([cx - 6, cy - 6, cx + 6, cy + 6], fill=SUN_OUTER)
    d.ellipse([cx - 4, cy - 4, cx + 4, cy + 4], fill=SUN_MID)
    d.ellipse([cx - 2, cy - 2, cx + 2, cy + 2], fill=SUN_CORE)

    # 能量母线 + 三个节点
    d.line([(6, 20), (25, 20)], fill=ACCENT)
    for x in (10, 16, 22):
        d.point((x, 20), fill=ACCENT_HI)

    # 光能板：金框 + 三片电芯
    d.rectangle([4, 22, 28, 29], fill=GOLD)
    d.rectangle([5, 23, 27, 28], fill=CELL_DARK)
    rows = [(124, 196, 255), (84, 156, 232), (54, 118, 198), (36, 88, 162), (24, 64, 128), (18, 48, 98)]
    for x0 in (5, 13, 21):
        for i, col in enumerate(rows):
            d.line([(x0, 23 + i), (x0 + 6, 23 + i)], fill=col + (255,))
        d.point((x0, 23), fill=(220, 244, 255, 255))
    d.rectangle([12, 23, 12, 28], fill=GOLD)
    d.rectangle([20, 23, 20, 28], fill=GOLD)
    d.line([(4, 22), (28, 22)], fill=GOLD_HI)

    return img


def main() -> None:
    icon = draw_icon()
    logo = icon.resize((S * SCALE, S * SCALE), Image.NEAREST)
    logo = logo.convert("RGBA")
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    logo.save(OUT, format="PNG")
    print(f"logo: {logo.size[0]}x{logo.size[1]} mode={logo.mode} -> {OUT}")


if __name__ == "__main__":
    main()
