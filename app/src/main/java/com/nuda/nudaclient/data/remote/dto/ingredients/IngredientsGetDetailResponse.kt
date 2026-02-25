package com.nuda.nudaclient.data.remote.dto.ingredients

data class IngredientsGetDetailResponse(
    val ingredientId: Int,
    val name: String,
    val riskLevel: String,
    val layerType: String,
    val description: String,
    val preference: Boolean?,
    val caution: String?,
    val hcodes: List<Hcode>
) {
    data class Hcode(
        val code: String,
        val description: String
    )
}