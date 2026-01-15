package com.nuda.nudaclient.data.remote.dto.auth

data class AuthLoginResponse(
    val code: String,
    val `data`: Data?,
    val message: String,
    val success: Boolean
) {
    data class Data(
        val accessToken: String,
        val meResponse: MeResponse,
        val refreshToken: String
    ) {
        data class MeResponse(
            val email: String,
            val id: Int,
            val nickname: String,
            val profileImg: String,
            val username: String
        )
    }
}