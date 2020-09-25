package com.pratiksahu.dokify.ui.viewPagerHome.viewPagerAdapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pratiksahu.dokify.ui.viewPagerHome.imagePager.ImageViewPagerFragment
import com.pratiksahu.dokify.ui.viewPagerHome.pdfPager.PdfViewPagerFragment

class ImageAndPdfAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {

        when (position) {
            0 ->
                return ImageViewPagerFragment()
            1 ->
                return PdfViewPagerFragment()
        }
        return ImageViewPagerFragment()
    }
}