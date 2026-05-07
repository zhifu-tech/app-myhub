# JWT 认证快速配置指南

> 如何配置 JWT 认证系统

## 🚀 快速开始

### 1. 复制配置文件

```bash
cd server
cp .env.example .env
```

### 2. 生成 JWT Secret（必需）

**方式 1：使用提供的脚本**（推荐）

```bash
./scripts/generate-jwt-secret.sh
```

**方式 2：使用 OpenSSL**

```bash
openssl rand -base64 32
```

**方式 3：使用在线工具**
访问 https://www.grc.com/passwords.htm 生成强随机密码

### 3. 更新 .env 文件

将生成的 Secret 填入 `server/.env` 文件：

```bash
# 编辑 .env 文件
nano .env

# 找到 JWT_SECRET 行，替换为生成的密钥
JWT_SECRET=your-generated-secret-here
```

### 4. 验证配置

启动服务器，如果配置正确，JWT 认证将正常工作：

```bash
./gradlew :server:run
```

---

## 📝 配置说明

### 必需配置

| 变量名          | 说明       | 示例值                                            | 必需  |
|--------------|----------|------------------------------------------------|-----|
| `JWT_SECRET` | JWT 签名密钥 | `LpkmaSkAtM3WbGLWSsiMbwAJX+d+07QdILQKySTkhg4=` | ✅ 是 |

### 可选配置

| 变量名            | 说明        | 默认值            | 必需 |
|----------------|-----------|----------------|----|
| `JWT_ISSUER`   | Token 发行者 | `myhub-api`    | 否  |
| `JWT_AUDIENCE` | Token 受众  | `myhub-client` | 否  |

---

## 🔒 安全建议

### 开发环境

- 可以使用 `.env.example` 中的示例值
- 或使用脚本生成一个密钥

### 生产环境

1. **必须生成新的 Secret**：
   ```bash
   openssl rand -base64 32
   ```

2. **不要使用默认值**：
    - 不要使用 `.env.example` 中的示例值
    - 不要使用代码中的默认值

3. **妥善保管 Secret**：
    - 使用密钥管理服务（AWS Secrets Manager、HashiCorp Vault 等）
    - 不要提交到版本控制
    - 限制访问权限

4. **定期轮换**：
    - 建议每 90 天轮换一次 Secret
    - 轮换时需要重新生成所有 token

---

## ✅ 验证配置

### 检查环境变量是否加载

启动服务器后，检查日志中是否有 JWT 配置相关的错误。

### 测试登录

```bash
# 测试登录 API
curl -X POST http://localhost:8083/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"userId":"user-123"}'
```

如果返回 token，说明配置正确。

---

## 🐛 常见问题

### Q1: 如何知道 JWT Secret 是否正确配置？

**A**: 启动服务器后，尝试登录。如果返回 401 错误或 token 验证失败，可能是 Secret 配置不正确。

### Q2: 可以多个环境使用同一个 Secret 吗？

**A**: 不建议。每个环境（开发、测试、生产）应该使用不同的 Secret。

### Q3: Secret 丢失了怎么办？

**A**: 需要重新生成新的 Secret，并更新所有环境的配置。旧的 token 将无法使用，用户需要重新登录。

---

**文档版本**: v1.0  
**创建日期**: 2026-01-27  
**状态**: 配置指南
