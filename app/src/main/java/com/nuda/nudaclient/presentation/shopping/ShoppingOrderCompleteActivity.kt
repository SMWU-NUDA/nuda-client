package com.nuda.nudaclient.presentation.shopping

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.databinding.ActivityShoppingOrderCompleteBinding
import com.nuda.nudaclient.presentation.common.activity.BaseActivity

class ShoppingOrderCompleteActivity : BaseActivity() {

    private lateinit var binding: ActivityShoppingOrderCompleteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityShoppingOrderCompleteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setToolbar()

    }


    // 툴바 설정
    private fun setToolbar() {
        setToolbarTitle("주문 완료") // 타이틀
        setToolbarBackBtn(false) // 뒤로가기 버튼 숨김
        setToolbarShadow(false) // 툴바 그림자 숨김
    }
}