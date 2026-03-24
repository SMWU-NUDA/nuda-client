package com.nuda.nudaclient.presentation.navigation

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.nuda.nudaclient.R
import com.nuda.nudaclient.presentation.common.activity.BaseActivity
import com.nuda.nudaclient.utils.CustomToast

class NavigationActivity : BaseActivity() {

    private val TAG = "NavigationActivity"

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

        // 로그인 후 홈 화면 진입 시 토스트 메세지 띄우기
        val msg = intent.getStringExtra("SHOW_TOAST")
        if (!msg.isNullOrEmpty()) CustomToast.show(findViewById(android.R.id.content), msg)

        // 뷰 초기화
        initViews()

        if (savedInstanceState == null) {
            // 초기 화면 설정 (나중에 홈화면으로 변경)
            replaceFragment(HomeFragment(), "HOME")
            selectMenu(menuHome)
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
        // 홈 메뉴 클릭
        menuHome.setOnClickListener {
            replaceFragment(HomeFragment(), "HOME")
            selectMenu(menuHome)
            Log.d("API_DEBUG", "[$TAG] 홈 화면 로드")
        }

        // 맞춤 추천 메뉴 클릭
        menuRecommend.setOnClickListener {
            replaceFragment(RecommendFragment(), "RECOMMEND")
            selectMenu(menuRecommend)
            Log.d("API_DEBUG", "[$TAG] 맞품 추천 화면 로드")
        }

        // 관심 메뉴 클릭
        menuWishlist.setOnClickListener {
            replaceFragment(WishlistFragment(), "WISHLIST")
            selectMenu(menuWishlist)
            Log.d("API_DEBUG", "[$TAG] 나의 관심 화면 로드")
        }

        // 마이페이지 메뉴 클릭
        menuMypage.setOnClickListener {
            replaceFragment(MyPageFragment(), "MYPAGE")
            selectMenu(menuMypage)
            Log.d("API_DEBUG", "[$TAG] 마이페이지 화면 로드")
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

    // 맞춤 추천 화면으로 이동 (마이페이지 -> 맞춤 추천)
    fun navigateToRecommend() {
        replaceFragment(RecommendFragment(), "RECOMMEND")
        selectMenu(menuRecommend)
    }
}