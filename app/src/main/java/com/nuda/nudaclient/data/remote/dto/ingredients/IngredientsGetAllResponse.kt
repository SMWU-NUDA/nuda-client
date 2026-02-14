package com.nuda.nudaclient.data.remote.dto.ingredients

data class IngredientsGetAllResponse(
    val totalCount: Int,
    val ingredients: List<Ingredient>
) {
    data class Ingredient(
        val ingredientId: Int,
        val name: String,
        val riskLevel: String,
        val layerType: String
    )
}