package com.nuda.nudaclient.data.remote.dto.shopping

data class ShoppingGetOrderHistoryResponse(
    val content: List<Content>,
    val nextCursor: Int,
    val hasNext: Boolean
) {
    data class Content(
        val orderId: Int,
        val orderDate: String,
        val orderNum: Long,
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
                val thumbnailImg: String?,
                val productName: String,
                val quantity: Int,
                val price: Int,
                val totalPrice: Int
            )
        }
    }
}