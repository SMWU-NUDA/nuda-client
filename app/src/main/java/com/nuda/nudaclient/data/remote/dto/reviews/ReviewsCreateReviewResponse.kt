package com.nuda.nudaclient.data.remote.dto.reviews

import com.nuda.nudaclient.data.remote.dto.common.Me

data class ReviewsCreateReviewResponse(
    val reviewId: Int,
    val productId: Int,
    val me: Me,
    val rating: Double,
    val likeCount: Int,
    val likedByMe: Boolean,
    val content: String,
    val imageUrls: List<String>,
    val createdAt: String
    )