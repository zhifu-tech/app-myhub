package tech.zhifu.app.myhub.ui

data class State(
    val module: Module,
    val phase: Phase,
    val context: Context,
    val mode: Mode,
) {
    enum class Module {
        BOOTSTRAP, AUTH, DASHBOARD, CAPTURE, EDIT, CARD, COLLECTION
    }

    enum class Phase {
        INIT, READY, ACTIVE, PROCESSING, RESULT
    }

    enum class Context {
        GLOBAL, SYSTEM, INPUT, LIST, DETAIL, OUTPUT, AUTH, ERROR, DASHBOARD
    }

    enum class Mode {
        IDLE, FOCUSED, LOCKED, DISABLED, COMPLETED, PENDING, EXPIRED
    }
}
