package com.nuda.nudaclient.data.remote.api

import com.nuda.nudaclient.data.remote.dto.common.ApiResponse
import com.nuda.nudaclient.data.remote.dto.products.ProductsCreateWish
import com.nuda.nudaclient.data.remote.dto.products.ProductsGetBrandWishlist
import com.nuda.nudaclient.data.remote.dto.products.ProductsGetProductWishlist
import com.nuda.nudaclient.data.remote.dto.products.ProductsInfoResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductsService {

    // POST /admin/products : 상품 등록
//    @POST("admin/products")
//    fun createProduct()

    // GET /products/{productId} : 상품 상세 조회
    @GET("products/{productId}")
    fun getProductInfo(@Path("productId") productId: Int) : Call<ApiResponse<ProductsInfoResponse>>

    // GET /products/search?product={product} : 상품 검색

    // POST /products/{productId}/likes : 상품 찜하기
    @POST("products/{productId}/likes")
    fun createProductLike(@Path("productId") productId: Int) : Call<ApiResponse<ProductsCreateWish>>

    // GET /products/likes : 찜한 상품 조회
    @GET("products/likes")
    fun getProductWishlist(@Query("cursor") cursor: Int, @Query("size") size: Int) : Call<ApiResponse<ProductsGetProductWishlist>>

    // POST /brands/{brandId}/likes : 브랜드 찜하기
    @POST("brands/{brandId}/likes")
    fun createBrandLike(@Path("brandId") brandId : Int) : Call<ApiResponse<ProductsCreateWish>>

    // GET /brands/likes : 찜한 브랜드 조회
    @GET("brands/likes")
    fun getBrandWishlist(@Query("cursor") cursor: Int, @Query("size") size: Int) : Call<ApiResponse<ProductsGetBrandWishlist>>

    // GET /products/rankings?sort={sort} : 상품 랭킹 조회

    // GET /products/recommendations?type={type} : 맞춤 상품 추천 조회


}