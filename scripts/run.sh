#!/bin/bash

# ============================================
# MyHub - 统一管理脚本 (v3.0)
# ============================================
# 用于快速运行不同平台的应用
# 支持: Desktop, Web, Android, iOS, Server
# ============================================

set -e

# ============================================
# 路径设置
# ============================================

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# 项目根目录（scripts 的父目录）
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# 切换到项目根目录
cd "$PROJECT_ROOT"

# ============================================
# 颜色输出定义
# ============================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# ============================================
# 工具函数
# ============================================

# 打印信息消息
print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

# 打印成功消息
print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

# 打印警告消息
print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# 打印错误消息
print_error() {
    echo -e "${RED}✗${NC} $1"
}

# ============================================
# 显示函数
# ============================================

# 显示欢迎信息
show_welcome() {
    local cmd="${1:-}"
    echo -e "${CYAN}"
    echo " ________  __        _     ___           _________               __       "
    echo "|  __   _|[  |      (_)  .' ..]         |  _   _  |             [  |      "
    echo "|_/  / /   | |--.   __  _| |_  __   _   |_/ | | \_|.---.  .---.  | |--.   "
    echo "   .'.' _  | .-. | [  |'-| |-'[  | | |      | |   / /__\\\\/ /'\`\\] | .-. |  "
    echo " _/ /__/ | | | | |  | |  | |   | \_/ |,    _| |_  | \__.,| \__.  | | | |  "
    echo "|________|[___]|__][___][___]  '.__.'_/   |_____|  '.__.''.___.'[___]|__] "
    echo "                                                                          "
    echo -e "${NC}"
    echo -e "${YELLOW}命令: ${cmd:-help}${NC}"
    echo ""
}

# 显示帮助信息
show_help() {
    echo -e "${CYAN}MyHub - 统一管理脚本 - 使用帮助${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC}"
    echo "  ./scripts/run.sh [command] [options]"
    echo ""
    echo -e "${YELLOW}可用命令:${NC}"
    echo ""
    echo -e "  ${GREEN}desktop${NC}             运行 Desktop 应用"
    echo -e "  ${GREEN}web${NC}                 运行 Web 应用（浏览器）"
    echo -e "  ${GREEN}android${NC}             构建并安装 Android 应用"
    echo -e "  ${GREEN}ios${NC}                 构建并打开 iOS 项目（自动检测设备）"
    echo -e "  ${GREEN}ios simulator${NC}       构建并打开 iOS 项目（使用模拟器）"
    echo -e "  ${GREEN}ios device${NC}          构建并打开 iOS 项目（使用真机）"
    echo -e "  ${GREEN}ios list${NC}            列出所有可用设备"
    echo -e "  ${GREEN}server${NC}              运行服务器（SQLite）"
    echo -e "  ${GREEN}server dev${NC}          运行服务器（开发模式，支持热重载）"
    echo -e "  ${GREEN}server postgres${NC}     运行服务器（PostgreSQL）"
    echo -e "  ${GREEN}server docker${NC}       使用 Docker Compose 运行（PostgreSQL）"
    echo -e "  ${GREEN}build${NC}               构建所有模块"
    echo -e "  ${GREEN}clean${NC}               清理所有构建文件"
    echo ""
    echo -e "${YELLOW}可用选项:${NC}"
    echo ""
    echo -e "  ${GREEN}--debug${NC}            调试模式构建（默认）"
    echo -e "  ${GREEN}--release${NC}          发布模式构建"
    echo -e "  ${GREEN}--dev${NC}              开发环境（默认）"
    echo -e "  ${GREEN}--prod${NC}             生产环境"
    echo -e "  ${GREEN}--free${NC}             免费版（默认）"
    echo -e "  ${GREEN}--premium${NC}          收费版"
    echo -e "  ${GREEN}--help${NC} / ${GREEN}-h${NC}       显示此帮助信息"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  ./scripts/run.sh desktop                    # 运行桌面应用（默认：debug, dev, free）"
    echo "  ./scripts/run.sh desktop --release          # 运行桌面应用（release 模式）"
    echo "  ./scripts/run.sh desktop --prod --premium   # 运行桌面应用（生产环境，收费版）"
    echo "  ./scripts/run.sh web                        # 运行 Web 应用"
    echo "  ./scripts/run.sh android                    # 构建并安装 Android 应用（默认：debug, dev, free）"
    echo "  ./scripts/run.sh android --release          # 构建并安装 Android 应用（release 模式）"
    echo "  ./scripts/run.sh android --prod --premium   # 构建并安装 Android 应用（生产环境，收费版）"
    echo "  ./scripts/run.sh ios                 # 构建并打开 iOS 项目"
    echo "  ./scripts/run.sh ios simulator       # 构建并打开 iOS 项目（使用模拟器）"
    echo "  ./scripts/run.sh ios device          # 构建并打开 iOS 项目（使用真机）"
    echo "  ./scripts/run.sh ios list           # 列出所有可用设备"
    echo "  ./scripts/run.sh server             # 运行服务器（SQLite）"
    echo "  ./scripts/run.sh server dev         # 运行服务器（开发模式）"
    echo "  ./scripts/run.sh server postgres    # 运行服务器（PostgreSQL）"
    echo "  ./scripts/run.sh server docker      # Docker 运行（PostgreSQL）"
    echo "  ./scripts/run.sh build               # 构建所有模块"
    echo "  ./scripts/run.sh clean               # 清理所有构建文件"
    echo ""
}

