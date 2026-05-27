#!/bin/bash

# CocoaPods 环境检查脚本
# 用于诊断 "CocoaPods executable not found" 问题

echo "🔍 CocoaPods 环境检查"
echo "===================="
echo ""

# 1. 检查 pod 命令
echo "1️⃣  检查 pod 命令..."
if command -v pod &> /dev/null; then
    POD_PATH=$(which pod)
    POD_VERSION=$(pod --version 2>/dev/null || echo "无法获取版本")
    echo "✅ CocoaPods 已安装"
    echo "   路径: $POD_PATH"
    echo "   版本: $POD_VERSION"
else
    echo "❌ CocoaPods 未安装或不在 PATH 中"
    echo ""
    echo "💡 安装方法："
    echo "   sudo gem install cocoapods"
    echo "   或"
    echo "   brew install cocoapods"
    exit 1
fi
echo ""

# 2. 检查 PATH 环境变量
echo "2️⃣  检查 PATH 环境变量..."
echo "   当前 PATH:"
echo "$PATH" | tr ':' '\n' | sed 's/^/   /'
echo ""

# 3. 检查常见的 pod 路径
echo "3️⃣  检查常见的 pod 安装位置..."
COMMON_PATHS=(
    "/opt/homebrew/bin/pod"
    "/usr/local/bin/pod"
    "/usr/bin/pod"
    "$HOME/.gem/ruby/*/bin/pod"
)

FOUND=false
for path in "${COMMON_PATHS[@]}"; do
    if [ -f "$path" ] 2>/dev/null || ls $path 2>/dev/null | grep -q pod; then
        echo "   ✅ 找到: $path"
        FOUND=true
    fi
done

if [ "$FOUND" = false ]; then
    echo "   ⚠️  未在常见位置找到 pod 命令"
fi
echo ""

# 4. 检查 Ruby 环境
echo "4️⃣  检查 Ruby 环境..."
if command -v ruby &> /dev/null; then
    RUBY_VERSION=$(ruby --version)
    echo "✅ Ruby 已安装: $RUBY_VERSION"
    echo "   Ruby 路径: $(which ruby)"
else
    echo "❌ Ruby 未安装"
    echo "   CocoaPods 需要 Ruby，请先安装 Ruby"
fi
echo ""

# 5. 检查 gem 环境
echo "5️⃣  检查 gem 环境..."
if command -v gem &> /dev/null; then
    GEM_VERSION=$(gem --version 2>/dev/null || echo "无法获取版本")
    echo "✅ gem 已安装: $GEM_VERSION"
    echo "   gem 路径: $(which gem)"
    
    # 检查 CocoaPods gem
    if gem list | grep -q cocoapods; then
        echo "✅ CocoaPods gem 已安装"
    else
        echo "⚠️  CocoaPods gem 未找到，但 pod 命令可用"
    fi
else
    echo "❌ gem 未安装"
fi
echo ""

# 6. 检查 Gradle 是否能找到 pod
echo "6️⃣  检查 Gradle 配置..."
GRADLE_PROPERTIES="../gradle.properties"
if [ -f "$GRADLE_PROPERTIES" ]; then
    if grep -q "org.jetbrains.kotlin.native.cocoapods.podCommand" "$GRADLE_PROPERTIES"; then
        echo "✅ gradle.properties 中已配置 pod 路径"
        grep "org.jetbrains.kotlin.native.cocoapods.podCommand" "$GRADLE_PROPERTIES" | sed 's/^/   /'
    else
        echo "ℹ️  gradle.properties 中未配置 pod 路径（可选）"
        echo "   如果需要，可以添加："
        echo "   org.jetbrains.kotlin.native.cocoapods.podCommand=$POD_PATH"
    fi
else
    echo "⚠️  未找到 gradle.properties 文件"
fi
echo ""

# 7. 测试 pod 命令
echo "7️⃣  测试 pod 命令..."
if pod --version &> /dev/null; then
    echo "✅ pod 命令可以正常执行"
else
    echo "❌ pod 命令执行失败"
fi
echo ""

# 8. 提供建议
echo "📋 建议"
echo "======"
echo ""

if [ -n "$POD_PATH" ]; then
    if [[ "$PATH" != *"$(dirname "$POD_PATH")"* ]]; then
        echo "⚠️  pod 命令路径不在当前 PATH 中"
        echo ""
        echo "💡 解决方案："
        echo ""
        echo "1. 将以下内容添加到 ~/.zshrc 或 ~/.bash_profile："
        echo "   export PATH=\"$(dirname "$POD_PATH"):\$PATH\""
        echo ""
        echo "2. 重新加载配置："
        echo "   source ~/.zshrc  # 或 source ~/.bash_profile"
        echo ""
        echo "3. 或者在 gradle.properties 中指定 pod 路径："
        echo "   org.jetbrains.kotlin.native.cocoapods.podCommand=$POD_PATH"
    else
        echo "✅ 环境配置正常"
        echo ""
        echo "如果 Gradle 仍然找不到 pod，可以尝试："
        echo "1. 在 gradle.properties 中明确指定 pod 路径"
        echo "2. 确保使用正确的 shell 环境运行 Gradle"
    fi
fi

echo ""
echo "✅ 检查完成！"
