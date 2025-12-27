plugins {
    alias(libs.plugins.myhub.kmp)
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
        }
    }
    linkSqlite = true
}

