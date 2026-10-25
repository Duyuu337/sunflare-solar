# 用 IntelliJ IDEA 构建 Sunflare（Windows 详细步骤）

这份文档假设你的机器是 Windows，项目路径为 `D:\deepseek\helios`，已经装好 JDK 21。

---

## 0. 先理解一个前提

| 依赖 | 来源 | 国内能否直连 |
|------|------|--------------|
| Gradle 发行版 | 腾讯云镜像（已写进 wrapper） | ✅ |
| Gradle 插件 / Maven Central | 阿里云镜像（已写进 `settings.gradle`） | ✅ |
| Minecraft 1.21.1 本体 | Mojang（`piston-meta.mojang.com`） | ✅ 一般可以 |
| **NeoForge 21.1.x** | **`maven.neoforged.net`** | ❌ **没有任何国内镜像** |

所以：**阿里云只能加速前两类，NeoForge 必须走代理**。这一步绕不过去，先确认你的代理能用
（Clash / v2rayN / 公司 VPN 都行），记下它的 **HTTP 代理端口**（Clash 默认 `7890`，v2rayN 默认 `10809`）。

---

## 1. 安装 IntelliJ IDEA

### 1.1 下载

任选其一：

- 官网下载页：<https://www.jetbrains.com/idea/download/>
- 中文下载页：<https://www.jetbrains.com/zh-cn/idea/download/>
- 下载经常中断的话，用 JetBrains **Toolbox App**：<https://www.jetbrains.com/toolbox-app/>
  （Toolbox 支持断点续传，装完在列表里点 IDEA 的 Install 即可）

### 1.2 选哪个版本

- **Community（社区版，免费）**：足够。Gradle 集成、Java 21 支持、代码补全都齐全。
- Ultimate（旗舰版）：只有 30 天试用，对本项目没有任何额外价值，不用装。

### 1.3 安装

1. 双击 `ideaIC-xxxx.exe`。
2. 安装路径保持默认（`C:\Program Files\JetBrains\IntelliJ IDEA Community Edition xxxx`）。
3. 安装选项全部勾上即可，其中 **"Add "bin" folder to the PATH"** 勾不勾都行。
4. 安装完先不要打开项目，直接启动 IDEA 走首次向导。

---

## 2. 首次启动向导

1. 提示导入旧设置 → 选 **Do not import settings**。
2. 登录 JetBrains 账号 → 可以 **Skip / Continue without account**。
3. 插件推荐页 → 直接 **Next**，本项目不需要额外插件。
4. 欢迎页出现即可。

---

## 3. 确认 JDK 21

你的机器上已经有：

```
C:\Program Files\Java\jdk-21.0.11
```

在 IDEA 里：

1. `File → Project Structure`（快捷键 `Ctrl+Alt+Shift+S`）。
2. 左侧 `Project` → `SDK` → 下拉选 `Add SDK → JDK...` → 选上面的目录。
3. `Language level` 选 **21**。
4. 左侧 `SDKs` 里确认出现 `21`，路径正确。

> ⚠️ 不要用 `jdk-25.0.3`。NeoForge 21.1 / ModDevGradle 2.0.78 都不支持 Java 25，
> 用它会出现 `Unsupported class file major version 69` 之类的报错。

### 3.1 命令行构建时的 JAVA_HOME（很容易踩）

`gradlew.bat --version` 里会看到：

```
Launcher JVM:  25.0.3 (Oracle Corporation ...)
Daemon JVM:    C:\Program Files\Java\jdk-25.0.3
```

因为 `PATH` 里的 `javapath` 指向 JDK 25。而 **Gradle 8.10.2 官方只支持到 Java 23**，
用 25 当 launcher 可能在构建阶段直接崩。两个处理方式：

