# AI 捕获系统 V1.0

## 1. 文档定位
- 版本：V1.0
- 类型：AI 捕获系统设计文档
- 阅读方式：先看术语注释，再阅读方案正文。

## 2. 术语注释
- Capture：将输入内容转化为结构化信息。
- Card：系统中的标准信息单元。
- Draft：发布前可编辑的草稿状态。
- Tool：由模型触发、用于执行真实系统动作的能力接口。
- State Machine：用于约束对话流程的状态控制机制。

## 3. 方案正文


## 文档导航

### [AI捕获系统产品设计方案V1.0.md](AI%E6%8D%95%E8%8E%B7%E7%B3%BB%E7%BB%9F%E4%BA%A7%E5%93%81%E8%AE%BE%E8%AE%A1%E6%96%B9%E6%A1%88V1.0.md)

- 一套完整的 AI 捕获卡片系统设计方案。
- 从产品流程、交互体验、AI能力、数据结构到发布机制全链路描述。

### [AI捕获系统对话脚本V1.0.md](AI%E6%8D%95%E8%8E%B7%E7%B3%BB%E7%BB%9F%E5%AF%B9%E8%AF%9D%E8%84%9A%E6%9C%ACV1.0.md)

- 通过 2-5 轮对话生成卡片的标准脚本。
- 覆盖文本、图片、链接、想法记录等场景。

### [AI捕获系统的对话状态机V1.0.md](AI%E6%8D%95%E8%8E%B7%E7%B3%BB%E7%BB%9F%E7%9A%84%E5%AF%B9%E8%AF%9D%E7%8A%B6%E6%80%81%E6%9C%BAV1.0.md)

- 用状态机约束对话流程。
- 支持补全、编辑、中断恢复、发布确认。

### [AI捕获系统完整架构V1.0.md](AI%E6%8D%95%E8%8E%B7%E7%B3%BB%E7%BB%9F%E5%AE%8C%E6%95%B4%E6%9E%B6%E6%9E%84V1.0.md)

- 从 Client 到 Storage 的完整分层架构。
- 定义 Conversation、AI Pipeline、Card Engine 的职责边界。

### [AI捕获系统的UI交互设计V1.0.md](AI%E6%8D%95%E8%8E%B7%E7%B3%BB%E7%BB%9F%E7%9A%84UI%E4%BA%A4%E4%BA%92%E8%AE%BE%E8%AE%A1V1.0.md)

- Chat + Card 双区布局。
- 对话输入、动作组件、实时预览的协同交互。

### [AI捕获系统的操作组件体系V1.0.md](AI%E6%8D%95%E8%8E%B7%E7%B3%BB%E7%BB%9F%E7%9A%84%E6%93%8D%E4%BD%9C%E7%BB%84%E4%BB%B6%E4%BD%93%E7%B3%BBV1.0.md)

- 对话内可操作组件体系。
- 包含 Micro/Form/Card 三层组件设计。

### [AI捕获系统的Prompt+Tool调度架构（LLM Agent Architecture）V1.0.md](AI%E6%8D%95%E8%8E%B7%E7%B3%BB%E7%BB%9F%E7%9A%84Prompt%2BTool%E8%B0%83%E5%BA%A6%E6%9E%B6%E6%9E%84%EF%BC%88LLM%20Agent%20Architecture%EF%BC%89V1.0.md)

- LLM 负责决策，Tool 负责执行。
- 通过受控工具调用保证可靠性与可追溯性。

### [AI捕获系统的信息结构模型（Universal Card Schema）V1.0.md](AI%E6%8D%95%E8%8E%B7%E7%B3%BB%E7%BB%9F%E7%9A%84%E4%BF%A1%E6%81%AF%E7%BB%93%E6%9E%84%E6%A8%A1%E5%9E%8B%EF%BC%88Universal%20Card%20Schema%EF%BC%89V1.0.md)

- 统一卡片数据模型。
- 对齐字段命名、层级和扩展机制。

### [AI捕获系统的信息组织模型（Knowledge Organization Model）V1.0.md](AI%E6%8D%95%E8%8E%B7%E7%B3%BB%E7%BB%9F%E7%9A%84%E4%BF%A1%E6%81%AF%E7%BB%84%E7%BB%87%E6%A8%A1%E5%9E%8B%EF%BC%88Knowledge%20Organization%20Model%EF%BC%89V1.0.md)

- 从捕获走向组织。
- 支持多维度查看与动态集合。

### [AI捕获系统的卡片类型体系设计（Card Type System）V1.0.md](AI%E6%8D%95%E8%8E%B7%E7%B3%BB%E7%BB%9F%E7%9A%84%E5%8D%A1%E7%89%87%E7%B1%BB%E5%9E%8B%E4%BD%93%E7%B3%BB%E8%AE%BE%E8%AE%A1%EF%BC%88Card%20Type%20System%EF%BC%89V1.0.md)

- 定义卡片类型、字段差异、UI 映射与扩展方式。

### [AI捕获系统架构蓝图V1.0.md](AI%E6%8D%95%E8%8E%B7%E7%B3%BB%E7%BB%9F%E6%9E%B6%E6%9E%84%E8%93%9D%E5%9B%BEV1.0.md)

- 端到端流程蓝图。
- 面向评审与跨角色协作的统一视图。

### [AI捕获系统核心技术文档V1.0.md](others/AI%E6%8D%95%E8%8E%B7%E7%B3%BB%E7%BB%9F%E6%A0%B8%E5%BF%83%E6%8A%80%E6%9C%AF%E6%96%87%E6%A1%A3V1.0.md)

- LLM Tagging、Embedding Similarity、Vector Database 三大能力说明。

## 术语注释

- Capture：将输入内容转化为可管理信息单元的过程。
- Card：系统中的标准信息单位。
- Draft：发布前可编辑的卡片草稿状态。
- Tool：由模型触发、实际执行业务动作的能力接口。

## 4. 图示规范
- 图示统一使用 `text` 代码块呈现。
- 流程图默认从上到下，模块图默认从左到右。
- 节点命名使用英文技术词，描述使用中文。
