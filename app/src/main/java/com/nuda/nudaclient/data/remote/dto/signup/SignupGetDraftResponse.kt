package com.nuda.nudaclient.data.remote.dto.signup

data class SignupGetDraftResponse(
    val accountInfo: AccountInfo,
    val deliveryInfo: DeliveryInfo,
    val surveyInfo: SurveyInfo,
    val expiresAt: String,
    val currentStep: String
) {
    data class AccountInfo(
        val email: String,
        val nickname: String,
        val username: String
    )
    data class DeliveryInfo(
        val address1: String,
        val address2: String,
        val phoneNum: String,
        val postalCode: String,
        val recipient: String
    )
    data class SurveyInfo(
        val changeFrequency: String,
        val irritationLevel: String,
        val adhesion: String,
        val productIds: List<Int>,
        val scent: String,
        val thickness: String
    )
}