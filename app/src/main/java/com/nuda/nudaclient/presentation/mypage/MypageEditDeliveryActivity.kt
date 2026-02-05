package com.nuda.nudaclient.presentation.mypage

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.presentation.common.activity.BaseActivity

class MypageEditDeliveryActivity : BaseActivity() {

    // TODO: feat(members): (mypage) 배송정보 관리 기능 구현 (회원가입 참고, API 호출 + 데이터 바인딩)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mypage_edit_delivery)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // 툴바 타이틀 변경
        setToolbarTitle("배송정보 관리")


    }
}