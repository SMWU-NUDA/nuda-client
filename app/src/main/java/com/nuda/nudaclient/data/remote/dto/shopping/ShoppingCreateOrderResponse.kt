package com.nuda.nudaclient.data.remote.dto.shopping

data class ShoppingCreateOrderResponse(
    val orderId: Int,
    val orderNum: Int,
    val status: String,
    val totalAmount: Int,
    val brands: List<Brand>
) {
    data class Brand(
        val brandId: Int,
        val brandName: String,
        val products: List<Product>
    ) {
        data class Product(
            val productId: Int,
            val productName: String,
            val quantity: Int,
            val price: Int,
            val totalPrice: Int
        )
    }
}