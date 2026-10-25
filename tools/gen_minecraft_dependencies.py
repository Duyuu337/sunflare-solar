"""生成 net.neoforged:minecraft-dependencies:1.21.1 的 POM。

为什么需要它：
    这个构件是 NeoForge 构建链里的一个「聚合 POM」，本身没有 jar，只把 Minecraft 1.21.1
    用到的所有库声明成 dependencies，供 NeoForm 运行时组装 classpath。
    官方源 maven.neoforged.net 国内不可达，ForgeCDN 镜像又缺这个构件（404），
    但它的内容完全可以从 Mojang 的版本 JSON 重建。

用法:
    python tools/gen_minecraft_dependencies.py
输出:
    local-maven/net/neoforged/minecraft-dependencies/1.21.1/minecraft-dependencies-1.21.1.pom
"""
from __future__ import annotations

import json
import os
import urllib.request
from xml.sax.saxutils import escape

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT_DIR = os.path.join(ROOT, "local-maven", "net", "neoforged", "minecraft-dependencies", "1.21.1")
MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
MC_VERSION = "1.21.1"


def fetch_json(url: str) -> dict:
    req = urllib.request.Request(url, headers={"User-Agent": "sunflare-build/1.0"})
    with urllib.request.urlopen(req, timeout=60) as resp:
        return json.loads(resp.read().decode("utf-8"))


def main() -> None:
    manifest = fetch_json(MANIFEST)
    entry = next((v for v in manifest["versions"] if v["id"] == MC_VERSION), None)
    if entry is None:
        raise SystemExit(f"{MC_VERSION} not found in version manifest")

    version_json = fetch_json(entry["url"])
    seen: set[tuple[str, str, str, str]] = set()
    deps: list[tuple[str, str, str, str]] = []
    for lib in version_json.get("libraries", []):
        name = lib.get("name", "")
        parts = name.split(":")
        if len(parts) < 3:
            continue
        group, artifact, version = parts[0], parts[1], parts[2]
        # 第四段是 classifier（例如 netty 的 linux-x86_64、lwjgl 的 natives-windows）
        classifier = parts[3] if len(parts) > 3 else ""
        # 只要真实存在 jar 的库（纯 natives 的条目没有 artifact.downloads）
        if "artifact" not in lib.get("downloads", {}):
            continue
        key = (group, artifact, version, classifier)
        if key in seen:
            continue
        seen.add(key)
        deps.append(key)

    os.makedirs(OUT_DIR, exist_ok=True)
    lines = [
        '<?xml version="1.0" encoding="UTF-8"?>',
        '<project xmlns="http://maven.apache.org/POM/4.0.0"',
        '         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"',
        '         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">',
        "  <modelVersion>4.0.0</modelVersion>",
        "  <groupId>net.neoforged</groupId>",
        "  <artifactId>minecraft-dependencies</artifactId>",
        f"  <version>{MC_VERSION}</version>",
        "  <packaging>pom</packaging>",
        f"  <name>Minecraft {MC_VERSION} library dependencies (locally reconstructed)</name>",
        "  <dependencies>",
    ]
    for group, artifact, version, classifier in deps:
        lines += [
            "    <dependency>",
            f"      <groupId>{escape(group)}</groupId>",
            f"      <artifactId>{escape(artifact)}</artifactId>",
            f"      <version>{escape(version)}</version>",
        ]
        if classifier:
            lines.append(f"      <classifier>{escape(classifier)}</classifier>")
        lines.append("    </dependency>")
    lines += ["  </dependencies>", "</project>", ""]

    out = os.path.join(OUT_DIR, f"minecraft-dependencies-{MC_VERSION}.pom")
    with open(out, "w", encoding="utf-8") as fh:
        fh.write("\n".join(lines))
    print(f"wrote {out} ({len(deps)} dependencies)")


if __name__ == "__main__":
    main()
