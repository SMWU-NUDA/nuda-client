package com.nuda.nudaclient.data.remote.dto.reviews

data class ReviewsGetMyReviewsResponse(
    val content: List<Content>,
    val hasNext: Boolean,
    val nextCursor: Int
) {
    data class Content(
        val brandName: String,
        val content: String,
        val createdAt: String,
        val productId: Int,
        val productName: String,
        val productThumbnail: String,
        val rating: Int,
        val reviewId: Int
    )
}
