package com.example.android_sample.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.android_sample.databinding.ActivityMainBinding
import com.example.framework.crash.CrashReporter
import com.example.framework.ui.base.BaseActivity
import com.example.framework.ui.ext.toast

/**
 * 示例 MainActivity
 *
 * 演示：
 * - 继承 [BaseActivity]，通过 ViewBinding 访问 View
 * - 通过 [launchWhenStarted] 安全订阅 Flow
 * - 统一处理 loading / error 状态
 * - ACRA 崩溃上报测试（手动上报 & 触发崩溃）
 */
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private val viewModel: MainViewModel by viewModels()

    /** 请求通知权限（Android 13+，ACRA 崩溃通知需要） */
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            toast("✅ 通知权限已授权，崩溃后将弹出上报通知")
        } else {
            toast("⚠️ 通知权限被拒绝，崩溃上报通知将无法显示")
        }
    }

    override fun initViews() {
        supportActionBar?.title = "Android Framework Sample"

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }

        // Android 13+ 请求通知权限（ACRA 崩溃通知需要）
        requestNotificationPermissionIfNeeded()

        // 手动上报：模拟一个已被 try-catch 捕获的非致命异常（静默上报，不触发通知）
        binding.btnReportManual.setOnClickListener {
            val exception = RuntimeException("这是一个手动触发的测试异常（非致命，ACRA 静默上报）")
            CrashReporter.reportSilent(exception)
            toast("已静默上报，查看 logcat 确认（tag: CrashReporter）")
        }

        // 触发崩溃：抛出未捕获异常，App 崩溃重启后 ACRA 弹通知，点「发送报告」调起分享
        binding.btnTriggerCrash.setOnClickListener {
            toast("即将触发崩溃，重启后点通知中的「发送报告」按鈕…")
            binding.root.postDelayed({
                throw RuntimeException("ACRA 崩溃测试：这是一个故意触发的未捕获异常")
            }, 500)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(permission)
            }
        }
    }

    override fun initObservers() {
        launchWhenStarted {
            viewModel.posts.collect { posts ->
                binding.tvContent.text = buildString {
                    appendLine("✅ 已加载 ${posts.size} 条数据")
                    appendLine()
                    posts.take(5).forEachIndexed { index, post ->
                        appendLine("${index + 1}. ${post.title}")
                    }
                    if (posts.size > 5) appendLine("... 共 ${posts.size} 条")
                }
            }
        }

        launchWhenStarted {
            viewModel.isLoading.collect { loading ->
                showLoading(loading)
            }
        }

        launchWhenStarted {
            viewModel.errorEvent.collect { message ->
                showError(message)
            }
        }
    }

    override fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun showError(message: String) {
        toast("请求失败：$message")
    }
}

