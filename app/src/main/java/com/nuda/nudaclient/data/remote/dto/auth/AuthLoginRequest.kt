package com.nuda.nudaclient.data.remote.dto.auth

data class AuthLoginRequest(
    val password: String,
    val username: String
)