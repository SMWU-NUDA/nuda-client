package com.nuda.nudaclient.data.remote.dto.common

data class Product (
    val productId: Int,
    val thumbnailImg: String,
    val brandId: Int,
    val brandName: String,
    val productName: String,
    val ingredientLabels: List<String>,
    val averageRating: Int,
    val reviewCount: Int,
    val likeCount: Int,
    val costPrice: Int
)
