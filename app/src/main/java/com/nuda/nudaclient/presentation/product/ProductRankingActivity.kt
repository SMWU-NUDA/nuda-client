package com.nuda.nudaclient.presentation.product

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.databinding.ActivityProductRankingBinding
import com.nuda.nudaclient.presentation.common.activity.BaseActivity

class ProductRankingActivity : BaseActivity() {

    // TODO feat(products): 필터링 버튼 클릭 후 BottomSheetDialog 띄우기
    // TODO feat(products): 필터링 항목 클릭 시 폰트 변경 및 체크 아이콘 추가

    private lateinit var binding: ActivityProductRankingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityProductRankingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 툴바 설정
        setToolbar()


    }

    // 툴바 설정
    private fun setToolbar() {
        setToolbarTitle("제품 랭킹") // 타이틀
        setBackButton() // 뒤로가기 버튼
        setToolbarButtons() // 툴바 버튼 설정
    }
}