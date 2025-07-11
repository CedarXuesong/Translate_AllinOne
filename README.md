# Translate All in One

> **⚠️ Preview Warning**
>
> Currently only supports >=1.21.5
> This is a preview version of a mod that is actively being developed. Features may be unstable or have unforeseen bugs.
> You are welcome to report issues, suggest features, or share your ideas by submitting **Issues**. Your feedback is crucial to us!

A powerful in-game AI real-time translation mod for Minecraft, based on Fabric.

## ✨ Features

- **Chat Translation**: Real-time translation of chat messages, supporting streaming responses for a seamless communication experience.
- **Item Translation**: Automatically translates item names and descriptions (Lore), supporting template caching, efficient and resource-saving.
- **Scoreboard Translation**: Real-time translation of sidebar scoreboard content.
- **Highly Configurable**: Through the in-game menu (requires [ModMenu](https://www.curseforge.com/minecraft/mc-mods/modmenu)), you can easily configure:
    - Support for multiple AI service providers (OpenAI, Ollama).
    - Set independent translation models and parameters for different functions (chat, items, scoreboard).
    - Customize API address, key, model ID, etc.
- **Smart Caching**: Automatically caches translation results, reducing repeated requests and improving performance.

## 🛠️ Installation and Usage

1. Make sure you have installed [Fabric Loader](https://fabricmc.net/).
2. Download the latest version of this mod and the required dependencies:
    - [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) (required)
    - [Cloth Config API](https://www.curseforge.com/minecraft/mc-mods/cloth-config) (required)
    - [ModMenu](https://www.curseforge.com/minecraft/mc-mods/modmenu) (recommended, for in-game configuration)
3. Place all downloaded `.jar` files into the `mods` folder of your Minecraft instance.
4. Launch the game, find `Translate All in One` in the ModMenu configuration interface, and set your AI service provider information.

## 🔧 Configuration

This mod uses the Cloth Config API to provide a detailed configuration interface. You can perform the following settings in ModMenu:

- **General Settings**:
  - Target translation language.
- **Service Provider Settings (OpenAI / Ollama)**:
  - API Base URL
  - API Key (Only for OpenAI)
  - Model ID
  - Model Temperature (Temperature)
  - Other custom parameters

## 🤝 Contribution

We welcome any form of contribution! If you find any bugs or have feature suggestions, feel free to submit them on this project's GitHub Issues page.

## 📝 To-Do List (TODO)

- [x] Sidebar Scoreboard Translation

## 📜 License

This project is licensed under the [MIT License](./LICENSE).

# Translate All in One

> **⚠️ 预览版警告**
>
> 暂时只支持 >=1.21.5
> 这是一个正在积极开发中的模组预览版。功能可能尚不稳定或存在未预见的错误。
> 欢迎您通过提交 **Issues** 来报告问题、提出建议或分享您的想法。您的反馈对我们至关重要！

一款为 Minecraft 打造的功能强大的游戏内 AI 实时翻译模组，基于 Fabric。

## ✨ 功能特性

- **聊天翻译**: 实时翻译聊天信息，支持流式响应，带来无缝的交流体验。
- **物品翻译**: 自动翻译物品的名称和描述 (Lore)，支持模板缓存，高效且节省资源。
- **计分板翻译**: 实时翻译侧边栏计分板内容。
- **高度可配置**: 通过游戏内菜单 (需要 [ModMenu](https://www.curseforge.com/minecraft/mc-mods/modmenu))，您可以轻松配置：
    - 支持多种 AI 服务商 (OpenAI, Ollama)。
    - 为不同功能（聊天、物品、计分板）设置独立的翻译模型和参数。
    - 自定义 API 地址、密钥、模型 ID 等。
- **智能缓存**: 自动缓存翻译结果，减少重复请求，提升性能。

## 🛠️ 安装与使用

1.  确保您已安装 [Fabric Loader](https://fabricmc.net/)。
2.  下载本模组的最新版本以及所需的前置模组：
    - [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) (必需)
    - [Cloth Config API](https://www.curseforge.com/minecraft/mc-mods/cloth-config) (必需)
    - [ModMenu](https://www.curseforge.com/minecraft/mc-mods/modmenu) (推荐, 用于游戏内配置)
3.  将所有下载的 `.jar` 文件放入您 Minecraft 实例的 `mods` 文件夹中。
4.  启动游戏，在 ModMenu 的配置界面中找到 `Translate All in One`，然后设置您的 AI 服务商信息。

## 🔧 配置

本模组使用 Cloth Config API 提供详细的配置界面。您可以在 ModMenu 中进行以下设置：

- **通用设置**:
  - 目标翻译语言。
- **服务商设置 (OpenAI / Ollama)**:
  - API Base URL
  - API Key (仅 OpenAI)
  - 模型 ID
  - 模型温度 (Temperature)
  - 其他自定义参数

## 🤝 贡献

我们欢迎任何形式的贡献！如果您发现任何 Bug 或有功能建议，请随时在本项目的 GitHub Issues 页面提交。

## 📝 待办事项 (TODO)

- [x] 侧边栏计分板翻译

## 📜 许可证

本项目采用 [MIT License](./LICENSE) 授权。
