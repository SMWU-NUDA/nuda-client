package com.nuda.nudaclient.data.remote.dto.search

import com.nuda.nudaclient.data.remote.dto.common.Ingredient

data class SearchIngredientResponse(
    val ingredients : List<Ingredient>
)
