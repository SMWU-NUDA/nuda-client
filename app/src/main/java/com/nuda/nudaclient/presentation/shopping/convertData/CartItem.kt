package com.nuda.nudaclient.presentation.shopping.convertData

sealed class CartItem {
    // 브랜드 헤더 데이터
    data class BrandHeader(
        val brandId: Int,
        val brandName: String,
        var isChecked: Boolean = false
    ) : CartItem()

    // 상품 아이템 데이터
    data class Product(
        val cartItemId: Int,
        val brandId: Int,
        val brandName: String, // 브랜드 이름 추가
        val productId: Int,
        val productName: String,
        var quantity: Int,
        val price: Int,
        var totalPrice: Int,
        var isChecked: Boolean = false
    ) : CartItem()
}