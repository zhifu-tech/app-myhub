# 多阶段构建 Dockerfile for MyHub Server

# 阶段 1: 构建阶段
FROM gradle:8-jdk17 AS build

WORKDIR /app

# 复制整个项目（.dockerignore 会排除不需要的文件）
COPY . .

# 构建应用（只构建 server 模块，跳过测试以加快构建速度）
RUN ./gradlew :server:build --no-daemon -x test

# 阶段 2: 运行阶段
FROM eclipse-temurin:17-jre

WORKDIR /app

# 创建数据目录（用于 SQLite）
RUN mkdir -p /app/data

# 从构建阶段复制 JAR 文件
COPY --from=build /app/server/build/libs/server-*.jar app.jar

# 暴露端口
EXPOSE 8083

# 设置环境变量默认值
ENV SERVER_PORT=8083
ENV DB_TYPE=SQLITE
ENV DB_PATH=/app/data/myhub.db

# 安装 curl 用于健康检查（Debian/Ubuntu 基础镜像）
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:${SERVER_PORT}/health || exit 1

# 运行应用
ENTRYPOINT ["java", "-jar", "app.jar"]

