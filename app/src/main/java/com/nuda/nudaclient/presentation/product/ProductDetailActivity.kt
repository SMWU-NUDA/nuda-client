package com.nuda.nudaclient.presentation.product

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.productsService
import com.nuda.nudaclient.databinding.ActivityProductDetailBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.extensions.toFormattedPrice
import com.nuda.nudaclient.presentation.common.activity.BaseActivity

class ProductDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityProductDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 툴바 설정
        setToolbar()

        // 상품 상세페이지 정보 로드
        loadProductDetail()

    }

    // 툴바 설정
    private fun setToolbar() {
        setToolbarTitle("") // 타이틀
        setBackButton() // 뒤로가기 버튼
        binding.toolBar.toolbarShadow.visibility = View.GONE // 그림자 뷰 숨기기

        setToolbarButtons() // 툴바 버튼들 설정
    }

    // 상품 상세페이지 정보 로드
    private fun loadProductDetail() {
        loadProductInfo()
    }

    // 상품 정보 로드
    private fun loadProductInfo() {
        productsService.getProductInfo(1) // 묵업 데이터 입력 (productId = 1)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) { // data non-null로 보장
                        body.data?.let { data ->
                            // 뷰에 데이터 바인딩
                            binding.tvBrand.text = data.brandName
                            binding.tvProductName.text = data.name
                            binding.tvRatingScore.text = data.averageRating.toString()
                            binding.tvReview.text = "리뷰 ${data.reviewCount}개"
                            binding.tvPrice.text = data.price.toFormattedPrice()


                            // 리뷰 개수 텍스트에 밑줄 플래그 추가
                            binding.tvReview.paintFlags = binding.tvReview.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                        }
                    }
                }
            )
    }


}