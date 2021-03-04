package com.dj.baeminpractice.ui.a_home

import android.view.View
import com.dj.baeminpractice.model.BannerItem
import com.dj.baeminpractice.model.GridItem

interface Interaction: View.OnClickListener {
    fun onBannerItemClicked(bannerItem: BannerItem)
    fun onGridItemClicked(gridItem:GridItem)
}