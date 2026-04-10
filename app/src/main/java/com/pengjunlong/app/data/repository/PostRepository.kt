package com.pengjunlong.app.data.repository

import com.example.framework.network.ApiResult
import com.example.framework.network.NetworkManager
import com.example.framework.network.safeApiCall
import com.pengjunlong.app.data.model.Post
import com.pengjunlong.app.data.remote.PostApiService

// TODO: [SAMPLE] 这是演示用的 Repository，展示 safeApiCall + ApiResult 用法
//       接入新项目时删除此文件，替换为自己的业务 Repository
//
// 通过 [safeApiCall] 将网络请求包装为 [ApiResult]，ViewModel 只需处理结果，无需关心异常。
class PostRepository {

    private val api: PostApiService by lazy {
        NetworkManager.createService(PostApiService::class.java)
    }

    suspend fun fetchPosts(): ApiResult<List<Post>> = safeApiCall {
        api.getPosts()
    }

    suspend fun fetchPost(id: Int): ApiResult<Post> = safeApiCall {
        api.getPost(id)
    }
}

