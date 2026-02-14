package com.nuda.nudaclient.data.remote.dto.products

data class ProductsGetBrandWishlist(
    val content: List<Content>,
    val nextCursor: Int,
    val hasNext: Boolean
) {
    data class Content(
        val likeId: Int,
        val brandId: Int,
        val logoImg: String,
        val name: String
    )
}