package tech.zhifu.app.myhub.ui

data class State(
    val module: Module,
    val phase: Phase,
    val context: Context,
    val mode: Mode,
) {
    enum class Module {
        BOOTSTRAP, AUTH, DASHBOARD, CAPTURE, EDIT, CARD, COLLECTION, SETTINGS
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

    companion object {
        fun initGlobalLoading(module: Module) = State(
            module = module,
            phase = Phase.INIT,
            context = Context.GLOBAL,
            mode = Mode.PENDING,
        )


        fun resultErrorDisabled(module: Module) = State(
            module = module,
            phase = Phase.RESULT,
            context = Context.ERROR,
            mode = Mode.DISABLED,
        )

        fun resultOutputCompleted(module: Module) = State(
            module = module,
            phase = Phase.RESULT,
            context = Context.OUTPUT,
            mode = Mode.COMPLETED,
        )
    }
}

fun State.isInitGlobalLoading() = phase == State.Phase.INIT && context == State.Context.GLOBAL &&
    mode == State.Mode.PENDING

fun State.isResultErrorDisabled() = phase == State.Phase.RESULT && context == State.Context.ERROR &&
    mode == State.Mode.DISABLED

fun State.isResultOutputCompleted() = phase == State.Phase.RESULT && context == State.Context.OUTPUT &&
    mode == State.Mode.COMPLETED
