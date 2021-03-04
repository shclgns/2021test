package com.dj.baeminpractice.ui.a_home

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentViewHolder
import androidx.viewpager2.widget.ViewPager2
import com.dj.baeminpractice.R
import com.dj.baeminpractice.model.BannerItem
import com.dj.baeminpractice.model.GridItem
import com.dj.baeminpractice.ui.EventActivity
import com.dj.baeminpractice.ui.MapsActivity
import com.dj.baeminpractice.ui.collapse
import com.dj.baeminpractice.ui.expand
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.security.Provider

class HomeFragment : Fragment(R.layout.fragment_home), Interaction {

    private lateinit var gridRecyclerViewAdapter: GridRecyclerViewAdapter
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private val homeViewModel: HomeViewModel by viewModels()

    // 위치정보
    var locationManager: LocationManager? =null
    private val REQUEST_CODE_LOACTION:Int =2
    var currentLocation : String = ""
    var latitude:Double? =null
    var longitude:Double? =null
    var title:String? =null




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_see_detail.setOnClickListener(this)
        iv_arrow.setOnClickListener(this)

        initViewPager2()
        initGridRecyclerView()
        autoScrollViewPager()
        subscribeObservers()
    }

    private fun subscribeObservers() {
        homeViewModel.bannerItemList.observe(viewLifecycleOwner, Observer {
            viewPagerAdapter.submitList(it)
        })
        homeViewModel.gridItemList.observe(viewLifecycleOwner, Observer {
            gridRecyclerViewAdapter.submitList(it)
        })
        homeViewModel.currentPosition.observe(viewLifecycleOwner, Observer {
            viewPager2.currentItem = it
        })
    }

    private fun autoScrollViewPager() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            while (viewLifecycleOwner.lifecycleScope.isActive) {
                delay(3000)
                homeViewModel.getCurrentPosition()?.let {
                    homeViewModel.setCurrentPosition(it.plus(1) % 5)
                }
            }
        }
    }

    private fun initGridRecyclerView() {
        gridRecyclerView.apply {
            gridRecyclerViewAdapter = GridRecyclerViewAdapter(this@HomeFragment)
            layoutManager = GridLayoutManager(this@HomeFragment.context, 4)
            adapter = gridRecyclerViewAdapter

        }
    }

    private fun initViewPager2() {
        viewPager2.apply {
            viewPagerAdapter = ViewPagerAdapter(this@HomeFragment)
            adapter = viewPagerAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    tv_page_number.text = "${position + 1}"
                    homeViewModel.setCurrentPosition(position)
                }
            })
        }
    }

    override fun onBannerItemClicked(bannerItem: BannerItem) {
        startActivity(Intent(requireContext(), MapsActivity::class.java))
    }

    override fun onGridItemClicked(gridItem: GridItem) {
        startActivity(Intent(this@HomeFragment.context, EventActivity::class.java))
    }

    override fun onClick(v: View?) {
        v?.let {
            when (it.id) {
                R.id.tv_see_detail, R.id.iv_arrow -> { // 자세히보기 클릭
                    if (ll_detail.visibility == View.GONE) {
                        ll_detail.expand(nested_scroll_view)
                        tv_see_detail.text = "닫기"
                        iv_arrow.setImageResource(R.drawable.arrow_up)
                    } else {
                        ll_detail.collapse()
                        tv_see_detail.text = "자세히보기"
                        iv_arrow.setImageResource(R.drawable.arrow_down)
                    }
                }
                //다른걸 클릭했을때는 여기서 처리
                R.id.tv_address,R.id.tv_adr_btn->{ // 주소클릭

                    
                }
                
            }
        }
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.getBannerItems()
        homeViewModel.getGridItems()
    }
}