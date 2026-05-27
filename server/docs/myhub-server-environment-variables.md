# 环境变量配置指南

本文档介绍在 MyHub Server 中配置环境变量的各种方式。

## 📋 支持的配置方式

### 1. 命令行直接设置（临时）

适用于：单次运行、快速测试

```bash
# 方式 1: 在命令前设置（仅对当前命令有效）
DB_TYPE=POSTGRESQL DB_HOST=localhost ./gradlew :server:run

# 方式 2: 导出到当前 shell 会话
export DB_TYPE=POSTGRESQL
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=myhub
export DB_USER=postgres
export DB_PASSWORD=your_password
./gradlew :server:run
```

**优点**：快速、灵活  
**缺点**：每次需要重新设置，不持久化

---

### 2. Shell 配置文件（系统级持久化）

适用于：个人开发环境、固定配置

#### macOS/Linux (zsh)

```bash
# 编辑 ~/.zshrc
nano ~/.zshrc

# 添加环境变量
export DB_TYPE=POSTGRESQL
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=myhub
export DB_USER=postgres
export DB_PASSWORD=your_password

# 重新加载配置
source ~/.zshrc
```

#### macOS/Linux (bash)

```bash
# 编辑 ~/.bashrc 或 ~/.bash_profile
nano ~/.bashrc
# ... 添加相同的 export 语句 ...
source ~/.bashrc
```

#### Windows (PowerShell)

```powershell
# 设置用户级环境变量（永久）
[System.Environment]::SetEnvironmentVariable("DB_TYPE", "POSTGRESQL", "User")
[System.Environment]::SetEnvironmentVariable("DB_HOST", "localhost", "User")

# 设置会话级环境变量（临时）
$env:DB_TYPE = "POSTGRESQL"
$env:DB_HOST = "localhost"
```

#### Windows (CMD)

```cmd
# 设置用户级环境变量（永久）
setx DB_TYPE "POSTGRESQL"
setx DB_HOST "localhost"

# 设置会话级环境变量（临时）
set DB_TYPE=POSTGRESQL
set DB_HOST=localhost
```

**优点**：系统级持久化，所有项目可用  
**缺点**：影响全局环境，不适合多项目不同配置

---

### 3. .env 文件（项目级，推荐）

适用于：开发环境、团队协作

#### 创建 .env 文件

在 `server/` 目录下创建 `.env` 文件：

```bash
# server/.env
DB_TYPE=POSTGRESQL
DB_HOST=localhost
DB_PORT=5432
DB_NAME=myhub
DB_USER=postgres
DB_PASSWORD=your_password
SERVER_PORT=8083
```

#### 使用方式

**方式 A: 使用 dotenv 工具（需要安装）**

```bash
# 安装 dotenv-cli (Node.js)
npm install -g dotenv-cli

# 使用 dotenv 运行
dotenv -e server/.env -- ./gradlew :server:run
```

**方式 B: 使用 shell 脚本加载**

创建 `server/load-env.sh`:

```bash
#!/bin/bash
# 加载 .env 文件
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi
./gradlew :server:run
```

**方式 C: 在代码中支持 .env（推荐）**

可以添加 Kotlin 库来读取 .env 文件，例如使用 `dotenv-kotlin`：

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
}

// DatabaseConfig.kt
import io.github.cdimascio.dotenv.dotenv

fun fromEnvironment(): DatabaseConfig {
    val dotenv = dotenv {
        directory = "./server"
        ignoreIfMissing = true
    }

    val dbType = dotenv["DB_TYPE"] ?: System.getenv("DB_TYPE") ?: "SQLITE"
    // ...
}
```

**优点**：项目级配置，版本控制友好（.env.example），团队协作方便  
**缺点**：需要额外工具或代码支持

---

### 4. Docker Compose（容器化部署）

适用于：Docker 部署、生产环境

#### 方式 A: 直接在 docker-compose.yml 中定义

```yaml
# server/docker-compose.yml
services:
  myhub-server:
    environment:
      - DB_TYPE=POSTGRESQL
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=myhub
      - DB_USER=myhub_user
      - DB_PASSWORD=myhub_password
```

#### 方式 B: 使用 env_file 指令

```yaml
# server/docker-compose.yml
services:
  myhub-server:
    env_file:
      - .env
      - .env.production # 可以指定多个文件，后面的会覆盖前面的
```

#### 方式 C: 使用环境变量替换

```yaml
# server/docker-compose.yml
services:
  myhub-server:
    environment:
      - DB_TYPE=${DB_TYPE:-SQLITE} # 使用环境变量，默认值为 SQLITE
      - DB_HOST=${DB_HOST:-localhost}
      - DB_PASSWORD=${DB_PASSWORD}
```

**优点**：容器化标准做法，配置集中管理  
**缺点**：仅适用于 Docker 部署

---

### 5. Dockerfile ENV 指令（镜像默认值）

适用于：Docker 镜像构建

```dockerfile
# server/Dockerfile
ENV DB_TYPE=SQLITE
ENV DB_PATH=/app/data/myhub.db
ENV SERVER_PORT=8083
```

**注意**：这些是默认值，可以被运行时环境变量覆盖。

**优点**：提供镜像默认配置  
**缺点**：硬编码在镜像中，不够灵活

---

### 6. Docker run 命令参数

适用于：单容器运行、临时配置

```bash
# 使用 -e 参数
docker run -d \
  --name myhub-server \
  -p 8083:8083 \
  -e DB_TYPE=POSTGRESQL \
  -e DB_HOST=postgres \
  -e DB_PORT=5432 \
  -e DB_NAME=myhub \
  -e DB_USER=postgres \
  -e DB_PASSWORD=your_password \
  myhub-server:latest

