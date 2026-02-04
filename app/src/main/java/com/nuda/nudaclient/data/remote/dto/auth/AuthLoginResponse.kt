package com.nuda.nudaclient.data.remote.dto.auth

import com.nuda.nudaclient.data.remote.dto.common.Me

data class AuthLoginResponse(
    val accessToken: String,
    val meResponse: Me,
    val refreshToken: String
)
