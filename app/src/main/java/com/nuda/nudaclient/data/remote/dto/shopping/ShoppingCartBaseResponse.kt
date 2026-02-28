package com.nuda.nudaclient.data.remote.dto.shopping

data class ShoppingCartBaseResponse(
    val productId: Int,
    val quantity: Int,
    val costPrice: Int,
    val totalPrice: Int
)