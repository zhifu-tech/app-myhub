#!/bin/bash

# 生成 JWT Secret Key 脚本
# 用法: ./scripts/generate-jwt-secret.sh

echo "生成 JWT Secret Key..."
SECRET=$(openssl rand -base64 32)

echo ""
echo "生成的 JWT Secret Key:"
echo "$SECRET"
echo ""
echo "请将以下内容添加到 server/.env 文件中："
echo "JWT_SECRET=$SECRET"
echo ""
