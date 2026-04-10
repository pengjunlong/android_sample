package com.pengjunlong.app

import com.example.framework.core.BaseApplication
import com.example.framework.core.initializer.FrameworkInitializer
import com.example.framework.crash.CrashConfig
import com.example.framework.crash.CrashReporter
import com.example.framework.logger.L
import com.example.framework.logger.LoggerInitializer
import com.example.framework.network.NetworkConfig
import com.example.framework.network.NetworkManager
import com.example.framework.storage.StorageInitializer

/**
 * Application 入口
 *
 * 通过 [BaseApplication] 接入框架各模块，各模块均通过 [FrameworkInitializer] 按优先级有序初始化。
 *
 * ### 接入新项目时需关注：
 * - [BuildConfig.API_BASE_URL]：替换为实际 API 地址（在 build.gradle.kts 配置）
 * - [BuildConfig.UPDATE_REPO_OWNER] / [BuildConfig.UPDATE_REPO_NAME]：替换为自己的 GitHub 仓库
 * - [CrashConfig]：按需调整崩溃上报策略
 */
class App : BaseApplication() {

    override fun registerInitializers() {
        // 1. 崩溃上报（最高优先级，在 attachBaseContext 阶段初始化）
        //    崩溃后立即发出通知，点击「发送报告」调起系统分享菜单
        FrameworkInitializer.register(
            CrashReporter.initializer(
                CrashConfig.Builder()
                    .enableInDebug(BuildConfig.DEBUG) // Debug 包也启用，方便测试
                    .toastEnabled(true)               // 崩溃时 Toast 提示
                    .crashListener { _, throwable ->
                        L.e(throwable, "App crashed!")
                    }
                    .build()
            )
        )

        // 2. 日志（优先级 -100）
        FrameworkInitializer.register(LoggerInitializer())

        // 3. 存储（优先级 -80）
        FrameworkInitializer.register(StorageInitializer())

        // 4. 网络（优先级 -50）
        FrameworkInitializer.register(
            NetworkManager.initializer(
                NetworkConfig(
                    baseUrl = BuildConfig.API_BASE_URL,
                    enableLogging = BuildConfig.DEBUG,
                )
            )
        )
    }

    override fun onAppCreate() {
        L.i("App started. versionName=${BuildConfig.VERSION_NAME}")
    }
}

