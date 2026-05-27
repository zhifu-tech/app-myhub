#!/bin/bash

# Docker 清理脚本
# 用于清理 Docker 缓存和未使用的资源以释放磁盘空间

echo "🧹 开始清理 Docker 资源..."

# 1. 停止所有容器
echo "1. 停止所有运行中的容器..."
docker stop $(docker ps -aq) 2>/dev/null || echo "  没有运行中的容器"

# 2. 删除所有停止的容器
echo "2. 删除所有停止的容器..."
docker container prune -f

# 3. 删除所有未使用的镜像
echo "3. 删除所有未使用的镜像..."
docker image prune -a -f

# 4. 删除所有未使用的卷
echo "4. 删除所有未使用的卷..."
docker volume prune -f

# 5. 删除所有未使用的网络
echo "5. 删除所有未使用的网络..."
docker network prune -f

# 6. 清理构建缓存
echo "6. 清理构建缓存..."
docker builder prune -a -f

# 7. 显示清理后的空间使用情况
echo ""
echo "📊 Docker 磁盘使用情况:"
docker system df

echo ""
echo "✅ 清理完成！"
echo ""
echo "💡 如果问题仍然存在，请尝试："
echo "   1. 重启 Docker Desktop"
echo "   2. 检查系统磁盘空间（当前磁盘已满）"
echo "   3. 清理系统其他文件以释放空间"


