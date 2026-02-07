package com.nuda.nudaclient.presentation.mypage

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.membersService
import com.nuda.nudaclient.data.remote.dto.common.ApiResponse
import com.nuda.nudaclient.data.remote.dto.members.MembersDeliveryInfoResponse
import com.nuda.nudaclient.databinding.ActivityMypageEditDeliveryBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.presentation.common.activity.BaseActivity

class MypageEditDeliveryActivity : BaseActivity() {

    // TODO: feat(members): (mypage) 배송정보 관리 기능 구현 (회원가입 참고, API 호출 + 데이터 바인딩)
    // TODO: feat(members): (mypage) 배송정보 관리 기본 틀 작성 (계정정보 조회 API 호출 / 조회 데이터 화면 반영)
    // TODO: feat(members): (mypage) 배송정보 관리 유효성 검사 설정 (회원가입 참고 및 진행 상황 복원)
    // TODO: feat(members): (mypage) 배송정보 관리 기능 구현 완료 및 테스트

    private lateinit var binding: ActivityMypageEditDeliveryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMypageEditDeliveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // 툴바 타이틀 변경
        setToolbarTitle("배송정보 관리")
        // 툴바 뒤로가기 설정
        setBackButton()

        // 계정 정보 로드
        loadDeliveryInfo()


    }

    // 배송 정보 로드
    private fun loadDeliveryInfo() {
        membersService.getDeliveryInfo()
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if(body.success == true) {
                        // 기존 배송 정보 입력창 텍스트로 설정
                        saveAndSetDeliveryInfo(body)
                        // 유효성 검사 UI 복원
                        setupProcess()

                        Log.d("API_DEBUG", "배송정보 로드 성공")
                    } else {
                        Log.e("API_ERROR", "배송정보 로드 실패")
                    }
                }
            )
    }

    // 기본 배송 정보 입력창 텍스트로 설정
    private fun saveAndSetDeliveryInfo(body: ApiResponse<MembersDeliveryInfoResponse>) {
       binding.etRecipient.setText(body.data?.recipient)
       binding.etPhone.setText(body.data?.phoneNum)
       binding.etZip.setText(body.data?.postalCode)
       binding.etAddress.setText(body.data?.address1)
       binding.etAddressDetail.setText(body.data?.address2)
    }

    // 기존 진행상황 복원


}