# ============================================
# 检查函数
# ============================================

# 检查 Gradle wrapper 是否存在
check_gradle() {
    if [ ! -f "./gradlew" ]; then
        print_error "未找到 gradlew，请确保在项目根目录运行此脚本"
        exit 1
    fi
    
    # 确保 gradlew 有执行权限
    chmod +x ./gradlew
}

# 检查 Xcode 是否安装
check_xcode() {
    if ! command -v xcodebuild &> /dev/null; then
        print_error "未找到 Xcode，请先安装 Xcode"
        exit 1
    fi
}

# 检查并终止占用端口的进程
check_and_kill_port() {
    local port="${1:-8083}"
    
    # 检查端口是否被占用
    local pid=$(lsof -ti :"$port" 2>/dev/null)
    
    if [ -n "$pid" ]; then
        print_warning "检测到端口 $port 被进程占用 (PID: $pid)"
        print_info "正在终止占用端口的进程..."
        
        # 尝试优雅终止
        kill "$pid" 2>/dev/null
        
        # 等待进程终止（最多等待 3 秒）
        local count=0
        while [ $count -lt 3 ] && kill -0 "$pid" 2>/dev/null; do
            sleep 1
            count=$((count + 1))
        done
        
        # 如果进程仍在运行，强制终止
        if kill -0 "$pid" 2>/dev/null; then
            print_warning "进程未响应，强制终止..."
            kill -9 "$pid" 2>/dev/null
        fi
        
        # 再次检查端口是否已释放
        if lsof -ti :"$port" >/dev/null 2>&1; then
            print_error "无法释放端口 $port，请手动检查"
            return 1
        else
            print_success "端口 $port 已释放"
            return 0
        fi
    else
        print_info "端口 $port 未被占用"
        return 0
    fi
}

# ============================================
# 命令执行函数
# ============================================

# 运行 Desktop 应用
run_desktop() {
    local build_type="${1:-debug}"
    local environment="${2:-dev}"
    local version="${3:-free}"
    
    print_info "正在运行 Desktop 应用（${build_type} 模式，${environment} 环境，${version} 版）..."
    
    # 设置环境变量传递给应用
    export APP_BUILD_TYPE="$build_type"
    export APP_ENVIRONMENT="$environment"
    export APP_VERSION="$version"
    
    if [ "$build_type" = "release" ]; then
        ./gradlew :composeApp:runDistributable --args="--release"
    else
        ./gradlew :composeApp:runDistributable
    fi
}

