# 用户偏好新增字段操作流程

本文档记录在用户偏好（`user_preferences`）新增字段的完整改造路径，作为后续复用模板。

## 目标

将新字段从数据库层贯通到应用层：
数据库表 -> SQLDelight -> Domain 模型 -> 本地数据源 -> Repository -> UI 读写。

本次示例字段：

- `layout_as_list`（是否列表视图）
- `sort_as_date`（是否按日期排序）

## 操作步骤

### 1. 数据库表与 SQLDelight

文件：

- `datastore/database/src/commonMain/sqldelight/tech/zhifu/app/myhub/datastore/database/user_preferences.sq`

操作：

1. 在 `CREATE TABLE user_preferences` 中新增列：
    - `layout_as_list INTEGER NOT NULL DEFAULT 1`
    - `sort_as_date INTEGER NOT NULL DEFAULT 1`
2. 同步更新 SQLDelight 查询：
    - `insertUserPreferences` 增加列与 values 参数。
    - `updateUserPreferences` 增加 set 字段与参数。

### 2. Domain 模型

文件：

- `datastore/model/src/commonMain/kotlin/tech/zhifu/app/myhub/datastore/model/domain/UserPreferences.kt`

操作：

1. 新增字段并设置默认值：
    - `layoutAsList: Boolean = true`
    - `sortAsDate: Boolean = true`

### 3. 本地数据源映射

文件：

- `datastore/datasource-local/src/commonMain/kotlin/tech/zhifu/app/myhub/datastore/datasource/impl/LocalDataSourceImpl.kt`

操作：

1. `DbUserPreferences.toDomain()` 补充映射：
    - `layout_as_list -> layoutAsList`
    - `sort_as_date -> sortAsDate`

文件：

- `datastore/datasource-local/src/commonMain/kotlin/tech/zhifu/app/myhub/datastore/datasource/impl/LocalUserDataSourceImpl.kt`

操作：

1. `insertUserPreferences` 写入新字段。
2. `updateUserPreferences` 写入新字段。

### 4. Repository API

文件：

- `datastore/repository-client-api/src/commonMain/kotlin/tech/zhifu/app/myhub/datastore/repository/user/UserRepository.kt`

操作：

1. 新增更新接口：
    - `updateUserPreferencesLayoutAsList(userId, layoutAsList)`
    - `updateUserPreferencesSortAsDate(userId, sortAsDate)`

文件：

- `datastore/repository-client/src/commonMain/kotlin/tech/zhifu/app/myhub/datastore/repository/user/UserRepositoryImpl.kt`

操作：

1. 基于当前偏好 `current.copy(...)` 生成新对象并写回。

### 5. 测试 Mock 更新

文件：

- `feature/settings/src/commonTest/kotlin/tech/zhifu/app/myhub/feature/settings/test/MockHelpers.kt`

操作：

1. Mock `UserRepository` 补齐新增接口。

### 6. Bootstrap 默认偏好

文件：

- `datastore/bootstrap/src/commonMain/composeResources/files/bootstrap_default/user_preferences.json`

操作：

1. 新增默认值：
    - `"layoutAsList": true`
    - `"sortAsDate": true`

### 7. UI 读写偏好

位置示例：

- `feature/dashboard` 内 Menu 相关逻辑

操作：

1. ViewModel 监听 `UserPreferences` 的流。
2. 将字段映射进 UI State。
3. 菜单点击触发更新接口，写回数据库。

## 数据库迁移提示

- 旧库需要迁移新增列，否则会出现缺列错误。
- 常见处理：
    1. 增量迁移脚本。
    2. 清库重建。

## 可选：迁移开关交互

如果需要让用户选择是否执行迁移，建议在“启动流程”或“设置页”提供显式选项（例如：首次升级时弹窗）。具体落点由业务决定。