# 使用 --env-file
docker run -d \
  --name myhub-server \
  -p 8083:8083 \
  --env-file server/.env \
  myhub-server:latest
```

**优点**：灵活，适合临时测试  
**缺点**：命令较长，不适合生产环境

---

### 7. IDE 运行配置（开发环境）

适用于：IDE 中直接运行

#### IntelliJ IDEA / Android Studio

1. 打开 **Run** → **Edit Configurations**
2. 选择运行配置
3. 在 **Environment variables** 中添加：
   ```
   DB_TYPE=POSTGRESQL;DB_HOST=localhost;DB_PORT=5432;DB_NAME=myhub;DB_USER=postgres;DB_PASSWORD=your_password
   ```
   或使用 **Environment file** 指向 `.env` 文件

#### VS Code

在 `.vscode/launch.json` 中配置：

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "kotlin",
      "request": "launch",
      "name": "Run Server",
      "env": {
        "DB_TYPE": "POSTGRESQL",
        "DB_HOST": "localhost",
        "DB_PORT": "5432",
        "DB_NAME": "myhub",
        "DB_USER": "postgres",
        "DB_PASSWORD": "your_password"
      }
    }
  ]
}
```

**优点**：IDE 集成，开发方便  
**缺点**：\*\* 配置在 IDE 中，不便于版本控制和团队共享

---

### 8. Gradle 配置（构建时）

适用于：构建时注入配置

#### 方式 A: gradle.properties

```properties
# gradle.properties
dbType=POSTGRESQL
dbHost=localhost
```

在 `build.gradle.kts` 中读取：

```kotlin
val dbType = project.findProperty("dbType") as String? ?: "SQLITE"
```

#### 方式 B: 命令行参数

```bash
./gradlew :server:run -PdbType=POSTGRESQL -PdbHost=localhost
```

**注意**：这种方式需要修改代码来读取 Gradle 属性，不如环境变量灵活。

**优点**：构建时配置  
**缺点**：需要修改构建脚本，不够灵活

---

### 9. CI/CD 平台配置

适用于：持续集成/部署

#### GitHub Actions

```yaml
# .github/workflows/deploy.yml
env:
  DB_TYPE: POSTGRESQL
  DB_HOST: ${{ secrets.DB_HOST }}
  DB_PASSWORD: ${{ secrets.DB_PASSWORD }}

jobs:
  deploy:
    steps:
      - name: Run server
        run: ./gradlew :server:run
```

#### GitLab CI

```yaml
# .gitlab-ci.yml
variables:
  DB_TYPE: POSTGRESQL
  DB_HOST: $DB_HOST
  DB_PASSWORD: $DB_PASSWORD
```

#### Jenkins

在 Jenkins 项目配置中：

- **Build Environment** → **Inject environment variables**
- 或使用 **Environment Injector Plugin**

**优点**：自动化部署，安全存储密钥  
**缺点**：仅适用于 CI/CD 流程

---

### 10. 云平台配置

适用于：云服务部署

#### Heroku

```bash
heroku config:set DB_TYPE=POSTGRESQL
heroku config:set DB_HOST=your-host
```

#### AWS (ECS/Elastic Beanstalk)

在任务定义或环境配置中设置环境变量。

#### Google Cloud (Cloud Run)

```bash
gcloud run deploy myhub-server \
  --set-env-vars DB_TYPE=POSTGRESQL,DB_HOST=your-host
```

#### Railway / Fly.io / Render

在平台的控制面板中配置环境变量。

**优点**：平台集成  
**缺点**：\*\* 平台特定，需要登录平台配置

---

## 🎯 推荐配置方案

### 开发环境

1. **个人开发**：使用 `.env` 文件 + `.gitignore` 排除
2. **团队协作**：提供 `.env.example` 模板文件

```bash
# server/.env.example（提交到版本控制）
DB_TYPE=SQLITE
DB_PATH=.myhub/myhub.db
# 或
# DB_TYPE=POSTGRESQL
# DB_HOST=localhost
# DB_PORT=5432
# DB_NAME=myhub
# DB_USER=postgres
# DB_PASSWORD=your_password
```

### 生产环境

1. **Docker 部署**：使用 `docker-compose.yml` + `.env` 文件
2. **云平台部署**：使用平台的环境变量配置功能
3. **Kubernetes**：使用 ConfigMap 和 Secret

---

## 📝 最佳实践

1. **永远不要提交敏感信息到版本控制**

    - 使用 `.gitignore` 排除 `.env` 文件
    - 提供 `.env.example` 作为模板

2. **优先级顺序**（从高到低）

    - 命令行环境变量
    - `.env` 文件
    - 系统环境变量
    - 代码中的默认值

3. **使用环境变量管理工具**

    - 开发：`.env` 文件
    - 生产：密钥管理服务（AWS Secrets Manager, HashiCorp Vault 等）

4. **验证配置**
    - 启动时检查必需的环境变量
    - 提供清晰的错误信息

---

## 🔒 安全注意事项

1. **密码和密钥**：永远不要硬编码在代码中
2. **版本控制**：确保 `.env` 在 `.gitignore` 中
3. **生产环境**：使用密钥管理服务，不要使用 `.env` 文件
4. **权限控制**：限制 `.env` 文件的访问权限（chmod 600）

---

## 📚 相关文档

- [数据库配置文档](myhub-server-database.md)
- [Docker 部署文档](../README.md#docker-部署)
- [12-Factor App 配置原则](https://12factor.net/config)


