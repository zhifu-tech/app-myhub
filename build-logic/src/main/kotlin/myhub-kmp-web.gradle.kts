import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

// 如果项目启用了 webpack（配置了 JS 或 WASM 目标），则复制 rootProject 的 webpack 和 karma 配置文件
// 使用独立的复制任务，通过输入/输出声明实现增量构建
// 只有当源文件变化或目标文件不存在时才复制

afterEvaluate {
    val kotlinExtension = project.extensions.findByType<KotlinMultiplatformExtension>()

    // 检查项目是否配置了 JS 或 WASM 目标
    val hasJsTarget = kotlinExtension?.targets?.any {
        it.platformType == KotlinPlatformType.js
    } ?: false

    val hasWasmTarget = kotlinExtension?.targets?.any {
        it.platformType == KotlinPlatformType.wasm
    } ?: false

    if (!hasJsTarget && !hasWasmTarget) {
        return@afterEvaluate
    }

    // 在配置阶段捕获路径（支持配置缓存）
    val rootProjectDir = rootProject.projectDir
    val projectDir = project.projectDir

    val webpackConfigSourceDir = rootProjectDir.resolve("webApp/webpack.config.d")
    val karmaConfigSourceDir = rootProjectDir.resolve("webApp/karma.config.d")
    val webpackConfigTargetDir = projectDir.resolve("webpack.config.d")
    val karmaConfigTargetDir = projectDir.resolve("karma.config.d")

    // 创建复制 webpack 配置文件的任务
    val copyWebpackConfigTask = tasks.register("copyWebpackConfigFiles", Copy::class.java) {
        group = "webpack"
        description =
            "Copy webpack configuration files from rootProject/webApp/webpack.config.d to project directory"

        // 输入：源目录中的所有文件
        from(webpackConfigSourceDir) {
            include("**/*")
            includeEmptyDirs = false
        }

        // 输出：目标目录
        into(webpackConfigTargetDir)

        // 只复制文件，不复制子目录结构（在任务级别处理）
        eachFile {
            // 将文件直接放在目标目录下，不保留子目录结构
            path = name
        }

        // 只有当源目录存在时才执行
        onlyIf {
            webpackConfigSourceDir.exists() && webpackConfigSourceDir.isDirectory
        }
    }

    // 创建复制 karma 配置文件的任务
    val copyKarmaConfigTask = tasks.register("copyKarmaConfigFiles", Copy::class.java) {
        group = "karma"
        description = "Copy karma configuration files from rootProject/webApp/karma.config.d to project directory"

        // 输入：源目录中的所有文件
        from(karmaConfigSourceDir) {
            include("**/*")
            includeEmptyDirs = false
        }

        // 输出：目标目录
        into(karmaConfigTargetDir)

        // 只复制文件，不复制子目录结构（在任务级别处理）
        eachFile {
            // 将文件直接放在目标目录下，不保留子目录结构
            path = name
        }

        // 只有当源目录存在时才执行
        onlyIf {
            karmaConfigSourceDir.exists() && karmaConfigSourceDir.isDirectory
        }
    }

    // 创建组合任务，复制所有配置文件
    val copyAllConfigFilesTask = tasks.register("copyWebpackAndKarmaConfigFiles") {
        group = "webpack"
        description = "Copy all webpack and karma configuration files"
        dependsOn(copyWebpackConfigTask, copyKarmaConfigTask)
    }

    // 让编译任务依赖复制任务
    tasks.matching { task ->
        task.name.startsWith("compileKotlinJs") ||
            task.name.startsWith("compileTestKotlinJs") ||
            task.name.startsWith("compileKotlinWasmJs") ||
            task.name.startsWith("compileTestKotlinWasmJs")
    }.configureEach {
        dependsOn(copyAllConfigFilesTask)
    }

    // 让 webpack 任务依赖复制任务
    tasks.withType<KotlinWebpack>().configureEach {
        dependsOn(copyAllConfigFilesTask)
    }

    // 让测试任务依赖复制任务
    tasks.matching { task ->
        task.name.contains("jsBrowserTest") ||
            task.name.contains("wasmJsBrowserTest") ||
            task.name.contains("jsTest") ||
            task.name.contains("wasmJsTest")
    }.configureEach {
        dependsOn(copyAllConfigFilesTask)
    }
}
