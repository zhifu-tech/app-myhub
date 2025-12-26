package tech.zhifu.app.myhub.di

import org.koin.core.module.Module

/**
 * 平台特定模块接口
 * 各平台需要提供具体实现
 */
expect fun platformModule(): Module


