package com.recordlab.dailyscoop.network.response

data class ResponseSignIn(
    val data: UserInfoData
)

data class UserInfoData(
    val nickname: String,
    val token: String
)