# 运行 Web 应用
run_web() {
    local build_type="${1:-debug}"
    local environment="${2:-dev}"
    local version="${3:-free}"
    
    print_info "正在运行 Web 应用（${build_type} 模式，${environment} 环境，${version} 版）..."
    print_info "应用将在浏览器中自动打开"
    
    # 设置环境变量传递给应用
    export APP_BUILD_TYPE="$build_type"
    export APP_ENVIRONMENT="$environment"
    export APP_VERSION="$version"
    
    if [ "$build_type" = "release" ]; then
        ./gradlew :composeApp:jsBrowserProductionRun
    else
        ./gradlew :composeApp:jsBrowserDevelopmentRun
    fi
}

# 构建并安装 Android 应用
run_android() {
    local build_type="${1:-debug}"
    local environment="${2:-dev}"
    local version="${3:-free}"
    
    # 根据环境 and 版本组合变体名称
    # 注意：Gradle 生成的目录名格式是 {env小写}{Version首字母大写}，如 prodFree, devPremium
    local version_title=$(echo "$version" | awk '{print toupper(substr($0,1,1)) substr($0,2)}')
    local variant="${environment}${version_title}"
    
    print_info "变体: ${variant} (${environment} 环境 + ${version} 版)"
    
    if [ "$build_type" = "release" ]; then
        print_info "正在构建 Android 应用（Release 模式，变体: ${variant}）..."
        ./gradlew :androidApp:assemble${variant}Release
    else
        print_info "正在构建 Android 应用（Debug 模式，变体: ${variant}）..."
        ./gradlew :androidApp:assemble${variant}Debug
    fi
    
    # 根据环境 and 版本调整包名
    local package_name="tech.zhifu.app.myhub"
    if [ "$environment" = "dev" ]; then
        if [ "$version" = "free" ]; then
            package_name="tech.zhifu.app.myhub.dev.free"
        else
            package_name="tech.zhifu.app.myhub.dev.premium"
        fi
    else
        if [ "$version" = "free" ]; then
            package_name="tech.zhifu.app.myhub.free"
        else
            package_name="tech.zhifu.app.myhub.premium"
        fi
    fi
    
    print_info "正在安装到设备..."
    if [ "$build_type" = "release" ]; then
        # Release 版本没有 install 任务，需要手动使用 adb 安装
        # APK 路径格式: androidApp/build/outputs/apk/{variant驼峰命名}/{buildType}/androidApp-{variant小写}-{buildType}[-unsigned].apk
        # 注意：目录名使用驼峰命名（如 prodFree），文件名使用小写加连字符（如 prod-free）
        
        # 使用驼峰命名的目录查找 APK 文件（Gradle 生成的目录名是驼峰命名）
        local apk_path=$(find androidApp/build/outputs/apk -path "*/${variant}/${build_type}/*.apk" -type f 2>/dev/null | head -1)
        
        if [ -z "$apk_path" ] || [ ! -f "$apk_path" ]; then
            print_error "未找到 APK 文件，请检查构建是否成功"
            print_info "预期路径: androidApp/build/outputs/apk/${variant}/${build_type}/"
            print_info "请确保已成功构建 Release 版本"
            exit 1
        fi
        
        print_info "找到 APK: $apk_path"
        
        if command -v adb &> /dev/null; then
            # 先卸载旧版本（如果存在）
            adb uninstall "$package_name" 2>/dev/null || true
            
            # 安装新版本（-r 表示替换已存在的应用）
            adb install -r "$apk_path"
            if [ $? -ne 0 ]; then
                print_error "安装失败，请确保设备已连接或模拟器正在运行"
                print_info "如果是因为签名问题，请使用 Debug 模式或配置签名"
                exit 1
            fi
        else
            print_error "未找到 adb 命令，无法安装 Release 版本"
            print_info "APK 文件位置: $apk_path"
            print_info "请手动使用 adb install 安装，或使用 Debug 模式"
            exit 1
        fi
    else
        # Debug 版本使用 Gradle install 任务
        ./gradlew :androidApp:install${variant}Debug
        if [ $? -ne 0 ]; then
            print_error "安装失败，请确保设备已连接或模拟器正在运行"
            exit 1
        fi
    fi
    
    local build_type_capitalized=$(echo "$build_type" | awk '{print toupper(substr($0,1,1)) substr($0,2)}')
    print_success "Android 应用已成功安装（变体: ${variant}${build_type_capitalized}）"
    
    # 启动 MainActivity
    print_info "正在启动应用..."
    
    # 使用 adb 启动 MainActivity
    if command -v adb &> /dev/null; then
        adb shell am start -n "${package_name}/tech.zhifu.app.myhub.app.MainActivity" 2>/dev/null
        if [ $? -eq 0 ]; then
            print_success "应用已启动"
        else
            print_warning "无法自动启动应用，请手动打开"
        fi
    else
        print_warning "未找到 adb 命令，无法自动启动应用"
        print_info "请手动在设备上打开应用"
    fi
}

