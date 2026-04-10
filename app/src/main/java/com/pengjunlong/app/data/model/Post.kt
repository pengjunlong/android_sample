package com.pengjunlong.app.data.model

import com.google.gson.annotations.SerializedName

// TODO: [SAMPLE] 这是演示用的数据模型（对应 jsonplaceholder.typicode.com API）
//       接入新项目时删除此文件，替换为自己的业务模型
data class Post(
    @SerializedName("id")     val id: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("title")  val title: String,
    @SerializedName("body")   val body: String,
)

