package com.nuda.nudaclient.presentation.mypage.adapter

import android.app.Dialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.dto.reviews.ReviewsGetMyReviewsResponse
import com.nuda.nudaclient.databinding.ItemMyReviewCardBinding
import com.nuda.nudaclient.databinding.ItemReviewImageBinding
import kotlin.invoke

// TODO 리뷰 사진 추가되면 리스트 아이템 동적 추가

class MypageMyReviewAdapter
    : RecyclerView.Adapter<MypageMyReviewAdapter.MyReviewViewholder>() {

    // 어댑터 내부에서 리뷰 데이터를 저장하는 리스트 생성
    private val content = mutableListOf<ReviewsGetMyReviewsResponse.Content>()

    // 삭제 버튼 클릭 리스너 인터페이스
    var onDeleteClickListner: ((reviewId: Int, position: Int) -> Unit)? = null


    // 중첩 클래스로 뷰 홀더 정의. ViewHolder는 리뷰 카드 1개를 표시하는 틀
    class MyReviewViewholder(
        private val binding: ItemMyReviewCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // 리뷰 카드 아이템 뷰를 변수에 저장
        val btn_deleteReview = binding.btnDeleteReview
        val iv_productImage = binding.ivProductImage
        val ll_review_photo = binding.llReviewPhoto

        fun bind(review: ReviewsGetMyReviewsResponse.Content) {
            binding.ratingBar.rating = review.rating.toFloat()
            binding.tvDate.text = review.createdAt.substringBefore(" (")
            binding.tvBrand.text = review.brandName
            binding.tvProductName.text = review.productName
            binding.tvReviewText.text = review.content

            // URL 문자열 이미지로 로드 및 업데이트
            Glide.with(binding.root.context)
                .load(review.productThumbnail)
                .placeholder(R.drawable.image_product2)
                .error(R.drawable.image_product)
                .centerCrop()
                .into(binding.ivProductImage)

            // 리뷰 상품 이미지 리스트
//            binding.llReviewPhoto.removeAllViews() // 기존 뷰 제거
//
//            if (review.imageUrls.isNotEmpty()) { // 리뷰 이미지가 있다면
//                review.imageUrls.forEach { imageUrl ->
//                    // 리뷰 이미지 아이템 바인딩
//                    val itemBinding = ItemReviewImageBinding.inflate(
//                        LayoutInflater.from(binding.root.context),
//                        binding.llReviewPhoto,
//                        false
//                    )
//                    // 문자열 리뷰 이미지 로드
//                    Glide.with(itemBinding.root.context)
//                        .load(imageUrl)
//                        .placeholder(R.drawable.image_product2)
//                        .error(R.drawable.image_product)
//                        .centerCrop()
//                        .into(itemBinding.root as ImageView)
//
//                    binding.llReviewPhoto.addView(itemBinding.root) // 컨테이너에 뷰 추가
//
//                    // 이미지 클릭 시 확대
//                    itemBinding.root.setOnClickListener {
//                        val dialog = Dialog(binding.root.context)
//
//                        val density = binding.root.context.resources.displayMetrics.density
//                        val size = (300 * density).toInt() // 300dp를 px로 변환
//
//                        val imageView = ImageView(binding.root.context).apply {
//                            layoutParams = ViewGroup.LayoutParams(size, size)
//                            scaleType = ImageView.ScaleType.FIT_CENTER
//                        }
//
//                        Glide.with(binding.root.context)
//                            .load(imageUrl)
//                            .into(imageView)
//
//                        dialog.setContentView(imageView)
//                        dialog.window?.apply {
//                            setLayout(size, size)
//                            setBackgroundDrawableResource(android.R.color.transparent)
//                        }
//                        dialog.show()
//                    }
//                }
//            } else { // 리뷰 이미지가 없을 때
//                binding.llReviewPhoto.visibility = View.GONE
//            }

        }
    }

    // 뷰 홀더를 만드는 함수
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyReviewViewholder { // inner 클래스로 정의한 뷰 홀더를 객체로 반환
        // item_my_review_card.xml을 inflate (XML -> View 객체로 반환)
        val binding: ItemMyReviewCardBinding = ItemMyReviewCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MyReviewViewholder(binding)
    }

    // 만든 뷰 홀더와 데이터를 묶어주는 함수
    // 스크롤할 때마다 계속 호출
    override fun onBindViewHolder(
        holder: MyReviewViewholder,
        position: Int
    ) {
        // 리뷰 content 리스트에서 리뷰를 한 개 가져옴
        val reviewContent = content[position]

        holder.bind(reviewContent)

        // 삭제 버튼 클릭 이벤트 설정
        holder.btn_deleteReview.setOnClickListener {
            // 클릭 시점에 해당 아이템이 현재 어댑터에서 실제 몇 번째 위치인지 계산 후 반환
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                onDeleteClickListner?.invoke(reviewContent.reviewId, currentPosition)
            }
        }

    }

    override fun getItemCount(): Int {
        return content.size
    }

    // 새로운 아이템 추가 함수 (무한 스크롤용)
    fun addItems(newItems: List<ReviewsGetMyReviewsResponse.Content>) {
        val startPositions = content.size // 현재 리스트 크기 저장
        content.addAll(newItems) // 새 데이터 추가
        notifyItemRangeInserted(startPositions, newItems.size) // 리사이클러뷰에 데이터 변경을 알리는 함수
    }

    // 아이템 삭제 함수 (삭제 버튼용)
    fun removeItemById(reviewId: Int) {
        val index = content.indexOfFirst { it.reviewId == reviewId }
        if (index != -1) {
            content.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}