- **在 IDEA 里**：第 4 步已经把 `Gradle JVM` 设成 21，IDEA 不使用系统 `JAVA_HOME`，没问题。
- **在命令行里**：当前窗口先执行

  ```bat
  set JAVA_HOME=C:\Program Files\Java\jdk-21.0.11
  ```

  或者永久生效：把 `gradle.properties` 里这行取消注释

  ```properties
  org.gradle.java.home=C:\\Program Files\\Java\\jdk-21.0.11
  ```

---

## 4. 导入项目

1. `File → Open`。
2. 选中 **`D:\deepseek\helios`** 这个目录（不要选里面的单个文件），点 OK。
3. IDEA 问 **"Trust and Open Project 'sunflare'?"** → 点 **Trust Project**。
4. 右下角出现 "Gradle project detected" 提示时，选择：
   **Use Gradle from: `gradle-wrapper.properties` file**
   （这一步很关键，原因见下一节）
5. 如果没弹提示，手动设置：
   `File → Settings → Build, Execution, Deployment → Build Tools → Gradle`
   - **Distribution**：选 `Wrapper`（不是 `Local installation`）
   - **Gradle JVM**：选 `21`
   - Gradle user home：保持默认

### 为什么必须用 wrapper，不能用你装的 Gradle 9.8.2

本工程的 `build.gradle` 用的是 **ModDevGradle 2.0.78**，它只支持 **Gradle 8.x**。
你系统里装的是 9.8.2，直接用会报 `Unsupported Gradle version` 之类的错误。
工程里已经带好了 wrapper，固定的版本是 **8.10.2**：

```
gradle/wrapper/gradle-wrapper.jar
gradle/wrapper/gradle-wrapper.properties   →  distributionUrl 指向腾讯云镜像
gradlew / gradlew.bat
```

所以：**让 IDEA 用 Wrapper，不要让 IDEA 用你 PATH 里的 gradle。**

---

## 5. 配置代理（本项目最关键的一步）

代理要配 **两个地方**，缺一不可。

### 5.1 IDEA 自己的代理（负责 Check connection、插件下载）

1. `File → Settings → Appearance & Behavior → System Settings → HTTP Proxy`。
2. 选 **Manual proxy configuration** → **HTTP**。
3. 填：
   - `Host name`：`127.0.0.1`
   - `Port number`：你的代理端口（Clash `7890`，v2rayN `10809`）
4. 点 **Check connection**，输入：

   ```
   https://maven.neoforged.net/releases/
   ```

   返回 `200` 或 `404` 都算通（404 只是目录没索引）；如果显示 `Connection failed`，说明代理没开或端口不对。
5. 点 `OK`。

### 5.2 Gradle 的代理（真正下载依赖的那个进程）

**Gradle 跑在独立 JVM 里，完全不吃 IDEA 上面的代理设置。** 二选一：

**方案 A（推荐，最可控）**：编辑项目根目录的 `gradle.properties`，把这几行注释去掉：

```properties
systemProp.http.proxyHost=127.0.0.1
systemProp.http.proxyPort=7890
systemProp.https.proxyHost=127.0.0.1
systemProp.https.proxyPort=7890
systemProp.http.nonProxyHosts=localhost|127.0.0.1
```

端口改成你自己的。**改完必须重启 Gradle daemon**，否则旧进程还在用旧配置：

```bat
cd /d D:\deepseek\helios
gradlew.bat --stop
```

**方案 B**：把你的代理软件切成 **系统代理** 或 **TUN / 虚拟网卡模式**。
这样 Gradle 不写配置也能继承系统代理，但有时对 Java 进程不生效，不如方案 A 稳。

### 5.3 验证代理对 Gradle 生效

在 IDEA 底部 `Terminal` 里执行：

```bat
gradlew.bat --version
```

只要不报错就说明 wrapper 正常。真正的验证在下一步同步时看日志。

---

## 6. 第一次同步（下载依赖）

