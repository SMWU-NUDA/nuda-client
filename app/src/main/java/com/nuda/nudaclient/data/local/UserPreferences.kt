package com.nuda.nudaclient.data.local

import android.content.Context
import androidx.core.content.edit
import com.nuda.nudaclient.data.remote.dto.auth.AuthLoginResponse
import com.nuda.nudaclient.data.remote.dto.common.Me

object UserPreferences {
    // TODO: remove(auth): 회원 정보 삭제? 필요한지 보고 필요 없으면 삭제하기

    private const val PREF_NAME = "user_preferences"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"
    private const val KEY_NICKNAME = "nickname"
    private const val KEY_PROFILE_IMG = "profile_img"
    private const val KEY_EMAIL = "email"

    // 로그인 시 회원 정보 저장
    fun saveUserInfo(context: Context, user: Me) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit {
                putInt(KEY_USER_ID, user.id)
                putString(KEY_USERNAME, user.username)
                putString(KEY_NICKNAME, user.nickname)
                putString(KEY_PROFILE_IMG, user.profileImg)
                putString(KEY_EMAIL, user.email)
            }
    }

    // 사용자 고유번호
    fun getUserId(context: Context): Int {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_USER_ID, -1)
    }

    // 사용자 아이디
    fun getUsername(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_USERNAME, null)
    }

    // 사용자 닉네임
     fun getNickname(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_NICKNAME, null)
    }

    // 사용자 이메일
    fun getEmail(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_EMAIL, null)
    }

    // 사용자 프로필사진 url
    fun getProfileImg(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PROFILE_IMG, null)
    }

    
    // 프로필 수정 시 변경

    // 로그아웃 시 회원 정보 삭제
    fun clearUserInfo(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit {
                clear()
            }
    }

}