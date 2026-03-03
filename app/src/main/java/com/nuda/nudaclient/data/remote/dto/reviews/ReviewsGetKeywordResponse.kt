package com.nuda.nudaclient.data.remote.dto.reviews

data class ReviewsGetKeywordResponse(
    val negative: List<String>,
    val positive: List<String>
)