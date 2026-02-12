package com.nuda.nudaclient.presentation.common.activity

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.nuda.nudaclient.R

open class BaseActivity : AppCompatActivity() {

    // TODO: feat: 로딩 화면 추가 (BaseActivity)
    // TODO: refactor: 툴바 뒤로가기 공통 로직으로 수정(BaseActivity)

    // 툴바 타이틀 설정 메소드
    fun setToolbarTitle(title: String) {
        findViewById<TextView>(R.id.tv_toolbar_title).text = title
    }

    // 툴바 뒤로가기 버튼 설정 메소드
    fun setBackButton() {
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            // 기본 동작: 현재 액티비티 종료 및 뒤로가기
            onBackButtonClicked()
        }
    }

    // 뒤로가기 버튼 클릭 시 이벤트 (오버라이딩 가능)
    protected open fun onBackButtonClicked() {
        finish() // 기본 동작
    }

    // 장바구니, 검색 버튼 설정
    fun setToolbarButtons() {
        // 버튼 컨테이너 설정
        val container = findViewById<LinearLayout>(R.id.toolbar_btnContainer)
        container.visibility = View.VISIBLE // 컨테이너 보이도록 설정
        container.removeAllViews() // 기존 뷰 제거 (중복 방지)

        // 검색 아이콘
        val searchIcon = ImageView(this).apply {
            setImageResource(R.drawable.img_toolbar_search)
            layoutParams = LinearLayout.LayoutParams(
                20.dpToPx(),
                20.dpToPx()
            ).apply {
                marginEnd = 15.dpToPx()
            }
            setOnClickListener { navigationToSearch() }
        }

        // 장바구니 아이콘
        val cartIcon = ImageView(this).apply {
            setImageResource(R.drawable.img_toolbar_basket)
            layoutParams = LinearLayout.LayoutParams(
                20.dpToPx(),
                22.dpToPx()
            )
            setOnClickListener { navigationToCart() }
        }

        // 툴바 컨테이너에 아이콘 추가
        container.apply{
            addView(searchIcon)
            addView(cartIcon)
        }
    }

    // 검색 화면으로 이동 (오버라이딩 가능)
    protected open fun navigationToSearch() {

    }

    // 장바구니 화면으로 이동 (오버라이딩 가능)
    protected open fun navigationToCart() {

    }

    protected fun Int.dpToPx() : Int {
        return (this * resources.displayMetrics.density).toInt()
    }

}
