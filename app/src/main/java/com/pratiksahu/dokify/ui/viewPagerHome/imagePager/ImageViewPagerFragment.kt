package com.pratiksahu.dokify.ui.viewPagerHome.imagePager

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.pratiksahu.dokify.R
import com.pratiksahu.dokify.`interface`.ToBlackWhite
import com.pratiksahu.dokify.databinding.CommonViewPagerBinding
import com.pratiksahu.dokify.model.DocInfo
import com.pratiksahu.dokify.ui.recyclerViewAdapter.ImportedImagesAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.common_view_pager.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject


@AndroidEntryPoint
class ImageViewPagerFragment : Fragment(R.layout.common_view_pager) {

    private val TAG_DELETE = "IMAGE_DELETE"
    private val TAG_IMAGE_LIST = "IMAGE_LIST"

    @Inject
    lateinit var imagePagerViewModel: ImagePagerViewModel

    lateinit var binding: CommonViewPagerBinding


    private val imageList = ArrayList<DocInfo>()  //Source of images from directory
    private val selectedItems = ArrayList<Int>() // Storing index of items for checkbox state
    private val selectedItemsImage =
        ArrayList<Uri>()  //Storing uri of images for deletion and conversion

    private val navController by lazy { NavHostFragment.findNavController(this) }

    //Adapter
    private var importedImagesAdapter: ImportedImagesAdapter? = null

    lateinit var progressCircle: CircularProgressDrawable

    lateinit var currentPhotoPath: String
    lateinit var photoURI: Uri
    private var toBlackWhite = ToBlackWhite()

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
        createPdfDirectroy()

        actionsTab.layoutTransition.setAnimateParentHierarchy(false)
        progressCircle = CircularProgressDrawable(requireContext())
        progressCircle.strokeWidth = 5f
        progressCircle.centerRadius = 30f
        progressCircle.start()