1. 右侧边栏点 **Gradle** 图标（找不到就 `View → Tool Windows → Gradle`）。
2. 点面板左上角的 **Reload All Gradle Projects**（两个箭头的刷新按钮）。
3. 底部 `Build` 窗口会依次下载：

| 顺序 | 内容 | 大致体积 |
|------|------|----------|
| 1 | Gradle 8.10.2（腾讯云镜像） | ~130 MB |
| 2 | ModDevGradle 插件（阿里云 / 插件门户） | ~5 MB |
| 3 | NeoForge 21.1.250 userdev（**走代理**） | ~50–150 MB |
| 4 | Minecraft 1.21.1 客户端/服务端（Mojang） | ~250 MB |
| 5 | 各种库（Mixin、ASM、Gson…） | ~100 MB |

总计数百 MB，视网速 5–20 分钟。缓存落在 `C:\Users\Administrator\.gradle\caches`。

**判断是否卡住**：`Build` 窗口如果超过 5 分钟没有任何新行，且最后一行是
`Downloading https://maven.neoforged.net/...`，就是代理没生效，回到 5.2 检查。

---

## 7. 构建 jar

同步成功（Gradle 面板出现 `Tasks`）后：

### 方式一：图形界面

`Gradle` 面板 → `Tasks` → `build` → 双击 **`build`**。

### 方式二：终端（推荐，报错更清楚）

```bat
cd /d D:\deepseek\helios
gradlew.bat build
```

首次构建会执行 ModDevGradle 的 NeoForm 处理（合并、打补丁、重映射），
大概 3–10 分钟，需要 ~2 GB 内存（`gradle.properties` 已设 `-Xmx3G`）。

### 产物路径

```
D:\deepseek\helios\build\libs\sunflare-0.0.2-beta+mc1.21.1.jar
```

文件名里的版本来自 `gradle.properties` 的 `mod_version`，每次改内容递增即可。

这个就是要装进游戏的 jar。

---

## 8. 测试

### 8.1 开发环境直接跑（推荐先这样验证）

```bat
gradlew.bat runClient
```

第一次会额外下载游戏资源（assets），比较慢。起来后：

- 创造模式物品栏里找 **Sunflare Solar（耀阳光能板）** 标签页；
- 放一块板子，空手右键能看到额定输出与缓冲；
- 白天看顶面的能量流动动画。

### 8.2 装进正式游戏

1. 用启动器给 **1.21.1** 装 **NeoForge 21.1.x**。
2. 把 `build\libs\sunflare-0.0.2-beta+mc1.21.1.jar` 复制到：
   ```
   %APPDATA%\.minecraft\mods
   ```
3. 启动游戏，确认 `Mods` 列表里有 Sunflare Solar。

---

## 9. 常见报错对照表

| 报错关键字 | 原因 | 处理 |
|---|---|---|
| `Could not resolve net.neoforged:neoforge:21.1.250` | 代理没生效，或该 build 不存在 | 先确认 5.2；再换 `neo_version=21.1.190` 之类实际存在的版本 |
| `Could not resolve net.neoforged:neoform-runtime:1.0.13` | **没挂代理时最典型的报错**：这是构建过程中第一次真正访问 neoforged | 配好 5.2 的 `systemProp.*` 后 `gradlew.bat --stop` 再试 |
| `Connect to maven.neoforged.net:443 ... timed out` | Gradle 没走代理 | 写 `systemProp.*`，然后 `gradlew.bat --stop` |
| `Plugin [id: 'net.neoforged.moddev'] was not found` | 插件仓库不通 | `settings.gradle` 已配阿里云 + 插件门户；检查代理后重新同步 |
| `Unsupported class file major version 69` | 用了 JDK 25 | Gradle JVM 与 Project SDK 都改成 21 |
| `No matching toolchains found ... languageVersion=21` | 没装/没识别 JDK 21 | Project Structure 里指定 `jdk-21.0.11` |
| `Unsupported Gradle version: 9.8.2` | 用了系统 Gradle | 改用 wrapper（第 4 步第 4 点） |
| `Could not find or load main class org.gradle.wrapper.GradleWrapperMain` | wrapper jar 丢失 | 工程里已带 `gradle/wrapper/gradle-wrapper.jar`，别删 |
| `OutOfMemoryError: Java heap space` | 内存不够 | 调大 `org.gradle.jvmargs=-Xmx4G` |
| `Read timed out`（下载大文件时） | 代理慢 | 换节点；或在 `gradle.properties` 加 `systemProp.org.gradle.internal.http.socketTimeout=180000` |
| 中文乱码（控制台） | 编码 | 本项目源码全 UTF-8，`build.gradle` 已设 `options.encoding = 'UTF-8'` |

