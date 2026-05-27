#!/bin/bash

# iOS 开发问题快速修复脚本
# 用于解决 TLS 错误和 Provisioning Profile 问题

set -e

echo "🔧 iOS 开发问题修复脚本"
echo "=========================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
XCODE_PROJECT="$PROJECT_DIR/iosApp.xcodeproj"

echo -e "${YELLOW}步骤 1: 清理 Xcode 缓存...${NC}"
echo "清理 DerivedData..."
rm -rf ~/Library/Developer/Xcode/DerivedData/* 2>/dev/null || true
echo "✓ DerivedData 已清理"

echo ""
echo -e "${YELLOW}步骤 2: 清理构建产物...${NC}"
if [ -d "$XCODE_PROJECT" ]; then
    cd "$PROJECT_DIR"
    xcodebuild clean -project iosApp.xcodeproj -scheme iosApp 2>/dev/null || echo "⚠ 清理构建产物时出现警告（可能正常）"
    echo "✓ 构建产物已清理"
else
    echo -e "${RED}✗ 未找到 Xcode 项目文件${NC}"
    exit 1
fi

echo ""
echo -e "${YELLOW}步骤 3: 检查网络连接...${NC}"
if ping -c 1 developer.apple.com &> /dev/null; then
    echo -e "${GREEN}✓ 可以连接到 Apple 服务器${NC}"
else
    echo -e "${RED}✗ 无法连接到 Apple 服务器，请检查网络${NC}"
fi

echo ""
echo -e "${YELLOW}步骤 4: 检查代码签名证书...${NC}"
CERT_COUNT=$(security find-identity -v -p codesigning | grep -c "iPhone Developer\|Apple Development" || echo "0")
if [ "$CERT_COUNT" -gt 0 ]; then
    echo -e "${GREEN}✓ 找到 $CERT_COUNT 个有效的代码签名证书${NC}"
    security find-identity -v -p codesigning | grep "iPhone Developer\|Apple Development" | head -3
else
    echo -e "${RED}✗ 未找到有效的代码签名证书${NC}"
    echo "  请在 Xcode 中登录 Apple ID 并下载证书"
fi

echo ""
echo -e "${YELLOW}步骤 5: 检查可用的 iOS 模拟器...${NC}"
SIMULATORS=$(xcrun simctl list devices available | grep -i "iphone\|ipad" | head -5 || echo "")
if [ -n "$SIMULATORS" ]; then
    echo -e "${GREEN}✓ 可用的 iOS 模拟器:${NC}"
    echo "$SIMULATORS" | sed 's/^/   /'
else
    echo -e "${YELLOW}⚠ 未找到可用的模拟器，可能需要安装${NC}"
fi

echo ""
echo -e "${YELLOW}步骤 6: 验证项目配置...${NC}"
if [ -f "$XCODE_PROJECT/project.pbxproj" ]; then
    TEAM_ID=$(grep -o "DEVELOPMENT_TEAM = [^;]*" "$XCODE_PROJECT/project.pbxproj" | head -1 | awk '{print $3}' || echo "")
    if [ -n "$TEAM_ID" ]; then
        echo -e "${GREEN}✓ 开发团队 ID: $TEAM_ID${NC}"
    else
        echo -e "${YELLOW}⚠ 未找到开发团队 ID${NC}"
    fi
    
    CODE_SIGN_STYLE=$(grep -o "CODE_SIGN_STYLE = [^;]*" "$XCODE_PROJECT/project.pbxproj" | head -1 | awk '{print $3}' || echo "")
    if [ "$CODE_SIGN_STYLE" = "Automatic" ]; then
        echo -e "${GREEN}✓ 使用自动签名 (Automatic)${NC}"
    else
        echo -e "${YELLOW}⚠ 签名方式: $CODE_SIGN_STYLE${NC}"
    fi
fi

echo ""
echo -e "${GREEN}=========================="
echo "修复步骤完成！"
echo "==========================${NC}"
echo ""
echo "📋 接下来的操作："
echo ""
echo "1. 打开 Xcode:"
echo "   open $XCODE_PROJECT"
echo ""
echo "2. 在 Xcode 中："
echo "   - 选择项目 > iosApp target"
echo "   - 进入 'Signing & Capabilities'"
echo "   - 确保 'Automatically manage signing' 已勾选"
echo "   - 确保选择了正确的 Team"
echo ""
echo "3. 选择运行目标："
echo "   - 在顶部设备选择器中，选择 'iOS Simulator'（如 iPhone 15 Pro）"
echo "   - ⚠️  不要选择 Mac 设备"
echo ""
echo "4. 如果仍有 TLS 错误："
echo "   - Xcode > Settings > Accounts"
echo "   - 移除并重新添加 Apple ID"
echo "   - 点击 'Download Manual Profiles'"
echo ""
echo "5. 重新构建项目 (Cmd+B)"
echo ""
