package com.nuda.nudaclient.presentation.review

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.reviewsService
import com.nuda.nudaclient.databinding.ActivityReviewAllBinding
import com.nuda.nudaclient.databinding.ItemReviewKeywordsBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.presentation.common.activity.BaseActivity

class ReviewAllActivity : BaseActivity() {
    
    // TODO 별점, 리뷰 수 바인딩 필요

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

        loadReviewKeyword() // 긍정/부정 키워드 로드
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

    // 긍정/부정 키워드 로드
    private fun loadReviewKeyword() {
        reviewsService.getReviewKeywords(productId)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        body.data?.let { data ->
                            // 긍정 키워드
                            if (data.positive.isEmpty()) { // 긍정 키워드가 없는 경우
                                binding.llPositiveItems.visibility = View.GONE
                                binding.tvNoPositiveKeyword.visibility = View.VISIBLE
                                Log.d("API_DEBUG", "긍정키워드가 없습니다")
                            } else { // 긍정 키워드가 있는 경우
                                binding.llPositiveItems.visibility = View.VISIBLE
                                binding.tvNoPositiveKeyword.visibility = View.GONE
                                addKeywordItems(binding.llPositiveItems, data.positive)
                                Log.d("API_DEBUG", "positive: ${data.positive}")
                            }
                            // 부정 키워드
                            if (data.negative.isEmpty()) { // 부정 키워드가 없는 경우
                                binding.llNegativeItems.visibility = View.GONE
                                binding.tvNoNegativeKeyword.visibility = View.VISIBLE
                                Log.d("API_DEBUG", "긍정키워드가 없습니다")
                            } else { // 부정 키워드가 있는 경우
                                binding.llNegativeItems.visibility = View.VISIBLE
                                binding.tvNoNegativeKeyword.visibility = View.GONE
                                addKeywordItems(binding.llNegativeItems, data.negative)
                                Log.d("API_DEBUG", "negative: ${data.negative}")
                            }
                        }
                    }
                }
            )
    }

    // 긍정/부정 키워드 추가
    private fun addKeywordItems(container: LinearLayout, keywords: List<String>) {
        container.removeAllViews() // 기존 뷰 제거

        keywords.forEachIndexed { index, keyword ->
            val itemBinding = ItemReviewKeywordsBinding.inflate(
                layoutInflater,
                container,
                false
            )
            // 키워드 설정
            itemBinding.tvRank.text = "${index+1}"
            itemBinding.tvKeyword.text = keyword

            // 키워드 컨테이너에 추가
            container.addView(itemBinding.root)
        }
    }


}