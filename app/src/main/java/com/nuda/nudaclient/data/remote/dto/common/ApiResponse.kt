package com.nuda.nudaclient.data.remote.dto.common

data class ApiResponse<T>(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: T?
)
