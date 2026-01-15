package com.nuda.nudaclient.data.remote.dto.common

data class BaseResponse(
    val code: String,
    val `data`: String,
    val message: String,
    val success: Boolean
)