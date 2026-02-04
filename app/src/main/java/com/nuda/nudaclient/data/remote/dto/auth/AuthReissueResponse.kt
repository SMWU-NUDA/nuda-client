package com.nuda.nudaclient.data.remote.dto.auth

data class AuthReissueResponse(
    val accessToken: String,
    val refreshToken: String
)