package com.pratiksahu.dokify.ui.viewPagerHome.imagePager

import android.net.Uri
import android.os.Bundle
import android.transition.Fade
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
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
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class ImageViewPagerFragment : Fragment(R.layout.common_view_pager) {

    private val TAG_DELETE = "IMAGE_DELETE"
    private val TAG_IMAGE_LIST = "IMAGE_LIST"

    @Inject
    lateinit var imagePagerViewModel: ImagePagerViewModel
    lateinit var binding: CommonViewPagerBinding


    private val imageList = ArrayList<DocInfo>()
    private val selectedItems = ArrayList<Int>()
    private val selectedItemsImage = ArrayList<Uri>()

    private val navController by lazy { NavHostFragment.findNavController(this) }

    //Adapter
    private var importedImagesAdapter: ImportedImagesAdapter? = null


    var flagForSelection = 0

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
        val transition = Fade()
        transition.duration = 400
        transition.addTarget(R.id.actionsTab)
        TransitionManager.beginDelayedTransition(binding.actionsTab, transition)
        actionsTab.visibility = GONE
        setupSelectAllCheckBoxListener()
        setupCancelButtonListener()
        setupDeleteImageButtonListener()
        initDocsAdapter()
    }

    fun setObservables() {
        imagePagerViewModel.loading.observe(viewLifecycleOwner, Observer {
            Log.d(TAG_IMAGE_LIST, "LOADING IMAGES STATUS :" + it.toString())
        })
        imagePagerViewModel.initImages()
        imagePagerViewModel.imagesInFolder.observe(viewLifecycleOwner, Observer {
            imageList.clear()
            imageList.addAll(it)
            Log.d(TAG_IMAGE_LIST, imageList.toString())
            importedImagesAdapter?.items = imageList
        }
        )
    }

    fun initDocsAdapter() {
        importedImagesAdapter = ImportedImagesAdapter(
            imageList,
            { view, pos, dockItem ->

                //IMAGE CLICKED OR NOT
                if (flagForSelection == 0) {
                    if (dockItem != null) {
                        imagePagerViewModel.setSelectedImage(dockItem)
                        navController.navigate(R.id.action_landingPage_to_crop_or_convert_dialog)
                    }
                }
                //IMAGE CLICKED FOR CHECKBOX ACTION OR NOT
                if (flagForSelection == 1) {
                    if (!selectedItems.contains(pos)) {
                        selectedItems.add(pos)
                        if (!selectedItemsImage.contains(dockItem!!.imageUri)) {
                            selectedItemsImage.add(dockItem.imageUri)
                        }
                    } else {
                        if (selectedItems.contains(pos))
                            selectedItems.remove(pos)
                        if (selectedItemsImage.contains(dockItem!!.imageUri))
                            selectedItemsImage.remove(dockItem.imageUri)
                    }
                    importedImagesAdapter?.setSelectedItems(selectedItems)
                }
            },
            { item, pos, isChecked ->
                //CHECKBOX CHECKED OR NOT
                if (isChecked) {
                    selectedItems.add(pos)
                    selectedItemsImage.add(item!!.imageUri)
                } else {
                    if (selectedItems.contains(pos))
                        selectedItems.remove(pos)
                    if (selectedItemsImage.contains(item!!.imageUri))
                        selectedItemsImage.remove(item.imageUri)
                }
                importedImagesAdapter?.setSelectedItems(selectedItems)
            }
        ) { view, pos, dockItem ->

            //LONG PRESS
            actionsTab.visibility = VISIBLE
            flagForSelection = 1
            importedImagesAdapter?.setIsLongClicked(true)

        }
        importedocks.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            itemAnimator = DefaultItemAnimator()
            adapter = importedImagesAdapter
        }

    }

    fun setupDeleteImageButtonListener() {
        deleteFileButton.setOnClickListener {
            if (selectedItemsImage.size > 0)
                selectedItemsImage.forEach {
                    File(it.path).delete()
                        .let { result ->
                            Log.d(TAG_DELETE, it.path + " <-- $result")
                        }
                }
            flagForSelection = 0
            actionsTab.visibility = GONE
            selectAllCheckBox.isChecked = false
            selectedItemsImage.clear()
            selectedItems.clear()
            importedImagesAdapter?.setIsLongClicked(false)
            importedImagesAdapter?.setSelectedItems(selectedItems)
            imageList.clear()
            imagePagerViewModel.initImages()
            importedImagesAdapter?.items = imageList
        }
    }

    fun setupCancelButtonListener() {
        cancelSelectionButton.setOnClickListener(null)
        cancelSelectionButton.setOnClickListener {
            flagForSelection = 0
            importedImagesAdapter?.setIsLongClicked(false)
            selectedItems.clear()
            importedImagesAdapter?.setSelectedItems(selectedItems)
            actionsTab.visibility = GONE
            selectAllCheckBox.isChecked = false
        }
    }

    fun setupSelectAllCheckBoxListener() {
        selectAllCheckBox.setOnCheckedChangeListener(null)
        selectAllCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                //Storing Uri
                imageList.forEach {
                    selectedItemsImage.add(it.imageUri)
                }
                //Storing index
                selectedItems.clear()
                imageList.forEachIndexed { index, _ ->
                    selectedItems.add(index)
                }
                importedImagesAdapter?.setSelectedItems(selectedItems)
            } else {
                selectedItemsImage.clear()
                selectedItems.clear()
                importedImagesAdapter?.setSelectedItems(selectedItems)
            }
        }
        selectAllCheckBox.isChecked = false
    }

    fun ToastMessage(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
    }
}