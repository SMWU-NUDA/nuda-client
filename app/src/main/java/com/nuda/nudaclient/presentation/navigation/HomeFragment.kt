package com.nuda.nudaclient.presentation.navigation

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nuda.nudaclient.R
import com.nuda.nudaclient.databinding.FragmentHomeBinding
import com.nuda.nudaclient.databinding.FragmentMypageBinding
import com.nuda.nudaclient.presentation.product.ProductRankingActivity


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNavigation()
    }

    // 뷰 파괴 (프래그먼트 객체는 유지)
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // 화면 이동 네비게이션 설정
    private fun setupNavigation() {
        // 전체 상품 랭킹 화면으로 이동
        binding.tvGoToRanking.setOnClickListener {
            startActivity(Intent(requireContext(), ProductRankingActivity::class.java))
        }
    }


}