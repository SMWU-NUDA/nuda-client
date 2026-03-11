package com.nuda.nudaclient.data.remote.dto.reviews

import com.nuda.nudaclient.data.remote.dto.common.Me

data class ReviewsGetRankingByKeywordResponse(
    val content: List<Review>,
    val nextCursor: Int,
    val hasNext: Boolean
) {
    data class Review(
        val reviewId: Int,
        val productId: Int,
        val me: Me,
        val rating: Double,
        var likeCount: Int,
        var likedByMe: Boolean,
        val content: String,
        val imageUrls: List<String>,
        val createdAt: String
    )
}