package com.nuda.nudaclient.data.remote.dto.auth

data class AuthVerifyEmailResponse(
    val code: String,
    val `data`: Boolean,
    val message: String,
    val success: Boolean
)