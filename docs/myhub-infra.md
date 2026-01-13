# MyHub 基础设施

MyHub 基础设施（Infra）模块为 MyHub 应用提供底层支撑，包括**数据存储**、**服务端接口**、**依赖注入**、**核心工具与组件**等范畴。它主要负责以下职责：

1. **通用能力的抽象与实现**：提供与具体业务无关、可被各功能模块复用的基础框架与能力（如日志、数据模型、存储、数据库、网络通信等）。
2. **统一技术选型与架构规范**：对 MyHub 各端（服务端、客户端、共享代码）所使用的基础设施组件进行统一封装和规范，确保架构一致性、易扩展性与可维护性。
3. **支撑业务特性开发**：为业务特性模块（如 Dashboard、Profile 等）屏蔽底层实现细节，提供高质量、标准化的底座支撑。
4. **多端适配能力**：针对 KMP 场景，抽象通用接口，并结合多端实现，实现代码的最大复用。

基础设施模块通常包括但不限于：核心数据层（Datastore/Repository）、数据库、服务器基础模块、依赖注入框架初始化、日志工具、测试工具、通用网络能力等。

该模块是支撑 MyHub 长期演进和业务敏捷开发的“地基”。

## [AI 基础设施规则](./myhub-infra-rules.md)

> **本规则详细定义了 MyHub 基础设施层（Infra）的架构设计文档结构、内容标准与评审要求**。其核心目的是确保所有基础设施相关技术方案文档具备**清晰的结构**、**一致的表达**和**可评审性**，从而提升架构决策的透明度和可追溯性。**规则内容包括但不限于：文档需包含的头部元信息、目录结构、核心章节（如需求背景、设计目标、技术对比、实现细节、实施计划等），以及具体的内容检查清单。**所有 Infra 层（如数据存储、依赖注入、日志、数据库等）相关的重要架构方案和技术 RFC，均应严格遵循本规则撰写和提交，以保障架构方案的可理解性与后续维护扩展的高效性。

## [导航模块方案设计](../core/navigation/docs/myhub-nav-infra.md)

## [日志模块方案设计](../core/logger/docs/myhub-logger-infra-v1.0.md)

## [应用构建配置模块方案设计](../core/app-build-config/docs/myhub-app-build-config-infra-v1.0.md)

## [平台抽象模块方案设计](../core/platform/docs/myhub-platform-infra-v1.0.md)

## [Compose UI 平台抽象模块方案设计](../core/platform-compose/docs/myhub-platform-compose-infra-v1.0.md)

## [网络层模块方案设计](../core/network/docs/myhub-network-infra-v1.0.md)

## [统计框架模块方案设计](../core/analytics/docs/myhub-analytics-infra-v1.0.md)

## [数据存储套件总体架构设计](../core/datastore/docs/myhub-datastore-infra-v1.0.md)

## [数据模型模块方案设计](../core/datastore-model/docs/myhub-datastore-model-infra-v1.0.md)

## [数据库模块方案设计](../core/datastore-database/docs/myhub-datastore-database-infra-v1.0.md)

## [数据库测试模块方案设计](../core/datastore-database-test/docs/myhub-datastore-database-test-infra-v1.0.md)

## [数据库管理模块方案设计](../core/datastore-database-manage/docs/myhub-datastore-database-manage-infra-v1.0.md)

## [数据库客户端模块方案设计](../core/datastore-database-client/docs/myhub-datastore-database-client-infra-v1.0.md)

## [本地数据源模块方案设计](../core/datastore-datasource-local/docs/myhub-datastore-datasource-local-infra-v1.0.md)

## [远程数据源模块方案设计](../core/datastore-datasource-remote/docs/myhub-datastore-datasource-remote-infra-v1.0.md)

## [数据库服务端模块方案设计](../core/datastore-database-server/docs/myhub-datastore-database-server-infra-v1.0.md)

## [数据仓库客户端模块方案设计](../core/datastore-repository-client/docs/myhub-datastore-repository-client-infra-v1.0.md)

## [数据仓库服务端模块方案设计](../core/datastore-repository-server/docs/myhub-datastore-repository-server-infra-v1.0.md)
