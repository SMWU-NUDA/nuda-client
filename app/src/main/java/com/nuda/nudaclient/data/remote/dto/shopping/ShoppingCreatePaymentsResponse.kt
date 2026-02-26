package com.nuda.nudaclient.data.remote.dto.shopping

data class ShoppingCreatePaymentsResponse(
    val paymentId: Int,
    val orderId: Int,
    val orderNum: Int,
    val amount: Int,
    val status: String,
    val paymentKey: String,
    val redirectUrl: String
)