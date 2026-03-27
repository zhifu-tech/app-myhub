# AI捕获系统-客户端本地存储设计 V1.0

## 1. 文档定位

- 版本：V1.0
- 目标：定义 AI 捕获系统在客户端的本地存储方案（数据库 + 文件系统 + 媒体索引）。
- 范围：Android / iOS / JVM 桌面三端统一设计。
- 原则：Local-first、离线可用、可恢复、可扩展。

## 2. 设计目标

1. 卡片资产默认仅存本地。
2. 支持文本、图片、视频、文件等多种内容载体。
3. 支持草稿态与发布态并存，避免编辑中断造成数据丢失。
4. 支持高性能查询（列表、筛选、搜索）与轻量全文检索。
5. 支持后续可选同步（不改变本地为主的核心架构）。

## 3. 总体架构

```text
App Layer
  -> Repository
    -> Local DB (structured metadata)
    -> File Store (binary assets)
    -> Search Index (optional local FTS)
```

分工：

- Local DB：卡片结构、标签、关系、媒体元数据、任务状态。
- File Store：图片/视频/附件二进制文件。
- Search Index：可选，做本地全文检索与标题摘要检索。

## 4. 本地存储分层

### 4.1 结构化层（Database）

建议使用 SQLDelight（KMP 统一），以 SQLite 为底层。

现有表复用：

- `user.sq`：用户基础信息主表（继续复用）。
- `user_preferences.sq`：用户偏好配置表（继续复用）。

核心表建议：

1. `card`
    - `id` TEXT PK
    - `type` TEXT
    - `title` TEXT
    - `summary` TEXT
    - `content` TEXT  (JSON-TEXT)
    - `ui` TEXT (JSON-TEXT)
    - `location` TEXT NULL (JSON-TEXT，示例：`{"name":"上海","latitude":31.2304,"longitude":121.4737}`)
    - `tags` TEXT (JSON-TEXT)
    - `status` TEXT (`draft|published|archived`)
    - `source` TEXT  (JSON-TEXT)
    - `created_at` INTEGER
    - `updated_at` INTEGER
    - `version` INTEGER
    - `deleted` INTEGER (软删除)

2. `user_card`（用户与卡片关联表）
    - `user_id` TEXT
    - `card_id` TEXT
    - PK(`user_id`, `card_id`)

3. `card_entity`
    - `card_id` TEXT
    - `entity_type` TEXT
    - `entity_name` TEXT
    - PK(`card_id`, `entity_type`, `entity_name`)

4. `card_relation`
    - `id` TEXT PK
    - `from_card_id` TEXT
    - `to_card_id` TEXT
    - `relation_type` TEXT
    - `created_at` INTEGER

5. `media_asset`
    - `id` TEXT PK
    - `card_id` TEXT
    - `media_type` TEXT (`image|video|file|audio`)
    - `mime_type` TEXT
    - `local_uri` TEXT
    - `thumb_uri` TEXT NULL
    - `width` INTEGER NULL
    - `height` INTEGER NULL
    - `duration_ms` INTEGER NULL
    - `size_bytes` INTEGER
    - `sha256` TEXT
    - `created_at` INTEGER

6. `draft_session`
    - `id` TEXT PK
    - `card_id` TEXT NULL
    - `state` TEXT
    - `draft_json` TEXT
    - `missing_fields_json` TEXT
    - `updated_at` INTEGER

7. `ai_job`
    - `id` TEXT PK
    - `provider` TEXT
    - `request_json` TEXT
    - `response_json` TEXT NULL
    - `status` TEXT (`queued|running|succeeded|failed|cancelled`)
    - `error_message` TEXT NULL
    - `created_at` INTEGER
    - `updated_at` INTEGER

结构字段存储说明：

- `content` 使用 JSON-TEXT：因为内容块是嵌套结构，后续扩展字段不需要频繁改表。
- `ui` 使用 JSON-TEXT：用于存储封面 icon/color/image_ref 等展示结构，与正文内容分离。
- `location` 使用 JSON-TEXT：统一存储地点名称与经纬度（`name/latitude/longitude`），便于地图定位。
- `source` 使用 JSON-TEXT：与 `content` 保持一致，便于支持多来源形态（link/import/manual/share 等）和附加属性扩展。
- 常用筛选字段（`type/status/updated_at/location`）仍保持为独立列，保证查询性能。

索引建议：

- `card(updated_at DESC)`
- `card(type, status, updated_at DESC)`
- `user_card(user_id, updated_at DESC)`
- `user_card(card_id)`
- `tag(name)`
- `card_tag(tag_id, card_id)`
- `media_asset(card_id)`

### 4.2 二进制层（File Store）

建议目录结构：

```text
/app-data
  /cards
    /{cardId}
      card.json (可选缓存)
      /media
        {mediaId}.orig
        {mediaId}.thumb.jpg
```

