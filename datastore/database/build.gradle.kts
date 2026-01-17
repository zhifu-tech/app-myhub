plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.myhub.kmp.android)
    alias(libs.plugins.myhub.kmp.ios)
    alias(libs.plugins.myhub.kmp.jvm)
    alias(libs.plugins.myhub.kmp.js)
    alias(libs.plugins.myhub.kmp.wasmJs)
    alias(libs.plugins.sqldelight)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.datastore.database"
    }
}

sqldelight {
    databases {
        create("MyHubDatabase") {
            packageName.set("tech.zhifu.app.myhub.datastore.database")
            generateAsync.set(true)
            version = 2
        }
    }
    linkSqlite = true
}

