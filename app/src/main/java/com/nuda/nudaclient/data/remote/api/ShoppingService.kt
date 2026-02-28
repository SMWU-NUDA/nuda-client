package com.nuda.nudaclient.data.remote.api

import com.nuda.nudaclient.data.remote.dto.common.ApiResponse
import com.nuda.nudaclient.data.remote.dto.common.BaseResponse
import com.nuda.nudaclient.data.remote.dto.shopping.ShoppingCartBaseResponse
import com.nuda.nudaclient.data.remote.dto.shopping.ShoppingChangeQuantityRequest
import com.nuda.nudaclient.data.remote.dto.shopping.ShoppingCreateOrderRequest
import com.nuda.nudaclient.data.remote.dto.shopping.ShoppingCreateOrderResponse
import com.nuda.nudaclient.data.remote.dto.shopping.ShoppingCreatePaymentsResponse
import com.nuda.nudaclient.data.remote.dto.shopping.ShoppingDeleteSelectedCartItemRequest
import com.nuda.nudaclient.data.remote.dto.shopping.ShoppingGetCartItemsResponse
import com.nuda.nudaclient.data.remote.dto.shopping.ShoppingGetOrderHistoryResponse
import com.nuda.nudaclient.data.remote.dto.shopping.ShoppingPaymentCompleteResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ShoppingService {

    /**
     * 장바구니 API
     */
    // POST /carts/items/{productId} : 장바구니에 상품 추가
    @POST("carts/items/{productId}")
    fun addToCart(@Path("productId") productId: Int) : Call<ApiResponse<ShoppingCartBaseResponse>>

    // GET /carts : 장바구니 조회
    @GET("carts")
    fun getCartItems() : Call<ApiResponse<ShoppingGetCartItemsResponse>>

    // PATCH /carts/items/{cartItemId} : 상품 수량 수정
    @PATCH("carts/items/{cartItemId}")
    fun changeQuantity(@Path("cartItemId") cartItemId: Int, @Body request: ShoppingChangeQuantityRequest) : Call<ApiResponse<ShoppingCartBaseResponse>>


    // DELETE /carts/items : 선택 상품 삭제
    // DELETE에 BODY 못 넘기므로 HTTP로 설정 후 Body 가질 수 있도록 설정
    @HTTP(method = "DELETE", path = "carts/items", hasBody = true)
    fun deleteSelectedCartitems(@Body request: ShoppingDeleteSelectedCartItemRequest) : Call<BaseResponse>

    // DELETE /carts/items/all : 전체 상품 삭제
    @DELETE("carts/items/all")
    fun deleteAllCartItems() : Call<BaseResponse>

    // DELETE /carts/items/{cartItemId} : 단건 상품 삭제
    @DELETE("carts/items/{cartItemId}")
    fun deleteCartItem(@Path("cartItemId") cartItemId: Int) : Call<BaseResponse>

    /**
     * 주문 API
     */
    // POST /orders : 주문 등록
    @POST("orders")
    fun createOrder(@Body request: ShoppingCreateOrderRequest) : Call<ApiResponse<ShoppingCreateOrderResponse>>

    // GET /orders : 나의 주문 목록 조회
    @GET("orders")
    fun getOrderHistory(@Query("cursor") cursor: Int, @Query("size") size: Int) : Call<ApiResponse<ShoppingGetOrderHistoryResponse>>

    /**
     * 결제 API
     */
    // POST /payments/orders/{orderId} : 결제(Mock) 요청
    @POST("payments/orders/{orderId}")
    fun createPayment(@Path("orderId") orderId: Int) : Call<ApiResponse<ShoppingCreatePaymentsResponse>>

    // POST /payments/{paymentId}/complete-test : 결제(Mock) 테스트용 완료
    @POST("payments/{paymentId}/complete-test")
    fun CompletePayment(@Path("paymentId") paymentId: Int) : Call<ApiResponse<ShoppingPaymentCompleteResponse>>

}