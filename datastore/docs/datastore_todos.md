# Datastore 待办事项

> 本文档列出了 `datastore` 模块的待办事项和功能完善计划。

## 📋 概述

本文档跟踪 datastore 模块的待办事项，包括功能完善、性能优化和功能增强等任务。

## ✅ 已完成的工作

### 核心功能

- ✅ **数据模型** - Card、Tag、Template、User、Statistics、SearchFilter
- ✅ **数据传输层** - CardDto 及转换函数
- ✅ **本地数据源** - 所有 LocalDataSource 实现（SQLDelight）
    - ✅ LocalCardDataSourceImpl
    - ✅ LocalTagDataSourceImpl
    - ✅ LocalTemplateDataSourceImpl
    - ✅ LocalUserDataSourceImpl
    - ✅ LocalStatisticsDataSourceImpl
- ✅ **远程数据源** - RemoteCardDataSourceImpl（Ktor Client）
- ✅ **仓库层** - 所有 Repository 实现
    - ✅ CardRepositoryImpl
    - ✅ TagRepositoryImpl
    - ✅ TemplateRepositoryImpl
    - ✅ UserRepositoryImpl
    - ✅ StatisticsRepositoryImpl
- ✅ **依赖注入** - DataModule、NetworkModule、DatabaseModule
- ✅ **测试** - 所有核心组件的单元测试

## 📝 待办事项

### 🔴 高优先级

#### Repository 功能完善

- [ ] **TagRepository**

    - [ ] 完善 `getTagStats()` 实现（需要从卡片数据计算收藏和最近卡片数）

- [ ] **StatisticsRepository**
    - [ ] 实现 `getCardStatistics()` - 卡片统计详情
    - [ ] 实现 `incrementCardViewCount()` - 增加查看次数
    - [ ] 实现 `incrementCardEditCount()` - 增加编辑次数

#### ViewModel 集成

- [ ] 创建 ViewModel 使用 Repository
- [ ] 实现 UI State 管理
- [ ] 处理加载和错误状态
- [ ] ViewModel 测试（待 ViewModel 实现后添加）

### 🟡 中优先级

#### RemoteDataSource 完善

- [ ] 实现其他 RemoteDataSource
    - [ ] RemoteTagDataSourceImpl
    - [ ] RemoteTemplateDataSourceImpl
    - [ ] RemoteUserDataSourceImpl
    - [ ] RemoteStatisticsDataSourceImpl

#### 网络层增强

- [ ] 添加请求重试机制
- [ ] 添加认证拦截器（如果需要）
- [ ] 实现请求缓存策略

#### 性能优化

- [ ] 实现分页加载
- [ ] 添加缓存策略
- [ ] 优化数据库查询

### 🟢 低优先级

#### 功能增强

- [ ] 实现数据迁移策略
- [ ] 添加备份和恢复功能
- [ ] 实现数据同步冲突解决

## 🎯 功能状态

### LocalDataSource ✅

| 数据源                           | 状态   | 说明                                |
|-------------------------------|------|-----------------------------------|
| LocalCardDataSourceImpl       | ✅ 完成 | SQLDelight 实现，包含完整 CRUD 和 Flow 支持 |
| LocalTagDataSourceImpl        | ✅ 完成 | SQLDelight 实现                     |
| LocalTemplateDataSourceImpl   | ✅ 完成 | SQLDelight 实现                     |
| LocalUserDataSourceImpl       | ✅ 完成 | SQLDelight 实现                     |
| LocalStatisticsDataSourceImpl | ✅ 完成 | SQLDelight 实现                     |

### RemoteDataSource 🟡

| 数据源                            | 状态    | 说明             |
|--------------------------------|-------|----------------|
| RemoteCardDataSourceImpl       | ✅ 完成  | Ktor Client 实现 |
| RemoteTagDataSourceImpl        | ⏳ 待实现 | -              |
| RemoteTemplateDataSourceImpl   | ⏳ 待实现 | -              |
| RemoteUserDataSourceImpl       | ⏳ 待实现 | -              |
| RemoteStatisticsDataSourceImpl | ⏳ 待实现 | -              |

### Repository ✅

| 仓库                       | 状态      | 说明                   |
|--------------------------|---------|----------------------|
| CardRepositoryImpl       | ✅ 完成    | 协调本地和远程数据源           |
| TagRepositoryImpl        | 🟡 部分完成 | 需要完善 `getTagStats()` |
| TemplateRepositoryImpl   | ✅ 完成    | -                    |
| UserRepositoryImpl       | ✅ 完成    | -                    |
| StatisticsRepositoryImpl | 🟡 部分完成 | 需要实现统计相关方法           |

### 测试 ✅

| 测试类型               | 状态   | 说明                            |
|--------------------|------|-------------------------------|
| LocalDataSource 测试 | ✅ 完成 | 所有 5 个 LocalDataSource 都有完整测试 |
| Repository 测试      | ✅ 完成 | 所有 5 个 Repository 都有完整测试      |
| 数据库基础功能测试          | ✅ 完成 | Schema、事务、外键约束等               |
| 跨平台测试支持            | ✅ 完成 | Android、iOS、JVM、Web           |

## 📊 进度统计

- **核心功能**: 90% ✅
- **测试覆盖**: 100% ✅
- **功能完善**: 60% 🟡
- **性能优化**: 30% 🟡
- **功能增强**: 10% 🟢

## 🔗 相关文档

- [架构设计文档](./datastore_architecture.md) - 详细的架构设计说明
- [测试指南](./datastore_test_guide.md) - 测试相关文档

---

**最后更新**: 2025-12-25  
**维护者**: MyHub Team  
**状态**: ✅ 核心功能已完成，待完善功能增强
