# Server 待办事项

> 本文档列出了 MyHub Server 模块的待办事项和功能开发计划。

## 📋 概述

本文档跟踪 Server 模块的待办事项，包括 API 实现、功能完善、性能优化和安全增强等任务。

## ✅ 已完成的工作

### 基础设施

- ✅ **项目结构** - 基础项目结构搭建
- ✅ **Ktor 框架集成** - Ktor + Netty 服务器配置
- ✅ **日志配置** - Logback 日志框架配置
- ✅ **基础路由** - 根路径 `/` 路由实现
- ✅ **测试框架** - Ktor Test Host 测试配置

## 📝 待办事项

### 🔴 高优先级

#### API 端点实现

- [ ] **卡片 API** (`/api/cards`)
  - [ ] `GET /api/cards` - 获取所有卡片（支持分页、筛选、排序）
  - [ ] `GET /api/cards/{id}` - 根据 ID 获取卡片
  - [ ] `POST /api/cards` - 创建新卡片
  - [ ] `PUT /api/cards/{id}` - 更新卡片
  - [ ] `DELETE /api/cards/{id}` - 删除卡片
  - [ ] `POST /api/cards/{id}/favorite` - 切换收藏状态
  - [ ] `GET /api/cards/search` - 搜索卡片（支持多条件查询）

- [ ] **标签 API** (`/api/tags`)
  - [ ] `GET /api/tags` - 获取所有标签
  - [ ] `GET /api/tags/{id}` - 根据 ID 获取标签
  - [ ] `POST /api/tags` - 创建新标签
  - [ ] `PUT /api/tags/{id}` - 更新标签
  - [ ] `DELETE /api/tags/{id}` - 删除标签

- [ ] **模板 API** (`/api/templates`)
  - [ ] `GET /api/templates` - 获取所有模板
  - [ ] `GET /api/templates/{id}` - 根据 ID 获取模板
  - [ ] `POST /api/templates` - 创建新模板
  - [ ] `PUT /api/templates/{id}` - 更新模板
  - [ ] `DELETE /api/templates/{id}` - 删除模板

- [ ] **用户 API** (`/api/users`)
  - [ ] `GET /api/users/current` - 获取当前用户信息
  - [ ] `PUT /api/users/current` - 更新当前用户信息

- [ ] **统计 API** (`/api/statistics`)
  - [ ] `GET /api/statistics` - 获取统计信息

#### 数据持久化

- [ ] **数据库集成**
  - [ ] 选择数据库方案（PostgreSQL / SQLite）
  - [ ] 数据库连接配置
  - [ ] 数据库迁移脚本
  - [ ] Repository 层实现

- [ ] **数据模型**
  - [ ] Card 数据表设计
  - [ ] Tag 数据表设计
  - [ ] Template 数据表设计
  - [ ] User 数据表设计
  - [ ] Statistics 数据表设计

### 🟡 中优先级

#### Ktor 插件配置

- [ ] **Content Negotiation**
  - [ ] JSON 序列化配置（Kotlinx Serialization）
  - [ ] 请求/响应格式统一

- [ ] **CORS 配置**
  - [ ] 跨域请求支持
  - [ ] 安全配置

- [ ] **Call Logging**
  - [ ] 请求日志记录
  - [ ] 响应日志记录
  - [ ] 性能日志

- [ ] **Status Pages**
  - [ ] 统一错误处理
  - [ ] 自定义错误响应格式
  - [ ] 异常映射

- [ ] **Compression**
  - [ ] 响应压缩（Gzip）
  - [ ] 性能优化

#### 业务逻辑层

- [ ] **Service 层实现**
  - [ ] CardService - 卡片业务逻辑
  - [ ] TagService - 标签业务逻辑
  - [ ] TemplateService - 模板业务逻辑
  - [ ] UserService - 用户业务逻辑
  - [ ] StatisticsService - 统计业务逻辑

- [ ] **数据验证**
  - [ ] 请求参数验证
  - [ ] 数据格式验证
  - [ ] 业务规则验证

- [ ] **异常处理**
  - [ ] 自定义异常类
  - [ ] 异常处理中间件
  - [ ] 错误响应格式

### 🟢 低优先级

#### 安全功能

- [ ] **身份验证**
  - [ ] JWT Token 实现
  - [ ] 登录接口
  - [ ] Token 刷新机制
  - [ ] 登出功能

- [ ] **授权**
  - [ ] 基于角色的访问控制（RBAC）
  - [ ] 资源级权限控制
  - [ ] API 权限验证中间件

