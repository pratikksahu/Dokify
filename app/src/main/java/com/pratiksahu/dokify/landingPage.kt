package com.pratiksahu.dokify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.pratiksahu.dokify.databinding.LandingPageFragmentBinding
import com.pratiksahu.dokify.ui.viewPagerHome.viewPagerAdapter.ImageAndPdfAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.landing_page_fragment.*

@AndroidEntryPoint
class landingPage : Fragment(R.layout.landing_page_fragment) {

    private val navController by lazy { NavHostFragment.findNavController(this) }

    private val mainActivityViewModel: MainActivityViewModel by viewModels()
    lateinit var binding: LandingPageFragmentBinding
    private var recentAndHistoryAdapter: ImageAndPdfAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LandingPageFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AddFilesButtonSetup()
        initViewPager()
    }

    fun AddFilesButtonSetup() {
        addFiles.setOnClickListener(null)
        addFiles.setOnClickListener {
            navController.navigate(R.id.action_landingPage_to_add_files_popup)
        }
    }

    fun initViewPager() {
        recentAndHistoryAdapter = ImageAndPdfAdapter(
            requireParentFragment()
        )
        binding.recentWithHistory.adapter = recentAndHistoryAdapter

        binding.recentWithHistory.isUserInputEnabled = true

        TabLayoutMediator(TabLayoutRecentHistory, recentWithHistory) { tab, position ->
            recentWithHistory.setCurrentItem(tab.position, true)
            when (position) {
                0 -> {
                    tab.text = "Images"
                }
                1 -> {
                    tab.text = "PDF"
                }
            }
        }.attach()

        TabLayoutRecentHistory.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.view?.elevation = 10F
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.view?.elevation = 0F
            }

        })
    }
}