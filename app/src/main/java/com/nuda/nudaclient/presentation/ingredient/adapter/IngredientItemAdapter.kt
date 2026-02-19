package com.nuda.nudaclient.presentation.ingredient.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.dto.ingredients.IngredientsGetAllResponse
import com.nuda.nudaclient.databinding.ItemIngredientCardBinding

// 성분 아이템 목록 어댑터
class IngredientItemAdapter(
    private val ingredientList: List<IngredientsGetAllResponse.Ingredient>,
    private val onItemClick: (Int) -> Unit)
    : RecyclerView.Adapter<IngredientItemAdapter.IngredientItemViewHolder>() {

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
        val ingredientItem = ingredientList[position]

        // 성분 아이템 카드에 데이터 바인딩
        holder.bind(ingredientItem)
        // 성분 아이템 카드 클릭 이벤트
        holder.ingredientItemCard.setOnClickListener {
            onItemClick(ingredientItem.ingredientId)
        }
    }

    override fun getItemCount(): Int {
        return ingredientList.size
    }

    // 중첩 클래스 뷰 홀더
    class IngredientItemViewHolder(
        private val binding: ItemIngredientCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        val ingredientItemCard = binding.root

        // 데이터 바인딩 메소드
        fun bind(ingredientItem: IngredientsGetAllResponse.Ingredient) {
            // 위험도 색 업데이트
            val color = when (ingredientItem.riskLevel) {
                "SAFE" -> R.color.riskLevel_mint
                "WARN" -> R.color.riskLevel_yellow
                "DANGER" -> R.color.riskLevel_red
                else -> R.color.gray4
            }
            binding.ivRiskLevel.setColorFilter(color)

            // 성분 이름, 구성 요소 반영
            binding.tvIngredientName.text = ingredientItem.name
            binding.tvComponent.text = ingredientItem.layerType
        }
    }
}