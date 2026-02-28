package com.nuda.nudaclient.presentation.shopping.convertData

sealed class OrderHistoryItem {
    // 날짜 헤더 데이터
    data class DateHeader(
        val orderDate: String,
        val orderNum: Long
    ) : OrderHistoryItem()

    // 상품 아이템 데이터
    data class Product(
        val brandId: Int,
        val brandName: String,
        val productId: Int,
        val thumbnailImg: String?,
        val productName: String,
        val quantity: Int,
        val price: Int,
        val totalPrice: Int,
    ) : OrderHistoryItem()

    // 총 금액 푸터 데이터
    data class PriceFooter(
        val totalAmount: Int
    ) : OrderHistoryItem()
}