package com.nuda.nudaclient.data.remote.api

import com.nuda.nudaclient.data.remote.dto.signup.SignupAccountRequest
import com.nuda.nudaclient.data.remote.dto.signup.SignupCommitResponse
import com.nuda.nudaclient.data.remote.dto.signup.SignupDeliveryRequest
import com.nuda.nudaclient.data.remote.dto.signup.SignupDraftResponse
import com.nuda.nudaclient.data.remote.dto.signup.SignupSurveyRequest
import com.nuda.nudaclient.data.remote.dto.common.BaseResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface SignupService {
    // PUT /signup/draft/account : 계정 정보(2단계)
    @PUT("signup/draft/account")
    fun updateAccount(
        @Header("Signup-Token") signupToken : String,
        @Body request : SignupAccountRequest
    ) : Call<BaseResponse>

    // PUT /signup/draft/delivery : 배송지 입력(3단계)
    @PUT("signup/draft/delivery")
    fun updateDelivery(
        @Header("Signup-Token") signupToken : String,
        @Body request : SignupDeliveryRequest
    ) : Call<BaseResponse>

    // PUT /signup/draft/survey : 설문조사(4단계)
    @PUT("signup/draft/survey")
    fun updateSurvey(
        @Header("Signup-Token") signupToken : String,
        @Body request : SignupSurveyRequest
    ) : Call<BaseResponse>

    // GET /signup/draft : 회원가입 임시 저장 조회
    @GET("signup/draft")
    fun getDraft(@Header("Signup-Token") signupToken : String) : Call<SignupDraftResponse>

    // POST /signup/draft : 임시 저장 draft 생성
    @POST("signup/draft")
    fun createDraft() : Call<SignupDraftResponse>

    // POST /signup/commit : 회원가입 완료(5단계)
    @POST("signup/commit")
    fun createSignup(@Header("Signup-Token") signupToken: String): Call<SignupCommitResponse>
}