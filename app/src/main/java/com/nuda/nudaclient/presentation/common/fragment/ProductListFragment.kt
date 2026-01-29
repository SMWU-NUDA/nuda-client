package com.nuda.nudaclient.presentation.common.fragment
//
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.nuda.nudaclient.R
//import com.nuda.nudaclient.databinding.FragmentProductListBinding
//import com.nuda.nudaclient.presentation.common.adapter.ProductAdapter
//
//// 상품 목록 Fragment (재사용 가능)
//class ProductListFragment : Fragment() {
//
//    // 뷰바인딩
//    private lateinit var binding: FragmentProductListBinding
//
//    // 어댑터 설정
//    private lateinit var productAdapter: ProductAdapter
//
//    // 순위 표시 여부 (newInstance로 전달받음)
//    private var showRank: Boolean = false
//
//    // Fragment 생성 시점
//    // arguments에서 순위 표시 여부를 받아옴
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        showRank = arguments?.getBoolean(ARG_SHOW_RANK, false) ?: false
//    }
//
//    // Fragment의 View 생성
//    // 뷰바인딩을 사용하여 레이아웃 inflate
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        binding = FragmentProductListBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    // View가 생성된 직후
//    // 리사이클러뷰 설정 등 초기화 작업 수행
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        setupRecyclerView()
//    }
//
//    // 리사이클러뷰 초기 설정
//    private fun setupRecyclerView() {
//        // 1. 어댑터 설정
//        productAdapter = ProductAdapter(
//            showRank = showRank,
//            onItemClick = { product ->
//                // 아이템 클릭 시 처리
//                onProductClicked(product)
//            })
//
//        // 2. 리사이클러뷰 설정
//        binding.rvProductList.apply {
//            adapter = productAdapter
//            layoutManager = LinearLayoutManager(context) // 세로 스크롤 리스트
//
//            // 스크롤 성능 최적화 (아이템 크기가 고정일 때)
//            setHasFixedSize(true)
//        }
//    }
//
//    /**
//     * 상품 목록 업데이트
//     * Activity나 다른 곳에서 이 메서드를 호출해서 데이터 전달
//     */
//    fun updateProducts(products: List<Product>) {
//        productAdapter.submitList(products) // ListAdapter의 메서드 submitList
//    }
//
//    /**
//     * 상품 아이템 클릭 처리
//     * 필요 시 Activity로 이벤트 전달 가능
//     */
//    private fun onProductClicked(product: Product) {
//        // 상품 상세페이지로 이동(상품 랭킹, 맞춤 추천)
//        // 검색 화면에서는 이동x 구현
//    }
//
//    // View가 파괴될 때
//    // 메모리 누수 방지를 위해 binding을 null로 설정 ?
//    override fun onDestroy() {
//        super.onDestroy()
////        binding = null
//    }
//
//    companion object {
//        // arguments key
//        private const val ARG_SHOW_RANK = "show_rank"
//
//        /**
//         * Fragment 인스턴스 생성 (Factory 패턴)
//         * @param showRank true면 순위 있는 목록, false면 순위 없는 목록
//         * @return ProductListFragment 인스턴스
//         */
//        fun newInstance(showRank: Boolean): ProductListFragment {
//            return ProductListFragment().apply {
//                arguments = Bundle().apply {
//                    putBoolean(ARG_SHOW_RANK, showRank)
//                }
//            }
//        }
//    }
//
//
//}