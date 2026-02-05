package com.nuda.nudaclient.presentation.mypage

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.presentation.common.activity.BaseActivity

class MypageMyreviewActivity : BaseActivity() {

    // TODO: feat(reviews): (mypage) 내 리뷰 프래그먼트 & 어댑터 생성 및 설정 - common에
    // TODO: feat(reviews): (mypage) 내 리뷰 리사이클러뷰 연결
    // TODO: feat(reviews): (mypage) 내 리뷰 기능 구현 및 API 호출

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mypage_myreview)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // 툴바 타이틀 변경
        setToolbarTitle("내 리뷰")


    }
}