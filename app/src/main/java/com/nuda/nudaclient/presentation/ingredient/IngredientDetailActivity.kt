package com.nuda.nudaclient.presentation.ingredient

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.ingredientsService
import com.nuda.nudaclient.databinding.ActivityIngredientDetailBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.presentation.common.activity.BaseActivity

class IngredientDetailActivity : BaseActivity() {
    // 성분 상세페이지 진입 시 intent에 ingredientId 함께 전달 필요!!

    // TODO 버튼 클릭 시 폰트 변경 적용

    private lateinit var binding: ActivityIngredientDetailBinding

    private var ingredientId: Int = -1

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

        // Intent에서 ingredientId 받기
        ingredientId = intent.getIntExtra("INGREDIENT_ID", -1)

        setToolbar()
        setIngredientWishButtons() // 성분 즐겨찾기 버튼 설정

    }

    // 툴바 설정
    private fun setToolbar() {
        setToolbarTitle("성분 정보") // 타이틀
        setBackButton() // 뒤로가기 버튼
        setToolbarShadow(false) // 그림자 숨김
    }

    // 성분 즐겨찾기 버튼 설정
    private fun setIngredientWishButtons() {
        val btnHighlight = binding.btnHighlight
        val btnAvoid = binding.btnAvoid

        // 버튼 클릭 시 변경할 폰트 저장
        val typefaceUnclicked = ResourcesCompat.getFont(this, R.font.pretendard_medium)
        val typefaceClicked = ResourcesCompat.getFont(this, R.font.pretendard_bold)

        // 관심 버튼 클릭
        btnHighlight.setOnClickListener {
            ingredientsService.createIngredientLike(
                ingredientId = ingredientId,
                preference = true
            ).executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        body.data?.let { data ->
                            when(data.preference) {
                                true -> { // 관심 등록
                                    btnHighlight.typeface = typefaceClicked // 버튼 폰트 변경
                                    Log.d("API_DEBUG", "성분 관심 등록 성공")
                                }
                                null -> { // 관심 취소
                                    btnHighlight.typeface = typefaceUnclicked
                                    Log.d("API_DEBUG", "성분 관심 등록 취소")
                                }
                                else -> Log.d("API_DEBUG", "성분 관심 등록 실패")
                            }
                        }
                    }
                }
            )
        }
        // 피하기 버튼 클릭
        btnAvoid.setOnClickListener {
            ingredientsService.createIngredientLike(
                ingredientId = ingredientId,
                preference = false
            ).executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        body.data?.let { data ->
                            when(data.preference) {
                                true -> { // 피하기 등록
                                    btnAvoid.typeface = typefaceClicked // 버튼 폰트 변경
                                    Log.d("API_DEBUG", "성분 피하기 등록 성공")
                                }
                                null -> { // 피하기 취소
                                    btnAvoid.typeface = typefaceUnclicked
                                    Log.d("API_DEBUG", "성분 피하기 등록 취소")
                                }
                                else -> Log.d("API_DEBUG", "성분 피하기 등록 실패")
                            }
                        }
                    }
                }
            )
        }
    }
}