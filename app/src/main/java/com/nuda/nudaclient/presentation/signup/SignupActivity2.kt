package com.nuda.nudaclient.presentation.signup

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.databinding.ActivitySignup2Binding

class SignupActivity2 : AppCompatActivity() {

    // 뷰 바인딩 객체 선언
    lateinit var binding : ActivitySignup2Binding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 뷰 바인딩 설정
        binding = ActivitySignup2Binding.inflate(layoutInflater)
        setContentView(binding.root)




        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 버튼 클릭 이벤트
        setupButtons()


    }

    // 버튼 클릭 설정
    private fun setupButtons() {
        // 이전 버튼 클릭 이벤트 처리
        binding.btnPrevPage.setOnClickListener {
            // 회원가입 1단계 페이지로 이동 (SignupAccountActivity)
            val intent = Intent(this, SignupAccountActivity::class.java)
            startActivity(intent)
        }

        // 다음 버튼 클릭 이벤트 처리
        binding.btnNextPage.setOnClickListener {
            // 회원가입 3단계 페이지로 이동 (SignupActivity3)
            val intent = Intent(this, SignupActivity3::class.java)
            startActivity(intent)
        }
    }
}