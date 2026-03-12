# AI 捕获系统

## [AI 捕获系统产品设计方案.md](AI%20%E6%8D%95%E8%8E%B7%E7%B3%BB%E7%BB%9F%E4%BA%A7%E5%93%81%E8%AE%BE%E8%AE%A1%E6%96%B9%E6%A1%88.md)

- 一套 完整的AI 捕获卡片系统产品设计方案（AI Conversational Capture System）`。
- 很多 AI 产品（知识管理、笔记、信息捕获、收藏类产品）正在采用的 对话式结构化生产模型。
- 内容包括：产品架构 → 交互设计 → AI系统 → 数据结构 → 状态机 → Prompt → UI组件。

### [AI 捕获系统对话脚本.md](AI%20%E6%8D%95%E8%8E%B7%E7%B3%BB%E7%BB%9F%E5%AF%B9%E8%AF%9D%E8%84%9A%E6%9C%AC.md)

- 让 用户通过 2–5 轮对话就能生成一张卡片，同时保持 自然聊天 + 结构化信息收集。

### [AI 捕获系统的对话状态机](AI%20%E6%8D%95%E8%8E%B7%E7%B3%BB%E7%BB%9F%E7%9A%84%E5%AF%B9%E8%AF%9D%E7%8A%B6%E6%80%81%E6%9C%BA.md)

- 重要性：真正决定系统是否稳定的核心架构
- 目标：让系统：
    - 稳定管理对话流程
    - 支持多轮补充
    - 支持用户随时修改
    - 避免对话混乱

### [AI 捕获系统完整架构](AI%20%E6%8D%95%E8%8E%B7%E7%B3%BB%E7%BB%9F%E5%AE%8C%E6%95%B4%E6%9E%B6%E6%9E%84.md)

* Conversation Engine
* Card Engine
* AI Pipeline
* Prompt System
* Memory / Context

### [AI捕获系统的 UI 交互设计](AI%E6%8D%95%E8%8E%B7%E7%B3%BB%E7%BB%9F%E7%9A%84%20UI%20%E4%BA%A4%E4%BA%92%E8%AE%BE%E8%AE%A1.md)

- 如何显示 Chat 对话 和 Card 预览

### [AI 捕获系统的操作组件体系](AI%20%E6%8D%95%E8%8E%B7%E7%B3%BB%E7%BB%9F%E7%9A%84%E6%93%8D%E4%BD%9C%E7%BB%84%E4%BB%B6%E4%BD%93%E7%B3%BB.md)

- 如何在聊天中 **动态插入 UI 操作模块**，让用户不需要输入。
- 这其实是 **对话型产品体验差异最大的地方**。

### [AI 捕获系统的 Prompt + Tool 调度架构（LLM Agent Architecture）](AI%20%E6%8D%95%E8%8E%B7%E7%B3%BB%E7%BB%9F%E7%9A%84%20Prompt%20%2B%20Tool%20%E8%B0%83%E5%BA%A6%E6%9E%B6%E6%9E%84%EF%BC%88LLM%20Agent%20Architecture%EF%BC%89.md)

- 让 LLM 不只是聊天，而是 **可控地调用系统能力（Tools）来完成卡片生成与编辑**。

### [AI 捕获系统的信息结构模型（Universal Card Schema）.md](AI%20%E6%8D%95%E8%8E%B7%E7%B3%BB%E7%BB%9F%E7%9A%84%E4%BF%A1%E6%81%AF%E7%BB%93%E6%9E%84%E6%A8%A1%E5%9E%8B%EF%BC%88Universal%20Card%20Schema%EF%BC%89.md)

- 解决问题：为什么很多 AI 记录系统 **越用越乱、越来越难搜索**。

### [AI 捕获系统的信息组织模型（Knowledge Organization Model）.md](AI%20%E6%8D%95%E8%8E%B7%E7%B3%BB%E7%BB%9F%E7%9A%84%E4%BF%A1%E6%81%AF%E7%BB%84%E7%BB%87%E6%A8%A1%E5%9E%8B%EF%BC%88Knowledge%20Organization%20Model%EF%BC%89.md)

- 解决问题：为什么很多 AI 笔记产品 **一开始很好用，但用 3 个月就乱了**。

### [AI 捕获系统的卡片类型体系设计（Card Type System）.md](AI%20%E6%8D%95%E8%8E%B7%E7%B3%BB%E7%BB%9F%E7%9A%84%E5%8D%A1%E7%89%87%E7%B1%BB%E5%9E%8B%E4%BD%93%E7%B3%BB%E8%AE%BE%E8%AE%A1%EF%BC%88Card%20Type%20System%EF%BC%89.md)

- 解决问题：为什么很多产品 **卡片越多越难用**。

## [AI 捕获系统架构蓝图.md](AI%20%E6%8D%95%E8%8E%B7%E7%B3%BB%E7%BB%9F%E6%9E%B6%E6%9E%84%E8%93%9D%E5%9B%BE.md)

## 其他

### 1.[AI 捕获系统核心技术文档](others/AI%20%E6%8D%95%E8%8E%B7%E7%B3%BB%E7%BB%9F%E6%A0%B8%E5%BF%83%E6%8A%80%E6%9C%AF%E6%96%87%E6%A1%A3.md)

- **Embedding Similarity**: 将文本、图片或其他内容转换为高维向量（embedding），用向量空间计算语义相似度。
- **LLM tagging** 利用大语言模型（LLM）根据用户输入或卡片内容自动生成标签、抽取实体、判断卡片类型。
- **Vector Database**: 专门存储和检索 **高维向量（embedding）** 的数据库，用于语义搜索、相似度检索和推荐系统。
- **AI 捕获系统 + LLM Tagging + Vector Database 集成流程图**
