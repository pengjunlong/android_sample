package com.pengjunlong.app.data.remote

import com.pengjunlong.app.data.model.Post
import retrofit2.http.GET
import retrofit2.http.Path

// TODO: [SAMPLE] 这是演示用的 API 接口（对应 https://jsonplaceholder.typicode.com）
//       接入新项目时删除此文件，替换为自己的业务 API 接口
interface PostApiService {

    @GET("posts")
    suspend fun getPosts(): List<Post>

    @GET("posts/{id}")
    suspend fun getPost(@Path("id") id: Int): Post
}

