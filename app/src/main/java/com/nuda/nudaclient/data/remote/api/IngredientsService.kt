package com.nuda.nudaclient.data.remote.api

import com.nuda.nudaclient.data.remote.dto.common.ApiResponse
import com.nuda.nudaclient.data.remote.dto.ingredients.IngredientsCreateLikeResponse
import com.nuda.nudaclient.data.remote.dto.ingredients.IngredientsGetAllResponse
import com.nuda.nudaclient.data.remote.dto.ingredients.IngredientsGetDetailResponse
import com.nuda.nudaclient.data.remote.dto.ingredients.IngredientsGetSummaryResponse
import com.nuda.nudaclient.data.remote.dto.ingredients.IngredientsGetWishlistResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface IngredientsService {

    // GET /ingredients/{ingredientId} : 성분 상세 조회
    @GET("ingredients/{ingredientId}")
    fun getIngredientDetail(@Path("ingredientId") ingredientId: Int) : Call<ApiResponse<IngredientsGetDetailResponse>>

    // GET /ingredients/search?ingredient={ingredient} : 성분 검색
//    @GET("ingredients/search")

    // PUT /ingredients/{ingredientId}/likes : 성분 즐겨찾기
    @POST("ingredients/{ingredientId}/likes")
    fun createIngredientLike(@Path("ingredientId") ingredientId: Int, @Query("preference") preference: Boolean) : Call<ApiResponse<IngredientsCreateLikeResponse>>

    // GET /ingredients/likes : 성분 즐겨찾기 조회
    @GET("/ingredients/likes")
    fun getIngredientWishlist(
        @Query("cursor") cursor: Int?,
        @Query("size") size: Int = 20,
        @Query("preference") preference: Boolean):
            Call<ApiResponse<IngredientsGetWishlistResponse>>

    // GET products/{productId}/ingredient-summary : 상품 성분 구성 요약
    @GET("products/{productId}/ingredient-summary")
    fun getIngredientSummary(@Path("productId") productId: Int) : Call<ApiResponse<IngredientsGetSummaryResponse>>

    // GET products/{productId}/ingredients : 상품 성분 전성분
    @GET("products/{productId}/ingredients")
    fun getAllIngredients(@Path("productId") productId: Int, @Query("filter") filter: String) : Call<ApiResponse<IngredientsGetAllResponse>>

}