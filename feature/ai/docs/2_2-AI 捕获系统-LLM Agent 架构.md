# 📄 PRD：AI 捕获系统 LLM Agent 架构（Prompt + Tool 调度）V1.0

---

# 一、文档信息

## 1.1 基本信息

* 版本：V1.0
* 类型：LLM Agent 架构设计（Prompt + Tool 调度）
* 适用对象：AI工程 / 后端 / 架构师

---

## 1.2 文档目标

构建一个：

```text id="agent_goal"
可控决策 + Tool执行 + 状态机约束 + 可扩展
```

的 AI Agent 系统，使 LLM 从“聊天模型”升级为：

```text id="agent_upgrade"
可执行任务的系统调度中枢
```

---

# 二、核心设计原则（统一架构约束）

## 2.1 三权分立（必须遵守）

```text id="core_principle"
LLM Agent → 负责决策（Reasoning）
Tool Layer → 负责执行（Execution）
State Machine → 负责流程控制（Control）
Card Engine → 负责数据一致性（Validation）
```

---

## 2.2 强约束规则

```text id="hard_rules"
1. AI 不允许直接修改数据
2. 所有写操作必须通过 Tool
3. Tool 必须经过 Card Engine
4. 状态流转必须受 State Machine 控制
```

---

# 三、整体架构（Agent Architecture）

## 3.1 标准流程

```text id="agent_flow"
User Input
    ↓
Conversation Engine（State Machine）
    ↓
Prompt System
    ↓
LLM Agent（Reasoning）
    ↓
Tool Planner（选择工具）
    ↓
Tool Execution（执行）
    ↓
Card Engine（校验）
    ↓
Storage
    ↓
Response Builder
    ↓
UI Response
```

---

## 3.2 完整结构图

```text id="agent_arch"
User
 │
 ▼
UI（Chat + Card）
 │
 ▼
Conversation Engine
（State Machine + Context + Slot）
 │
 ▼
LLM Agent
 │
 ├── Intent Reasoning
 ├── Decision Making
 └── Tool Call(JSON)
 │
 ▼
Tool Layer（协议化）
 │
 ▼
Card Engine（唯一写入口）
 │
 ▼
Storage（DB + Vector）
 │
 ▼
Knowledge Layer（RAG）
 │
 ▼
Response Builder
 │
 ▼
UI
```

---

# 四、Prompt 系统设计（分层）

## 4.1 三层 Prompt 架构

```text id="prompt_layers"
System Prompt（规则层）
Task Prompt（上下文层）
Tool Prompt（能力层）
```

---

## 4.2 System Prompt（系统规则）

定义 AI 行为边界（必须严格）。

```text id="system_prompt"
You are an AI assistant for structured information capture.

Rules:
1. You must not modify any data directly
2. You must use tools for any data operation
3. Never hallucinate missing information
4. Ask for missing fields if required
5. Follow the current conversation state strictly
```

---

## 4.3 Task Prompt（状态驱动）

必须包含（与状态机强绑定）：

```json id="task_prompt"
{
  "state": "INFO_COLLECT",
  "card_schema": "place",
  "current_card": {},
  "missing_fields": [],
  "allowed_actions": [],
  "forbidden_actions": []
}
```

---

## 4.4 关键改动（重要）

👉 新增：

```text id="task_upgrade"
allowed_actions（状态允许行为）
forbidden_actions（状态禁止行为）
```

用于：

```text id="state_guard"
约束 LLM 行为（State Guard）
```

---

## 4.5 Tool Prompt（工具协议）

统一工具描述：

```json id="tool_prompt"
{
  "name": "update_card",
  "description": "Update fields of a card",
  "parameters": {
    "card_id": "string",
    "fields": {
      "key": "value"
    }
  }
}
```

---

# 五、Tool Layer（协议化执行层）

## 5.1 统一调用协议（必须）

```json id="tool_protocol"
{
  "tool": "update_card",
  "arguments": {
    "card_id": "123",
    "fields": {
      "title": "一兰拉面体验"
    }
  }
}
```

---

## 5.2 Tool Registry（新增）

```text id="tool_registry"
统一注册工具
统一校验参数
统一权限控制
```

---

## 5.3 标准工具集合

```text id="tool_list"
create_card
update_card
suggest_tags
analyze_image
summarize_content
extract_entities
publish_card
search_cards
retrieve_similar_cards（新增）
```

---

## 5.4 新增工具（关键）

```text id="rag_tool"
retrieve_similar_cards
```

用于：

```text id="rag_usage"
增强上下文（RAG）
避免重复记录
提高标签与分类质量
```

---

# 六、LLM Agent 决策流程