# 列出可用的 iOS 设备
list_ios_devices() {
    print_info "正在查找可用设备..."
    echo ""
    
    print_info "📱 iOS 模拟器:"
    local simulators=$(xcrun simctl list devices available 2>/dev/null | grep -E "(iPhone|iPad)" | sed 's/^[[:space:]]*/  /' || echo "  未找到模拟器")
    if [ -n "$simulators" ] && [ "$simulators" != "  未找到模拟器" ]; then
        echo "$simulators" | head -10
    else
        echo "  未找到可用的模拟器"
    fi
    echo ""
    
    print_info "📲 真机设备:"
    local devices=$(xcrun xctrace list devices 2>/dev/null | grep -E "(iPhone|iPad|Mac)" | grep -v "Simulator" | sed 's/^[[:space:]]*/  /' || echo "  未找到真机")
    if [ -n "$devices" ] && [ "$devices" != "  未找到真机" ]; then
        echo "$devices" | head -10
    else
        echo "  未找到已连接的真机设备"
    fi
    echo ""
}

# 构建 iOS Framework 并打开 Xcode 项目
run_ios() {
    check_xcode
    
    local device_type="${1:-auto}"
    
    # 使用 .xcworkspace（CocoaPods 集成）或 .xcodeproj（非 CocoaPods）
    local xcode_workspace="iosApp/iosApp.xcworkspace"
    local xcode_project="iosApp/iosApp.xcodeproj"
    
    if [ -d "$xcode_workspace" ]; then
        local xcode_target="$xcode_workspace"
        print_info "使用 CocoaPods 工作空间: $xcode_workspace"
    elif [ -d "$xcode_project" ]; then
        local xcode_target="$xcode_project"
        print_info "使用 Xcode 项目: $xcode_project"
    else
        print_error "未找到 Xcode 项目或工作空间"
        exit 1
    fi
    
    # 如果是 list 命令，只列出设备，不构建 Framework
    if [ "$device_type" = "list" ]; then
        list_ios_devices
        echo ""
        print_info "💡 提示: 使用以下命令打开 Xcode 项目并构建:"
        echo "  ./scripts/run.sh ios              # 自动检测设备"
        echo "  ./scripts/run.sh ios simulator     # 使用模拟器"
        echo "  ./scripts/run.sh ios device        # 使用真机"
        return 0
    fi
    
    print_info "正在构建 iOS Framework..."
    print_info "注意: Framework 将在 Xcode 构建时自动生成"
    
    # 设置环境变量以匹配 Xcode 构建脚本的期望
    export OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED="NO"
    
    # 尝试构建 framework（如果失败也不影响，Xcode 会自动构建）
    # 注意：使用 CocoaPods 时，应该使用 syncFramework 而不是 embedAndSignAppleFrameworkForXcode
    ./gradlew :composeApp:syncFramework \
        -Pkotlin.native.cocoapods.platform=iphonesimulator \
        -Pkotlin.native.cocoapods.archs="arm64" \
        -Pkotlin.native.cocoapods.configuration=Debug 2>/dev/null || {
        print_warning "预构建 Framework 失败，将在 Xcode 中自动构建"
    }
    
    # 根据设备类型显示提示信息
    case "$device_type" in
        simulator|sim)
            print_info "设备类型: iOS 模拟器"
            print_info "提示: 在 Xcode 中选择 iOS 模拟器（iPhone 或 iPad）"
            ;;
        device|physical|real)
            print_info "设备类型: 真机设备"
            print_info "提示: 在 Xcode 中选择已连接的真机设备（iPhone 或 iPad）"
            ;;
        mac)
            print_info "设备类型: Mac"
            print_info "提示: 在 Xcode 中选择 Mac 设备（需要 Mac Catalyst 支持）"
            ;;
        auto|*)
            print_info "设备类型: 自动检测"
            list_ios_devices
            ;;
    esac
    
    print_info "正在打开 Xcode..."
    open "$xcode_target"
    print_success "Xcode 已打开"
    
    print_info "请在 Xcode 中选择目标设备并点击运行按钮"
    echo ""
    print_info "💡 提示:"
    echo "  - 模拟器: 选择 'iOS Simulator' > iPhone 或 iPad"
    echo "  - 真机: 选择已连接的 iPhone 或 iPad 设备"
    echo "  - Mac: 选择 Mac 设备（需要 Mac Catalyst 支持）"
}

