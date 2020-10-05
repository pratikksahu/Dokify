package com.pratiksahu.dokify.homepage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.pratiksahu.dokify.R
import com.pratiksahu.dokify.databinding.HomePageFragmentBinding
import com.pratiksahu.dokify.homepage.adapter.HomePageItemAdapter
import com.pratiksahu.dokify.model.CardData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.home_page_fragment.*

@AndroidEntryPoint
class HomePageActivity : Fragment(R.layout.home_page_fragment) {

    lateinit var binding: HomePageFragmentBinding

    private var homePageItemAdapter: HomePageItemAdapter? = null

    private val navController by lazy { NavHostFragment.findNavController(this) }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HomePageFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()
    }


    fun initAdapter() {
        homePageItemAdapter = HomePageItemAdapter(
            listOf(
                CardData(getString(R.string.createPdf), R.drawable.ic_button_pdf),
                CardData(getString(R.string.viewPdf), R.drawable.ic_button_folder),
                CardData(getString(R.string.imageToText), R.drawable.ic_image_text),
                CardData(getString(R.string.appSetting), R.drawable.ic_button_settings)
            )

        ) { view, pos, item ->
            when (item!!.cardName) {
                getString(R.string.createPdf) -> navController.navigate(R.id.action_landingPage_to_createPdfFragment)
                getString(R.string.viewPdf) -> navController.navigate(R.id.action_landingPage_to_viewPdfFragment)
                getString(R.string.appSetting) -> navController.navigate(R.id.action_landingPage_to_appSetting)
            }

        }

        dashboardCards.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            itemAnimator = DefaultItemAnimator()
            adapter = homePageItemAdapter
        }

    }
}