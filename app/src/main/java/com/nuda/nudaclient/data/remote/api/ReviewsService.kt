package com.nuda.nudaclient.data.remote.api

import com.nuda.nudaclient.data.remote.dto.common.ApiResponse
import com.nuda.nudaclient.data.remote.dto.common.BaseResponse
import com.nuda.nudaclient.data.remote.dto.reviews.ReviewsCreateReviewRequest
import com.nuda.nudaclient.data.remote.dto.reviews.ReviewsCreateReviewResponse
import com.nuda.nudaclient.data.remote.dto.reviews.ReviewsGetMyReviewsResponse
import com.nuda.nudaclient.data.remote.dto.reviews.ReviewsLikeReviewResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ReviewsService {

    // POST /admin/reviews : 리뷰 등록

    // POST /reviews : 리뷰 작성
    @POST("reviews")
    fun createReview(@Body request: ReviewsCreateReviewRequest): Call<ApiResponse<ReviewsCreateReviewResponse>>

    // GET products/{productId}/reviews : 리뷰 전체 조회

    // GET products/{productId}/review-summary : 리뷰 요약 조회

    // DELETE /reviews/{reviewId} : 나의 리뷰 삭제
    @DELETE("reviews/{reviewId}")
    fun deleteMyReview(@Query("reviewId") reviewId: Int) : Call<BaseResponse>

    // POST /reviews/{reviewId}/likes : 리뷰 좋아요
    @POST("reviews/{reviewId}/likes")
    fun likeReview(@Query("reviewId") reviewId: Int) : Call<ApiResponse<ReviewsLikeReviewResponse>>

    // GET /reviews/me : 나의 리뷰 조회
    @GET("reviews/me")
    fun getMyReviews(@Query("cursor") cursor: Int? = null, @Query("size") size: Int = 20)
    : Call<ApiResponse<ReviewsGetMyReviewsResponse>>
}