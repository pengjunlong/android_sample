package com.example.framework.core.initializer

import android.app.Application
import android.util.Log

/**
 * 框架统一初始化调度器
 *
 * 负责按 [IInitializer.priority] 顺序执行所有已注册的模块初始化器。
 *
 * ### 使用方式
 * 在 [com.example.framework.core.BaseApplication.onAppCreate] **之前**，
 * 重写 [com.example.framework.core.BaseApplication.registerInitializers] 注册各模块初始化器：
 *
 * ```kotlin
 * class MyApp : BaseApplication() {
 *     override fun registerInitializers() {
 *         FrameworkInitializer.register(CrashReporter.initializer(crashConfig))
 *         FrameworkInitializer.register(LoggerInitializer())
 *         FrameworkInitializer.register(NetworkInitializer(networkConfig))
 *         FrameworkInitializer.register(StorageInitializer())
 *     }
 *
 *     override fun onAppCreate() {
 *         // 所有框架模块已初始化完毕，在此做业务初始化
 *     }
 * }
 * ```
 */
object FrameworkInitializer {

    private const val TAG = "FrameworkInitializer"

    private val initializers = mutableListOf<IInitializer>()

    @Volatile
    private var initialized = false

    /**
     * 注册初始化器（必须在 [init] 调用之前注册）
     */
    fun register(initializer: IInitializer) {
        check(!initialized) {
            "Cannot register initializer after FrameworkInitializer.init() has been called."
        }
        initializers.add(initializer)
    }

    /**
     * 按优先级顺序执行所有已注册的初始化器
     * 由 [com.example.framework.core.BaseApplication] 内部调用
     */
    internal fun init(application: Application) {
        if (initialized) return

        initializers.sortBy { it.priority() }

        initializers.forEach { initializer ->
            val name = initializer::class.java.simpleName
            val start = System.currentTimeMillis()
            runCatching {
                initializer.initialize(application)
            }.onSuccess {
                Log.d(TAG, "$name initialized in ${System.currentTimeMillis() - start}ms")
            }.onFailure { e ->
                Log.e(TAG, "Failed to initialize $name", e)
            }
        }

        initialized = true
        Log.i(TAG, "All ${initializers.size} framework module(s) initialized.")
    }

    val isInitialized: Boolean get() = initialized
}

