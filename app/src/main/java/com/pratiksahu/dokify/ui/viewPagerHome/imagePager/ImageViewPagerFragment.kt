package com.pratiksahu.dokify.ui.viewPagerHome.imagePager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.pratiksahu.dokify.R
import com.pratiksahu.dokify.databinding.CommonViewPagerBinding
import com.pratiksahu.dokify.model.DocInfo
import com.pratiksahu.dokify.ui.recyclerViewAdapter.ImportedImagesAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.common_view_pager.*
import javax.inject.Inject


@AndroidEntryPoint
class ImageViewPagerFragment : Fragment(R.layout.common_view_pager) {

    @Inject
    lateinit var imagePagerViewModel: ImagePagerViewModel
    lateinit var binding: CommonViewPagerBinding

    private val imageList = ArrayList<DocInfo>()


    private val navController by lazy { NavHostFragment.findNavController(this) }

    //Adapter
    private var importedImagesAdapter: ImportedImagesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setObservables()
        binding = CommonViewPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initDocsAdapter()
    }

    fun setObservables() {
        imagePagerViewModel.getLiveData().observe(viewLifecycleOwner, Observer {
            imageList.addAll(it)
            importedImagesAdapter?.items = imageList
        }
        )
    }

    fun initDocsAdapter() {
        importedImagesAdapter = ImportedImagesAdapter(
            imageList
        ) { _, _, dockItem ->

            if (dockItem != null) {
                imagePagerViewModel.setSelectedImage(dockItem)
                navController.navigate(R.id.action_landingPage_to_crop_or_convert_dialog)
            }

        }
        importedocks.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            itemAnimator = DefaultItemAnimator()
            adapter = importedImagesAdapter
        }
    }

    fun ToastMessage(msg: String) {
        println("Testing ${msg}")
        Toast.makeText(requireContext(), "Testing : ${msg}", Toast.LENGTH_SHORT).show()
    }
}