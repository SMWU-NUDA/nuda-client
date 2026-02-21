package com.nuda.nudaclient.presentation.common.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nuda.nudaclient.R
import com.nuda.nudaclient.databinding.FragmentBottomSheetBinding
import okio.Options

// 정렬 BottomSheetDialog 코드 (재사용 가능)
class SortBottomSheet : BottomSheetDialogFragment() {
    private var _binding: FragmentBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var options: List<String> = emptyList()
    private var selectedIndex: Int = 0
    private var onSelected: ((Int) -> Unit)? = null

    companion object {
        fun newInstance(
            options: List<String>,
            selectedIndex: Int = 0,
            onSelected: (Int) -> Unit
        ): SortBottomSheet {
            return SortBottomSheet().apply {
                this.options = options
                this.selectedIndex = selectedIndex
                this.onSelected = onSelected
            }
        }
    }

    // xml 바인딩
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 뷰 생성 후
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val radioGroup = binding.rgSort

        options.forEachIndexed { index, text ->
            val rb = RadioButton(requireContext()).apply {
                id = View.generateViewId()
                this.text = text
                setButtonDrawable(android.R.color.transparent) // 기본 동그라미 제거
                textSize = 16f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.gray1))
                typeface = resources.getFont(R.font.pretendard_medium)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            radioGroup.addView(rb) // 라디오그룹에 라디오버튼 추가

            // 선택된 항목 초기 표시
            if (index == selectedIndex) {
                rb.isChecked = true // 체크 상태로 변경
                rb.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.img_checked,
                    0
                ) // 라디오버튼 오른 쪽에 체크 아이콘 추가
                rb.typeface =
                    ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold) // 폰트 변경
            }
        }

        //
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            // 모든 라디오버튼 스타일 초기화
            for (i in 0 until group.childCount) {
                val rb = group.getChildAt(i) as RadioButton
                rb.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0) // 아이콘 제거
                rb.typeface =
                    ResourcesCompat.getFont(requireContext(), R.font.pretendard_medium) // 기본 폰트로 변경
            }

            // 선택된 항목만 체크 + bold 처리
            val selectedRb = group.findViewById<RadioButton>(checkedId) // 선택된 라디오버튼
            selectedRb.setCompoundDrawablesWithIntrinsicBounds(  // 라디오버튼 오른 쪽에 체크 아이콘 추가
                0,
                0,
                R.drawable.img_checked,
                0
            )
            selectedRb.typeface =   // 폰트 변경
                ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold)

            // 몇 번째 인덱스인지 계산해서 콜백 전달
            val selectedIdx = (0 until group.childCount)
                .first { group.getChildAt(it).id == checkedId} // 선택된 ID와 일치하는 첫 번째 인덱스 찾아서 저장

            onSelected?.invoke(selectedIdx) // 선택된 인덱스를 onSelected에 넘김
            dismiss() // BottomSheetDialogFragment 닫기
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}