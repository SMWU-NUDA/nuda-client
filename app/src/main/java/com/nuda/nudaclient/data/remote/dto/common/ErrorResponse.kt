package com.nuda.nudaclient.data.remote.dto.common

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: Any?
)
