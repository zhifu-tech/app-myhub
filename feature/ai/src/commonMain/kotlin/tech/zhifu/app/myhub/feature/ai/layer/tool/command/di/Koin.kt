package tech.zhifu.app.myhub.feature.ai.layer.tool.command.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommand
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandDispatcher
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandWorker
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.addTag.ToolCommandAddTagExecutor
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.addTag.ToolCommandAddTagValidator
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.attachPickedMedia.MediaAttachmentSupport
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.attachPickedMedia.ToolCommandAttachPickedMediaExecutor
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.attachPickedMedia.ToolCommandAttachPickedMediaValidator
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.captureMediaPhoto.ToolCommandCaptureMediaPhotoExecutor
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.captureMediaPhoto.ToolCommandCaptureMediaPhotoValidator
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.clearLocation.ToolCommandClearLocationExecutor
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.clearLocation.ToolCommandClearLocationValidator
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.generateImage.CaptureImagePromptRefiner
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.generateImage.ToolCommandGenerateImageExecutor
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.generateImage.ToolCommandGenerateImageValidator
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.publishCard.ToolCommandPublishCardExecutor
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.publishCard.ToolCommandPublishCardValidator
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.removeTag.ToolCommandRemoveTagExecutor
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.removeTag.ToolCommandRemoveTagValidator
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.updateLocation.ToolCommandUpdateLocationExecutor
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.updateLocation.ToolCommandUpdateLocationValidator
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.updateSummary.ToolCommandUpdateSummaryExecutor
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.updateSummary.ToolCommandUpdateSummaryValidator
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.updateTitle.ToolCommandUpdateTitleExecutor
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.updateTitle.ToolCommandUpdateTitleValidator
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.updateType.ToolCommandUpdateTypeExecutor
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.updateType.ToolCommandUpdateTypeValidator

fun toolCommandModule() = module {
    factoryOf(::ToolCommandAddTagValidator)
    factoryOf(::ToolCommandAddTagExecutor)
    factory(named<ToolCommand.AddTag>()) {
        ToolCommandWorker(
            commandClass = ToolCommand.AddTag::class,
            validator = get<ToolCommandAddTagValidator>(),
            executorProvider = { get<ToolCommandAddTagExecutor>() },
        )
    } bind ToolCommandWorker::class

    factoryOf(::ToolCommandUpdateTitleValidator)
    factoryOf(::ToolCommandUpdateTitleExecutor)
    factory(named<ToolCommand.UpdateTitle>()) {
        ToolCommandWorker(
            commandClass = ToolCommand.UpdateTitle::class,
            validator = get<ToolCommandUpdateTitleValidator>(),
            executorProvider = { get<ToolCommandUpdateTitleExecutor>() },
        )
    } bind ToolCommandWorker::class

    factoryOf(::ToolCommandRemoveTagValidator)
    factoryOf(::ToolCommandRemoveTagExecutor)
    factory(named<ToolCommand.RemoveTag>()) {
        ToolCommandWorker(
            commandClass = ToolCommand.RemoveTag::class,
            validator = get<ToolCommandRemoveTagValidator>(),
            executorProvider = { get<ToolCommandRemoveTagExecutor>() },
        )
    } bind ToolCommandWorker::class

    factoryOf(::ToolCommandUpdateSummaryValidator)
    factoryOf(::ToolCommandUpdateSummaryExecutor)
    factory(named<ToolCommand.UpdateSummary>()) {
        ToolCommandWorker(
            commandClass = ToolCommand.UpdateSummary::class,
            validator = get<ToolCommandUpdateSummaryValidator>(),
            executorProvider = { get<ToolCommandUpdateSummaryExecutor>() },
        )
    } bind ToolCommandWorker::class

    factoryOf(::ToolCommandUpdateTypeValidator)
    factoryOf(::ToolCommandUpdateTypeExecutor)
    factory(named<ToolCommand.UpdateType>()) {
        ToolCommandWorker(
            commandClass = ToolCommand.UpdateType::class,
            validator = get<ToolCommandUpdateTypeValidator>(),
            executorProvider = { get<ToolCommandUpdateTypeExecutor>() },
        )
    } bind ToolCommandWorker::class

    factoryOf(::ToolCommandUpdateLocationValidator)
    factoryOf(::ToolCommandUpdateLocationExecutor)
    factory(named<ToolCommand.UpdateLocation>()) {
        ToolCommandWorker(
            commandClass = ToolCommand.UpdateLocation::class,
            validator = get<ToolCommandUpdateLocationValidator>(),
            executorProvider = { get<ToolCommandUpdateLocationExecutor>() },
        )
    } bind ToolCommandWorker::class

    factoryOf(::ToolCommandClearLocationValidator)
    factoryOf(::ToolCommandClearLocationExecutor)
    factory(named<ToolCommand.ClearLocation>()) {
        ToolCommandWorker(
            commandClass = ToolCommand.ClearLocation::class,
            validator = get<ToolCommandClearLocationValidator>(),
            executorProvider = { get<ToolCommandClearLocationExecutor>() },
        )
    } bind ToolCommandWorker::class

    factoryOf(::ToolCommandPublishCardValidator)
    factoryOf(::ToolCommandPublishCardExecutor)
    factory(named<ToolCommand.PublishCard>()) {
        ToolCommandWorker(
            commandClass = ToolCommand.PublishCard::class,
            validator = get<ToolCommandPublishCardValidator>(),
            executorProvider = { get<ToolCommandPublishCardExecutor>() },
        )
    } bind ToolCommandWorker::class

    factoryOf(::MediaAttachmentSupport)
    factoryOf(::ToolCommandAttachPickedMediaValidator)
    factoryOf(::ToolCommandAttachPickedMediaExecutor)
    factory(named<ToolCommand.AttachPickedMedia>()) {
        ToolCommandWorker(
            commandClass = ToolCommand.AttachPickedMedia::class,
            validator = get<ToolCommandAttachPickedMediaValidator>(),
            executorProvider = { get<ToolCommandAttachPickedMediaExecutor>() },
        )
    } bind ToolCommandWorker::class

    factoryOf(::ToolCommandCaptureMediaPhotoValidator)
    factoryOf(::ToolCommandCaptureMediaPhotoExecutor)
    factory(named<ToolCommand.CaptureMediaPhoto>()) {
        ToolCommandWorker(
            commandClass = ToolCommand.CaptureMediaPhoto::class,
            validator = get<ToolCommandCaptureMediaPhotoValidator>(),
            executorProvider = { get<ToolCommandCaptureMediaPhotoExecutor>() },
        )
    } bind ToolCommandWorker::class

    factoryOf(::CaptureImagePromptRefiner)
    factoryOf(::ToolCommandGenerateImageValidator)
    factoryOf(::ToolCommandGenerateImageExecutor)
    factory(named<ToolCommand.GenerateImage>()) {
        ToolCommandWorker(
            commandClass = ToolCommand.GenerateImage::class,
            validator = get<ToolCommandGenerateImageValidator>(),
            executorProvider = { get<ToolCommandGenerateImageExecutor>() },
        )
    } bind ToolCommandWorker::class

    factory {
        ToolCommandDispatcher(
            workers = getAll<ToolCommandWorker>(),
        )
    }
}
