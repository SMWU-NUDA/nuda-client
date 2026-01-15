package com.nuda.nudaclient.data.remote.dto.auth

data class AuthReissueResponse(
    val code: String,
    val `data`: Data,
    val message: String,
    val success: Boolean
) {
    data class Data(
        val accessToken: String,
        val refreshToken: String
    )
}