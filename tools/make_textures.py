"""生成 Sunflare 太阳能板贴图（32x32 + 顶部动画）。

用法:
    python tools/make_textures.py

输出:
    src/main/resources/assets/sunflare/textures/block/solar_panel_t{1..6}_top.png     32x512 竖排帧序列
    src/main/resources/assets/sunflare/textures/block/solar_panel_t{1..6}_top.png.mcmeta
    src/main/resources/assets/sunflare/textures/block/solar_panel_t{1..6}_side.png
    src/main/resources/assets/sunflare/textures/block/solar_panel_t{1..6}_bottom.png
    textures_preview 预览: docs/preview-static.png
    preview_filmstrip.png     16 帧胶片图 -> docs/
    preview_animation.gif     六档动画预览 -> docs/
"""
from __future__ import annotations

import json
import math
import os

from PIL import Image, ImageDraw, ImageFont

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "sunflare", "textures", "block")
ITEM_OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "sunflare", "textures", "item")
DOCS = os.path.join(ROOT, "docs")
PREVIEW = os.path.join(DOCS, "preview-static.png")

S = 32
FRAMES = 16
FRAMETIME = 2
CELL = 8
CELL_ORIGINS = (2, 12, 22)
GUTTERS = ((10, 11), (20, 21))

# frame=框体, edge=凹槽/暗部, cell=电池片, glint=玻璃反光, glow=指示灯（仅 4-6 档）
TIERS = {
    "t1": dict(frame=(172, 178, 186), edge=(84, 88, 94), cell=(12, 32, 64), glint=(120, 188, 240)),
    "t2": dict(frame=(238, 196, 68), edge=(132, 96, 20), cell=(12, 34, 66), glint=(255, 232, 156)),
    "t3": dict(frame=(96, 228, 218), edge=(24, 124, 120), cell=(10, 32, 64), glint=(186, 255, 250)),
    "t4": dict(frame=(78, 212, 124), edge=(22, 118, 60), cell=(10, 34, 62), glint=(184, 255, 202),
               glow=(150, 255, 190)),
    "t5": dict(frame=(122, 108, 114), edge=(48, 38, 42), cell=(20, 42, 80), glint=(170, 214, 250),
               glow=(206, 160, 255)),
    "t6": dict(frame=(250, 236, 196), edge=(150, 122, 66), cell=(22, 42, 82), glint=(255, 255, 240),
               glow=(255, 250, 208)),
    "t7": dict(frame=(186, 112, 72), edge=(104, 56, 30), cell=(12, 34, 70), glint=(255, 186, 120),
               glow=(255, 204, 148)),
    "t8": dict(frame=(224, 210, 255), edge=(130, 110, 176), cell=(14, 30, 72), glint=(255, 245, 255),
               glow=(255, 240, 255)),
}


def clamp(*channels: int) -> tuple[int, int, int]:
    return tuple(max(0, min(255, c)) for c in channels)  # type: ignore[return-value]


def shade(rgb: tuple[int, int, int], factor: float) -> tuple[int, int, int]:
    return clamp(*(int(round(c * factor)) for c in rgb))


def mix(a: tuple[int, int, int], b: tuple[int, int, int], t: float) -> tuple[int, int, int]:
    t = max(0.0, min(1.0, t))
    return clamp(*(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3)))


def dither(a: tuple[int, int, int], b: tuple[int, int, int], x: int, y: int) -> tuple[int, int, int]:
    """棋盘抖动：用点阵在两个色阶之间过渡，保持像素感。"""
    return a if (x + y) % 2 == 0 else b


def is_gutter(x: int, y: int) -> bool:
    if 2 <= x <= 29 and 2 <= y <= 29:
        for lo, hi in GUTTERS:
            if lo <= x <= hi or lo <= y <= hi:
                return True
        return False
    return True


# ---------------------------------------------------------------- top


BANDS = (0.58, 0.42, 0.25, 0.09)


