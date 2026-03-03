package com.nuda.nudaclient.data.remote.dto.products

data class ProductsGetKeywordRankingResponse(
    val content: List<Content>,
    val nextCursor: Int,
    val hasNext: Boolean
) {
    data class Content(
        val productId: Int,
        val thumbnailImg: String,
        val brandId: Int,
        val brandName: String,
        val productName: String,
        val ingredientLabels: List<String>,
        val averageRating: Int,
        val reviewCount: Int,
        val likeCount: Int,
        val costPrice: Int
    )
}