package com.nuda.nudaclient.data.remote.dto.search

data class SearchIngredientResponse(
    val ingredients : List<Ingredient>
) {
    data class Ingredient(
        val ingredientId : Int,
        val name : String,
        val riskLevel : String,
        val layerType : String
    )
}
