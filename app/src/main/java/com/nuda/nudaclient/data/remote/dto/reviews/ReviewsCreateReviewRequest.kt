package com.nuda.nudaclient.data.remote.dto.reviews

data class ReviewsCreateReviewRequest(
    val productId: Int,
    val rating: Int,
    val content: String,
    val imageUrls: List<String>,
)