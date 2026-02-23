package com.nuda.nudaclient.presentation.mypage.activity

import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.enableEdgeToEdge
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.membersService
import com.nuda.nudaclient.data.remote.dto.members.MembersChangeKeywordRequest
import com.nuda.nudaclient.databinding.ActivityMypageManageKeywordBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.presentation.common.activity.BaseActivity
import com.nuda.nudaclient.utils.CustomToast

class MypageManageKeywordActivity : BaseActivity() {

    private lateinit var binding : ActivityMypageManageKeywordBinding

    private lateinit var irritationLevel: String
    private lateinit var scent: String
    private lateinit var adhesion: String
    private lateinit var thickness: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMypageManageKeywordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setToolbar()
        
        // 초기 키워드 화면 로드
        loadKeywords()

        // 저장 버튼
        setupSaveButton()

    }

    // 툴바 설정
    private fun setToolbar() {
        setToolbarTitle("키워드 관리") // 타이틀
        setBackButton() // 뒤로가기 버튼
    }

    // 키워드 조회 API 호출 및 키워드 설정
    private fun loadKeywords() {
        membersService.getKeywords()
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        body.data?.let { data ->
                            when (data.irritationLevel) {
                                "NONE" -> {
                                    irritationLevel = "NONE"
                                    binding.tvIrritationLevel.text = "민감도 낮음"
                                }
                                "SOMETIMES" -> {
                                    irritationLevel = "SOMETIMES"
                                    binding.tvIrritationLevel.text = "민감도 보통"
                                }
                                "OFTEN" -> {
                                    irritationLevel = "OFTEN"
                                    binding.tvIrritationLevel.text = "민감도 높음"
                                }
                                else -> {
                                    irritationLevel = "UNKNOWN"
                                    binding.tvIrritationLevel.text = "UNKNOWN"
                                }
                            }
                            when (data.scent) {
                                "NONE" -> {
                                    scent = "NONE"
                                    binding.tvScent.text = "무향"
                                }
                                "MILD" -> {
                                    scent = "MILD"
                                    binding.tvScent.text = "보통 향"
                                }
                                "STRONG" -> {
                                    scent = "STRONG"
                                    binding.tvScent.text = "강한 향"
                                }
                                else -> {
                                    scent = "UNKNOWN"
                                    binding.tvScent.text = "UNKNOWN"
                                }
                            }
                            when (data.adhesion) {
                                "WEAK" -> {
                                    adhesion =  "WEAK"
                                    binding.tvAdhesion.text = "접착력 무관"
                                }
                                "NORMAL" -> {
                                    adhesion = "NORMAL"
                                    binding.tvAdhesion.text = "접착력 보통"
                                }
                                "STRONG" -> {
                                    adhesion = "STRONG"
                                    binding.tvAdhesion.text = "접착력 중시"
                                }
                                else -> {
                                    adhesion = "UNKNOWN"
                                    binding.tvAdhesion.text = "UNKNOWN"
                                }
                            }
                            when (data.thickness) {
                                "THIN" -> {
                                    thickness = "THIN"
                                    binding.tvThickness.text = "약한 흡수력"
                                }
                                "NORMAL" -> {
                                    thickness = "NORMAL"
                                    binding.tvThickness.text = "보통 흡수력"
                                }
                                "THICK" -> {
                                    thickness = "THICK"
                                    binding.tvThickness.text = "높은 흡수력"
                                }
                                else -> {
                                    thickness = "UNKNOWN"
                                    binding.tvThickness.text = "UNKNOWN"
                                }
                            }
                            // 키워드 설정
                            setKeywords()
                        }
                    }
                }
            )
    }

    // 키워드 설정 선택된 버튼 로드
    private fun setKeywords() {
        selectedRadioButtonByTag(binding.rgIrritationLevel, irritationLevel)
        selectedRadioButtonByTag(binding.rgScent, scent)
        selectedRadioButtonByTag(binding.rgAdhesion, adhesion)
        selectedRadioButtonByTag(binding.rgThickness, thickness)

        // 라디오그룹 폰트 변경 리스너 설정
        setRadioGroupFontListener(binding.rgIrritationLevel) { selectedTag ->
            irritationLevel = selectedTag // 태그 저장
        }
        setRadioGroupFontListener(binding.rgScent) { selectedTag ->
            scent = selectedTag
        }
        setRadioGroupFontListener(binding.rgAdhesion) { selectedTag ->
            adhesion = selectedTag
        }
        setRadioGroupFontListener(binding.rgThickness) { selectedTag ->
            thickness = selectedTag
        }
    }

    // 라디오그룹 선택 변경 시 폰트 변경
    private fun setRadioGroupFontListener(radioGroup: RadioGroup, onSelected: (String) -> Unit) {
        val typefaceNormal = ResourcesCompat.getFont(this, R.font.pretendard_extralight)
        val typefaceBold = ResourcesCompat.getFont(this, R.font.pretendard_bold)

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            for (i in 0 until group.childCount) {
                val radioButton = group.getChildAt(i) as RadioButton
                if (radioButton.id == checkedId) {
                    radioButton.typeface = typefaceBold // 선택된 폰트 변경
                    onSelected(radioButton.tag.toString()) // 선택된 버튼의 태그 전달
                }
                else {
                    radioButton.typeface = typefaceNormal // 그 외 항목 폰트 기본 설정
                }
            }
        }
    }

    // 태그로 라디오버튼 선택
    private fun selectedRadioButtonByTag(radioGroup: RadioGroup, tagValue: String) {
        val typeface = ResourcesCompat.getFont(this, R.font.pretendard_bold)
        for (i in 0 until radioGroup.childCount) {
            val radioButton = radioGroup.getChildAt(i) as RadioButton
            if (radioButton.tag == tagValue) {
                radioButton.isChecked = true // 선택 설정
                radioButton.typeface = typeface // 폰트 설정
                return
            }
        }
    }

    // 저장 버튼
    private fun setupSaveButton() {
        binding.btnSavePage.setOnClickListener {
            // 키워드 수정 API 호출
            membersService.changeKeywords(MembersChangeKeywordRequest(
                irritationLevel = irritationLevel,
                scent = scent,
                adhesion = adhesion,
                thickness = thickness
            )).executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        CustomToast.show(binding.root, "키워드 수정이 완료되었습니다")
                        loadKeywords() // 수정한 키워드 화면 로드
                        Log.d("API_DEBUG", "키워드 수정에 성공했습니다")
                    } else {
                        Log.d("API_DEBUG", "키워드 수정에 실패했습니다")
                    }
                }
            )
        }
    }

}