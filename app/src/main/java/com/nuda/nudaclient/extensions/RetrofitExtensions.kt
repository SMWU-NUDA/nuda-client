package com.nuda.nudaclient.extensions

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.nuda.nudaclient.data.remote.dto.common.ErrorResponse
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response

// API 호출 확장 함수

// 제네릭 확장 함수
// <T> : 어떤 타입이든 올 수 있음 (BaseResponse, AuthLoginResponse 등)
fun <T> Call<T>.executeWithHandler(
    context: Context, // Toast를 띄울 때 필요
    onSuccess: (T) -> Unit, // 성공했을 때 실행할 코드 묶음
    onError: ((ErrorResponse?) -> Unit)? = null // 실패했을 때 실행할 코드 묶음
) {
    // this : authService.getNickname() 같은 Call 객체. API 호출의 응답으로 오는 Call 객체
    this.enqueue(object : Callback<T>{
        override fun onResponse(call: Call<T>, response: Response<T>) {
            if(response.isSuccessful) { // HTTP 상태(200~299면 true, 성공)
                response.body()?.let(onSuccess) // body가 null이 아니면 onSuccess 실행. onSuccess(response.body())
            }  else { // 응답 실패 (HTTP 400, 500 등 HTTP 서버 에러 응답)
                // 에러 바디 파싱
                val errorResponse = try {
                    val errorBody = response.errorBody()?.string()
                    val gson = Gson()

                    Log.e("API_ERROR", "에러 코드: ${response.code()}")
                    Log.e("API_ERROR", "에러 메세지: ${response.message()}")
                    Log.e("API_ERROR", "Error Body: $errorBody")

                    gson.fromJson(errorBody, ErrorResponse::class.java)

                } catch (e: Exception) {
                    Log.e("API_ERROR", "에러 파싱 실패", e)
                    null
                }

                onError?.invoke(errorResponse) ?:
                Toast.makeText(context,"서버 오류", Toast.LENGTH_LONG).show()
            }
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            Log.e("API_ERROR", "네트워크 오류 발생", t)
            Log.e("API_ERROR", "에러 메시지 : ${t.message}")
            Log.e("API_ERROR", "에러 타입 : ${t.javaClass.simpleName}")

            Toast.makeText(context,"네트워크 오류", Toast.LENGTH_LONG).show()
        }
    })
}