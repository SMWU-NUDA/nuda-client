package com.nuda.nudaclient.data.remote.dto.products

import com.nuda.nudaclient.data.remote.dto.common.Product

data class ProductsGetAllRankingResponse(
    val content: List<Product>,
    val nextCursor: NextCursor,
    val hasNext: Boolean
) {
    data class NextCursor(
        val sortValue: Double,
        val id: Int
    )
}