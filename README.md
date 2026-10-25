# Sunflare Solar（耀阳光能板）

Minecraft **1.21.1** / **NeoForge 21.1.x** 的太阳能发电模组。八档薄板太阳能板，输出 **Forge Energy (FE)**——
AE2 与通用机械（Mekanism）都能直接接的能量单位。

规模对齐 **日光通量（Solar Flux Reborn）**：它最高档是 32,768 RF/t、128M RF 缓冲、256,000 RF/t 输出上限，
而 RF 与 FE 1:1（[面板 I](https://www.mcmod.cn/item/71535.html)、[面板 VIII](https://www.mcmod.cn/item/71542.html)）。
本模组顶档取同样的 32,768 FE/t 与 131,072,000 FE 缓冲，逐档 ×4；**只有配方是扁平的，不套娃**。

## 为什么是 FE

| 模组 | 自己的单位 | 与 FE 的关系 | 怎么接 |
|------|-----------|-------------|--------|
| AE2 | AE | **2 FE = 1 AE**，单向转换 | 面板 → AE2 **Energy Acceptor**（或 ME 控制器面）→ 网络 |
| 通用机械 | J（焦耳） | **0.4 FE = 1 J**（1 FE = 2.5 J） | 面板 → 通用线缆 / 机器，直接吃 FE |

来源：[AE2 官方指南 · Energy](https://guide.appliedenergistics.org/1.20.1/ae2-mechanics/energy/)、
[Mekanism 官方 Wiki · Energy Unit Conversion](https://wiki.aidancbrady.com/wiki/Energy_Unit_Conversion)。

AE2 的内部转换是**单向**的，且必须经过 Energy Acceptor 或 ME 控制器——这是 AE2 自己的设计，不是本模组的缺陷。
通用机械则直接接受 FE，线缆贴上就能用。

## 八档输出

| 档位 | 额定 FE/t | ≈ AE/t | ≈ J/t | 缓冲 FE | 推送上限 FE/t | 对标日光通量 |
|------|-----------|--------|-------|---------|---------------|--------------|
| 太阳能板 I | 32 | 16 | 12 | 128,000 | 256 | 介于 I 与 II 之间（它 I 是 1 RF/t，太弱） |
| 太阳能板 II | 128 | 64 | 51 | 512,000 | 1,024 | ≈ II–III |
| 太阳能板 III | 512 | 256 | 204 | 2,048,000 | 4,096 | ≈ IV |
| 太阳能板 IV | 2,048 | 1,024 | 819 | 8,192,000 | 16,384 | ≈ V |
| 太阳能板 V | 8,192 | 4,096 | 3,276 | 32,768,000 | 65,536 | ≈ VI–VII |
| 太阳能板 VI | 32,768 | 16,384 | 13,107 | 131,072,000 | 262,144 | **= VIII（它的顶档）** |
| 太阳能板 VII | 131,072 | 65,536 | 52,428 | 524,288,000 | 1,048,576 | 继续向上 ×4 |
| 太阳能板 VIII | 524,288 | 262,144 | 209,715 | 1,000,000,000（封顶） | 4,194,304 | 继续向上 ×4 |

J 一列是整数取整后的值（`FE × 0.4` 后向下取整），和游戏内右键读数一致。
缓冲按 `额定 × 4000` tick（约 200 秒满发）算，但封顶 10 亿——否则 T8 的 `524288 × 4000` 会顶到 int 上限。

**后期意味着什么**

- 顶档 **262,144 AE/t**：AE2 里几千 AE/t 就算大网络了，这块板子是给"几百台机器 + 大规模自动合成"准备的余量。
- 顶档 **209,715 J/t**：通用机械基础机器多在 100–200 J/t，一块板子等价于上千台。
- T6 = 日光通量顶档；T7/T8 是在它之上继续翻倍的存在。

## 输出曲线

```
输出 = 额定 × sin(π · 时间 / 12000) × (上方天光 / 15) × 天气系数
```

- 日出为 0、正午满发、日落为 0，夜间 0。
- 上方被方块遮挡按天光比例衰减（玻璃不挡光）。
- 雨天 ×0.5，雷雨 ×0.25。
- 无天空光照的维度（下界、末地）恒为 0。

## 配方（八档平级，不套娃）

任何一档的配方里都**不会出现另一档的太阳能板**。想跳级就直接做顶档，不用先做前七块。

| 档位 | 图案 | 四角 | 四边 | 中心 |
|------|------|------|------|------|
| I | `IGI / GDG / IGI` | 铁锭 | 玻璃 | 日光探测器 |
| II | `AGA / GOG / AGA` | 金锭 | 玻璃 | 侦测器 |
| III | `DGD / GQG / DGD` | 钻石 | 玻璃 | 紫水晶块 |
| IV | `EGE / GWG / EGE` | 钻石块 | 玻璃 | 荧石 |
| V | `NGN / GSG / NGN` | 下界合金锭 | 玻璃 | 下界之星 |
| VI | `NGN / GBG / NGN` | 下界合金锭 | 玻璃 | 信标 |
| VII | `BGB / GSG / BGB` | 下界合金块 | 玻璃 | 下界之星 |
| VIII | `BGB / GEG / BGB` | 下界合金块 | 玻璃 | 信标 |

成本从"四块铁"一路走到"四个下界合金块 + 一个信标"，但没有一条配方引用前一级的产物。

## 使用

- 板子是 **3/16 格高的薄板**（比半砖 8/16 薄），碰撞箱、选中框、交互框都是这个高度，可以直接走上去。
- 放置后**上方留空**。
- 空手右键面板，动作栏显示额定输出、AE/J 换算、当前缓冲。
- 没有 GUI、没有配置文件：数值都在 `SolarTier` 枚举里，改一行即可重新平衡（`bufferFe()` 与 `pushFePerTick()` 都是按 `peakFePerTick` 算出来的）。

## 版本约定

测试期用 `0.0.x-beta`，每次内容改动递增补丁号，正式发布再走 `0.1.0` / `1.0.0`：

| 阶段 | 版本串 | 说明 |
|------|--------|------|
| 现在 | `0.0.2-beta` | 八档 + 3/16 薄板 + 顶档 524,288 FE/t |
| 修 bug | `0.0.2-beta` | 每次改动 +1 |
| 功能成形 | `0.1.0-beta` | 内容结构基本确定 |
| 发布 | `1.0.0` | 去掉 `-beta` 后缀 |

**只改一个地方**：`gradle.properties` 里的 `mod_version`。jar 文件名、`neoforge.mods.toml`
里的版本、游戏内模组列表显示的版本，全部由它派生，不用改别的文件。

## 构建

工程自带 Gradle wrapper（固定 **8.10.2**，别用系统的 9.x —— ModDevGradle 2.0.78 不支持）。需要 JDK 21。

```bash
cd sunflare
gradlew.bat build          # Windows
# ./gradlew build          # macOS / Linux
```

产物：`build/libs/sunflare-0.0.2-beta+mc1.21.1.jar`（文件名跟着 `gradle.properties` 里的 `mod_version` 走）。

**国内网络注意**：官方源 `maven.neoforged.net` 直连和代理都不通，本机已经做了两层绕行，直接 `gradlew.bat build` 即可：

1. `~/.gradle/init.gradle`（来自 `gradle/neoforged-mirror.init.gradle`）把任何指向
   `maven.neoforged.net` 的仓库 URL 重写到 ForgeCDN 镜像 `https://neoforged.forgecdn.net/releases`。
2. 镜像缺 `net.neoforged:minecraft-dependencies:1.21.1` 这个聚合 POM，由
   `tools/gen_minecraft_dependencies.py` 从 Mojang 版本 JSON 重建到 `local-maven/`，
   `build.gradle` 已把它注册成第一个仓库。

换机器时：拷走 `local-maven/` 与 `~/.gradle/init.gradle`，或重跑一次生成脚本。
完整的 IDEA 导入与报错对照见 [docs/BUILD-WITH-IDEA.md](docs/BUILD-WITH-IDEA.md)。

若 `21.1.250` 这个 build 不存在，改 `gradle.properties` 的 `neo_version` 为任意 `21.1.x`。

## 未验证的部分

本机 shell 不可用（`pwsh` 退出码 `0xC0000142`），所以**没有编译过、也没有进游戏跑过**。以下三处
如果 API 有出入，会直接编译失败，改起来都是一两行：

1. `BlockEntityType.Builder.of(...).build(null)` —— 1.21.1 若已改成无参 `build()`，去掉 `null`。
2. `level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, side)` —— 能力查询的三参形式。
3. `event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, type, (panel, side) -> ...)` —— 能力注册的 lambda 形状。

## 贴图与动画

八档各三张 **32×32** 贴图，顶面是 16 帧动画，全部由 `tools/make_textures.py` 用 Pillow 程序化生成：

```bash
python tools/make_textures.py
```

**三个面**

- **顶面**：3×3 电池片（每片 8×8）。每片四段硬色阶表现玻璃反光，段间只在收尾行留 1px 棋盘过渡；
  带竖向汇流条、铜色横向走线与焊盘、片内右下斜面。外圈是 2px 对称金属框，**最外 1px 保持纯色**，
  所以 2×2 / 3×3 阵列拼接时是一条平直金属导轨，不会出现双倍粗的深缝。
- **侧面**：横向拉丝（逐行亮度微变 + 抖动）、上下收边、中央深色能源接口（外斜面 + 三组铜触点）、
  四角 5×5 立体十字槽螺丝。
- **底面**：散热槽、螺丝、收边。

**动画**

- 16 帧：一条斜向能量带扫过电池片，T4–T6 的指示灯同步呼吸。全部用离散色阶，不做插值，保留像素感。
- `solar_panel_tN_top.png` 是 32×512 的竖排帧序列，配套 `solar_panel_tN_top.png.mcmeta`：
  `frametime: 2`、`interpolate: false`、`frames: 0..15`，一轮约 1.6 秒。
- 想改速度就改 `frametime`（1 = 每 tick 换帧，即 20 帧/秒）。

**调色**

`TIERS` 字典里每档四个颜色（`frame` / `edge` / `cell` / `glint`），T4–T6 多一个 `glow` 作为指示灯色。

**预览产物**：`docs/preview-static.png`（8× 静态对照）、`docs/preview-filmstrip.png`（16 帧胶片）、
`docs/preview-animation.gif`（动画预览）。

## 目录

```
src/main/java/dev/sunflare/
  Sunflare.java                       @Mod 入口
  SunflareRegistry.java               八档方块 / 物品 / 方块实体 / 创造标签页 / FE 能力
  SolarTier.java                    八档数值与 AE、J 换算
  block/SolarPanelBlock.java        方块、ticker、右键读数
  block/SolarPanelBlockEntity.java  发电曲线、遮挡与天气、IEnergyStorage、相邻推送
src/main/resources/assets/sunflare/   blockstates / models / lang / textures(PNG)
src/main/resources/data/sunflare/     recipe / loot_table
tools/make_textures.py              贴图/动画生成脚本（Pillow）
docs/BUILD-WITH-IDEA.md             IDEA 导入与构建的详细步骤
docs/preview-static.png             8× 静态对照图
docs/preview-filmstrip.png          16 帧胶片图
docs/preview-animation.gif          动画预览
```
