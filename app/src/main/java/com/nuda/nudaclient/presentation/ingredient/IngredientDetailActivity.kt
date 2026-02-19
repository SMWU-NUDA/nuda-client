package com.nuda.nudaclient.presentation.ingredient

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.databinding.ActivityIngredientDetailBinding
import com.nuda.nudaclient.presentation.common.activity.BaseActivity

class IngredientDetailActivity : BaseActivity() {

    // TODO 버튼 클릭 시 폰트 변경 적용

    private lateinit var binding: ActivityIngredientDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityIngredientDetailBinding.inflate(layoutInflater)
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
        setToolbarTitle("성분 정보") // 타이틀
        setBackButton() // 뒤로가기 버튼
        setToolbarShadow(false) // 그림자 숨김
    }

}