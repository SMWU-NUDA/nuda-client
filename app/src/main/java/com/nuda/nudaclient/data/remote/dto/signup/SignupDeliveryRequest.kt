package com.nuda.nudaclient.data.remote.dto.signup

data class SignupDeliveryRequest(
    val address1: String,
    val address2: String,
    val phoneNum: String,
    val postalCode: String,
    val recipient: String
)