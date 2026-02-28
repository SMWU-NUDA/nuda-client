package com.nuda.nudaclient.data.remote.dto.shopping

data class ShoppingPaymentCompleteResponse(
    val orderNum: Long,
    val deliveryResponse: DeliveryResponse,
    val brands: List<Brand>
) {
    data class DeliveryResponse(
        val recipient: String,
        val phoneNum: String,
        val postalCode: String,
        val address1: String,
        val address2: String
    )
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