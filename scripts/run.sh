#!/bin/bash

# ============================================
# MyHub - 统一管理脚本 (v2.0)
# ============================================
# 用于快速运行不同平台的应用
# 支持: Desktop, Web, Android, iOS, Server
# 支持模块化命令结构: [module] [command] [options]
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
    local module="${1:-}"
    local cmd="${2:-}"
    echo -e "${CYAN}"
    echo " ________  __        _     ___           _________               __       "
    echo "|  __   _|[  |      (_)  .' ..]         |  _   _  |             [  |      "
    echo "|_/  / /   | |--.   __  _| |_  __   _   |_/ | | \_|.---.  .---.  | |--.   "
    echo "   .'.' _  | .-. | [  |'-| |-'[  | | |      | |   / /__\\\\/ /'\`\\] | .-. |  "
    echo " _/ /__/ | | | | |  | |  | |   | \_/ |,    _| |_  | \__.,| \__.  | | | |  "
    echo "|________|[___]|__][___][___]  '.__.'_/   |_____|  '.__.''.___.'[___]|__] "
    echo "                                                                          "
    echo -e "${NC}"
    echo -e "${YELLOW}模块: ${module:-ALL} | 命令: ${cmd:-help}${NC}"
    echo ""
}

# 显示帮助信息
show_help() {
    echo -e "${CYAN}MyHub - 统一管理脚本 - 使用帮助${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC}"
    echo "  ./scripts/run.sh [module] [command] [options]"
    echo "  ./scripts/run.sh [command]                    # 简化用法（自动推断模块）"
    echo ""
    echo -e "${YELLOW}可用模块:${NC}"
    echo ""
    echo -e "  ${GREEN}composeApp${NC}     主应用模块（Desktop, Web, Android, iOS）"
    echo -e "  ${GREEN}server${NC}          服务器模块"
    echo -e "  ${GREEN}shared${NC}          共享代码模块"
    echo -e "  ${GREEN}all${NC}             所有模块"
    echo ""
    echo -e "${YELLOW}可用命令:${NC}"
    echo ""
    echo -e "  ${GREEN}composeApp 模块命令:${NC}"
    echo -e "  ${GREEN}desktop${NC}        运行 Desktop 应用"
    echo -e "  ${GREEN}web${NC}            运行 Web 应用（浏览器）"
    echo -e "  ${GREEN}android${NC}        构建并安装 Android 应用"
    echo -e "  ${GREEN}ios${NC}            构建 iOS Framework 并打开 Xcode 项目"
    echo -e "  ${GREEN}build${NC}          构建应用"
    echo -e "  ${GREEN}clean${NC}          清理构建文件"
    echo ""
    echo -e "  ${GREEN}server 模块命令:${NC}"
    echo -e "  ${GREEN}run${NC}            运行服务器（默认 SQLite）"
    echo -e "  ${GREEN}run-dev${NC}        运行服务器（开发模式，支持热重载）"
    echo -e "  ${GREEN}run-postgres${NC}   运行服务器（使用 PostgreSQL）"
    echo -e "  ${GREEN}docker${NC}         使用 Docker Compose 运行（PostgreSQL）"
    echo -e "  ${GREEN}docker-sqlite${NC}   使用 Docker Compose 运行（SQLite）"
    echo -e "  ${GREEN}build${NC}          构建服务器"
    echo -e "  ${GREEN}clean${NC}          清理构建文件"
    echo ""
    echo -e "  ${GREEN}shared 模块命令:${NC}"
    echo -e "  ${GREEN}build${NC}          构建共享模块"
    echo -e "  ${GREEN}clean${NC}          清理构建文件"
    echo ""
    echo -e "  ${GREEN}all 模块命令:${NC}"
    echo -e "  ${GREEN}build${NC}          构建所有模块"
    echo -e "  ${GREEN}clean${NC}          清理所有构建文件"
    echo ""
    echo -e "${YELLOW}可用选项:${NC}"
    echo ""
    echo -e "  ${GREEN}--debug${NC}         调试模式构建"
    echo -e "  ${GREEN}--release${NC}       发布模式构建"
    echo -e "  ${GREEN}--clean${NC}          构建前清理"
    echo -e "  ${GREEN}--help${NC} / ${GREEN}-h${NC}       显示此帮助信息"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  ./scripts/run.sh composeApp desktop          # 运行桌面应用"
    echo "  ./scripts/run.sh composeApp web             # 运行 Web 应用"
    echo "  ./scripts/run.sh composeApp android         # 构建并安装 Android 应用"
    echo "  ./scripts/run.sh composeApp ios              # 构建并打开 iOS 项目"
    echo "  ./scripts/run.sh server run                  # 运行服务器（SQLite）"
    echo "  ./scripts/run.sh server run-dev              # 运行服务器（开发模式）"
    echo "  ./scripts/run.sh server run-postgres         # 运行服务器（PostgreSQL）"
    echo "  ./scripts/run.sh server docker               # Docker 运行（PostgreSQL）"
    echo "  ./scripts/run.sh all build                  # 构建所有模块"
    echo "  ./scripts/run.sh composeApp build --release # 发布模式构建"
    echo ""
    echo -e "${YELLOW}简化用法（向后兼容）:${NC}"
    echo "  ./scripts/run.sh desktop                    # 等同于 composeApp desktop"
    echo "  ./scripts/run.sh web                        # 等同于 composeApp web"
    echo "  ./scripts/run.sh server                     # 等同于 server run"
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

# ============================================
# 命令执行函数
# ============================================

# 运行 Desktop 应用
run_desktop() {
    local build_type="${1:-debug}"
    print_info "正在运行 Desktop 应用（${build_type} 模式）..."
    if [ "$build_type" = "release" ]; then
        ./gradlew :composeApp:runDistributable --args="--release"
    else
        ./gradlew :composeApp:runDistributable
    fi
}

# 运行 Web 应用
run_web() {
    print_info "正在运行 Web 应用..."
    print_info "应用将在浏览器中自动打开"
    ./gradlew :composeApp:jsBrowserDevelopmentRun
}

# 运行服务器
run_server() {
    local mode="${1:-normal}"
    
    case "$mode" in
        dev|development)
            print_info "正在启动服务器（开发模式，支持热重载）..."
            print_info "数据库: SQLite (默认)"
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
            docker-compose up -d
            print_success "服务器已启动"
            print_info "查看日志: docker-compose logs -f myhub-server"
            print_info "停止服务: docker-compose down"
            ;;
        docker-sqlite)
            print_info "正在使用 Docker Compose 启动服务器（SQLite）..."
            cd "$PROJECT_ROOT/server" || exit 1
            if [ ! -f "docker-compose.sqlite.yml" ]; then
                print_error "未找到 docker-compose.sqlite.yml 文件"
                exit 1
            fi
            docker-compose -f docker-compose.sqlite.yml up -d
            print_success "服务器已启动"
            print_info "查看日志: docker-compose -f docker-compose.sqlite.yml logs -f myhub-server"
            print_info "停止服务: docker-compose -f docker-compose.sqlite.yml down"
            ;;
        *)
            print_info "正在启动服务器（SQLite 模式）..."
            print_info "端口: 8083"
            print_info "数据库: SQLite (.myhub/myhub.db)"
            ./gradlew :server:run
            ;;
    esac
}

