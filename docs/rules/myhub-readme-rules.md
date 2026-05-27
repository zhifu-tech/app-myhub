# MyHub README.md 生成规则

**本规则定义了 MyHub 各 module 的 `README.md` 入口页规范。**

- **版本编号**：v2.1
- **适用对象**：`AI Copilot`、开发人员

## 1. 核心原则

1. `README.md` 只能作为**入口页**，不承载详细设计、完整使用教程、长篇分析、评审记录或实现细节。
2. 所有详细内容必须落地到对应 module 的 `docs/` 目录中。
3. 如果一个 module 的详细文档较多，应在 `docs/` 下建立普通索引文档；如果只有一个主文档，则直接链接该主文档。
4. 入口页必须短、稳、可扫描，目标是让人或 AI 在 10 秒内知道“这个 module 是什么、去哪里看详细内容”。

## 2. README.md 必须保留的内容

每个 module 的 `README.md` 只保留以下内容：

### 2.1 标题（必需）

使用一级标题，命名为：

```markdown
# Core [Module Name] Module
```

或根据模块类型使用更贴切的标题，例如：

- `# Feature Dashboard Module`
- `# Server Module`
- `# iOS App Module`

### 2.2 一段式概述（必需）

用 1 段话说明：

- 模块作用
- 适用场景
- 关键技术选型

要求：

- 不超过 2 句
- 不展开架构细节
- 不写长列表

### 2.3 文档入口（必需）

使用 `## 文档` 或 `## 相关文档`，只放指向 `docs/` 的链接。

推荐格式：

```markdown
## 文档

- [主方案文档](./docs/myhub-xxx-infra-v1.0.md)
- [模块概览](./docs/myhub-xxx-module-overview.md)
```

规则：

- 详细内容只能链接到 `./docs/` 或子目录下的文档
- 入口页不直接写大段实现细节
- 入口页不直接放完整代码示例

## 3. README.md 不允许承载的内容

以下内容必须下沉到 `docs/`：

- 架构设计全文
- 核心组件详解
- 完整使用示例
- 平台差异说明
- 测试策略和测试样例
- 迁移记录、评审意见、过程稿
- 研究材料、调研记录、评估报告
- 长篇 FAQ

## 4. docs/ 目录要求

1. 每个 module 的详细内容都应放在自己的 `docs/` 下。
2. 如果细节较多，应提供普通索引文档作为目录入口，不再新增 `docs/README.md`。
3. 版本化方案文档应采用统一命名：

```text
[module]-[topic]-infra-v[version].md
```

4. 如果 module 已经有 canonical 方案文档，`README.md` 只链接 canonical 文件，不重复内容。

## 5. 推荐结构

一个合格的 module `README.md` 示例结构如下：

```markdown
# Core Logger Module

本模块用于...（1-2 句概述）

## 文档

- [MyHub 日志模块方案设计](../docs/myhub-logger-infra-v1.0.md)
- [模块概览](./docs/myhub-logger-module-overview.md)
```

## 6. 维护要求

1. 新建或重构 module 时，优先把详细材料写入 `docs/`。
2. 修改 README 时，只维护入口层，不把详细章节搬回 README。
3. 删除或迁移文档后，必须检查 README 和其他文档中的旧链接。
