package com.pratiksahu.dokify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.pratiksahu.dokify.databinding.LandingPageFragmentBinding
import com.pratiksahu.dokify.ui.viewPagerHome.viewPagerAdapter.ImageAndPdfAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.landing_page_fragment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class landingPage : Fragment(R.layout.landing_page_fragment) {

    private val navController by lazy { NavHostFragment.findNavController(this) }

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
        CoroutineScope(IO).launch {
            createPdfDirectroy()
            createTempDirectory()
        }
        AddFilesButtonSetup()
        initViewPager()
    }

    fun createTempDirectory() {
        //Temporary directory
        val path = "/storage/emulated/0/Android/data/com.pratiksahu.dokify/files/TMP"
        val directory = File(path)
        if (directory.exists())
            directory.delete()
        directory.mkdir()
    }

    fun createPdfDirectroy() {
        //PDF directory
        val path = "/storage/emulated/0/Android/data/com.pratiksahu.dokify/files/PDF"
        val directory = File(path)
        if (directory.exists())
            directory.delete()
        directory.mkdir()
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


        TabLayoutMediator(TabLayoutRecentHistory, recentWithHistory) { tab, position ->
            recentWithHistory.setCurrentItem(tab.position, true)
            when (position) {
                0 -> {
                    tab.text = "Image To PDF"
                }
                1 -> {
                    tab.text = "Recent PDF's"
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