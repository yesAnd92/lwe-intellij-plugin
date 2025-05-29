
# LWE IntelliJ Plugin
lwe是leave work early的缩写，也就是"早点下班"！，早完活，早摸鱼！🤣🤣🤣,它是 [lwe](https://github.com/yesAnd92/lwe)的IntelliJ 的插件版本。借助LLM的AI能力，提高工作效率。

## 主要功能

### 🤖 AI 提交消息生成

插件的核心功能是通过 AI 分析代码变更自动生成提交消息：

- **智能分析**：自动分析暂存的代码变更和 Git diff
- **流式响应**：实时显示 AI 生成的提交消息
- **规范格式**：生成符合 Conventional Commits 规范的提交消息
- **多语言支持**：支持根据配置的语言环境生成对应语言的提交消息

### 🔍 MyBatis SQL 日志解析

将 MyBatis 输出的 SQL 日志转换为可执行的 SQL 语句：

- **日志解析**：自动识别 MyBatis 日志中的 "Preparing:" 和 "Parameters:" 部分
- **参数替换**：将 SQL 模板中的 `?` 占位符替换为实际参数值 
- **类型处理**：根据参数类型自动添加引号（String、Timestamp、Date、Time 类型
- **控制台集成**：在控制台编辑器中右键菜单提供转换功能 

### 🛠️ 代码转换工具

提供实用的代码转换功能：

- **Java PO 转 JSON**：将 Java POJO 类转换为 JSON 格式
- **Go 结构体转 JSON**：支持 Go 语言结构体的 JSON 转换

## 使用方式

### AI 提交消息生成
1. 在 IntelliJ IDEA 中安装插件
2. 配置 LLM 服务提供商的 API 密钥和端点
3. 在 VCS 提交界面中暂存需要提交的代码变更
4. 点击 AI 提交按钮，插件将自动分析变更并生成提交消息
5. 查看并确认生成的提交消息，然后完成提交

### MyBatis SQL 日志解析
1. 在控制台中选择包含 MyBatis 日志的文本
2. 右键选择 "Sql log parse" 选项
3. 插件自动解析日志并生成可执行的 SQL 语句
4. 转换后的 SQL 自动复制到剪贴板 

### 代码转换工具
1. 选择要转换的代码或复制到剪贴板
2. 使用转换动作将代码转换为 JSON 格式
3. 转换结果自动复制到剪贴板

## 支持的服务

- **SiliconFlow**：当前支持的 LLM 服务提供商

## 特性

- ✅ 与 IntelliJ IDEA VCS 系统深度集成
- ✅ 支持多仓库项目的差异分析
- ✅ 实时流式响应显示
- ✅ 错误处理和用户友好的通知
- ✅ 多语言提示模板支持
- ✅ 支持多种编程语言的代码转换
- ✅ MyBatis 日志智能解析和 SQL 生成

## 技术栈

- **开发语言**：Java
- **构建工具**：Gradle
- **IDE 平台**：IntelliJ Platform SDK
- **HTTP 客户端**：OkHttp
- **JSON 处理**：Gson
- **VCS 集成**：Git4Idea

Wiki pages you might want to explore:
- [Plugin Configuration (yesAnd92/lwe-intellij-plugin)](/wiki/yesAnd92/lwe-intellij-plugin#2)
- [SQL Log Conversion (yesAnd92/lwe-intellij-plugin)](/wiki/yesAnd92/lwe-intellij-plugin#3.2)
