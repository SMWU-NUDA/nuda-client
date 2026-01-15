package com.nuda.nudaclient.data.remote.dto.signup

data class SignupAccountRequest(
    val email: String,
    val nickname: String,
    val password: String,
    val username: String
)