plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.myhub.kmp.android)
    alias(libs.plugins.myhub.kmp.ios)
    alias(libs.plugins.myhub.kmp.jvm)
    alias(libs.plugins.myhub.kmp.js)
    alias(libs.plugins.myhub.kmp.wasmJs)
    alias(libs.plugins.kotlinSerialization)
    // Compose 编译器插件 @Composable
    alias(libs.plugins.jb.composeCompiler)
    alias(libs.plugins.jb.composeMultiplatform)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.analytics"
    }

    // iOS 平台 cinterop 配置（用于调用 Umeng SDK）
    iosTargets().forEach { iosTarget ->
        iosTarget.compilations.getByName("main") {
            if (project.isChannelUmeng()) {
                cinterops {
                    val umeng by creating {
                        defFile(project.file("src/iosUmengMain/cinterop/tech/zhifu/app/myhub/analytics/provider/umeng.def"))
                        packageName("tech.zhifu.app.myhub.analytics.provider.umeng")

                        // 查找 UMCommon 框架路径（支持版本号变化）
                        val podsDir = project.rootDir.resolve("iosApp/Pods/UMCommon")
                        val umCommonDir = podsDir.listFiles()?.firstOrNull {
                            it.isDirectory && it.name.startsWith("UMCommon_")
                        }

                        if (umCommonDir != null) {
                            val xcframework = umCommonDir.resolve("UMCommon.xcframework")
                            // 对于模拟器，使用 ios-arm64_x86_64-simulator
                            // 对于真机，使用 ios-arm64
                            val simulatorFramework =
                                xcframework.resolve("ios-arm64_x86_64-simulator/UMCommon.framework")
                            val deviceFramework = xcframework.resolve("ios-arm64/UMCommon.framework")
                            val simulatorHeaders = simulatorFramework.resolve("Headers")
                            val deviceHeaders = deviceFramework.resolve("Headers")

                            // 添加框架搜索路径（-F 用于查找 .framework）
                            compilerOpts("-F${xcframework.parent}")
                            // 添加头文件搜索路径（-I 用于查找头文件）
                            // 注意：cinterop 需要能够找到 UMCommon/UMConfigure.h，所以需要添加 Headers 目录
                            compilerOpts("-I${simulatorHeaders}")
                            compilerOpts("-I${deviceHeaders}")
                            // 注意：linkerOpts 应该在 .def 文件中配置，而不是在这里
                        } else {
                            // 如果找不到框架，使用 Pods 根目录（CocoaPods 会自动处理）
                            compilerOpts("-F${podsDir}")
                            linkerOpts("-F${podsDir}")
                            linkerOpts("-framework", "UMCommon")
                        }
                    }
                }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.jb.compose.runtime.runtime)

            implementation(projects.core.platform)
            implementation(projects.core.logger)
            implementation(projects.core.appBuildConfig)
            implementation(projects.core.startup)

            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.kotlinx.serialization.json)

            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            when {
                project.isChannelGooglePlay() -> {
                    implementation(libs.firebase.analytics)
                    implementation(project.dependencies.platform(libs.firebase.bom))
                }

                project.isChannelUmeng() -> {
                    implementation(libs.umeng.common)
                    implementation(libs.umeng.asms)
                }
            }
        }

        iosMain.dependencies {
            if (project.isChannelGooglePlay()) {
                implementation(libs.firebase.analytics)
            }
        }

//        jsMain.dependencies {
//            if (project.isChannelGooglePlay()) {
//                implementation(libs.firebase.analytics)
//            }
//        }

        jvmTest.dependencies {
            implementation(libs.kotlin.testJunit)
        }
    }
}