def draw_cell(img: Image.Image, d: ImageDraw.ImageDraw, x0: int, y0: int,
              p: dict, frame: int) -> None:
    cell, glint = p["cell"], p["glint"]
    colors = [mix(cell, glint, lv) for lv in BANDS]

    for r in range(CELL):
        base = colors[r // 2]
        for c in range(CELL):
            col = base
            # 只在每个色块的收尾行做 1px 棋盘过渡，其余保持纯色块
            if r % 2 == 1 and r // 2 + 1 < len(colors):
                if (x0 + c + y0 + r) % 2 == 0:
                    col = mix(colors[r // 2], colors[r // 2 + 1], 0.5)
            d.point((x0 + c, y0 + r), fill=col + (255,))

    # 左上高光块（玻璃反光）
    for c, r in ((1, 0), (2, 0), (1, 1)):
        d.point((x0 + c, y0 + r), fill=mix(cell, glint, 0.86) + (255,))

    # 竖向汇流条
    d.line([(x0 + 4, y0 + 2), (x0 + 4, y0 + 6)], fill=mix(cell, glint, 0.34) + (255,))

    # 横向电路走线 + 焊盘
    trace = mix(p["edge"], (198, 156, 92), 0.66)
    d.line([(x0 + 5, y0 + 6), (x0 + 7, y0 + 6)], fill=trace + (255,))
    d.point((x0 + 5, y0 + 7), fill=shade(trace, 0.72) + (255,))
    d.point((x0 + 6, y0 + 7), fill=shade(trace, 0.72) + (255,))

    # 片内右下斜面，和凹槽拉开层次
    d.line([(x0 + 7, y0 + 1), (x0 + 7, y0 + 7)], fill=shade(colors[-1], 0.82) + (255,))
    d.line([(x0 + 1, y0 + 7), (x0 + 7, y0 + 7)], fill=shade(colors[-1], 0.68) + (255,))


def draw_flow(img: Image.Image, d: ImageDraw.ImageDraw, p: dict, frame: int) -> None:
    """斜向流动的能量带：离散色阶，不做平滑插值。"""
    phase = 2.0 * math.pi * frame / FRAMES
    for y in range(3, 30):
        for x in range(3, 30):
            if is_gutter(x, y):
                continue
            v = math.sin(phase - (x + y) * 0.30)
            if v <= 0.52:
                continue
            step = 16 if v < 0.82 else 32
            cur = img.getpixel((x, y))
            d.point((x, y), fill=tuple(min(255, c + step) for c in cur[:3]) + (255,))


def draw_leds(d: ImageDraw.ImageDraw, p: dict, frame: int) -> None:
    glow = p.get("glow")
    if glow is None:
        return
    phase = 2.0 * math.pi * frame / FRAMES
    pulse = 0.5 + 0.5 * math.sin(phase)
    core = mix(p["cell"], glow, 0.50 + 0.50 * pulse)
    halo = mix(p["cell"], glow, 0.20 + 0.26 * pulse)
    for cy in CELL_ORIGINS:
        for cx in CELL_ORIGINS:
            x, y = cx + 1, cy + 1
            d.point((x + 1, y - 1), fill=halo + (255,))
            d.point((x - 1, y + 1), fill=halo + (255,))
            d.rectangle([x, y, x + 1, y + 1], fill=core + (255,))


def make_top(p: dict, frame: int) -> Image.Image:
    frame_col = p["frame"]
    img = Image.new("RGBA", (S, S), frame_col + (255,))
    d = ImageDraw.Draw(img)

    # 2px 对称外框：拼接处只会形成一条均匀的金属导轨，不会出现双倍深缝
    d.rectangle([0, 0, S - 1, S - 1], outline=shade(frame_col, 0.86) + (255,))
    d.rectangle([1, 1, S - 2, S - 2], outline=shade(frame_col, 1.10) + (255,))
    # 最外 1px 保持纯色（阵列拼接时是平直接缝），只在第 2 圈做抖动
    for x in range(S):
        d.point((x, 1), fill=dither(shade(frame_col, 1.10), shade(frame_col, 1.04), x, 1) + (255,))

    # 内凹槽
    d.rectangle([2, 2, S - 3, S - 3], fill=shade(p["edge"], 0.82) + (255,))

    for y0 in CELL_ORIGINS:
        for x0 in CELL_ORIGINS:
            draw_cell(img, d, x0, y0, p, frame)

    draw_flow(img, d, p, frame)
    draw_leds(d, p, frame)
    return img


# ---------------------------------------------------------------- side / bottom


def draw_screw(d: ImageDraw.ImageDraw, x: int, y: int, base: tuple[int, int, int]) -> None:
    """5x5 立体十字槽螺丝。"""
    d.rectangle([x, y, x + 4, y + 4], fill=shade(base, 0.62) + (255,))
    d.rectangle([x + 1, y + 1, x + 3, y + 3], fill=shade(base, 1.16) + (255,))
    d.point((x + 1, y + 1), fill=shade(base, 1.34) + (255,))
    d.line([(x + 2, y + 1), (x + 2, y + 3)], fill=shade(base, 0.52) + (255,))
    d.line([(x + 1, y + 2), (x + 3, y + 2)], fill=shade(base, 0.52) + (255,))
    d.point((x + 3, y + 3), fill=shade(base, 0.86) + (255,))


def make_side(p: dict) -> Image.Image:
    base = p["frame"]
    img = Image.new("RGBA", (S, S), base + (255,))
    d = ImageDraw.Draw(img)

    # 横向拉丝：每行亮度微变 + 抖动
    for y in range(S):
        wobble = 0.030 if y % 4 == 0 else (-0.022 if y % 4 == 2 else 0.0)
        col = shade(base, 1.0 + wobble - 0.010 * y)
        col2 = shade(col, 0.955)
        for x in range(S):
            d.point((x, y), fill=dither(col, col2, x, y) + (255,))

    # 上下收边
    d.rectangle([0, 0, S - 1, 0], fill=shade(base, 0.58) + (255,))
    d.rectangle([0, 1, S - 1, 1], fill=shade(base, 1.16) + (255,))
    d.rectangle([0, S - 2, S - 1, S - 2], fill=shade(base, 0.70) + (255,))
    d.rectangle([0, S - 1, S - 1, S - 1], fill=shade(base, 0.50) + (255,))

    # 中央能源接口：外斜面 + 深色凹陷 + 三组金属触点
    px0, py0, px1, py1 = 8, 10, 23, 21
    d.rectangle([px0, py0, px1, py1], fill=shade(base, 0.52) + (255,))
    d.line([(px0, py0), (px1, py0)], fill=shade(base, 1.20) + (255,))
    d.line([(px0, py0), (px0, py1)], fill=shade(base, 1.20) + (255,))
    d.line([(px0, py1), (px1, py1)], fill=shade(base, 0.38) + (255,))
    d.line([(px1, py0), (px1, py1)], fill=shade(base, 0.38) + (255,))
    d.rectangle([px0 + 1, py0 + 1, px1 - 1, py1 - 1], fill=shade(base, 0.22) + (255,))

    contact = mix(shade(base, 0.55), (222, 176, 104), 0.72)
    for cx in (10, 15, 20):
        d.rectangle([cx, 12, cx + 1, 19], fill=contact + (255,))
        d.point((cx, 12), fill=mix(contact, (255, 255, 255), 0.35) + (255,))
        d.point((cx + 1, 12), fill=mix(contact, (255, 255, 255), 0.20) + (255,))
        d.point((cx, 13), fill=mix(contact, (255, 255, 255), 0.12) + (255,))

    for x, y in ((3, 3), (24, 3), (3, 24), (24, 24)):
        draw_screw(d, x, y, base)
    return img


def make_bottom(p: dict) -> Image.Image:
    base = shade(p["frame"], 0.66)
    img = Image.new("RGBA", (S, S), base + (255,))
    d = ImageDraw.Draw(img)

    for y in range(S):
        col = shade(base, 1.0 - 0.010 * y)
        col2 = shade(col, 0.955)
        for x in range(S):
            d.point((x, y), fill=dither(col, col2, x, y) + (255,))

    # 散热槽
    for top in (12, 17, 22):
        d.rectangle([9, top, 22, top + 2], fill=shade(base, 0.55) + (255,))
        d.line([(9, top), (22, top)], fill=shade(base, 0.40) + (255,))
        d.line([(9, top + 3), (22, top + 3)], fill=shade(base, 1.18) + (255,))

    for x, y in ((3, 3), (24, 3), (3, 24), (24, 24)):
        draw_screw(d, x, y, base)

    d.rectangle([0, 0, S - 1, 0], fill=shade(base, 0.82) + (255,))
    d.rectangle([0, S - 1, S - 1, S - 1], fill=shade(base, 1.16) + (255,))
    return img


# ---------------------------------------------------------------- output


def make_core(p: dict, size: int = 32) -> Image.Image:
    """光能核心图标：同心环渐变 + 四向卡扣。"""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    c = size // 2
    frame, glint, edge = p["frame"], p["glint"], p["edge"]

    rings = [
        (12, shade(edge, 0.55)),
        (11, frame),
        (9, shade(frame, 0.72)),
        (8, mix(frame, glint, 0.55)),
        (6, glint),
        (3, mix(glint, (255, 255, 255), 0.75)),
    ]
    for radius, color in rings:
        d.ellipse([c - radius, c - radius, c + radius, c + radius], fill=color + (255,))

    notch = shade(edge, 0.45)
    for box in (
        (c - 2, 2, c + 1, 5),
        (c - 2, size - 6, c + 1, size - 3),
        (2, c - 2, 5, c + 1),
        (size - 6, c - 2, size - 3, c + 1),
    ):
        d.rectangle(box, fill=notch + (255,))

    d.point((c - 5, c - 5), fill=(255, 255, 255, 255))
    d.point((c - 4, c - 5), fill=mix(glint, (255, 255, 255), 0.6) + (255,))
    return img


def build_core_preview(tiers: dict) -> Image.Image:
    scale, pad = 4, 8
    tile = 32 * scale
    canvas = Image.new("RGB", (pad + len(tiers) * (tile + pad), tile + 2 * pad), (28, 30, 34))
    for i, (_, p) in enumerate(tiers.items()):
        big = make_core(p).resize((tile, tile), Image.NEAREST)
        canvas.paste(big, (pad + i * (tile + pad), pad), big)
    return canvas


def build_sheet(p: dict) -> Image.Image:
    sheet = Image.new("RGBA", (S, S * FRAMES))
    for i in range(FRAMES):
        sheet.paste(make_top(p, i), (0, i * S))
    return sheet


MCMETA = json.dumps({
    "animation": {
        "frametime": FRAMETIME,
        "interpolate": False,
        "frames": list(range(FRAMES)),
    }
}, indent=2) + "\n"


def build_static_preview(images: dict) -> Image.Image:
    scale, pad, label_h, cols = 8, 16, 22, 4
    tile = S * scale
    width = pad + cols * (tile + pad)
    height = pad + len(images) * (tile + label_h + pad)
    canvas = Image.new("RGB", (width, height), (28, 30, 34))
    draw = ImageDraw.Draw(canvas)
    font = ImageFont.load_default()
    for row, (name, parts) in enumerate(images.items()):
        tiled = Image.new("RGBA", (S * 2, S * 2))
        for tx in (0, S):
            for ty in (0, S):
                tiled.paste(parts["top"], (tx, ty))
        views = [("top (frame 0)", parts["top"]), ("side", parts["side"]),
                 ("bottom", parts["bottom"]), ("2x2 array", tiled)]
        for col, (label, img) in enumerate(views):
            big = img.resize((tile, tile), Image.NEAREST)
            x = pad + col * (tile + pad)
            y = pad + row * (tile + label_h + pad)
            canvas.paste(big, (x, y), big)
            draw.text((x, y + tile + 5), f"{name} {label}", fill=(216, 218, 222), font=font)
    return canvas


def build_filmstrip(name: str, p: dict) -> Image.Image:
    scale, pad, cols = 4, 8, 4
    rows = math.ceil(FRAMES / cols)
    tile = S * scale
    width = pad + cols * (tile + pad)
    height = pad + rows * (tile + 16 + pad)
    canvas = Image.new("RGB", (width, height), (28, 30, 34))
    draw = ImageDraw.Draw(canvas)
    font = ImageFont.load_default()
    for i in range(FRAMES):
        big = make_top(p, i).resize((tile, tile), Image.NEAREST)
        x = pad + (i % cols) * (tile + pad)
        y = pad + (i // cols) * (tile + 16 + pad)
        canvas.paste(big, (x, y), big)
        draw.text((x, y + tile + 2), f"frame {i:02d}", fill=(210, 212, 216), font=font)
    return canvas


def build_animation_gif(tiers: dict) -> None:
    scale, pad, cols = 4, 6, 3
    tile = S * scale
    rows = math.ceil(len(tiers) / cols)
    width = pad + cols * (tile + pad)
    height = pad + rows * (tile + pad)
    frames = []
    for f in range(FRAMES):
        canvas = Image.new("RGB", (width, height), (28, 30, 34))
        for i, (name, p) in enumerate(tiers.items()):
            big = make_top(p, f).resize((tile, tile), Image.NEAREST)
            x = pad + (i % cols) * (tile + pad)
            y = pad + (i // cols) * (tile + pad)
            canvas.paste(big, (x, y))
        frames.append(canvas.convert("P", palette=Image.ADAPTIVE, colors=96))
    frames[0].save(
        os.path.join(DOCS, "preview-animation.gif"),
        save_all=True,
        append_images=frames[1:],
        duration=FRAMETIME * 50,
        loop=0,
        disposal=2,
    )


def main() -> None:
    os.makedirs(OUT, exist_ok=True)
    os.makedirs(ITEM_OUT, exist_ok=True)
    os.makedirs(DOCS, exist_ok=True)
    statics = {}
    for name, palette in TIERS.items():
        sheet = build_sheet(palette)
        sheet.save(os.path.join(OUT, f"solar_panel_{name}_top.png"))
        with open(os.path.join(OUT, f"solar_panel_{name}_top.png.mcmeta"), "w", encoding="utf-8") as fh:
            fh.write(MCMETA)
        side = make_side(palette)
        side.save(os.path.join(OUT, f"solar_panel_{name}_side.png"))
        bottom = make_bottom(palette)
        bottom.save(os.path.join(OUT, f"solar_panel_{name}_bottom.png"))
        make_core(palette).save(os.path.join(ITEM_OUT, f"solar_core_{name}.png"))
        # 静态顶面：不发电时用这一帧，避免动画一直在跑
        make_top(palette, 0).save(os.path.join(OUT, f"solar_panel_{name}_top_static.png"))
        statics[name] = {"top": make_top(palette, 0), "side": side, "bottom": bottom}

    build_static_preview(statics).save(PREVIEW)
    build_core_preview(TIERS).save(os.path.join(DOCS, "preview-cores.png"))
    build_filmstrip("t6", TIERS["t6"]).save(os.path.join(DOCS, "preview-filmstrip.png"))
    build_animation_gif({k: TIERS[k] for k in ("t1", "t3", "t6")})
    print(f"textures : {len(TIERS) * 3} png + {len(TIERS)} mcmeta -> {OUT}")
    print(f"sheet    : {S}x{S * FRAMES} ({FRAMES} frames, frametime {FRAMETIME})")
    print(f"previews : docs/preview-static.png / docs/preview-filmstrip.png / docs/preview-animation.gif")


if __name__ == "__main__":
    main()