# 运行服务器
run_server() {
    local mode="${1:-normal}"
    local server_port="${SERVER_PORT:-8083}"
    
    case "$mode" in
        dev|development)
            print_info "正在启动服务器（开发模式，支持热重载）..."
            print_info "数据库: SQLite (默认)"
            print_info "端口: $server_port"
            
            # 检查并终止占用端口的进程
            if ! check_and_kill_port "$server_port"; then
                print_error "无法启动服务器，端口 $server_port 仍被占用"
                exit 1
            fi
            
            echo ""
            ./gradlew :server:run -Pdevelopment
            ;;
        postgres|postgresql)
            print_info "正在启动服务器（使用 PostgreSQL）..."
            print_info "请确保已设置以下环境变量:"
            print_info "  DB_TYPE=POSTGRESQL"
            print_info "  DB_HOST=localhost"
            print_info "  DB_PORT=5432"
            print_info "  DB_NAME=myhub"
            print_info "  DB_USER=postgres"
            print_info "  DB_PASSWORD=your_password"
            echo ""
            
            # 检查环境变量
            if [ -z "$DB_TYPE" ] || [ "$DB_TYPE" != "POSTGRESQL" ]; then
                print_warning "未检测到 DB_TYPE=POSTGRESQL，将使用默认值"
                export DB_TYPE=POSTGRESQL
                export DB_HOST=${DB_HOST:-localhost}
                export DB_PORT=${DB_PORT:-5432}
                export DB_NAME=${DB_NAME:-myhub}
                export DB_USER=${DB_USER:-postgres}
                export DB_PASSWORD=${DB_PASSWORD:-postgres}
                print_info "使用默认配置:"
                print_info "  DB_HOST=${DB_HOST}"
                print_info "  DB_PORT=${DB_PORT}"
                print_info "  DB_NAME=${DB_NAME}"
                print_info "  DB_USER=${DB_USER}"
            fi
            
            ./gradlew :server:run
            ;;
        docker)
            print_info "正在使用 Docker Compose 启动服务器（PostgreSQL）..."
            cd "$PROJECT_ROOT/server" || exit 1
            if [ ! -f "docker-compose.yml" ]; then
                print_error "未找到 docker-compose.yml 文件"
                exit 1
            fi
            # 优先使用 docker compose（新版本），回退到 docker-compose（旧版本）
            if command -v docker &> /dev/null && docker compose version &> /dev/null; then
                docker compose up -d
                print_success "服务器已启动"
                print_info "查看日志: docker compose logs -f myhub-server"
                print_info "停止服务: docker compose down"
            elif command -v docker-compose &> /dev/null; then
                docker-compose up -d
                print_success "服务器已启动"
                print_info "查看日志: docker-compose logs -f myhub-server"
                print_info "停止服务: docker-compose down"
            else
                print_error "未找到 docker 或 docker-compose 命令"
                exit 1
            fi
            ;;
        *)
            print_info "正在启动服务器（SQLite 模式）..."
            print_info "端口: $server_port"
            print_info "数据库: SQLite (.myhub/myhub.db)"
            
            # 检查并终止占用端口的进程
            if ! check_and_kill_port "$server_port"; then
                print_error "无法启动服务器，端口 $server_port 仍被占用"
                exit 1
            fi
            
            echo ""
            ./gradlew :server:run
            ;;
    esac
}