        hideActionsTabView()
        pdfButtonListener()
        setupSelectAllCheckBoxListener()
        setupCancelButtonListener()
        setupDeleteImageButtonListener()
        initDocsAdapter()
    }

    fun createTempDirectory() {
        //Temporary directory
        var path = "/storage/emulated/0/Android/data/com.pratiksahu.dokify/files/TMP"
        var directory = File(path)
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


    fun setObservables() {
        imagePagerViewModel.setIsConverted((false))
        imagePagerViewModel.isConverted.observe(viewLifecycleOwner, Observer {
            if (it)
                cancelSelectionButton.performClick()
        })
        imagePagerViewModel.loading.observe(viewLifecycleOwner, Observer {
            Log.d(TAG_IMAGE_LIST, "LOADING IMAGES STATUS :" + it.toString())
            if (it) {
                actionsTab.visibility = GONE
                importedocks.visibility = GONE
                loadingData.visibility = VISIBLE
                waitMessage.visibility = VISIBLE
            } else {
                actionsTab.visibility = VISIBLE
                loadingData.visibility = GONE
                waitMessage.visibility = GONE
                importedocks.visibility = VISIBLE
            }
        })
        imagePagerViewModel.initTempImages()
        imagePagerViewModel.initImages()

        imagePagerViewModel.isEmpty.observe(viewLifecycleOwner, Observer { emptyFolder ->
            if (emptyFolder)
                guideText.text = getString(R.string.emptyFolderMessage)
            else
                guideText.text = getString(R.string.notEmptyFolderMessage)
        })
        imagePagerViewModel.imagesInFolder.observe(viewLifecycleOwner, Observer {
            imageList.clear()
            imageList.addAll(it)
            Log.d(TAG_IMAGE_LIST, imageList.toString())
            importedImagesAdapter?.items = imageList
        }
        )

    }

    fun initDocsAdapter() {

        CoroutineScope(Main).launch {
            importedImagesAdapter = ImportedImagesAdapter(
                imageList,
                progressCircle,
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
                        if (selectAllCheckBox.isChecked)
                            selectAllCheckBox.isChecked = false
                        if (selectedItems.contains(pos))
                            selectedItems.remove(pos)
                        if (selectedItemsImage.contains(item!!.imageUri))
                            selectedItemsImage.remove(item.imageUri)
                    }
                    importedImagesAdapter?.setSelectedItems(selectedItems)
                }
            ) { view, pos, dockItem ->

                //LONG PRESS
                showActionsTabView()
                flagForSelection = 1
                importedImagesAdapter?.setIsLongClicked(true)

            }
            importedocks.apply {
                layoutManager = GridLayoutManager(requireContext(), 2)
                itemAnimator = DefaultItemAnimator()
                adapter = importedImagesAdapter
            }
        }

    }

    fun pdfButtonListener() {
        pdfDialog.setOnClickListener {
            imagePagerViewModel.setImagesToConvert(selectedItemsImage)
            CoroutineScope(Main).launch {
                createTempDirectory()
                importedocks.visibility = GONE
                waitMessage.text = "Please Wait"
                loadingData.visibility = VISIBLE
                waitMessage.visibility = VISIBLE
            }
            val task = CoroutineScope(IO).launch {

                for (i in selectedItemsImage.indices) {
                    createTempFile("_${i}", "TEMP", ".jpg")
                    blackAndWhite(selectedItemsImage[i])
                }
            }
            task.invokeOnCompletion {
                CoroutineScope(Main).launch {
                    importedocks.visibility = VISIBLE
                    waitMessage.visibility = GONE
                    loadingData.visibility = GONE
                    navController.navigate(R.id.action_landingPage_to_convert_to_pdf_dialog)
                }
            }

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
            hideActionsTabView()
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
            selectedItemsImage.clear()
            importedImagesAdapter?.setSelectedItems(selectedItems)
            hideActionsTabView()
            selectAllCheckBox.isChecked = false
            imagePagerViewModel.setIsConverted(false)
        }
    }

    fun setupSelectAllCheckBoxListener() {
        selectAllCheckBox.setOnCheckedChangeListener(null)
        selectAllCheckBox.isChecked = false
        selectAllCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                selectAllCheckBox.text = "Unselect All"
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
                selectAllCheckBox.text = "Select All"
                selectedItemsImage.clear()
                selectedItems.clear()
                importedImagesAdapter?.setSelectedItems(selectedItems)
            }
        }
        selectAllCheckBox.isChecked = false
    }

    fun hideActionsTabView() {
        //Hide buttons
        selectAllCheckBox.visibility = GONE
        deleteFileButton.visibility = GONE
        pdfDialog.visibility = GONE
        shareMultiple.visibility = GONE
        cancelSelectionButton.visibility = GONE

        //Show guide text
        guideText.visibility = VISIBLE

    }

    fun showActionsTabView() {
        //Hide buttons
        selectAllCheckBox.visibility = VISIBLE
        deleteFileButton.visibility = VISIBLE
        pdfDialog.visibility = VISIBLE
        shareMultiple.visibility = GONE
        cancelSelectionButton.visibility = VISIBLE

        //Hide guide text
        guideText.visibility = GONE

    }

    fun createTempFile(fileName: String, prefix: String, suffix: String) {
        val name = prefix + fileName + suffix
        File("/storage/emulated/0/Android/data/com.pratiksahu.dokify/files/TMP/$name").apply {
            currentPhotoPath = absolutePath
        }.createNewFile().also {
            photoURI = FileProvider.getUriForFile(
                requireContext(),
                "com.pratiksahu.android.fileprovider",
                File(currentPhotoPath)
            )
        }
    }

    fun blackAndWhite(uri: Uri) {
        //getting bitmap
        val result = toBlackWhite.convertToBW(requireContext(), uri)
        //creating outputStream to store grayscaled version
        val opstream = FileOutputStream(currentPhotoPath)

        //creating byteoutputstream
        val btopstream = ByteArrayOutputStream(1024)
        result?.compress(Bitmap.CompressFormat.JPEG, 80, btopstream)
        opstream.write(btopstream.toByteArray())
        opstream.flush()
        opstream.close()
    }


    fun ToastMessage(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
    }
}