# 构建并安装 Android 应用
run_android() {
    local build_type="${1:-debug}"
    local should_install="${2:-true}"
    
    if [ "$build_type" = "release" ]; then
        print_info "正在构建 Android 应用（Release 模式）..."
        ./gradlew :composeApp:assembleRelease
    else
        print_info "正在构建 Android 应用（Debug 模式）..."
        ./gradlew :composeApp:assembleDebug
    fi
    
    if [ "$should_install" = "true" ]; then
        print_info "正在安装到设备..."
        if [ "$build_type" = "release" ]; then
            ./gradlew :composeApp:installRelease
        else
            ./gradlew :composeApp:installDebug
        fi
        
        if [ $? -eq 0 ]; then
            print_success "Android 应用已成功安装"
        else
            print_error "安装失败，请确保设备已连接或模拟器正在运行"
            exit 1
        fi
    fi
}

# 构建 iOS Framework 并打开 Xcode 项目
run_ios() {
    check_xcode
    
    local xcode_project="iosApp/iosApp.xcodeproj"
    if [ ! -d "$xcode_project" ]; then
        print_error "未找到 Xcode 项目: $xcode_project"
        exit 1
    fi
    
    print_info "正在构建 iOS Framework..."
    print_info "注意: Framework 将在 Xcode 构建时自动生成"
    
    # 可选：预先构建 framework（Xcode 也会自动构建）
    # 设置环境变量以匹配 Xcode 构建脚本的期望
    export OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED="NO"
    
    # 尝试构建 framework（如果失败也不影响，Xcode 会自动构建）
    ./gradlew :composeApp:embedAndSignAppleFrameworkForXcode 2>/dev/null || {
        print_warning "预构建 Framework 失败，将在 Xcode 中自动构建"
    }
    
    print_info "正在打开 Xcode 项目..."
    open "$xcode_project"
    print_success "Xcode 项目已打开"
    print_info "请在 Xcode 中选择目标设备（模拟器或真机）并点击运行按钮"
}

# 构建模块
build_module() {
    local module="$1"
    local build_type="${2:-debug}"
    
    if [ "$build_type" = "release" ]; then
        print_info "正在构建 ${module} 模块（Release 模式）..."
        ./gradlew :${module}:build -PbuildType=release
    else
        print_info "正在构建 ${module} 模块（Debug 模式）..."
        ./gradlew :${module}:build
    fi
    print_success "${module} 模块构建完成"
}

