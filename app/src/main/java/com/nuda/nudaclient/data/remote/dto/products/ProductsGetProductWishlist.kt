package com.nuda.nudaclient.data.remote.dto.products

data class ProductsGetProductWishlist(
    val content: List<Content>,
    val nextCursor: Int,
    val hasNext: Boolean
) {
    data class Content(
        val likeId: Int,
        val productId: Int,
        val thumbnailImg: String,
        val brandName: String,
        val productName: String,
        val averageRating: Int,
        val reviewCount: Int
    )
}