import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import java.nio.file.Files
import java.nio.file.LinkOption

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

// 如果项目启用了 webpack（配置了 JS 或 WASM 目标），
// 则将 project 下的 webpack.config.d / karma.config.d 软链接到 rootProject/webApp 对应目录。
// 这样全项目共享同一份配置，不再拷贝多份文件。

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

    fun ensureSymlink(sourceDir: File, targetDir: File) {
        if (!sourceDir.exists() || !sourceDir.isDirectory) return

        val targetPath = targetDir.toPath()
        val sourcePath = sourceDir.toPath()
        targetDir.parentFile?.mkdirs()

        if (Files.exists(targetPath, LinkOption.NOFOLLOW_LINKS)) {
            if (Files.isSymbolicLink(targetPath)) {
                val rawLinkedPath = Files.readSymbolicLink(targetPath)
                val resolvedLinkedPath = targetPath.parent.resolve(rawLinkedPath).normalize()
                if (resolvedLinkedPath.toFile().canonicalFile == sourceDir.canonicalFile) return
            }
            targetDir.deleteRecursively()
        }

        Files.createSymbolicLink(targetPath, sourcePath)
    }

    // 创建链接 webpack 配置目录的任务
    val linkWebpackConfigTask = tasks.register("linkWebpackConfigDir") {
        group = "webpack"
        description = "Link project webpack.config.d to rootProject/webApp/webpack.config.d"
        inputs.dir(webpackConfigSourceDir)
        outputs.dir(webpackConfigTargetDir)
        onlyIf {
            webpackConfigSourceDir.exists() && webpackConfigSourceDir.isDirectory
        }
        doLast {
            ensureSymlink(webpackConfigSourceDir, webpackConfigTargetDir)
        }
    }

    // 创建链接 karma 配置目录的任务
    val linkKarmaConfigTask = tasks.register("linkKarmaConfigDir") {
        group = "karma"
        description = "Link project karma.config.d to rootProject/webApp/karma.config.d"
        inputs.dir(karmaConfigSourceDir)
        outputs.dir(karmaConfigTargetDir)
        onlyIf {
            karmaConfigSourceDir.exists() && karmaConfigSourceDir.isDirectory
        }
        doLast {
            ensureSymlink(karmaConfigSourceDir, karmaConfigTargetDir)
        }
    }

    // 创建组合任务，链接所有配置目录
    val linkAllConfigDirsTask = tasks.register("linkWebpackAndKarmaConfigDirs") {
        group = "webpack"
        description = "Link webpack and karma config directories from root webApp"
        dependsOn(linkWebpackConfigTask, linkKarmaConfigTask)
    }

    // 让编译任务依赖复制任务
    tasks.matching { task ->
        task.name.startsWith("compileKotlinJs") ||
            task.name.startsWith("compileTestKotlinJs") ||
            task.name.startsWith("compileKotlinWasmJs") ||
            task.name.startsWith("compileTestKotlinWasmJs")
    }.configureEach {
        dependsOn(linkAllConfigDirsTask)
    }

    // 让 webpack 任务依赖复制任务
    tasks.withType<KotlinWebpack>().configureEach {
        dependsOn(linkAllConfigDirsTask)
    }

    // 让测试任务依赖复制任务
    tasks.matching { task ->
        task.name.contains("jsBrowserTest") ||
            task.name.contains("wasmJsBrowserTest") ||
            task.name.contains("jsTest") ||
            task.name.contains("wasmJsTest")
    }.configureEach {
        dependsOn(linkAllConfigDirsTask)
    }
}
