package com.nuda.nudaclient.data.remote.dto.reviews

data class ReviewsGetMyReviewsResponse(
    val content: List<Content>,
    val hasNext: Boolean,
    val nextCursor: Int?
) {
    data class Content(
        val productId: Int,
        val productThumbnail: String,
        val productName: String,
        val brandName: String,
        val reviewId: Int,
        val rating: Double,
        val content: String,
        val createdAt: String
    )
}
