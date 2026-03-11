package com.nuda.nudaclient.presentation.review.adapter

import android.app.Dialog
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.dto.common.Product
import com.nuda.nudaclient.data.remote.dto.reviews.ReviewsGetRankingByKeywordResponse
import com.nuda.nudaclient.databinding.ItemReviewCardBinding
import com.nuda.nudaclient.databinding.ItemReviewImageBinding

class ReviewAdapter(
    private val onLikeClick: (Int, Int) -> Unit
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    private val items = mutableListOf<ReviewsGetRankingByKeywordResponse.Review>()

    // 처음 로드 or 필터 변경 시 전체 교체
    fun submitList(newItems: List<ReviewsGetRankingByKeywordResponse.Review>) {
        items.clear() // 전체 리스트 제거
        items.addAll(newItems) // 새로운 리스트로 교체
        notifyDataSetChanged() // 리스트 전체 갱신
    }

    // 무한 스크롤 추가 로드 시
    fun appendItems(newItems: List<ReviewsGetRankingByKeywordResponse.Review>) {
        val startPosition = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(startPosition, newItems.size)
    }
    
    // 좋아요 업데이트 
    fun updateLikeState(position: Int, likedByMe: Boolean, likeCount: Int) {
        items[position].likedByMe = likedByMe
        items[position].likeCount = likeCount
        notifyItemChanged(position) // 해당 위치의 아이템만 갱신
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReviewViewHolder {
        val binding = ItemReviewCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ReviewViewHolder,
        position: Int
    ) {
        val item = items[position]

        holder.bind(item)

        // 좋아요 버튼 클릭 시 
        holder.likeButton.setOnClickListener {
            onLikeClick(item.reviewId, position) // 리뷰 아이디 전달
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }


    inner class ReviewViewHolder(
        private val binding: ItemReviewCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        val likeButton = binding.llBtnLike

        fun bind(review: ReviewsGetRankingByKeywordResponse.Review) {
            binding.tvNickname.text = review.me.nickname
            binding.tvReviewText.text = review.content

            // 별점
            binding.ratingBar.rating = review.rating.toFloat()

            // 날짜
            val date = review.createdAt.substringBefore(" (") // "2026.02.24"
            binding.tvDate.text = date

            // 좋아요 버튼
            if (review.likedByMe) { // 좋아요 선택 상태
                binding.ivLike.setImageResource(R.drawable.img_like_selected)
            } else {
                binding.ivLike.setImageResource(R.drawable.img_like_unselected)
            }
            binding.tvLikeCount.text = "좋아요 ${review.likeCount}"

            // 프로필 이미지
            Glide.with(binding.root.context)
                .load(review.me.profileImg)
                .placeholder(R.drawable.image_product2)
                .error(R.drawable.image_product)
                .centerCrop()
                .into(binding.ivProfile)

            // 리뷰 상품 이미지 리스트
            binding.llReviewPhoto.removeAllViews() // 기존 뷰 제거

            if (review.imageUrls.isNotEmpty()) { // 리뷰 이미지가 있다면
                review.imageUrls.forEach { imageUrl ->
                    // 리뷰 이미지 아이템 바인딩
                    val itemBinding = ItemReviewImageBinding.inflate(
                        LayoutInflater.from(binding.root.context),
                        binding.llReviewPhoto,
                        false
                    )
                    // 문자열 리뷰 이미지 로드
                    Glide.with(itemBinding.root.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.image_product2)
                        .error(R.drawable.image_product)
                        .centerCrop()
                        .into(itemBinding.root as ImageView)

                    binding.llReviewPhoto.addView(itemBinding.root) // 컨테이너에 뷰 추가

                    // 이미지 클릭 시 확대
                    itemBinding.root.setOnClickListener {
                        val dialog = Dialog(binding.root.context)

                        val density = binding.root.context.resources.displayMetrics.density
                        val size = (300 * density).toInt() // 300dp를 px로 변환

                        val imageView = ImageView(binding.root.context).apply {
                            layoutParams = ViewGroup.LayoutParams(size, size)
                            scaleType = ImageView.ScaleType.FIT_CENTER
                        }

                        Glide.with(binding.root.context)
                            .load(imageUrl)
                            .into(imageView)

                        dialog.setContentView(imageView)
                        dialog.window?.apply {
                            setLayout(size, size)
                            setBackgroundDrawableResource(android.R.color.transparent)
                        }
                        dialog.show()
                    }
                }
            } else { // 리뷰 이미지가 없을 때
                binding.llReviewPhoto.visibility = View.GONE
            }
        }
    }
}