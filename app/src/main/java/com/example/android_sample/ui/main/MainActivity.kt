package com.example.android_sample.ui.main

import android.view.View
import androidx.activity.viewModels
import com.example.android_sample.databinding.ActivityMainBinding
import com.example.framework.ui.base.BaseActivity
import com.example.framework.ui.ext.toast

/**
 * 示例 MainActivity
 *
 * 演示：
 * - 继承 [BaseActivity]，通过 ViewBinding 访问 View
 * - 通过 [launchWhenStarted] 安全订阅 Flow
 * - 统一处理 loading / error 状态
 */
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private val viewModel: MainViewModel by viewModels()

    override fun initViews() {
        supportActionBar?.title = "Android Framework Sample"

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
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

