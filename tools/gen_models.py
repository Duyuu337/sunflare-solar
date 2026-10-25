"""生成太阳能板的方块模型（薄板）+ blockstate + 物品模型。

每档两个方块模型：
    solar_panel_tN          静态顶面（active=false，物品图标也用这个）
    solar_panel_tN_active   动画顶面（active=true，发电时切换）

用法:
    python tools/gen_models.py
"""
from __future__ import annotations

import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "sunflare")
TIERS = range(1, 9)

HEIGHT = 3
HEIGHT_UNITS = 16


def plate_model(top: str, side: str, bottom: str) -> dict:
    return {
        # 继承原版 display 变换，物品栏才是等轴 3D 视角
        "parent": "minecraft:block/block",
        "textures": {
            "particle": top,
            "top": top,
            "side": side,
            "bottom": bottom,
        },
        "elements": [
            {
                "from": [0, 0, 0],
                "to": [HEIGHT_UNITS, HEIGHT, HEIGHT_UNITS],
                "faces": {
                    "down": {"texture": "#bottom", "uv": [0, 0, 16, 16], "cullface": "down"},
                    "up": {"texture": "#top", "uv": [0, 0, 16, 16], "cullface": "up"},
                    "north": {"texture": "#side", "uv": [0, 0, 16, HEIGHT], "cullface": "north"},
                    "south": {"texture": "#side", "uv": [0, 0, 16, HEIGHT], "cullface": "south"},
                    "west": {"texture": "#side", "uv": [0, 0, 16, HEIGHT], "cullface": "west"},
                    "east": {"texture": "#side", "uv": [0, 0, 16, HEIGHT], "cullface": "east"},
                },
            }
        ],
    }


def write_json(path: str, data: dict) -> None:
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as fh:
        json.dump(data, fh, indent=2)
        fh.write("\n")


def main() -> None:
    for tier in TIERS:
        name = f"solar_panel_t{tier}"
        tex = f"sunflare:block/{name}"
        side = f"{tex}_side"
        bottom = f"{tex}_bottom"

        write_json(
            os.path.join(ASSETS, "blockstates", f"{name}.json"),
            {
                "variants": {
                    "active=false": {"model": f"sunflare:block/{name}"},
                    "active=true": {"model": f"sunflare:block/{name}_active"},
                }
            },
        )
        write_json(os.path.join(ASSETS, "models", "block", f"{name}.json"),
                   plate_model(f"{tex}_top_static", side, bottom))
        write_json(os.path.join(ASSETS, "models", "block", f"{name}_active.json"),
                   plate_model(f"{tex}_top", side, bottom))

        # 物品图标用静态模型
        write_json(os.path.join(ASSETS, "models", "item", f"{name}.json"),
                   {"parent": f"sunflare:block/{name}"})

        # 光能核心：普通物品图标
        write_json(os.path.join(ASSETS, "models", "item", f"solar_core_t{tier}.json"),
                   {"parent": "minecraft:item/generated",
                    "textures": {"layer0": f"sunflare:item/solar_core_t{tier}"}})

    print(f"models: {len(list(TIERS))} tiers x2 + cores -> {ASSETS}")


if __name__ == "__main__":
    main()
