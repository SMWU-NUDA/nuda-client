package com.nuda.nudaclient.data.remote.dto.signup

data class SignupSurveyRequest(
    val changeFrequency: String,
    val irritationLevel: String,
    val priority: String,
    val productIds: List<Int>,
    val scent: String,
    val thickness: String
)