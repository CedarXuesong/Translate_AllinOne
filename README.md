# Translate All in One
- - -

- Minecraft 版本: 1.21.5
- Mod 加载器版本 (Fabric): 0.16.14
- Fabric API 版本: 0.128.1+1.21.5
- Java 版本: 21
- 项目包名: com.cedarxuesong.translate_allinone

这是一个游戏内AI翻译模组，有多种翻译功能。
使用openaiApi或ollamaApi,拥有多线程并行翻译能力,同时支持流式和非流式响应。
- TODO LIST:
  - 聊天翻译
    - 使用mixin注入聊天栏gui内
      - 多线程响应翻译结果在原位替换内容
    - 使用mixin注入聊天信息的数据包处理
      - 添加"[T]"(可自定义)在消息后，这串字符带单独的指令响应和鼠标悬浮文字，点击发送/translate_allinone translate chatline < MessageId >
    - 响应翻译命令
      - 使用多线程响应翻译命令，有流式和非流式
        - 非流式在响应结束后在消息原位置替换消息。
        - 流式响应实时替换翻译内容
  - 物品hover信息栏翻译
    - 使用mixin注入物品数据包和物品栏数据包
    - 使用翻译模板转换：将数字和使用符号分隔的数字转换为占位符。
    - 将翻译的模板缓存起来
    - 使用哈希匹配，模板原文和翻译后的模板供查找。
  - 侧板计分板翻译
    - 待定
  - 顶部Tab栏翻译
    - 待定
  - 配置管理器
    - 使用不同的配置项POJO方便查找
    - 使用cloth-config-fabric自动创建配置Gui
  - 缓存管理器
    - 将不同功能模块的翻译模板缓存到不同的文件。
  - 配置Gui (通过cloth-config完成)
    - 分别可以设置不同模块的翻译设置，不同模块的翻译模型等是分开的
      - openai兼容
        - 模型baseurl
        - 模型apikey
        - 模型id
        - 模型温度
        - 自定义参数
      - ollamaApi
        - 模型url
        - 模型id
        - 模型温度
        - 自定义参数
    - 可以设置目标语言
      - 暂时不提供可以直接修改的系统提示词

暂时这些内容，后面待定

## 已完成：
- llmapi [示例程序](doc/llmapi/Example.md)
  - openaiApi
  - ollamaApi
- 模组配置管理器
- 配置设置UI