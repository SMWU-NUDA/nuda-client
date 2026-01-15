package com.nuda.nudaclient.data.remote.dto.signup

data class SignupCommitResponse(
    val code: String,
    val `data`: Data,
    val message: String,
    val success: Boolean
) {
    data class Data(
        val currentStep: String,
        val requiredStep: String
    )
}