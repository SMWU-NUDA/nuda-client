package com.nuda.nudaclient.presentation.mypage

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.membersService
import com.nuda.nudaclient.data.remote.dto.common.ApiResponse
import com.nuda.nudaclient.data.remote.dto.members.MembersUserInfoResponse
import com.nuda.nudaclient.databinding.ActivityMypageEditAccountBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.presentation.common.activity.BaseActivity

class MypageEditAccountActivity : BaseActivity() {

    // TODO: feat(members): (mypage) 프로필 수정 기능 틀 구현
    // TODO: feat(members): (mypage) 프로필 수정 유효성 검사 추가 (회원가입 참고 / 닉네임, 아이디, 비밀번호, 이메일)
    // TODO: feat(members): (mypage) 프로필 수정 - 비밀번호 검증 기능 구현 및 API 호출
    // TODO: feat(members): (mypage) 프로필 수정 구현 및 테스트 완료 (프로필 수정 시 마이페이지 저장)

    private lateinit var binding: ActivityMypageEditAccountBinding

    // 기존 회원 정보
    private var originalNickname: String? = null
    private var originalUsername: String? = null
    private var originalEmail: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mypage_edit_account)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityMypageEditAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 툴바 타이틀 변경
        setToolbarTitle("프로필 수정")
        // 툴바 뒤로가기 설정
        setBackButton()

        // 회원 정보 로드
        loadUserInfo()

    }

    // 회원 정보 로드
    private fun loadUserInfo() {
        // 회원 정보 로드 API 호출
        membersService.getUserInfo()
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    // 기존 회원 정보 저장 및 입력창 텍스트 설정
                    saveAndSetUserInfo(body)
                }
            )
    }

    // 기존 회원 정보 저장 및 입력창 텍스트 설정
    private fun saveAndSetUserInfo(body: ApiResponse<MembersUserInfoResponse>) {
        // 기존 회원 정보 저장
        originalNickname = body.data?.me?.nickname
        originalUsername = body.data?.me?.username
        originalEmail = body.data?.me?.email

        // 입력창 텍스트 설정
        binding.etNickname.setText(originalNickname)
        binding.etUsername.setText(originalUsername)
        binding.etEmail.setText(originalEmail)
    }

    // 수정된 프로필 정보 저장
    private fun updateProfile() {
        // 수정된 회원 정보 찾아서 request 객체에 전달
        val request = createUpdateProfileRequest()

        // 프로필 수정 업데이트 API 호출
        membersService.updateProfile(request)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->


                }
            )
    }

    // 수정된 회원 정보 찾아서 request 객체에 전달
    private fun createUpdateProfileRequest() : Map<String, String> {
        val request = mutableMapOf<String, String>()

        // 현재 입력된 회원 정보
        val currentNickname = binding.etNickname.text.toString().trim() // 기존 회원 정보와 비교
        val currentUsername = binding.etUsername.text.toString().trim()
        val currentEmail = binding.etEmail.text.toString().trim()
        val currentPreviousPw = binding.etPwNow.text.toString().trim() // ""와 비교
        val currentNewPw = binding.etPw.text.toString().trim() // ""

        // 기존 회원 정보와 비교 후 수정된 항목 리퀘스트에 추가
        if (currentNickname != originalNickname) request["nickname"] = currentNickname
        if (currentUsername != originalUsername) request["username"] = currentUsername
        if (currentEmail != originalEmail) request["email"] = currentEmail
        if (currentPreviousPw.isNotEmpty()) request["currentPassword"] = currentPreviousPw
        if (currentNewPw.isNotEmpty()) request["newPassword"] = currentNewPw

        return request
    }

}