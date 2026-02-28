package com.nuda.nudaclient.presentation.shopping

data class OrderProduct (
    val brandId: Int,
    val brandName: String,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val price: Int,
    val totalPrice: Int
)