---

## 10. 镜像与地址速查

| 用途 | 地址 | 已配置在哪 |
|---|---|---|
| Gradle 发行版 | `https://mirrors.cloud.tencent.com/gradle/gradle-8.10.2-bin.zip` | `gradle/wrapper/gradle-wrapper.properties` |
| Gradle 插件镜像 | `https://maven.aliyun.com/repository/gradle-plugin` | `settings.gradle` |
| Maven Central 镜像 | `https://maven.aliyun.com/repository/public` | `settings.gradle` + `build.gradle` |
| NeoForge 官方 | `https://maven.neoforged.net/releases` | `settings.gradle`（必须走代理） |

想换 Gradle 版本：改 `gradle-wrapper.properties` 的 `distributionUrl`，
腾讯云镜像的目录结构是 `https://mirrors.cloud.tencent.com/gradle/gradle-<版本>-bin.zip`。

---

## 11. 项目结构

```
sunflare/
├─ build.gradle                        ModDevGradle 2.0.78 + 阿里云仓库
├─ settings.gradle                     插件仓库（阿里云镜像优先）
├─ gradle.properties                   版本号、代理开关、内存
├─ gradlew / gradlew.bat               Gradle 8.10.2 wrapper
├─ gradle/wrapper/                     wrapper jar + properties（已含腾讯镜像）
├─ src/main/templates/META-INF/
│   └─ neoforge.mods.toml              模组元数据（占位符由 Gradle 替换）
├─ src/main/resources/
│   ├─ pack.mcmeta
│   ├─ assets/sunflare/
│   │   ├─ blockstates/                6 个 blockstate
│   │   ├─ models/block|item/          6+6 个模型
│   │   ├─ lang/                       en_us / zh_cn
│   │   └─ textures/block/             18 张 PNG + 6 个 .mcmeta（顶面 16 帧动画）
│   └─ data/sunflare/
│       ├─ recipe/                     6 条扁平配方（互不嵌套）
│       └─ loot_table/blocks/          6 张掉落表
├─ src/main/java/dev/sunflare/
│   ├─ Sunflare.java                     @Mod 入口
│   ├─ SunflareRegistry.java             注册方块/物品/方块实体/标签页/FE 能力
│   ├─ SolarTier.java                  六档数值与 AE、J 换算
│   └─ block/
│       ├─ SolarPanelBlock.java        方块 + ticker + 右键读数
│       └─ SolarPanelBlockEntity.java  发电曲线、天气遮挡、IEnergyStorage
├─ tools/make_textures.py              贴图/动画生成脚本
├─ docs/
│   ├─ BUILD-WITH-IDEA.md              本文档
│   ├─ preview-static.png              8× 静态对照
│   ├─ preview-filmstrip.png           16 帧胶片
│   └─ preview-animation.gif           动画预览
├─ README.md
├─ LICENSE
└─ .gitignore
```

---

## 12. 命令行等价操作（不打开 IDEA 也行）

```bat
cd /d D:\deepseek\helios
gradlew.bat --stop
gradlew.bat build
dir build\libs
```

前提同样是：`gradle.properties` 里的代理已打开。
