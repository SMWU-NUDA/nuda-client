package com.nuda.nudaclient.data.remote.api

import com.nuda.nudaclient.data.remote.dto.common.ApiResponse
import com.nuda.nudaclient.data.remote.dto.common.BaseResponse
import com.nuda.nudaclient.data.remote.dto.reviews.ReviewsCreateReviewRequest
import com.nuda.nudaclient.data.remote.dto.reviews.ReviewsCreateReviewResponse
import com.nuda.nudaclient.data.remote.dto.reviews.ReviewsGetKeywordResponse
import com.nuda.nudaclient.data.remote.dto.reviews.ReviewsGetMyReviewsResponse
import com.nuda.nudaclient.data.remote.dto.reviews.ReviewsGetRankingByKeywordResponse
import com.nuda.nudaclient.data.remote.dto.reviews.ReviewsGetSummaryResponse
import com.nuda.nudaclient.data.remote.dto.reviews.ReviewsLikeReviewResponse
import com.nuda.nudaclient.data.remote.dto.reviews.ReviewsUploadImageRequest
import com.nuda.nudaclient.data.remote.dto.reviews.ReviewsUploadImagesResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ReviewsService {
    // POST /reviews : 리뷰 작성
    @POST("reviews")
    fun createReview(@Body request: ReviewsCreateReviewRequest): Call<ApiResponse<ReviewsCreateReviewResponse>>

    // GET products/{productId}/review-summary : 리뷰 AI 요약 조회
    @GET("products/{productId}/review-summary")
    fun getReviewSummary(
        @Path("productId") productId: Int,
        @Query("topN") topN: Int = 5
    ) : Call<ApiResponse<ReviewsGetSummaryResponse>>

    // GET products/{productId}/review-keywords : 리뷰 긍정/부정 키워드 조회
    @GET("products/{productId}/review-keywords")
    fun getReviewKeywords(
        @Path("productId") productId: Int,
        @Query("topN") topN: Int = 3
    ) : Call<ApiResponse<ReviewsGetKeywordResponse>>

    // GET products/{productId}/reviews : 키워드별 전체 리뷰 랭킹 조회
    /**
     * DEFAULT : 전체
     * IRRITATION_LEVEL : 민감도 순
     * SCENT : 향 순
     * ABSORBENCY : 흡수력 순
     * ADHESION : 접착력 순
     */
    @GET("products/{productId}/reviews")
    fun getReviewRankingByKeyword(
        @Path("productId") productId: Int,
        @Query("keyword") keyword: String,
        @Query("cursor") cursor: Int?,
        @Query("size") size: Int = 20
    ) : Call<ApiResponse<ReviewsGetRankingByKeywordResponse>>

    // DELETE /reviews/{reviewId} : 나의 리뷰 삭제
    @DELETE("reviews/{reviewId}")
    fun deleteMyReview(@Path("reviewId") reviewId: Int) : Call<BaseResponse>

    // POST /reviews/{reviewId}/likes : 리뷰 좋아요
    @POST("reviews/{reviewId}/likes")
    fun likeReview(@Path("reviewId") reviewId: Int) : Call<ApiResponse<ReviewsLikeReviewResponse>>

    // GET /reviews/me : 나의 리뷰 조회
    @GET("reviews/me")
    fun getMyReviews(@Query("cursor") cursor: Int? = null, @Query("size") size: Int = 20)
    : Call<ApiResponse<ReviewsGetMyReviewsResponse>>

    // POST /uploads/presigned-urls : S3 Presigned URL 발급
    @POST("uploads/presigned-urls")
    fun uploadReviewImages(@Body request: ReviewsUploadImageRequest): Call<ApiResponse<List<ReviewsUploadImagesResponse>>>


}
