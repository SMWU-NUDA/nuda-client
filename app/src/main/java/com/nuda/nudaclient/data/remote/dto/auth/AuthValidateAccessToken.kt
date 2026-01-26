package com.nuda.nudaclient.data.remote.dto.auth

data class AuthValidateAccessToken(
    val code: String,
    val `data`: Data,
    val message: String,
    val success: Boolean
) {
    data class Data(
        val email: String,
        val id: Int,
        val nickname: String,
        val profileImg: String,
        val username: String
    )
}