package com.nuda.nudaclient.data.remote.dto.auth

data class AuthVerifyEmailRequest(
    val code: String,
    val email: String
)