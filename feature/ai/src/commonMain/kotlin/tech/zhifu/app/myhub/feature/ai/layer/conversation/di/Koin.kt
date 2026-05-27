package tech.zhifu.app.myhub.feature.ai.layer.conversation.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tech.zhifu.app.myhub.feature.ai.layer.conversation.ConversationEngine
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionPlanner
import tech.zhifu.app.myhub.feature.ai.layer.conversation.context.ContextManager
import tech.zhifu.app.myhub.feature.ai.layer.conversation.slot.SlotManager
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.StateMachine
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.StateGuard

fun conversationModule() = module {
    singleOf(::StateMachine)
    singleOf(::SlotManager)
    singleOf(::ActionPlanner)
    singleOf(::ContextManager)
    singleOf(::StateGuard)
    singleOf(::ConversationEngine)
}
