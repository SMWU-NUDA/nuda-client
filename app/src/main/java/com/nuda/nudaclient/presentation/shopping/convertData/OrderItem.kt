package com.nuda.nudaclient.presentation.shopping.convertData

data class OrderProduct (
    val brandId: Int,
    val brandName: String,
    val productId: Int,
    val productName: String,
    val thumbnailImg: String?,
    val quantity: Int,
    val price: Int,
    val totalPrice: Int
)