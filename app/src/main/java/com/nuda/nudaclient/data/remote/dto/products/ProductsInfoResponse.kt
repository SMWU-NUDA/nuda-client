package com.nuda.nudaclient.data.remote.dto.products

data class ProductsInfoResponse(
    val productId: Int,
    val imageUrls: List<String>,
    val brandId: Int,
    val brandName: String,
    val brandLikedByMe: Boolean,
    val name: String,
    val averageRating: Double,
    val reviewCount: Int,
    val productLikedByMe: Boolean,
    val price: Int,
    val content: String
)