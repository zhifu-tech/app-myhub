package tech.zhifu.app.myhub

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform