package com.example.framework.crash

/**
 * 崩溃上报配置
 *
 * 通过 [Builder] 构建，传入 [CrashReporter.initializer] 完成模块初始化。
 *
 * ### 示例
 * ```kotlin
 * val config = CrashConfig.Builder()
 *     .reportUrl("https://your-server.com/acra/report")
 *     .enableInDebug(false)
 *     .toastEnabled(true)
 *     .crashListener { thread, throwable ->
 *         // 写本地日志 / 上报自定义监控平台
 *     }
 *     .build()
 * ```
 */
data class CrashConfig(
    /** HTTP 上报地址（null 则不启用 HTTP 上报）*/
    val reportUrl: String? = null,

    /** 是否在 Debug 模式也启用上报（默认 false）*/
    val enableInDebug: Boolean = false,

    /** 是否在 Debug 模式显示 Toast 提示（默认 true）*/
    val toastEnabled: Boolean = true,

    /** 自定义 Toast 提示文字（null 则用默认文案）*/
    val toastText: String? = null,

    /** 崩溃监听（可用于写本地日志等附加操作）*/
    val crashListener: CrashListener? = null,
) {
    /** HTTP 上报是否有效 */
    val httpReportEnabled: Boolean get() = !reportUrl.isNullOrBlank()

    class Builder {
        private var reportUrl: String? = null
        private var enableInDebug: Boolean = false
        private var toastEnabled: Boolean = true
        private var toastText: String? = null
        private var crashListener: CrashListener? = null

        fun reportUrl(url: String) = apply { this.reportUrl = url }
        fun enableInDebug(enabled: Boolean) = apply { this.enableInDebug = enabled }
        fun toastEnabled(enabled: Boolean) = apply { this.toastEnabled = enabled }
        fun toastText(text: String) = apply { this.toastText = text }
        fun crashListener(listener: CrashListener) = apply { this.crashListener = listener }

        fun build() = CrashConfig(
            reportUrl = reportUrl,
            enableInDebug = enableInDebug,
            toastEnabled = toastEnabled,
            toastText = toastText,
            crashListener = crashListener,
        )
    }
}

