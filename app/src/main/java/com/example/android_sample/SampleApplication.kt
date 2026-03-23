package com.example.android_sample

import com.example.android_sample.BuildConfig
import com.example.framework.core.BaseApplication
import com.example.framework.core.initializer.FrameworkInitializer
import com.example.framework.crash.CrashConfig
import com.example.framework.crash.CrashReporter
import com.example.framework.logger.L
import com.example.framework.logger.LoggerInitializer
import com.example.framework.logger.ReleaseTree
import com.example.framework.network.NetworkConfig
import com.example.framework.network.NetworkManager
import com.example.framework.storage.KVStore
import com.example.framework.storage.StorageInitializer

/**
 * 示例 Application
 *
 * 演示如何通过 [BaseApplication] 接入框架各模块。
 */
class SampleApplication : BaseApplication() {

    override fun registerInitializers() {
        // 1. 崩溃上报（最高优先级，MIN_VALUE）
        FrameworkInitializer.register(
            CrashReporter.initializer(
                CrashConfig.Builder()
                    // 生产环境替换为真实的崩溃收集服务器地址
                    // .reportUrl("https://your-acra-server.com/report")
                    .enableInDebug(false)          // Debug 包不上报（避免污染数据）
                    .toastEnabled(true)            // Debug 包崩溃时 Toast 提示
                    .crashListener { _, throwable ->
                        // 可在此写本地日志文件
                        L.e(throwable, "App crashed!")
                    }
                    .build()
            )
        )

        // 2. 日志（优先级 -100）
        FrameworkInitializer.register(
            LoggerInitializer()
            // Release 环境如需将 ERROR 上报崩溃平台：
            // object : LoggerInitializer() {
            //     override fun initialize(application: Application) {
            //         if (AppUtils.isDebug()) {
            //             Timber.plant(Timber.DebugTree())
            //         } else {
            //             Timber.plant(ReleaseTree { CrashReporter.reportSilent(it) })
            //         }
            //     }
            // }
        )

        // 3. 存储（优先级 -80）
        FrameworkInitializer.register(StorageInitializer())

        // 4. 网络（优先级 -50）
        FrameworkInitializer.register(
            NetworkManager.initializer(
                NetworkConfig(
                    baseUrl = "https://jsonplaceholder.typicode.com/",
                    enableLogging = BuildConfig.DEBUG,
                )
            )
        )
    }

    override fun onAppCreate() {
        L.i("SampleApplication started. version=${packageName}")

        // 示例：存储用户信息
        // KVStore.putString("app_first_launch", System.currentTimeMillis().toString())
    }
}

