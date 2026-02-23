package com.nuda.nudaclient.data.remote.dto.signup

data class SignupSurveyRequest(
    val irritationLevel: String,
    val scent: String,
    val adhesion: String,
    val changeFrequency: String,
    val thickness: String,
    val productIds: List<Int>
)