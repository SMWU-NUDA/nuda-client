package com.nuda.nudaclient.data.remote.dto.ingredients

import com.nuda.nudaclient.data.remote.dto.common.Ingredient

data class IngredientsGetAllResponse(
    val totalCount: Int,
    val ingredients: List<Ingredient>
)