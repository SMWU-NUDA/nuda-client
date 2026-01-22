package com.nuda.nudaclient.data.local

import android.content.Context


object TokenManager {
    private const val PREF_NAME = "app_tokens"
    private const val KEY_SIGNUP_TOKEN = "signup_token"

    // 회원가입 토큰
    fun saveSignupToken(context: Context, token : String) {
        // SharedPreferences 파일을 가져오거나 생성 (MODE_PRIVATE : 해당 앱에서만 접근 가능)
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .edit() // 수정 모드로 전환 (읽기 -> 쓰기)
        .putString(KEY_SIGNUP_TOKEN, token) // 키-값 쌍으로 저장
        .apply()
    }

    fun getSignupToken(context: Context) : String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SIGNUP_TOKEN, null)
    }

    // 모든 토큰 삭제 (로그아웃 시)
    fun clearAllTokens(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}