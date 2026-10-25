"""生成太阳能板 GUI 底图（AE2 风格，256x256 画布）。

注意：Minecraft 的 GuiGraphics.blit 默认按 256x256 取 UV，
所以贴图必须是 256x256 画布、面板画在左上角 176x112 区域，否则会被放大裁切。

用法:
    python tools/make_gui.py
输出:
    src/main/resources/assets/sunflare/textures/gui/solar_panel.png
"""
from __future__ import annotations

import os

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "sunflare", "textures", "gui", "solar_panel.png")

CANVAS = 256
W, H = 176, 112

BG = (198, 198, 198)
BORDER = (85, 85, 85)
SEPARATOR = (168, 168, 168)
INSET = (139, 139, 139)
INSET_DARK = (55, 55, 55)
INSET_LIGHT = (255, 255, 255)


def main() -> None:
    img = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # 面板底色 + 1px 深色描边（AE2 的扁平风格）
    d.rectangle([0, 0, W - 1, H - 1], fill=BG + (255,), outline=BORDER + (255,))

    # 标题下的分隔线
    d.line([(1, 17), (W - 2, 17)], fill=SEPARATOR + (255,))
    d.line([(1, 18), (W - 2, 18)], fill=INSET_LIGHT + (255,))

    # 内凹读数区：上左深、下右亮
    d.rectangle([8, 22, 167, 58], fill=INSET + (255,))
    d.line([(8, 22), (167, 22)], fill=INSET_DARK + (255,))
    d.line([(8, 22), (8, 58)], fill=INSET_DARK + (255,))
    d.line([(9, 58), (167, 58)], fill=INSET_LIGHT + (255,))
    d.line([(167, 23), (167, 58)], fill=INSET_LIGHT + (255,))

    # 下部信息行分隔线
    for y in (66, 80, 94):
        d.line([(8, y - 1), (167, y - 1)], fill=SEPARATOR + (255,))

    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    img.save(OUT)
    print(f"gui: canvas {CANVAS}x{CANVAS}, panel {W}x{H} -> {OUT}")


if __name__ == "__main__":
    main()
