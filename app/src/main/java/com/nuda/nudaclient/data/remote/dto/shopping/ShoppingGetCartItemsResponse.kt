package com.nuda.nudaclient.data.remote.dto.shopping

data class ShoppingGetCartItemsResponse(
    val brands: List<Brand>,
    val totalQuantity: Int,
    val totalPrice: Int
) {
    data class Brand(
        val brandId: Int,
        val brandName: String,
        val products: List<Product>
    ) {
        data class Product(
            val cartItemId: Int,
            val productId: Int,
            val thumbnailImg: String,
            val productName: String,
            val quantity: Int,
            val price: Int,
            val totalPrice: Int
        )
    }
}