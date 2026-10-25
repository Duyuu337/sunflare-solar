"""生成光能核心与太阳能板的配方。

防逃课设计：
    核心 N（N>=2）= 8 个该档材料 + 核心 N-1（中心）
    面板 N        = 4 个该档框体 + 4 玻璃 + 核心 N（中心）
所以做第 N 块板必须先拥有核心 N，而核心 N 又必须逐级合成上来。

用法:
    python tools/gen_recipes.py
"""
from __future__ import annotations

import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src", "main", "resources", "data", "sunflare", "recipe")

# 核心 N 的环材料；N=1 的中心是红石，其余中心是上一级核心
CORE_MATERIAL = {
    1: "minecraft:iron_ingot",
    2: "minecraft:gold_ingot",
    3: "minecraft:diamond",
    4: "minecraft:amethyst_shard",
    5: "minecraft:netherite_ingot",
    6: "minecraft:netherite_ingot",
    7: "minecraft:netherite_block",
    8: "minecraft:nether_star",
}

# 面板 N 的框体材料
PANEL_FRAME = {
    1: "minecraft:iron_ingot",
    2: "minecraft:gold_ingot",
    3: "minecraft:diamond",
    4: "minecraft:diamond_block",
    5: "minecraft:netherite_ingot",
    6: "minecraft:netherite_ingot",
    7: "minecraft:netherite_block",
    8: "minecraft:netherite_block",
}


def write(name: str, pattern: list[str], key: dict[str, str], result: str) -> None:
    data = {
        "type": "minecraft:crafting_shaped",
        "category": "redstone",
        "pattern": pattern,
        "key": {k: {"item": v} for k, v in key.items()},
        "result": {"id": result, "count": 1},
    }
    path = os.path.join(OUT, f"{name}.json")
    with open(path, "w", encoding="utf-8") as fh:
        json.dump(data, fh, indent=2)
        fh.write("\n")


def main() -> None:
    os.makedirs(OUT, exist_ok=True)

    for tier in range(1, 9):
        material = CORE_MATERIAL[tier]
        center = "minecraft:redstone" if tier == 1 else f"sunflare:solar_core_t{tier - 1}"
        write(
            f"solar_core_t{tier}",
            ["MMM", "MCM", "MMM"],
            {"M": material, "C": center},
            f"sunflare:solar_core_t{tier}",
        )

    for tier in range(1, 9):
        write(
            f"solar_panel_t{tier}",
            ["FGF", "GCG", "FGF"],
            {"F": PANEL_FRAME[tier], "G": "minecraft:glass", "C": f"sunflare:solar_core_t{tier}"},
            f"sunflare:solar_panel_t{tier}",
        )

    print(f"recipes: {len(os.listdir(OUT))} files -> {OUT}")


if __name__ == "__main__":
    main()