# 构建所有模块
build_all() {
    print_info "正在构建所有模块..."
    ./gradlew build
    print_success "构建完成"
}

# 清理构建文件
clean_all() {
    print_info "正在清理所有构建文件..."
    ./gradlew clean
    print_success "清理完成"
}

# ============================================
# 参数解析函数
# ============================================

# 解析参数
parse_args() {
    local command=""
    local subcommand=""
    local build_type="debug"
    local environment="dev"
    local version="free"
    
    # 如果没有参数，返回空
    if [ $# -eq 0 ]; then
        return 0
    fi
    
    # 处理选项和参数
    while [[ $# -gt 0 ]]; do
        case "$1" in
            --debug)
                build_type="debug"
                shift
                ;;
            --release)
                build_type="release"
                shift
                ;;
            --dev)
                environment="dev"
                shift
                ;;
            --prod)
                environment="prod"
                shift
                ;;
            --free)
                version="free"
                shift
                ;;
            --premium)
                version="premium"
                shift
                ;;
            --help|-h)
                show_help
                exit 0
                ;;
            *)
                if [ -z "$command" ]; then
                    command="$1"
                elif [ -z "$subcommand" ] && [[ "$command" == "ios" || "$command" == "server" ]]; then
                    subcommand="$1"
                fi
                shift
                ;;
        esac
    done
    
    # 如果没有提供命令，返回空
    if [ -z "$command" ]; then
        return 0
    fi
    
    echo "$command|$subcommand|$build_type|$environment|$version"
}

# ============================================
# 主函数
# ============================================

main() {
    # 如果没有参数，显示帮助
    if [ $# -eq 0 ]; then
        show_help
        exit 0
    fi
    
    # 检查是否是帮助请求
    for arg in "$@"; do
        if [ "$arg" = "--help" ] || [ "$arg" = "-h" ]; then
            show_help
            exit 0
        fi
    done
    
    local args_result
    args_result=$(parse_args "$@")
    
    # 如果 parse_args 返回空，显示帮助并退出
    if [ -z "$args_result" ]; then
        show_help
        exit 0
    fi
    
    IFS='|' read -r command subcommand build_type environment version <<< "$args_result"
    
    # 如果命令为空，显示帮助并退出
    if [ -z "$command" ]; then
        show_help
        exit 0
    fi
    
    show_welcome "$command${subcommand:+ $subcommand}"
    check_gradle
    
    # 执行命令
    case "$command" in
        desktop)
            run_desktop "$build_type" "$environment" "$version"
            ;;
        web)
            run_web "$build_type" "$environment" "$version"
            ;;
        android)
            run_android "$build_type" "$environment" "$version"
            ;;
        ios)
            if [ -z "$subcommand" ]; then
                run_ios "auto"
            else
                case "$subcommand" in
                    simulator|sim)
                        run_ios "simulator"
                        ;;
                    device|physical|real)
                        run_ios "device"
                        ;;
                    mac)
                        run_ios "mac"
                        ;;
                    list)
                        run_ios "list"
                        ;;
                    *)
                        print_error "未知的 iOS 子命令: $subcommand"
                        echo ""
                        print_info "可用的 iOS 子命令: simulator, device, mac, list"
                        show_help
                        exit 1
                        ;;
                esac
            fi
            ;;
        server)
            if [ -z "$subcommand" ]; then
                run_server "normal"
            else
                case "$subcommand" in
                    dev|development)
                        run_server "dev"
                        ;;
                    postgres|postgresql)
                        run_server "postgres"
                        ;;
                    docker)
                        run_server "docker"
                        ;;
                    *)
                        print_error "未知的 server 子命令: $subcommand"
                        echo ""
                        print_info "可用的 server 子命令: dev, postgres, docker"
                        show_help
                        exit 1
                        ;;
                esac
            fi
            ;;
        build)
            build_all
            ;;
        clean)
            clean_all
            ;;
        *)
            print_error "未知命令: $command"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

# ============================================
# 脚本入口
# ============================================

main "$@"
