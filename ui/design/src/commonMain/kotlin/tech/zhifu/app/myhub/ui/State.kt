package tech.zhifu.app.myhub.ui

enum class UiPhase {
    INIT, READY, ACTIVE, PROCESSING, RESULT
}

enum class UiContext {
    GLOBAL, SYSTEM, INPUT, LIST, DETAIL, OUTPUT, AUTH, ERROR, DASHBOARD
}

enum class UiMode {
    IDLE, FOCUSED, LOCKED, DISABLED, COMPLETED, PENDING, EXPIRED
}

enum class UiModule {
    BOOTSTRAP, AUTH, DASHBOARD, CAPTURE, EDIT, CARD, COLLECTION
}
