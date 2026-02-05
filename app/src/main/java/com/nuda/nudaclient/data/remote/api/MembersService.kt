package com.nuda.nudaclient.data.remote.api

import com.nuda.nudaclient.data.remote.dto.common.ApiResponse
import com.nuda.nudaclient.data.remote.dto.common.Me
import com.nuda.nudaclient.data.remote.dto.members.MembersDeliveryInfoResponse
import com.nuda.nudaclient.data.remote.dto.members.MembersUserInfoResponse
import com.nuda.nudaclient.data.remote.dto.signup.SignupDeliveryRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT

interface MembersService {

    // GET /members/me : 마이페이지 조회
    @GET("members/me")
    fun getUserInfo() : Call<ApiResponse<MembersUserInfoResponse>>

    // PATCH /members/me : 프로필 수정
    @PATCH("members/me")
    fun updateProfile(@Body request: Map<String, String>) : Call<ApiResponse<Me>>

    // GET /members/me/delivery : 배송정보 조회
    @GET("members/me/delivery")
    fun getDeliveryInfo() : Call<ApiResponse<MembersDeliveryInfoResponse>>

    // PUT /members/me/delivery : 배송정보 수정
    @PUT("members/me/delivery")
    fun updateDeliveryInfo(@Body request: SignupDeliveryRequest) : Call<ApiResponse<MembersDeliveryInfoResponse>>

    // GET /members/me/keywords : 키워드 조회

    // PATCH /members/me/keywords : 키워드 수정
}