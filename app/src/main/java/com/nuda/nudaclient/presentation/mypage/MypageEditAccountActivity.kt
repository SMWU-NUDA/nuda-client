package com.nuda.nudaclient.presentation.mypage

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.presentation.common.activity.BaseActivity

class MypageEditAccountActivity : BaseActivity() {

    // TODO: feat(members): (mypage) 프로필 수정 기능 틀 구성 (회원가입 참고, API 호출 + 데이터 바인딩)
    // TODO: feat(members): (mypage) 프로필 수정 - 비밀번호 검증 기능 구현 및 API 호출
    // TODO: feat(members): (mypage) 프로필 수정 구현 및 테스트 완료 (프로필 수정 시 마이페이지 저장)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mypage_edit_account)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 툴바 타이틀 변경
        setToolbarTitle("프로필 수정")
    }
}