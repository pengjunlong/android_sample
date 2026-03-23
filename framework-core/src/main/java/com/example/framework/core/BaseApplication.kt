package com.example.framework.core

import android.app.Application
import com.example.framework.core.initializer.FrameworkInitializer

/**
 * Application 基类
 *
 * 所有接入此框架的项目 Application 均应继承此类。继承后：
 * 1. 重写 [registerInitializers] 注册各框架模块初始化器（如日志、崩溃上报、网络等）
 * 2. 重写 [onAppCreate] 编写业务层自定义初始化逻辑
 *
 * ### 示例
 * ```kotlin
 * class MyApp : BaseApplication() {
 *
 *     override fun registerInitializers() {
 *         FrameworkInitializer.register(
 *             CrashReporter.initializer(
 *                 CrashConfig.Builder()
 *                     .reportUrl("https://your-server.com/report")
 *                     .build()
 *             )
 *         )
 *         FrameworkInitializer.register(LoggerInitializer())
 *         FrameworkInitializer.register(StorageInitializer())
 *         FrameworkInitializer.register(NetworkInitializer(NetworkConfig("https://api.example.com/")))
 *     }
 *
 *     override fun onAppCreate() {
 *         // 框架模块已全部就绪，在此做业务初始化
 *     }
 * }
 * ```
 */
abstract class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 1. 全局 Context 初始化（最优先）
        AppContext.init(this)
        // 2. 注册各模块初始化器
        registerInitializers()
        // 3. 按优先级执行所有初始化器
        FrameworkInitializer.init(this)
        // 4. 业务层自定义初始化
        onAppCreate()
    }

    /**
     * 在此方法中注册框架各模块的 [com.example.framework.core.initializer.IInitializer]。
     * 此时 [AppContext] 已就绪，可安全使用。
     */
    protected open fun registerInitializers() {}

    /**
     * 框架所有模块初始化完毕后回调，子类在此执行业务层初始化。
     */
    protected abstract fun onAppCreate()
}

