package com.nuda.nudaclient.data.remote.dto.search

import com.nuda.nudaclient.data.remote.dto.common.Product

data class SearchProductResponse(
    val content: List<Product>,
    val nextCursor: Int,
    val hasNext: Boolean
)