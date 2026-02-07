package com.nuda.nudaclient.presentation.common.activity

import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.nuda.nudaclient.R
import com.nuda.nudaclient.presentation.mypage.fragment.MyPageFragment

class NavigationActivity : AppCompatActivity() {

    private lateinit var menuHome : LinearLayout
    private lateinit var menuRecommend : LinearLayout
    private lateinit var menuWishlist : LinearLayout
    private lateinit var menuMypage : LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_navigation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 뷰 초기화
        initViews()

        if (savedInstanceState == null) {
            // 초기 화면 설정 (나중에 홈화면으로 변경)
            replaceFragment(MyPageFragment(), "MYPAGE")
            selectMenu(menuMypage)
        }

        // 네비게이션 메뉴 설정
        setNavigation()
    }

    private fun initViews() {
        menuHome = findViewById(R.id.menu_home)
        menuRecommend = findViewById(R.id.menu_recommend)
        menuWishlist = findViewById(R.id.menu_wishlist)
        menuMypage = findViewById(R.id.menu_mypage)
    }


    private fun setNavigation() {
//        // 홈 메뉴 클릭
//        menuHome.setOnClickListener {
//            replaceFragment()
//            selectMenu(menuHome)
//        }
//
//        // 맞춤 추천 메뉴 클릭
//        menuRecommend.setOnClickListener {
//            replaceFragment()
//            selectMenu(menuRecommend)
//        }
//
//
//        // 관심 메뉴 클릭
//        menuWishlist.setOnClickListener {
//            replaceFragment()
//            selectMenu(menuWishlist)
//        }

        // 마이페이지 메뉴 클릭
        menuMypage.setOnClickListener {
            replaceFragment(MyPageFragment(), "MYPAGE")
            selectMenu(menuMypage)
        }
    }

    // 선택 메뉴 상태 관리
    private fun selectMenu(selectedMenu: LinearLayout) {
        // 모든 메뉴를 비활성화
        menuHome.isSelected = false
        menuRecommend.isSelected = false
        menuWishlist.isSelected = false
        menuMypage.isSelected = false

        // 선택된 메뉴만 활성화 -> 텍스트, 아이콘 색상 변경
        selectedMenu.isSelected = true
    }

    // 프래그먼트 변경
    private fun replaceFragment(fragment: Fragment, tag: String) {
        // 같은 프래그먼트가 이미 있으면 교체하지 않음
        if (supportFragmentManager.findFragmentByTag(tag) != null) {
            return
        }

        supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, fragment, tag)
        .commit()
    }
}