命名规范：

- 文件名使用 `mediaId + 扩展名`，避免用户原始文件名冲突。
- 所有媒体由 `media_asset.local_uri` 反查，不直接依赖路径拼接。

### 4.3 检索层（可选）

可选建立本地 FTS 索引（SQLite FTS5）：

- 索引字段：`title`, `summary`, `content_plain_text`。
- 使用触发器或增量任务更新索引。

## 5. 媒体文件策略（图片/视频/附件）

### 5.1 图片

- 入库前处理：

1. 读取 EXIF（方向、时间）
2. 生成缩略图（列表场景）
3. 原图保存 + 缩略图保存

- 展示优先级：
  `thumb -> original`

### 5.2 视频

- 仅本地存路径和元数据，不把视频本体写入 DB。
- 抽取首帧缩略图，写入 `thumb_uri`。
- 存储元信息：时长、分辨率、大小、mime。

### 5.3 大文件

- 分片复制（避免一次性内存峰值）。
- 写入采用临时文件 + 原子重命名，防止中断导致坏文件。

### 5.4 媒体生命周期

1. 引入：写文件 + 记录 `media_asset`。
2. 替换：新文件写入后更新索引，旧文件延迟清理。
3. 删除：卡片软删除后进入垃圾回收队列。
4. 回收：后台任务清理无引用文件。

## 6. 多平台目录建议

### 6.1 Android

- 私有目录：`context.filesDir` / `context.noBackupFilesDir`
- 大文件缓存：`context.cacheDir`
- 备份策略：重要资产放 `filesDir`，临时资产放 `cacheDir`

### 6.2 iOS

- 主存储：`Application Support`
- 临时缓存：`Caches`
- 防 iCloud 自动备份：对可重建缓存设置 `isExcludedFromBackup`

### 6.3 JVM 桌面

- 默认目录：
  `~/Library/Application Support/<app>` (macOS)
  `%APPDATA%/<app>` (Windows)
  `~/.local/share/<app>` (Linux)
- 建议通过统一 `PlatformPathProvider` 屏蔽差异。

## 7. 数据一致性与事务策略

1. 先写文件，后写数据库事务提交索引。
2. 若 DB 提交失败，回滚并删除新写文件。
3. 若文件写失败，不落 DB 记录。
4. 关键操作使用事务边界：

- 新建卡片 + 绑定标签 + 写媒体索引
- 草稿提交为发布态

## 8. 本地安全与隐私

### 8.1 数据库加密

- 建议 SQLite 加密方案（按平台能力实现）。
- 密钥保存在系统安全区：
- Android Keystore
- iOS Keychain
- JVM 桌面使用系统凭据库或口令派生密钥

### 8.2 文件加密

- 默认可先明文存储（开发阶段）。
- 安全模式可启用文件级加密（媒体大文件按需）。

### 8.3 敏感信息最小化

- AI 请求仅发送必要字段。
- 本地保留完整资产，网络传输保留最小上下文。

## 9. 性能设计

1. 列表查询只取轻字段（title/summary/thumb/status/time）。
2. 内容详情按需加载 `content`（JSON-TEXT）与 media。
3. 缩略图优先，原图懒加载。
4. 后台任务处理：

- 缩略图生成
- 视频元信息提取
- 垃圾回收

## 10. 备份与恢复

### 10.1 本地导出

- 导出包结构：

```text
backup.zip
  manifest.json
  db.sqlite
  media/
```

### 10.2 本地恢复

- 校验 `manifest` 与校验和。
- 恢复顺序：DB -> media -> 索引修复。

### 10.3 可选云备份（非默认）

- 仅在用户主动开启时启用。
- 建议 E2EE 后上传密文。

## 11. 迁移策略（Schema Migration）

1. 每次 schema 升级使用显式 migration。
2. 不破坏旧数据，新增字段需有默认值。
3. 升级失败回滚，保留原数据库副本。
4. 大版本升级前自动触发本地快照备份。

## 12. 推荐实施顺序

1. 落地 `card + media_asset + draft_session` 三张核心表。
2. 落地文件存储与缩略图链路。
3. 完成删除与垃圾回收机制。
4. 增加检索索引和性能优化。
5. 增加加密与备份恢复。

## 13. 验收标准

- 离线可创建/编辑/发布卡片。
- 图片/视频可稳定导入、预览、删除、恢复。
- 重启应用后草稿可恢复。
- 大量卡片列表滚动性能稳定。
- 数据迁移后无丢失、无孤儿文件。

## 14. 术语注释

- Local-first：本地数据为主，网络为可选增强。
- Soft Delete：逻辑删除，后续再物理清理。
- Garbage Collection（文件回收）：清理无引用媒体文件。
- FTS：全文检索索引能力。
