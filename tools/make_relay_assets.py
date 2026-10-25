"""生成亚空间中继系统的贴图/模型/配方/掉落物。"""
from __future__ import annotations

import json
import os

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "sunflare")
DATA = os.path.join(ROOT, "src", "main", "resources", "data", "sunflare")
S = 32


def beacon_icon() -> Image.Image:
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # 立柱
    d.rectangle([13, 6, 18, 25], fill=(46, 60, 86), outline=(24, 32, 48))
    d.rectangle([14, 7, 17, 24], fill=(78, 112, 158))
    # 顶部晶体
    d.polygon([(15, 2), (21, 8), (15, 14), (9, 8)], fill=(120, 210, 240), outline=(60, 140, 180))
    d.polygon([(15, 4), (18, 8), (15, 12), (12, 8)], fill=(220, 245, 255))
    # 底座
    d.rectangle([9, 25, 22, 29], fill=(60, 70, 88), outline=(30, 36, 48))
    d.line([(10, 26), (21, 26)], fill=(110, 124, 148))
    # 两侧光点
    d.point((10, 16), fill=(150, 230, 255))
    d.point((21, 16), fill=(150, 230, 255))
    return img


def coupler_icon() -> Image.Image:
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([4, 8, 27, 23], fill=(38, 56, 84), outline=(18, 26, 40))
    d.rectangle([6, 10, 25, 21], fill=(56, 96, 140))
    # 电路
    d.line([(8, 15), (13, 15)], fill=(140, 220, 255))
    d.line([(13, 15), (13, 12)], fill=(140, 220, 255))
    d.rectangle([15, 12, 19, 16], fill=(200, 240, 255))
    d.line([(19, 14), (24, 14)], fill=(140, 220, 255))
    # 金手指
    for x in (8, 11, 14, 17, 20, 23):
        d.line([(x, 19), (x, 21)], fill=(224, 186, 96))
    return img


def anchor_texture() -> Image.Image:
    img = Image.new("RGB", (S, S), (58, 66, 82))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, S - 1, S - 1], outline=(28, 32, 42))
    d.line([(1, 1), (S - 2, 1)], fill=(96, 108, 128))
    d.line([(1, 1), (1, S - 2)], fill=(96, 108, 128))
    d.line([(1, S - 2), (S - 2, S - 2)], fill=(30, 34, 44))
    d.line([(S - 2, 1), (S - 2, S - 2)], fill=(30, 34, 44))
    # 中央传送门式方块
    d.rectangle([7, 7, 24, 24], fill=(20, 28, 44), outline=(12, 16, 24))
    d.rectangle([10, 10, 21, 21], fill=(64, 140, 190))
    d.rectangle([12, 12, 19, 19], fill=(160, 225, 250))
    d.line([(15, 10), (15, 21)], fill=(210, 245, 255))
    d.line([(10, 15), (21, 15)], fill=(210, 245, 255))
    # 四角螺丝
    for x, y in ((3, 3), (28, 3), (3, 28), (28, 28)):
        d.point((x, y), fill=(120, 132, 152))
    return img


def write_json(path: str, data: dict) -> None:
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as fh:
        json.dump(data, fh, indent=2)
        fh.write("\n")


def main() -> None:
    item_dir = os.path.join(ASSETS, "textures", "item")
    block_dir = os.path.join(ASSETS, "textures", "block")
    os.makedirs(item_dir, exist_ok=True)
    os.makedirs(block_dir, exist_ok=True)

    beacon_icon().save(os.path.join(item_dir, "subspace_relay_beacon.png"))
    coupler_icon().save(os.path.join(item_dir, "tesseract_coupler.png"))
    anchor_texture().save(os.path.join(block_dir, "tesseract_anchor.png"))

    for name in ("subspace_relay_beacon", "tesseract_coupler"):
        write_json(os.path.join(ASSETS, "models", "item", f"{name}.json"),
                   {"parent": "minecraft:item/generated",
                    "textures": {"layer0": f"sunflare:item/{name}"}})

    write_json(os.path.join(ASSETS, "blockstates", "tesseract_anchor.json"),
               {"variants": {"": {"model": "sunflare:block/tesseract_anchor"}}})
    write_json(os.path.join(ASSETS, "models", "block", "tesseract_anchor.json"),
               {"parent": "minecraft:block/cube_all",
                "textures": {"all": "sunflare:block/tesseract_anchor"}})
    write_json(os.path.join(ASSETS, "models", "item", "tesseract_anchor.json"),
               {"parent": "sunflare:block/tesseract_anchor"})
    write_json(os.path.join(DATA, "loot_table", "blocks", "tesseract_anchor.json"), {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1, "bonus_rolls": 0,
            "entries": [{"type": "minecraft:item", "name": "sunflare:tesseract_anchor"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}],
        }],
    })

    write_json(os.path.join(DATA, "recipe", "subspace_relay_beacon.json"), {
        "type": "minecraft:crafting_shaped",
        "category": "misc",
        "pattern": ["GEG", "ECE", "GEG"],
        "key": {
            "G": {"item": "minecraft:gold_ingot"},
            "E": {"item": "minecraft:ender_pearl"},
            "C": {"item": "sunflare:solar_core_t4"},
        },
        "result": {"id": "sunflare:subspace_relay_beacon", "count": 1},
    })
    write_json(os.path.join(DATA, "recipe", "tesseract_coupler.json"), {
        "type": "minecraft:crafting_shaped",
        "category": "misc",
        "pattern": ["PQP", "QCQ", "PQP"],
        "key": {
            "P": {"item": "minecraft:ender_pearl"},
            "Q": {"item": "minecraft:netherite_scrap"},
            "C": {"item": "sunflare:solar_core_t5"},
        },
        "result": {"id": "sunflare:tesseract_coupler", "count": 1},
    })
    write_json(os.path.join(DATA, "recipe", "tesseract_anchor.json"), {
        "type": "minecraft:crafting_shaped",
        "category": "misc",
        "pattern": ["EOE", "OCO", "EOE"],
        "key": {
            "E": {"item": "minecraft:ender_pearl"},
            "O": {"item": "minecraft:obsidian"},
            "C": {"item": "sunflare:solar_core_t4"},
        },
        "result": {"id": "sunflare:tesseract_anchor", "count": 1},
    })
    print("relay assets written")


if __name__ == "__main__":
    main()
