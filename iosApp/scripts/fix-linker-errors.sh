#!/bin/bash

# iOS 链接器错误修复脚本
# 用于解决 "ld invocation reported errors" 问题

set -e

echo "🔧 开始修复 iOS 链接器错误..."
echo ""

# 1. 清理 Gradle 构建
echo "📦 步骤 1: 清理 Gradle 构建..."
cd "$(dirname "$0")/.."
./gradlew :composeApp:clean
echo "✅ Gradle 构建已清理"
echo ""

# 2. 检查 Framework 是否存在
echo "🔍 步骤 2: 检查 Framework..."
FRAMEWORK_PATH="composeApp/build/cocoapods/framework/ComposeApp.framework"
if [ -d "$FRAMEWORK_PATH" ]; then
    echo "✅ Framework 存在: $FRAMEWORK_PATH"
    # 检查架构
    if command -v lipo &> /dev/null; then
        echo "📐 Framework 架构:"
        lipo -info "$FRAMEWORK_PATH/ComposeApp" 2>/dev/null || echo "⚠️  无法读取架构信息"
    fi
else
    echo "⚠️  Framework 不存在，将在下一步生成"
fi
echo ""

# 3. 重新生成 Framework
echo "🔨 步骤 3: 重新生成 Framework..."
./gradlew :composeApp:podInstall
echo "✅ Framework 已重新生成"
echo ""

# 4. 清理 CocoaPods 缓存（可选）
read -p "是否清理 CocoaPods 缓存并重新安装? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🧹 步骤 4: 清理 CocoaPods 缓存..."
    cd iosApp
    rm -rf Pods Podfile.lock
    pod install
    cd ..
    echo "✅ CocoaPods 依赖已重新安装"
else
    echo "⏭️  跳过 CocoaPods 清理"
fi
echo ""

# 5. 提示清理 Xcode 构建
echo "📋 接下来的步骤："
echo ""
echo "1. 在 Xcode 中清理构建："
echo "   Product > Clean Build Folder (⇧⌘K)"
echo ""
echo "2. 删除 Derived Data（可选但推荐）："
echo "   Xcode > Settings > Locations > Derived Data"
echo "   点击箭头打开文件夹，删除相关项目文件夹"
echo ""
echo "3. 重新构建项目："
echo "   Product > Build (⌘B)"
echo ""
echo "4. 如果问题仍然存在，请检查："
echo "   - 是否使用 .xcworkspace 文件打开项目"
echo "   - Framework Search Paths 是否正确配置"
echo "   - Other Linker Flags 是否包含 -framework ComposeApp"
echo ""
echo "✅ 修复脚本执行完成！"
