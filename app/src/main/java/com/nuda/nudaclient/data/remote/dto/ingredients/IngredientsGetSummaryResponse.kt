package com.nuda.nudaclient.data.remote.dto.ingredients

import com.google.gson.annotations.SerializedName

data class IngredientsGetSummaryResponse(
    val globalRiskCounts: GlobalRiskCounts,
    val ingredientCounts: IngredientCounts,
    val myIngredientCounts: MyIngredientCounts,
    val productId: Int,
    val totalCount: Int
) {
    data class RiskCounts(
        @SerializedName("SAFE") val safe: Int,
        @SerializedName("WARN") val warn: Int,
        @SerializedName("DANGER") val danger: Int,
        @SerializedName("UNKNOWN") val unknown: Int
    )

    data class GlobalRiskCounts(
        @SerializedName("SAFE") val safe: Int,
        @SerializedName("WARN") val warn: Int,
        @SerializedName("DANGER") val danger: Int,
        @SerializedName("UNKNOWN") val unknown: Int
    )

    data class IngredientCounts(
        @SerializedName("TOP_SHEET") val topSheet: TopSheet,
        @SerializedName("ABSORBER") val absorber: Absorber,
        @SerializedName("BACK_SHEET") val backSheet: BackSheet,
        @SerializedName("ADHESIVE") val adhesive: Adhesive,
        @SerializedName("ADDITIVE") val additive: Additive
    ) {
        data class TopSheet(
            val count: Int,
            val riskCounts: RiskCounts
        )

        data class Absorber(
            val count: Int,
            val riskCounts: RiskCounts
        )

        data class BackSheet(
            val count: Int,
            val riskCounts: RiskCounts
        )

        data class Adhesive(
            val count: Int,
            val riskCounts: RiskCounts
        )

        data class Additive(
            val count: Int,
            val riskCounts: RiskCounts
        )
    }

    data class MyIngredientCounts(
        val prefer: Int,
        val avoided: Int
    )
}