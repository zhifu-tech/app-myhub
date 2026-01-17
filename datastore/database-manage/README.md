# Database Manage Module

数据库管理模块，用于从 JSON 文件加载初始化数据到数据库。

## 📋 功能

- ✅ **从 JSON 文件加载数据**：从 Compose Resources 加载 JSON 数据并写入数据库
- ✅ **支持按表分类的数据文件**：Card, Tag, User, Template
- ✅ **支持重复加载**：可以多次写入数据
- ✅ **支持清空数据后重新加载**：`clearBeforeLoad` 参数
- ✅ **支持用户关联**：所有业务数据（Card、Tag、Template）会自动关联到用户
- ✅ **跨平台支持**：支持所有平台（Android、iOS、JVM、JS、WASM）
- ✅ **单元测试**：完整的测试覆盖

