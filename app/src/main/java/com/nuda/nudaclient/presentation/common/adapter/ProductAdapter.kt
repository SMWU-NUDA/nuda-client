package com.nuda.nudaclient.presentation.common.adapter
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import com.nuda.nudaclient.databinding.ItemProductCardBinding
//import com.nuda.nudaclient.databinding.ItemProductCardRankingBinding
//
///**
// * 상품 목록을 보여주는 RecyclerView의 어댑터
// * @param showRank가 true면 순위 있는 아이템, false면 순위 없는 아이템 사용
// */
//
//class ProductAdapter(
//    private val showRank: Boolean,// 순위 표시 여부
//    private val onItemClick: (Product) -> Unit = {} // 클릭 리스너 (기본값 = 빈 함수)
//) : ListAdapter<Product, RecyclerView.ViewHolder>(ProductDiffCallback()) {
//
//    // ViewType 상수 정의
//    private val VIEW_TYPE_RANKED = 1 // 순위 있는 뷰
//    private val VIEW_TYPE_UNRANKED = 2 // 순위 없는 뷰
//
//    /**
//     * 어떤 ViewType을 사용할지 결정 (showRank에 따라)
//     * 리사이클러뷰가 각 아이템을 그릴 때마다 자동 호출
//     * 리턴값을 onCreateViewHolder()로 전달
//     */
//    override fun getItemViewType(position: Int): Int {
//        return if(showRank) VIEW_TYPE_RANKED else VIEW_TYPE_UNRANKED
//    }
//
//    /**
//     * ViewHolder 객체 생성
//     * 화면에 보이는 만큼만 호출
//     * ViewType에 따라 다른 레이아웃을 inflate해서 ViewHolder 생성함
//     */
//    override fun onCreateViewHolder(
//        parent: ViewGroup, // parent: RecyclerView 자체
//        viewType: Int // getItemViewType()에서 리턴한 ViewType 값 (1 혹은 2)
//    ): RecyclerView.ViewHolder {
//        return when(viewType) {
//            VIEW_TYPE_RANKED -> {
//                val binding = ItemProductCardRankingBinding.inflate( // inflate: xml 레이아웃 -> 코드로 변환
//                    LayoutInflater.from(parent.context), // LayoutInflater: xml을 View 객체로 만드는 도구, parent.context: 리사이클러뷰가 속한 context(액티비티나 프래그먼트)
//                    parent, // 부모 ViewGroup -> 여기선 리사이클러뷰
//                    false // attachToRoot=false : 아직 parent에 붙이지 말라는 의미. RecyclerView가 알아서 붙임
//                )
//                ProductWithRankViewHolder(binding, onItemClick)
//            }
//            VIEW_TYPE_UNRANKED -> {
//                val binding = ItemProductCardBinding.inflate( // inflate: xml 레이아웃 -> 코드로 변환
//                    LayoutInflater.from(parent.context), // LayoutInflater: xml을 View 객체로 만드는 도구, parent.context: 리사이클러뷰가 속한 context(액티비티나 프래그먼트)
//                    parent, // 부모 ViewGroup -> 여기선 리사이클러뷰
//                    false // attachToRoot=false : 아직 parent에 붙이지 말라는 의미. RecyclerView가 알아서 붙임
//                )
//                ProductWithoutRankViewHolder(binding, onItemClick)
//            }
//            else -> throw IllegalArgumentException("Invalid view type")
//        }
//    }
//
//    /**
//     * ViewHolder에 데이터 바인딩
//     * position에 해당하는 상품 데이터를 ViewHolder에 전달
//     * 생성된 ViewHolder에 데이터를 연결하는 메서드
//     */
//    override fun onBindViewHolder(
//        holder: RecyclerView.ViewHolder,
//        position: Int
//    ) {
//        val product = getItem(position) // ListAdapter가 제공하는 메서드. 현재 position의 상품 데이터
//
//        when (holder) { // holder 타입을 체크하면서 자동으로 형변환
//            is ProductWithRankViewHolder -> { // holder를 ProductWithRankViewHolder로 자동 캐스팅
//                holder.bind(product, position + 1)
//            }
//            is ProductWithoutRankViewHolder -> {
//                holder.bind(product)
//            }
//        }
//    }
//
//    /**
//     * ViewHolder 클래스
//     * 뷰 홀더는 뷰를 재사용 하기 위한 그릇. 화면에 보이는 만큼의 뷰를 생성하고 이후 그 뷰를 재사용한다.
//     * 뷰를 생성할 때 뷰바인딩으로 뷰들을 미리 찾아서 저장한 뒤 재사용할 때 데이터만 바꿔서 넣는다.
//     */
//
//    // 상품 검색 API 완성되면 그때 다시 수정
//    // API 응답의 상품 데이터 값을 data class로 생성(Product/이후 변경 가능)
//
//
//    // 순위 있는 아이템용 ViewHolder
//    class ProductWithRankViewHolder(
//        private val binding: ItemProductCardRankingBinding, // 뷰바인딩 객체
//        private val onItemClick: (Product) -> Unit // 클릭 리스너
//    ) : RecyclerView.ViewHolder(binding.root) {
//
//        // 데이터를 View에 바인딩
//        fun bind(product: Product, rank: Int) {
//            // 순위 표시
//            binding.tvRank.text = rank.toString()
//
//            // 상품 정보 표시
//            // 상품 이미지 데이터 : binding.ivProduct.setBackgroundResource()
//            binding.tvProductBrand.text = product.brand
//            binding.tvProductName.text = product.name
//            binding.tvProductIngredient.text = product.ingredient
//            binding.tvProductStar.text = product.star
//            binding.tvProductReview.text = product.review
//            binding.tvProductPrice.text = "${product.price}원"
//
//            // 아이템 클릭 리스너 설정
//            binding.root.setOnClickListener {
//                onItemClick(product) // 클릭 시 콜백 호출
//            }
//        }
//    }
//
//    // 순위 없는 아이템용 ViewHolder
//    class ProductWithoutRankViewHolder(
//        private val binding: ItemProductCardBinding, // 뷰바인딩 객체
//        private val onItemClick: (Product) -> Unit // 클릭 리스너
//    ) : RecyclerView.ViewHolder(binding.root) {
//
//        // 데이터를 View에 바인딩
//        fun bind(product: Product) {
//            // 상품 정보 표시
//            // 상품 이미지 데이터 : binding.ivProduct.setBackgroundResource()
//            binding.tvProductBrand.text = product.brand
//            binding.tvProductName.text = product.name
//            binding.tvProductIngredient.text = product.ingredient
//            binding.tvProductStar.text = product.star
//            binding.tvProductReview.text = product.review
//            binding.tvProductPrice.text = "${product.price}원"
//
//            // 아이템 클릭 리스너 설정
//            binding.root.setOnClickListener {
//                onItemClick(product) // 클릭 시 콜백 호출
//            }
//        }
//    }
//}
//
//// ListAdapter가 리스트 변경을 감지하는 방법을 정의
//class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
//
//    // 같은 아이템인지 id로 비교
//    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
//        return oldItem.id == newItem.id
//    }
//
//    // 내용까지 똑같은지 비교. false면 UI 업데이트 필요
//    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
//        return oldItem == newItem
//    }
//}