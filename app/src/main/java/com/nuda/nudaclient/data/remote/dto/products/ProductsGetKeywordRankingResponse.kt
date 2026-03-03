package com.nuda.nudaclient.data.remote.dto.products

import com.nuda.nudaclient.data.remote.dto.common.Product

data class ProductsGetKeywordRankingResponse(
    val content: List<Product>,
    val nextCursor: Int,
    val hasNext: Boolean
)