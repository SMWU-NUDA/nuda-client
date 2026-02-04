package com.nuda.nudaclient.data.local

import android.content.Context
import androidx.core.content.edit


object TokenManager {
    private const val PREF_NAME = "app_tokens"
    private const val KEY_SIGNUP_TOKEN = "signup_token"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"


    // 회원가입 토큰 저장
    fun saveSignupToken(context: Context, token : String?) {
        // SharedPreferences 파일을 가져오거나 생성 (MODE_PRIVATE : 해당 앱에서만 접근 가능)
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit { // 수정 모드로 전환 (읽기 -> 쓰기)
                putString(KEY_SIGNUP_TOKEN, token) // 키-값 쌍으로 저장
            }
    }

    // 회원가입 토큰 받아오기
    fun getSignupToken(context: Context) : String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SIGNUP_TOKEN, null)
    }

    // 회원가입 토큰 만료 시 삭제
    fun clearSignupToken(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit { remove(KEY_SIGNUP_TOKEN) }
    }

    // 로그인 시 access 토큰 및 refresh 토큰 저장
    fun saveTokens(context: Context, accessToken: String?, refreshToken: String?) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit {
                putString(KEY_ACCESS_TOKEN, accessToken)
                putString(KEY_REFRESH_TOKEN, refreshToken)
            }
    }

    // access 토큰 받아오기
    fun getAccessToken(context: Context) : String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ACCESS_TOKEN, null)
    }

    // access 토큰 만료 시 삭제
    fun clearAccessToken(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit { remove(KEY_ACCESS_TOKEN) }
    }

    // refresh 토큰 받아오기
    fun getRefreshToken(context: Context) : String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_REFRESH_TOKEN, null)
    }

    // 모든 토큰 삭제 (로그아웃 시)
    fun clearAllTokens(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit {
                clear()
            }
    }

}