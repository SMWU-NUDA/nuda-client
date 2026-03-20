package com.nuda.nudaclient.presentation.search.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nuda.nudaclient.databinding.ItemSearchAutocompleteBinding

// TODO 검색어 자동 완성 API 완성 후 어댑터 코드 작성
class AutoCompleteAdapter(
    private val onItemClick: (String) -> Unit // 자동 완성 아이템 클릭 시
) : RecyclerView.Adapter<AutoCompleteAdapter.AutoCompleteViewHolder>() {

    private var items = mutableListOf<String>()

    // 처음 로드 시 새로운 아이템 추가
    fun submitList(newItems: List<String>) {
        items.clear() // 전체 리스트 제거
        items.addAll(newItems) // 새로운 리스트로 교체
        notifyDataSetChanged() // 리스트 전체 갱신
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AutoCompleteViewHolder {
        val binding = ItemSearchAutocompleteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AutoCompleteViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: AutoCompleteViewHolder,
        position: Int
    ) {
        // 마지막 아이템이면 구분선 숨기기
        val isLast = (position == items.size - 1)

        // 데이터 바인딩
        holder.bind(items[position], isLast)

    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class AutoCompleteViewHolder(
        private val binding: ItemSearchAutocompleteBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String, isLast: Boolean) {
            binding.tvAutocompleteItem.text = item
            // 마지막 아이템 구분선 숨기기
            binding.divider.visibility = if (isLast) View.GONE else View.VISIBLE

            // 자동 완성 검색어 클릭 시 이벤트 처리
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

}