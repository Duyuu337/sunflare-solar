"""生成通量调控阵列两个方块的贴图/模型/配方/掉落物。

用法:
    python tools/make_aux_assets.py
"""
from __future__ import annotations

import json
import os

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "sunflare")
DATA = os.path.join(ROOT, "src", "main", "resources", "data", "sunflare")

S = 32

METAL = (74, 82, 96)
METAL_DARK = (46, 52, 63)
METAL_LIGHT = (108, 118, 134)
SCREEN = (18, 26, 40)
ACCENT = (86, 176, 226)
COPPER = (198, 146, 84)


def bevel(d: ImageDraw.ImageDraw, x0: int, y0: int, x1: int, y1: int) -> None:
    d.line([(x0, y0), (x1, y0)], fill=METAL_LIGHT)
    d.line([(x0, y0), (x0, y1)], fill=METAL_LIGHT)
    d.line([(x0, y1), (x1, y1)], fill=METAL_DARK)
    d.line([(x1, y0), (x1, y1)], fill=METAL_DARK)


def governor_texture() -> Image.Image:
    img = Image.new("RGB", (S, S), METAL)
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, S - 1, S - 1], outline=METAL_DARK)
    bevel(d, 1, 1, S - 2, S - 2)

    # 屏幕
    d.rectangle([5, 5, S - 6, S - 6], fill=SCREEN, outline=METAL_DARK)

    # 仪表盘：外圈 + 蓝色弧 + 指针
    cx, cy, r = 16, 16, 8
    d.ellipse([cx - r, cy - r, cx + r, cy + r], outline=METAL_LIGHT)
    d.arc([cx - r + 1, cy - r + 1, cx + r - 1, cy + r - 1], start=200, end=340, fill=ACCENT)
    d.line([(cx, cy), (cx + 4, cy - 4)], fill=(226, 236, 246))
    d.point((cx, cy), fill=(226, 236, 246))

    # 四角螺丝
    for x, y in ((2, 2), (S - 3, 2), (2, S - 3), (S - 3, S - 3)):
        d.point((x, y), fill=METAL_LIGHT)
    return img


def bus_texture() -> Image.Image:
    img = Image.new("RGB", (S, S), (58, 64, 76))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, S - 1, S - 1], outline=(32, 36, 44))
    bevel(d, 1, 1, S - 2, S - 2)

    # 中央插槽
    d.rectangle([6, 6, S - 7, S - 7], fill=(30, 35, 45), outline=(20, 23, 29))

    # 三段铜触点
    for i, y in enumerate((10, 15, 20)):
        d.rectangle([9, y, S - 10, y + 3], fill=COPPER)
        d.line([(9, y), (S - 10, y)], fill=(230, 190, 130))

    # 右上角优先级刻度（三条）
    for i in range(3):
        d.line([(S - 8, 5 + i * 2), (S - 4, 5 + i * 2)], fill=ACCENT if i < 2 else (70, 78, 90))
    return img


def write_json(path: str, data: dict) -> None:
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as fh:
        json.dump(data, fh, indent=2)
        fh.write("\n")


def main() -> None:
    textures = {
        "flux_governor": governor_texture(),
        "priority_bus_node": bus_texture(),
    }
    for name, image in textures.items():
        out = os.path.join(ASSETS, "textures", "block", f"{name}.png")
        os.makedirs(os.path.dirname(out), exist_ok=True)
        image.save(out)

        write_json(os.path.join(ASSETS, "blockstates", f"{name}.json"),
                   {"variants": {"": {"model": f"sunflare:block/{name}"}}})
        write_json(os.path.join(ASSETS, "models", "block", f"{name}.json"),
                   {"parent": "minecraft:block/cube_all",
                    "textures": {"all": f"sunflare:block/{name}"}})
        write_json(os.path.join(ASSETS, "models", "item", f"{name}.json"),
                   {"parent": f"sunflare:block/{name}"})
        write_json(os.path.join(DATA, "loot_table", "blocks", f"{name}.json"), {
            "type": "minecraft:block",
            "pools": [{
                "rolls": 1,
                "bonus_rolls": 0,
                "entries": [{"type": "minecraft:item", "name": f"sunflare:{name}"}],
                "conditions": [{"condition": "minecraft:survives_explosion"}],
            }],
        })

    write_json(os.path.join(DATA, "recipe", "flux_governor.json"), {
        "type": "minecraft:crafting_shaped",
        "category": "redstone",
        "pattern": ["IRI", "RCR", "IGI"],
        "key": {
            "I": {"item": "minecraft:iron_ingot"},
            "R": {"item": "minecraft:redstone"},
            "G": {"item": "minecraft:glass"},
            "C": {"item": "sunflare:solar_core_t3"},
        },
        "result": {"id": "sunflare:flux_governor", "count": 1},
    })

    write_json(os.path.join(DATA, "recipe", "priority_bus_node.json"), {
        "type": "minecraft:crafting_shaped",
        "category": "redstone",
        "pattern": ["IRI", "RCR", "IRI"],
        "key": {
            "I": {"item": "minecraft:iron_ingot"},
            "R": {"item": "minecraft:redstone"},
            "C": {"item": "sunflare:solar_core_t2"},
        },
        "result": {"id": "sunflare:priority_bus_node", "count": 1},
    })

    print("aux assets: 2 textures, models, states, recipes, loot")


if __name__ == "__main__":
    main()
