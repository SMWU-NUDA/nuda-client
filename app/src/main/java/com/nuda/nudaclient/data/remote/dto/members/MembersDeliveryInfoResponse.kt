package com.nuda.nudaclient.data.remote.dto.members

data class MembersDeliveryInfoResponse(
    val recipient: String,
    val phoneNum: String,
    val postalCode: String,
    val address1: String,
    val address2: String
)
