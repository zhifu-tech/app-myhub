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
            dialect("app.cash.sqldelight:sqlite-3-35-dialect:2.2.1")
            generateAsync.set(true)
            version = 0
        }
    }
    linkSqlite = true
}
