package com.nuda.nudaclient.data.remote.dto.reviews

data class ReviewsGetSummaryResponse(
    val keywords: Keywords,
    val satisfactionRate: Int,
    val trendHighlights: List<String>
) {
    data class Keywords(
        val positive: List<String>,
        val negative: List<String>
    )
}