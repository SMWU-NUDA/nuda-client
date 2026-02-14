package com.nuda.nudaclient.data.remote.dto.ingredients

data class IngredientsGetWishlistResponse(
    val content: List<Content>,
    val nextCursor: Int,
    val hasNext: Boolean
) {
    data class Content(
        val likeId: Int,
        val ingredientId: Int,
        val name: String,
        val riskLevel: String,
        val layerType: String
    )
}