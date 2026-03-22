package com.nuda.nudaclient.data.remote.api

import com.nuda.nudaclient.data.remote.dto.common.ApiResponse
import com.nuda.nudaclient.data.remote.dto.common.Ingredient
import com.nuda.nudaclient.data.remote.dto.common.Product
import com.nuda.nudaclient.data.remote.dto.search.SearchProductResponse
import com.nuda.nudaclient.data.remote.dto.search.SearchIngredientResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchService {

    // GET /products/search : 상품 검색
    @GET("products/search")
    fun searchProduct(
        @Query("keyword") keyword: String,
        @Query("cursor") cursor: Int?,
        @Query("size") size: Int = 20
    ) : Call<ApiResponse<SearchProductResponse>>

    // GET products/search/popular : 상품 인기 검색어 조회
    @GET("products/search/popular")
    fun getProductTOP10() : Call<ApiResponse<List<String>>>

    // GET /ingredients/search : 성분 검색
    @GET("ingredients/search")
    fun searchIngredient(@Query("keyword") keyword: String) : Call<ApiResponse<List<Ingredient>>>

    // GET /ingredients/search/popular : 성분 인기 검색어 조회
    @GET("ingredients/search/popular")
    fun getIngredientTOP10() : Call<ApiResponse<List<String>>>

    // GET /search/suggest : 검색어 자동 완성
    /**
     * type 값
     * PRODUCT: 상품 검색 시 요청
     * INGREDIENT: 성분 검색 시 요청
     */
    @GET("search/suggest")
    fun searchAutoComplete(
        @Query("keyword") keyword: String,
        @Query("type") type: String = "INGREDIENT")
    : Call<ApiResponse<List<String>>>

    // GET /products/search/name : 상품 이름 검색 (회원가입 설문 단계)
    @GET("products/search/name")
    fun searchProductSignup(@Query("keyword") keyword: String) : Call<ApiResponse<List<Product>>>
}