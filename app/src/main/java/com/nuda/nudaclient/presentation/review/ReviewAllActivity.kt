package com.nuda.nudaclient.presentation.review

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.databinding.ActivityReviewAllBinding
import com.nuda.nudaclient.presentation.common.activity.BaseActivity

class ReviewAllActivity : BaseActivity() {

    private lateinit var binding: ActivityReviewAllBinding
    private var productId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityReviewAllBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Intent에 담긴 productId 저장
        productId = intent.getIntExtra("PRODUCT_ID", -1)

        setToolbar()

        setCreateReviewBtn()

    }

    // 툴바 설정
    private fun setToolbar() {
        setToolbarTitle("전체 리뷰") // 타이틀
        setBackButton() // 뒤로가기 버튼
        setToolbarShadow(false) // 그림자 숨김
    }

    // 리뷰 쓰기 버튼 클릭 설정
    private fun setCreateReviewBtn() {
        binding.btnWriteReview.setOnClickListener {
            val intent = Intent(this, ReviewCreateActivity::class.java)
            intent.putExtra("STATE", "product")
            intent.putExtra("PRODUCT_ID", productId)
            startActivity(intent)
        }
    }
}