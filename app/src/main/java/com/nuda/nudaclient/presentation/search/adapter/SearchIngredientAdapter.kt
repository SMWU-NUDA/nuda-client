package com.nuda.nudaclient.presentation.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.dto.common.Ingredient
import com.nuda.nudaclient.databinding.ItemIngredientCardBinding

class SearchIngredientAdapter(
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<SearchIngredientAdapter.IngredientItemViewHolder>() {

    private val items = mutableListOf<Ingredient>()

    // 처음 로드 or 필터 변경 시 전체 교체
    fun submitList(newItems: List<Ingredient>) {
        items.clear() // 전체 리스트 제거
        items.addAll(newItems) // 새로운 리스트로 교체
        notifyDataSetChanged() // 리스트 전체 갱신
    }

    // 무한 스크롤 추가 로드 시
    fun appendItems(newItems: List<Ingredient>) {
        val startPosition = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(startPosition, newItems.size)
    }


    // 뷰 홀더 만드는 함수
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int): IngredientItemViewHolder {
        val binding = ItemIngredientCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IngredientItemViewHolder(binding)
    }

    // 뷰 홀더와 데이터 바인딩
    override fun onBindViewHolder(
        holder: IngredientItemViewHolder,
        position: Int
    ) {
        val item = items[position]

        // 성분 아이템 카드에 데이터 바인딩
        holder.bind(item)
        // 성분 아이템 카드 클릭 이벤트
        holder.ingredientItemCard.setOnClickListener {
            onItemClick(item.ingredientId)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class IngredientItemViewHolder(
        private val binding: ItemIngredientCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        val ingredientItemCard = binding.root

        // 데이터 바인딩 메소드
        fun bind(ingredientItem: Ingredient) {
            // 위험도 색 업데이트
            val color = when (ingredientItem.riskLevel) {
                "SAFE" -> R.color.riskLevel_mint
                "WARN" -> R.color.riskLevel_yellow
                "DANGER" -> R.color.riskLevel_red
                else -> R.color.gray4
            }
            binding.ivRiskLevel.setColorFilter(ContextCompat.getColor(binding.root.context, color))

            // 성분 이름
            binding.tvIngredientName.text = ingredientItem.name

            // 구성 요소
            binding.tvComponent.text = when (ingredientItem.layerType) {
                "TOP_SHEET" -> "표지"
                "ABSORBER" -> "흡수체"
                "BACK_SHEET" -> "방수층"
                "ADHESIVE" -> "접착제"
                "ADDITIVE" -> "기타"
                else -> "UNKNOWN"
            }
        }
    }
}