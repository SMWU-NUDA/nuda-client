package com.nuda.nudaclient.data.remote.api

import com.nuda.nudaclient.data.remote.dto.auth.AuthEmailVerificationRequest
import com.nuda.nudaclient.data.remote.dto.auth.AuthLoginRequest
import com.nuda.nudaclient.data.remote.dto.auth.AuthLoginResponse
import com.nuda.nudaclient.data.remote.dto.auth.AuthReissueRequest
import com.nuda.nudaclient.data.remote.dto.auth.AuthReissueResponse
import com.nuda.nudaclient.data.remote.dto.auth.AuthVerifyEmailRequest
import com.nuda.nudaclient.data.remote.dto.common.BaseResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthService {

    // POST /auth/reissue : 토큰 재발급
    @POST("auth/reissue")
    fun reissue(@Body request : AuthReissueRequest) : Call<AuthReissueResponse>

    // POST /auth/logout : 로그아웃
    @POST("auth/logout")
    fun logout() : Call<BaseResponse>

    // POST /auth/login : 로그인
    @POST("auth/login")
    fun login(@Body request : AuthLoginRequest) : Call<AuthLoginResponse>

    // POST /auth/emails/verifications : 이메일 인증번호 검증
    @POST("auth/emails/verifications")
    fun verifyEmail(@Body request: AuthVerifyEmailRequest) : Call<BaseResponse>

    // POST /auth/emails/verification-requests : 이메일 인증번호 요청
    @POST("auth/emails/verification-requests")
    fun requestEmailVerification(@Body request : AuthEmailVerificationRequest) : Call<BaseResponse>

    // GET /auth/search/username : 아이디 중복 검사
    @GET("auth/search/username")
    fun getUsername(@Header("username") username : String) : Call<BaseResponse>

    // GET /auth/search/nickname : 닉네임 중복 검사
    @GET("auth/search/nickname")
    fun getNickname(@Header("nickname") nickname : String) : Call<BaseResponse>
}