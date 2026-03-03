package com.nuda.nudaclient.data.remote.api

import com.nuda.nudaclient.data.remote.dto.common.ApiResponse
import com.nuda.nudaclient.data.remote.dto.products.ProductsCreateWish
import com.nuda.nudaclient.data.remote.dto.products.ProductsGetAllRankingResponse
import com.nuda.nudaclient.data.remote.dto.products.ProductsGetBrandWishlist
import com.nuda.nudaclient.data.remote.dto.products.ProductsGetKeywordRankingResponse
import com.nuda.nudaclient.data.remote.dto.products.ProductsGetProductWishlist
import com.nuda.nudaclient.data.remote.dto.products.ProductsInfoResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductsService {

    // GET /products/{productId} : 상품 상세 조회
    @GET("products/{productId}")
    fun getProductInfo(@Path("productId") productId: Int) : Call<ApiResponse<ProductsInfoResponse>>

    // GET /products/search?product={product} : 상품 검색

    // POST /products/{productId}/likes : 상품 찜하기
    @POST("products/{productId}/likes")
    fun createProductLike(@Path("productId") productId: Int) : Call<ApiResponse<ProductsCreateWish>>

    // GET /products/likes : 찜한 상품 조회
    @GET("products/likes")
    fun getProductWishlist(@Query("cursor") cursor: Int?, @Query("size") size: Int = 20) : Call<ApiResponse<ProductsGetProductWishlist>>

    // POST /brands/{brandId}/likes : 브랜드 찜하기
    @POST("brands/{brandId}/likes")
    fun createBrandLike(@Path("brandId") brandId : Int) : Call<ApiResponse<ProductsCreateWish>>

    // GET /brands/likes : 찜한 브랜드 조회
    @GET("brands/likes")
    fun getBrandWishlist(@Query("cursor") cursor: Int?, @Query("size") size: Int = 20) : Call<ApiResponse<ProductsGetBrandWishlist>>

    // GET /products : 전체 상품 랭킹 조회
    /**
     * 1) sort 파라미터
     * DEFAULT : 기본순
     * REVIEW_COUNT_DESC : 리뷰 많은 순
     * RATING_DESC : 별점 높은 순
     * RATING_ASC : 별점 낮은 순
     * LIKE_COUNT_DESC : 찜 많은 순
     *
     * 2) cursor 파라미터는 "{sortValue}_{id}" 형식입니다. eg. 4.5_123
     */

    @GET("products")
    fun getAllProductRanking(
        @Query("sort") sort: String,
        @Query("cursor") cursor: String?,
        @Query("size") size: Int = 20
    ) : Call<ApiResponse<ProductsGetAllRankingResponse>>

    // GET /products/global-rankings : 키워드별 전체 상품 랭킹 조회
    /**
     * DEFAULT : 전체
     * IRRITATION_LEVEL : 민감도 순
     * SCENT : 향 순
     * ABSORBENCY : 흡수력 순
     * ADHESION : 접착력 순
     */
    @GET("products/global-rankings")
    fun getGlobalProductRanking(
        @Query("keyword") keyword: String,
        @Query("cursor") cursor: Int?,
        @Query("size") size: Int = 20
    ) : Call<ApiResponse<ProductsGetKeywordRankingResponse>>


    // GET /products/personal-rankings : 키워드별 맞춤 상품 랭킹 조회
    /**
     * DEFAULT : 전체
     * IRRITATION_LEVEL : 민감도 순
     * SCENT : 향 순
     * ABSORBENCY : 흡수력 순
     * ADHESION : 접착력 순
     */
    @GET("products/personal-rankings")
    fun getPersonalProductRanking(
        @Query("keyword") keyword: String,
        @Query("cursor") cursor: Int?,
        @Query("size") size: Int = 20
    ) : Call<ApiResponse<ProductsGetKeywordRankingResponse>>


}