# CAPTURE_REVIEW_EDITING_FOCUSED

> Review 编辑态：AI 结果已回填，用户进行复核、修订与确认。

实现细节参考：[code-spec.md](../code-spec.md#review-editing-focused)

## 1. State Definition（状态定义）

- State Name: `CAPTURE_REVIEW_EDITING_FOCUSED`
- Definition: AI 分析结果已生成并进入可编辑复核阶段，用户可调整内容后确认发布。
- Preconditions:
  - 来自 `CAPTURE_AI_PROCESSING_INPUT_LOCKED` 的成功分支。
  - Review 数据已可用。
- Exit Conditions:
  - 用户确认发布，进入 `CAPTURE_POST_WAITING_LOCKED`。
  - 用户离开流程，退出模块。

## 2. State Position in Flow（状态在流程中的位置）

- Previous: `CAPTURE_AI_PROCESSING_INPUT_LOCKED`
- Current: `CAPTURE_REVIEW_EDITING_FOCUSED`
- Next: `CAPTURE_POST_WAITING_LOCKED`
- Skip Allowed: No
- Rollback Allowed: No
- Parallel: No

## 3. Core Semantics（核心语义）

- User Perspective: 我正在审阅和修改 AI 结果，并决定是否发布。
- System Perspective: 提供结构化编辑能力与预览能力，等待显式确认。
- Why Not Merge: 与 Ready/Processing/Post 合并会破坏“可编辑审阅”语义。

## 4. Layout Contract（结构约束）

- Header Region: 关闭、意图切换、确认动作。
- Main Region: 复核编辑区（文本/代码/媒体/标题/样式/标签）。
- Side Region: 结果预览区。
- Background Region: 稳定背景语义。

## 5. Interaction Rules（交互规则）

- 主内容支持编辑与选择主内容类型。
- 样式选择与预览联动。
- 标签管理支持增删与输入。
- 确认动作为唯一流程前进入口。

## 6. Visual Rules（视觉规则）

- 编辑区应具备明确可编辑反馈。
- 预览区是次焦点，不应抢占主编辑任务。
- 不使用处理态视觉（遮罩、加载旋转）覆盖可编辑区域。

## 7. Motion Contract（动效约束）

- 允许：hover、focus、选中态过渡。
- 禁止：处理态持续动效覆盖编辑操作。
- 实现参数参考：[code-spec.md](../code-spec.md#motion-review-editing-focused)

## 8. Negative Requirements（明确禁止）
- 实现参数参考：[code-spec.md](../code-spec.md#motion-review-editing-focused)

- 不允许在该态锁定全部输入。
- 不允许隐藏确认路径。
- 不允许将失败提示常驻覆盖主编辑区域。

## 9. Validation Checklist（验收清单）

- Review 数据可见且可编辑。
- 预览区可见并保持次级层级。
- 确认动作清晰可触发。
- 状态语义与交互一致。

## 10. One-line Definition（一句话定义）

`CAPTURE_REVIEW_EDITING_FOCUSED` 是 Capture 的结果复核编辑状态。
