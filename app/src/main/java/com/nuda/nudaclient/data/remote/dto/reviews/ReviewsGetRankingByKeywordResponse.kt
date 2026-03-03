package com.nuda.nudaclient.data.remote.dto.reviews

import com.nuda.nudaclient.data.remote.dto.common.Me

data class ReviewsGetRankingByKeywordResponse(
    val content: List<Content>,
    val nextCursor: Int,
    val hasNext: Boolean
) {
    data class Content(
        val reviewId: Int,
        val productId: Int,
        val me: Me,
        val rating: Int,
        val likeCount: Int,
        val likedByMe: Boolean,
        val content: String,
        val imageUrls: List<String>,
        val createdAt: String
    )
}