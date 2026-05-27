package tech.zhifu.app.myhub.startup

interface StartupTask {
    val id: String
    val critical: Boolean
    val dependencies: Set<String>

    suspend fun run()
}

