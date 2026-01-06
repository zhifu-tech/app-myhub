# dSYM 说明文档

## 什么是 dSYM？

**dSYM (Debug Symbol)** 是 iOS/macOS 应用的调试符号文件，用于：

1. **崩溃报告符号化**：将崩溃日志中的内存地址转换为可读的函数名和行号
2. **调试支持**：在 Xcode 中调试时显示正确的函数名和变量名
3. **性能分析**：在 Instruments 中分析性能问题时显示符号信息

## 当前问题

Kotlin Multiplatform 生成的 Framework 的 dSYM 目录是**空的**（没有 DWARF 文件），这会导致：

1. **构建失败**：CocoaPods 的 `composeApp-copy-dsyms.sh` 脚本尝试复制 dSYM 时，会因为找不到 `Contents/Resources/DWARF` 目录而失败
2. **错误信息**：`ls: /path/to/dSYM/Contents/Resources/DWARF: No such file or directory`

## 解决方案

### 方案 1：修复脚本（当前方案）

在 `Podfile` 的 `post_install` hook 中修复 dSYM 复制脚本，添加空目录检查：

```ruby
post_install do |installer|
  compose_app_dsym_script = File.join(installer.sandbox.root, 'Target Support Files', 'composeApp', 'composeApp-copy-dsyms.sh')
  if File.exist?(compose_app_dsym_script)
    script_content = File.read(compose_app_dsym_script)
    # 添加空 dSYM 检查，跳过空的 dSYM 目录
    fixed_script = script_content.gsub(...)
    File.write(compose_app_dsym_script, fixed_script)
  end
end
```

**优点**：

- 允许构建继续，不会因为空的 dSYM 而失败
- 不影响其他功能

**缺点**：

- 需要每次 `pod install` 后修复脚本
- 无法进行崩溃符号化（但静态 Framework 通常不需要）

### 方案 2：禁用 dSYM 复制（推荐）

由于我们使用**静态 Framework**（`isStatic = true`），并且 dSYM 目录是空的，可以完全禁用 dSYM 复制：

1. 在 `composeApp.podspec` 中不声明 `vendored_dsym`
2. 或者在 `Podfile` 中禁用 dSYM 复制脚本

**优点**：

- 更简洁，不需要修复脚本
- 静态 Framework 通常不需要单独的 dSYM

**缺点**：

- 无法进行崩溃符号化（但对于静态 Framework 影响较小）

## 验证结果

**测试发现**：重新安装 Pods 后，CocoaPods **不会生成** `composeApp-copy-dsyms.sh` 脚本，因为：

1. `composeApp.podspec` 中**没有声明** `vendored_dsym`
2. CocoaPods 只会为声明了 `vendored_dsym` 的 pod 生成 dSYM 复制脚本
3. 因此**不需要** dSYM 修复脚本

## 建议

**对于静态 Framework**，**不需要 dSYM 修复脚本**，因为：

1. `podspec` 中没有声明 `vendored_dsym`，CocoaPods 不会生成 dSYM 复制脚本
2. dSYM 目录是空的，没有实际作用
3. 静态 Framework 的符号通常已经包含在主应用中
4. 简化配置，减少维护成本

**结论**：可以安全地去掉 `Podfile` 中的 dSYM 修复代码。

如果后续需要崩溃符号化，可以考虑：

- 使用动态 Framework（`isStatic = false`）
- 或者配置 Kotlin 生成完整的 dSYM 文件，并在 `podspec` 中声明 `vendored_dsym`
