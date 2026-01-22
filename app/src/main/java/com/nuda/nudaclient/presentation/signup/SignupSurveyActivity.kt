package com.nuda.nudaclient.presentation.signup

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.databinding.ActivitySignupSurveyBinding
import com.nuda.nudaclient.presentation.login.LoginActivity

class SignupSurveyActivity : AppCompatActivity() {

    // 뷰 바인딩 객체 선언
    lateinit var binding : ActivitySignupSurveyBinding


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 뷰 바인딩 설정
        binding = ActivitySignupSurveyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


    }

    // 버튼 클릭 설정
    private fun setupButtons() {
        // 이전 버튼 클릭 이벤트 처리
        binding.btnPrevPage.setOnClickListener {
            // 회원가입 2단계 페이지로 이동 (SignupDeliveryActivity)
            val intent = Intent(this, SignupDeliveryActivity::class.java)
            startActivity(intent)
        }

        // 회원가입 버튼 클릭 이벤트 처리
        binding.btnRegister.setOnClickListener {


            // 로그인 페이지로 이동 (LoginActivity)
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}