## 6.1 标准决策模型

```text id="reason_model"
Reason → Decide → Act
```

---

## 6.2 执行流程

```text id="agent_steps"
1. 读取 Task Prompt（状态 + 卡片）
2. 判断当前 State
3. 推理下一步动作
4. 判断是否调用 Tool
5. 输出 JSON（Tool 或 Response）
```

---

## 6.3 示例

用户：

```text id="example_input"
昨天在上海吃了一兰拉面
```

---

LLM 输出（标准）：

```json id="example_tool"
{
  "tool": "create_card",
  "arguments": {
    "title": "一兰拉面",
    "type": "place"
  }
}
```

---

# 七、Tool Chain（工具链）

## 7.1 多工具执行

复杂任务：

```text id="tool_chain"
analyze_content
↓
extract_entities
↓
summarize_content
↓
create_card
```

---

## 7.2 执行方式（重要调整）

```text id="chain_rule"
LLM 不直接执行链
由 Tool Orchestrator 执行
```

---

# 八、状态机集成（关键改造）

## 8.1 Agent 必须受 State Machine 控制

```text id="state_bind"
LLM 行为 = State + Allowed Actions
```

---

## 8.2 示例

```json id="state_example"
{
  "state": "INFO_COLLECT",
  "allowed_actions": [
    "add_field",
    "skip_field"
  ],
  "forbidden_actions": [
    "publish_card"
  ]
}
```

---

## 8.3 State Guard（新增）

作用：

```text id="state_guard_role"
拦截非法 Tool 调用
防止越权行为
```

---

# 九、Memory / Context（上下文系统）

## 9.1 数据结构

```json id="memory_struct"
{
  "conversation_id": "",
  "state": "",
  "card_id": "",
  "missing_fields": [],
  "last_action": "",
  "summary": ""
}
```

---

## 9.2 优化机制（新增）

```text id="memory_opt"
短期记忆（Redis）
长期记忆（DB）
对话压缩（Summarization）
```

---

# 十、Response Builder（响应构建）

## 10.1 输出结构

```json id="response_struct"
{
  "message": "",
  "components": [],
  "card_preview": {}
}
```

---

## 10.2 UI绑定（新增）

```text id="ui_bind"
组件必须绑定 State + Action
```

---

# 十一、完整执行流程（生产级）

```text id="full_flow"
User Input
↓
Conversation Engine（State）
↓
Retrieve Context（RAG）
↓
LLM Agent（Decision）
↓
Tool Call（JSON）
↓
Tool Layer
↓
Card Engine（校验）
↓
Storage
↓
Update State
↓
Response Builder
↓
UI
```

---

# 十二、稳定性策略（生产必须）

## 12.1 输出约束

```text id="output_rule"
必须 JSON 格式
```

---

## 12.2 失败处理

```text id="fail_strategy"
AI失败 → 重试
Tool失败 → 回滚
严重错误 → 降级（只生成草稿）
```

---

## 12.3 幂等性（新增）

```text id="idempotent"
Tool 调用必须支持幂等
```

---

# 十三、系统整体映射

```text id="mapping"
Conversation Engine → 控制流程
LLM Agent → 决策
Tool Layer → 执行
Card Engine → 校验
Storage → 存储
Knowledge Layer → 增强
```

---

# 十四、最终架构总结

## 14.1 完整结构

```text id="final_arch"
User
 │
UI
 │
Conversation Engine（State Machine）
 │
LLM Agent（Reasoning）
 │
Tool Layer（Execution）
 │
Card Engine（Validation）
 │
Storage
 │
Knowledge Layer
```

---

## 14.2 核心原则总结

```text id="final_rules"
LLM 只思考
Tool 只执行
State Machine 控制流程
Card Engine 控制数据
```

---

# 📌 最关键升级点总结

本版本相对原始设计已完成关键升级：

* ✅ 引入 **State Guard（防越权）**
* ✅ Tool 调用 **标准协议化**
* ✅ 引入 **Tool Registry**
* ✅ 接入 **RAG（Knowledge Layer）**
* ✅ Tool Chain 由系统编排（非 LLM）
* ✅ Prompt 与 State Machine 强绑定
* ✅ 增加幂等性与失败处理

---

# 🚀 最终一句话

```text id="final_sentence"
LLM Agent = 决策中枢，但必须被系统严格约束
```

---

如果你下一步继续深入，这一层可以直接再往下落：

* 👉 Tool API 定义（OpenAPI / 后端接口）
* 👉 Prompt 模板（可直接上线）
* 👉 Agent 执行器（伪代码 / 框架设计）

这一步已经进入“可直接开发 AI Agent”的阶段了。
