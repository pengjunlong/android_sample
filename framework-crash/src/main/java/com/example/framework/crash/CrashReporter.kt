package com.example.framework.crash

import android.app.Application
import android.util.Log
import com.example.framework.core.initializer.IInitializer
import com.example.framework.core.utils.AppUtils
import org.acra.ACRA
import org.acra.ReportField
import org.acra.config.CoreConfigurationBuilder
import org.acra.config.HttpSenderConfigurationBuilder
import org.acra.config.ToastConfigurationBuilder
import org.acra.data.StringFormat
import org.acra.sender.HttpSender

/**
 * 崩溃上报管理器（基于 ACRA）
 *
 * ### 功能
 * - 自动捕获未处理的 Uncaught Exception
 * - 支持 HTTP 上报到自建崩溃收集服务器
 * - Debug 模式下 Toast 提示崩溃信息
 * - 支持自定义 [CrashListener] 回调（写本地日志等）
 * - 支持手动上报 try-catch 捕获的非崩溃异常
 *
 * ### 接入方式
 * 在 `BaseApplication.registerInitializers()` 中注册：
 * ```kotlin
 * FrameworkInitializer.register(
 *     CrashReporter.initializer(
 *         CrashConfig.Builder()
 *             .reportUrl("https://your-server.com/acra/report")
 *             .crashListener { _, throwable -> LocalCrashLog.write(throwable) }
 *             .build()
 *     )
 * )
 * ```
 */
object CrashReporter {

    private const val TAG = "CrashReporter"

    @Volatile
    private var config: CrashConfig? = null

    /**
     * 创建崩溃模块初始化器
     */
    fun initializer(config: CrashConfig = CrashConfig()): IInitializer =
        CrashInitializer(config)

    /**
     * 手动上报一个可捕获的异常（如 try-catch 内的异常）
     */
    fun report(throwable: Throwable) {
        if (!ACRA.isInitialised()) {
            Log.w(TAG, "ACRA not initialized, skip reporting: ${throwable.message}")
            return
        }
        ACRA.getErrorReporter().handleException(throwable)
    }

    /**
     * 静默上报（不触发通知/Toast，适合非致命错误）
     */
    fun reportSilent(throwable: Throwable) {
        if (!ACRA.isInitialised()) {
            Log.w(TAG, "ACRA not initialized, skip silent reporting: ${throwable.message}")
            return
        }
        ACRA.getErrorReporter().handleSilentException(throwable)
    }

    /**
     * 附加自定义键值到崩溃报告（如用户 ID、AB 实验分组等）
     */
    fun putCustomData(key: String, value: String) {
        if (!ACRA.isInitialised()) return
        ACRA.getErrorReporter().putCustomData(key, value)
    }

    /** 获取当前配置 */
    fun getConfig(): CrashConfig? = config

    // ─── 内部初始化器 ────────────────────────────────────────────────────────────

    private class CrashInitializer(private val cfg: CrashConfig) : IInitializer {

        // 最高优先级，保证崩溃上报第一个就绪
        override fun priority(): Int = Int.MIN_VALUE

        override fun initialize(application: Application) {
            config = cfg

            // 注册自定义 UncaughtExceptionHandler（在 ACRA handler 链之前）
            val acraHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                runCatching { cfg.crashListener?.onCrash(thread, throwable) }
                acraHandler?.uncaughtException(thread, throwable)
            }

            // 构建 ACRA 配置
            val builder = CoreConfigurationBuilder()
                .withBuildConfigClass(BuildConfig::class.java)
                .withReportFields(
                    ReportField.APP_VERSION_CODE,
                    ReportField.APP_VERSION_NAME,
                    ReportField.ANDROID_VERSION,
                    ReportField.PHONE_MODEL,
                    ReportField.BRAND,
                    ReportField.STACK_TRACE,
                    ReportField.LOGCAT,
                    ReportField.CUSTOM_DATA,
                    ReportField.CRASH_CONFIGURATION,
                    ReportField.TOTAL_MEM_SIZE,
                    ReportField.AVAILABLE_MEM_SIZE,
                    ReportField.USER_APP_START_DATE,
                    ReportField.USER_CRASH_DATE,
                )

            // 配置 HTTP 上报
            if (cfg.httpReportEnabled && cfg.reportUrl != null) {
                builder.getPluginConfigurationBuilder(HttpSenderConfigurationBuilder::class.java)
                    .withUri(cfg.reportUrl)
                    .withHttpMethod(HttpSender.Method.POST)
                    .withReportFormat(StringFormat.JSON)
                    .withEnabled(true)
                Log.d(TAG, "ACRA HTTP sender enabled: ${cfg.reportUrl}")
            }

            // Debug 模式下启用 Toast 提示
            if (cfg.toastEnabled && AppUtils.isDebug()) {
                val toastMsg = cfg.toastText ?: "Crash detected! Report sent."
                builder.getPluginConfigurationBuilder(ToastConfigurationBuilder::class.java)
                    .withText(toastMsg)
                    .withEnabled(true)
                Log.d(TAG, "ACRA Toast reporter enabled")
            }

            ACRA.init(application, builder, cfg.enableInDebug)

            // 附加版本信息到每条崩溃报告
            ACRA.getErrorReporter().run {
                putCustomData("app_version_name", AppUtils.getVersionName())
                putCustomData("app_version_code", AppUtils.getVersionCode().toString())
                putCustomData("package_name", AppUtils.getPackageName())
            }

            Log.i(TAG, "CrashReporter (ACRA) initialized. HTTP=${cfg.httpReportEnabled}")
        }
    }
}

