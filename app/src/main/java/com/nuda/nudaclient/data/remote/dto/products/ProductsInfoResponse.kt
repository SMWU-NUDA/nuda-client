package com.nuda.nudaclient.data.remote.dto.products

data class ProductsInfoResponse(
    val productId: Int,
    val imageUrls: List<String>,
    val brandName: String,
    val likedByMe: Boolean,
    val name: String,
    val averageRating: Double,
    val reviewCount: Int,
    val price: Int,
    val content: String
)