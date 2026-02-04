package com.nuda.nudaclient.data.remote.dto.auth

data class AuthVerifyEmailResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val `data`: Boolean
)