package com.nuda.nudaclient.presentation.common.activity

import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.nuda.nudaclient.R

open class BaseActivity : AppCompatActivity() {

    // TODO: feat: 로딩 화면 추가 (BaseActivity)
    // TODO: refactor: 툴바 뒤로가기 공통 로직으로 수정(BaseActivity)

    // 툴바 타이틀 설정 메소드
    fun setToolbarTitle(title: String) {
        findViewById<TextView>(R.id.tv_toolbar_title).text = title
    }

    // 툴바 뒤로가기 버튼 설정 메소드
    fun setBackButton() {
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            // 기본 동작: 현재 액티비티 종료 및 뒤로가기
            onBackButtonClicked()
        }
    }

    // 뒤로가기 버튼 클릭 시 이벤트 (오버라이딩 가능)
    protected open fun onBackButtonClicked() {
        finish() // 기본 동작
    }
}