# 构建所有平台
build_all() {
    local build_type="${1:-debug}"
    if [ "$build_type" = "release" ]; then
        print_info "正在构建所有平台（Release 模式）..."
        ./gradlew build -PbuildType=release
    else
        print_info "正在构建所有平台（Debug 模式）..."
        ./gradlew build
    fi
    print_success "构建完成"
}

# 清理构建文件
clean_build() {
    local module="${1:-all}"
    if [ "$module" = "all" ]; then
        print_info "正在清理所有构建文件..."
        ./gradlew clean
    else
        print_info "正在清理 ${module} 模块构建文件..."
        ./gradlew :${module}:clean
    fi
    print_success "清理完成"
}

# ============================================
# 参数解析函数
# ============================================

# 解析参数
parse_args() {
    local module=""
    local command=""
    local build_type="debug"
    local should_clean=false
    
    # 处理选项
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
            --clean)
                should_clean=true
                shift
                ;;
            --help|-h)
                show_help
                exit 0
                ;;
            *)
                if [ -z "$module" ]; then
                    module="$1"
                elif [ -z "$command" ]; then
                    command="$1"
                else
                    print_error "未知参数: $1"
                    show_help
                    exit 1
                fi
                shift
                ;;
        esac
    done
    
    # 如果没有提供模块和命令，显示帮助
    if [ -z "$module" ] && [ -z "$command" ]; then
        show_help
        exit 0
    fi
    
    # 向后兼容：如果第一个参数是命令而不是模块，自动推断模块
    if [ -z "$command" ]; then
        case "$module" in
            desktop|web|android|ios)
                command="$module"
                module="composeApp"
                ;;
            server|server-run)
                command="run"
                module="server"
                ;;
            build|clean)
                command="$module"
                module="all"
                ;;
            help|--help|-h)
                show_help
                exit 0
                ;;
            *)
                # 尝试作为模块名
                if [ -d "$module" ] || [ "$module" = "all" ] || [ "$module" = "composeApp" ] || [ "$module" = "server" ] || [ "$module" = "shared" ]; then
                    print_error "模块 '$module' 需要指定命令"
                    echo ""
                    show_help
                    exit 1
                else
                    print_error "未知模块或命令: $module"
                    echo ""
                    show_help
                    exit 1
                fi
                ;;
        esac
    fi
    
    # 如果指定了 --clean，先清理
    if [ "$should_clean" = true ]; then
        clean_build "$module"
    fi
    
    echo "$module|$command|$build_type"
}

# ============================================
# 主函数
# ============================================

main() {
    local args_result
    args_result=$(parse_args "$@")
    
    if [ -z "$args_result" ]; then
        exit 0
    fi
    
    IFS='|' read -r module command build_type <<< "$args_result"
    
    show_welcome "$module" "$command"
    check_gradle
    
    # 执行命令
    case "$module" in
        composeApp)
            case "$command" in
                desktop)
                    run_desktop "$build_type"
                    ;;
                web)
                    run_web
                    ;;
                android)
                    run_android "$build_type"
                    ;;
                ios)
                    run_ios
                    ;;
                build)
                    build_module "composeApp" "$build_type"
                    ;;
                clean)
                    clean_build "composeApp"
                    ;;
                *)
                    print_error "composeApp 模块不支持命令: $command"
                    show_help
                    exit 1
                    ;;
            esac
            ;;
        server)
            case "$command" in
                run)
                    run_server "normal"
                    ;;
                run-dev|development)
                    run_server "dev"
                    ;;
                run-postgres|run-postgresql)
                    run_server "postgres"
                    ;;
                docker)
                    run_server "docker"
                    ;;
                docker-sqlite)
                    run_server "docker-sqlite"
                    ;;
                build)
                    build_module "server" "$build_type"
                    ;;
                clean)
                    clean_build "server"
                    ;;
                *)
                    print_error "server 模块不支持命令: $command"
                    show_help
                    exit 1
                    ;;
            esac
            ;;
        shared)
            case "$command" in
                build)
                    build_module "shared" "$build_type"
                    ;;
                clean)
                    clean_build "shared"
                    ;;
                *)
                    print_error "shared 模块不支持命令: $command"
                    show_help
                    exit 1
                    ;;
            esac
            ;;
        all)
            case "$command" in
                build)
                    build_all "$build_type"
                    ;;
                clean)
                    clean_build "all"
                    ;;
                *)
                    print_error "all 模块不支持命令: $command"
                    show_help
                    exit 1
                    ;;
            esac
            ;;
        *)
            print_error "未知模块: $module"
            show_help
            exit 1
            ;;
    esac
}

# ============================================
# 脚本入口
# ============================================

main "$@"
