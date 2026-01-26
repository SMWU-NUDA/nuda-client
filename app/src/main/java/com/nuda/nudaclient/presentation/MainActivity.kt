package com.nuda.nudaclient.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.local.TokenManager
import com.nuda.nudaclient.data.remote.RetrofitClient.authService
import com.nuda.nudaclient.presentation.login.LoginActivity

class MainActivity : AppCompatActivity() {

    // access 토큰
    val accessToken = TokenManager.getAccessToken(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 앱 진입 시 access 토큰 확인
        checkAccessToken()


    }

    private fun checkAccessToken() {
        if (accessToken == null) { // access 토큰이 없으면 로그인 화면으로 이동
            val intent = Intent(this, LoginActivity::class.java)
        } else { // access 토큰이 있으면 만료 여부 확인

        }
    }

    private fun validateAccessToken() {
        authService.validateAccessToken()
    }

}