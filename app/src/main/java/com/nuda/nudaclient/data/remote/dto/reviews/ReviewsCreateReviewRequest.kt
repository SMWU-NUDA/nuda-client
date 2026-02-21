package com.nuda.nudaclient.data.remote.dto.reviews

data class ReviewsCreateReviewRequest(
    val productId: Int,
    val rating: Double,
    val content: String,
    val imageUrls: List<String>,
)