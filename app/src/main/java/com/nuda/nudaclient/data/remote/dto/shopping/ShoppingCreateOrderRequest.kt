package com.nuda.nudaclient.data.remote.dto.shopping

data class ShoppingCreateOrderRequest(
    val items: List<Item>
) {
    data class Item(
        val productId: Int,
        val quantity: Int
    )
}