package com.nuda.nudaclient.presentation.product

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.databinding.ActivityProductDetailBinding
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

    }

    // 툴바 설정
    private fun setToolbar() {
        setToolbarTitle("") // 타이틀
        setBackButton() // 뒤로가기 버튼
        binding.toolBar.toolbarShadow.visibility = View.GONE // 그림자 뷰 숨기기

        setToolbarButtons() // 툴바 버튼들 설정
    }


//    // 툴바 설정
//    private fun setToolbar() {
//        setToolbarTitle("") // 타이틀
//        setBackButton() // 뒤로가기 버튼
//        binding.toolBar.toolbarShadow.visibility = View.GONE // 그림자 뷰 숨기기
//
//        // 버튼 컨테이너 설정
//        binding.toolBar.toolbarBtnContainer.visibility = View.VISIBLE // 컨테이너 보이도록 설정
//
//        // 검색 아이콘
//        val searchIcon = ImageView(this).apply {
//            setImageResource(R.drawable.img_toolbar_search)
//            layoutParams = LinearLayout.LayoutParams(
//                20.dpToPx(),
//                20.dpToPx()
//            ).apply {
//                marginEnd = 15.dpToPx()
//            }
//            setOnClickListener { navigationToSearch() }
//        }
//
//        // 장바구니 아이콘
//        val cartIcon = ImageView(this).apply {
//            setImageResource(R.drawable.img_toolbar_basket)
//            layoutParams = LinearLayout.LayoutParams(
//                20.dpToPx(),
//                22.dpToPx()
//            )
//            setOnClickListener { navigationToCart() }
//        }
//
//        // 툴바 컨테이너에 아이콘 추가
//        binding.toolBar.toolbarBtnContainer.apply{
//            addView(searchIcon)
//            addView(cartIcon)
//        }
//
//    }
//
//    private fun Int.dpToPx() : Int {
//        return (this * resources.displayMetrics.density).toInt()
//    }
//
//    // 검색 화면으로 이동
//    private fun navigationToSearch() {
//
//    }
//
//    // 장바구니 화면으로 이동
//    private fun navigationToCart() {
//
//    }

}