package com.nuda.nudaclient.presentation.mypage

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.membersService
import com.nuda.nudaclient.data.remote.dto.common.ApiResponse
import com.nuda.nudaclient.data.remote.dto.members.MembersUserInfoResponse
import com.nuda.nudaclient.databinding.FragmentMypageBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.utils.CustomToast

class MyPageFragment : Fragment() {

    // 프래그먼트 생명 주기의 onDestroyView() 콜백에서 뷰는 삭제되지만 프래그먼트는 유지
    private var _binding: FragmentMypageBinding? = null // nullabel, 뷰가 없을 때는 null로 초기화
    private val binding get() = _binding!! // non-null, 뷰가 있는 시점(onViewCreate~onDestoryView)에만 사용


//    // 프래그먼트 초기화 코드 (프래그먼트 최초 생성 시점에 호출되는 콜백)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//    }

    // 레이아웃 지정 및 뷰 생성 (뷰에 데이터 설정은 못함)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 뷰 초기화, 리사이클러뷰 설정, 데이터 표시, API 호출 등
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 프로필 업데이트
        loadUserInfo()

        // 버튼 설정
        setupButtons()
    }

    // 화면이 보일 때마다 실행 (탭 전환, 다른 앱 다녀 오기)
    override fun onResume() {
        super.onResume()

        // 프로필 최신 정보 업데이트
        loadUserInfo()
    }

    // 뷰 파괴 (프래그먼트 객체는 유지)
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // 마이페이지 회원 정보 로드
    private fun loadUserInfo() {
        membersService.getUserInfo()
            .executeWithHandler(
                context = requireContext(), // 프래그먼트의 Context
                onSuccess = { body ->
                    if (body.success == true) {
                        updateUserInfo(body)
                        CustomToast.show(binding.root, body?.message ?: "마이페이지 프로필 업데이트 완료")
                    } else {
                        CustomToast.show(binding.root, body.message ?: "서버 요청 실패")
                    }
                }
            )
    }

    // UI 업데이트 함수
    private fun updateUserInfo(body: ApiResponse<MembersUserInfoResponse>){
        binding.tvNickname.text = body.data?.me?.nickname
        binding.tvUsername.text = body.data?.me?.username

        // 프로필 이미지
        // URL 문자열 이미지로 로드 및 업데이트
        Glide.with(this)
            .load(body.data?.me?.profileImg)
            .error(R.drawable.image_product) // 이미지 로드 실패 시
            .into(binding.ivProfile)

        // 키워드 업데이트 (최대 3개)
        val keywords = body.data?.survey?.keywords // List<String>
        binding.tvKeywords.text = when {
            keywords.isNullOrEmpty() -> "키워드 없음"
            keywords.size <= 3 -> keywords.joinToString(", ")
            else -> keywords.take(3).joinToString(", ") + " ..."
        }
    }

    // 버튼 설정
    private fun setupButtons() {
        setupEditAccount()
        setupEditDelivery()
        setupManageKeyword()
        setupOrderHistory()
        setupCreateNewReview()
        setupMyReviews()
        setupLogout()
        setupDeleteAccount()
    }

    // 프로필 수정
    private fun setupEditAccount() {
        binding.btnEditAccount.setOnClickListener {
            // 프로필 수정 액티비티로 이동
            val intent = Intent(requireContext(), MypageEditAccountActivity::class.java)
            startActivity(intent)
        }
    }

    // 배송정보 관리
    private fun setupEditDelivery() {
        binding.btnEditDelivery.setOnClickListener {
            // 배송정보 관리 액티비티로 이동
            val intent = Intent(requireContext(), MypageEditDeliveryActivity::class.java)
            startActivity(intent)
        }
    }

    // 키워드 관리
    private fun setupManageKeyword() {
        binding.groupManageKeyword.setOnClickListener {
            // 키워드 관리 액티비티로 이동
            val intent = Intent(requireContext(), MypageManageKeywordActivity::class.java)
            startActivity(intent)
        }
    }

    // 주문 내역
    private fun setupOrderHistory() {
        binding.groupOrderHistory.setOnClickListener {
            // 주문 내역 액티비티로 이동

        }
    }

    // 새 리뷰 작성
    private fun setupCreateNewReview() {
        binding.groupCreateNewReview.setOnClickListener {
            // 새 리뷰 작성 액티비티로 이동
        }
    }

    // 내 리뷰
    private fun setupMyReviews() {
        binding.groupMyReviews.setOnClickListener {
            // 내 리뷰 액티비티로 이동
            val intent = Intent(requireContext(), MypageMyreviewActivity::class.java)
            startActivity(intent)
        }
    }

    // 로그아웃
    private fun setupLogout() {
        binding.logout.setOnClickListener {
            /** 로그아웃 팝업창 띄우기
             * 확인 시 로그아웃 API 호출
             * 로그인 시 제공되는 로컬 회원 정보 삭제 pref
             * 로그인 화면으로 이동
             */
        }
    }

    // 회원 탈퇴
    private fun setupDeleteAccount() {
        binding.deleteAccount.setOnClickListener {
            /** 회원 탈퇴 팝업창 띄우기
             * 확인 시 회원 탈퇴 API 호출
             * pref 및 로컬에 저장된 토큰과 회원 정보 삭제
             * 로그인 화면으로 이동
             */
        }
    }



}