- [ ] **安全措施**
  - [ ] HTTPS 配置
  - [ ] 输入验证和清理
  - [ ] SQL 注入防护
  - [ ] XSS 防护
  - [ ] Rate Limiting（限流）

#### 性能优化

- [ ] **缓存**
  - [ ] Redis 集成（可选）
  - [ ] 内存缓存实现
  - [ ] 缓存策略设计

- [ ] **数据库优化**
  - [ ] 连接池配置
  - [ ] 查询优化
  - [ ] 索引优化
  - [ ] 分页查询实现

- [ ] **异步处理**
  - [ ] 后台任务队列
  - [ ] 批量操作优化

#### 监控和运维

- [ ] **健康检查**
  - [ ] `/health` 端点
  - [ ] 数据库连接检查
  - [ ] 依赖服务检查

- [ ] **指标收集**
  - [ ] Prometheus 指标集成
  - [ ] 性能指标收集
  - [ ] 业务指标收集

- [ ] **日志增强**
  - [ ] 结构化日志（JSON）
  - [ ] 请求追踪 ID
  - [ ] 日志级别配置
  - [ ] 日志聚合（可选）

#### 测试

- [ ] **单元测试**
  - [ ] Service 层测试
  - [ ] Repository 层测试
  - [ ] 工具函数测试

- [ ] **集成测试**
  - [ ] API 端点测试
  - [ ] 数据库集成测试
  - [ ] 端到端测试

- [ ] **测试工具**
  - [ ] Mock 框架集成
  - [ ] 测试数据生成
  - [ ] 测试容器（Testcontainers）

#### 部署和 DevOps

- [ ] **Docker 支持**
  - [ ] Dockerfile 编写
  - [ ] Docker Compose 配置
  - [ ] 多阶段构建优化

- [ ] **CI/CD**
  - [ ] GitHub Actions 配置
  - [ ] 自动化测试
  - [ ] 自动化部署

- [ ] **环境配置**
  - [ ] 环境变量支持
  - [ ] 配置文件管理
  - [ ] 多环境配置（dev/staging/prod）

#### 文档

- [ ] **API 文档**
  - [ ] OpenAPI/Swagger 集成
  - [ ] API 文档生成
  - [ ] 交互式 API 文档

- [ ] **开发文档**
  - [ ] 开发指南
  - [ ] 部署指南
  - [ ] 故障排查指南

## 🔄 功能增强

### 实时功能（未来）

- [ ] **WebSocket 支持**
  - [ ] 实时数据推送
  - [ ] 客户端连接管理
  - [ ] 消息广播

- [ ] **Server-Sent Events (SSE)**
  - [ ] 实时事件推送
  - [ ] 客户端订阅管理

### 高级功能（未来）

- [ ] **全文搜索**
  - [ ] Elasticsearch 集成（可选）
  - [ ] 搜索索引构建
  - [ ] 搜索 API

- [ ] **文件上传**
  - [ ] 图片上传支持
  - [ ] 文件存储方案
  - [ ] 文件管理 API

- [ ] **数据导出/导入**
  - [ ] JSON 导出
  - [ ] CSV 导出
  - [ ] 批量导入

## 📊 优先级说明

- 🔴 **高优先级** - 核心功能，必须实现
- 🟡 **中优先级** - 重要功能，建议实现
- 🟢 **低优先级** - 增强功能，可选实现

## 📅 开发计划

### Phase 1: 基础 API（当前阶段）

- [ ] 实现所有 CRUD API 端点
- [ ] 数据持久化
- [ ] 基础错误处理

### Phase 2: 功能完善

- [ ] 身份验证和授权
- [ ] 数据验证
- [ ] 性能优化

### Phase 3: 高级功能

- [ ] 实时推送
- [ ] 全文搜索
- [ ] 监控和运维

## 📝 注意事项

1. **API 兼容性** - 确保 API 设计与客户端 `RemoteDataSource` 接口一致
2. **数据模型** - 与 `core:datastore` 模块的数据模型保持一致
3. **错误处理** - 统一的错误响应格式
4. **性能** - 考虑高并发场景的性能优化
5. **安全** - 生产环境必须实现身份验证和授权

## 🔗 相关文档

- [Server README](../README.md) - Server 模块概述
- [架构设计文档](architecture.md) - 详细的架构设计
- [MyHub 整体架构](../../../docs/myhub_architecture.md) - 项目整体架构
- [Datastore 模块文档](../../core/datastore/docs/) - 客户端数据层文档

