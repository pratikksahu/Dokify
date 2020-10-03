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
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.*
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.pratiksahu.dokify.MainActivityViewModel
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
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@AndroidEntryPoint
class ImageViewPagerFragment : Fragment(R.layout.common_view_pager) {

    private val TAG_DELETE = "IMAGE_DELETE"
    private val TAG_IMAGE_LIST = "IMAGE_LIST"

    @Inject
    lateinit var imagePagerViewModel: ImagePagerViewModel

    @Inject
    lateinit var mainActivityViewModel: MainActivityViewModel

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
    var flagForRearrange = 0


    //Drag and drop handler
    val callback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.START or ItemTouchHelper.END,
        0
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val fromPosition = viewHolder.adapterPosition
            val targetPosition = target.adapterPosition

            Collections.swap(imageList, fromPosition, targetPosition)

            recyclerView.adapter!!.notifyItemMoved(fromPosition, targetPosition)
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            TODO("Not yet implemented")
        }
    }
    val helper = ItemTouchHelper(callback)

    //

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

        importedDocksPdf.visibility = GONE
        hideActionsTabView()
        rearrangeButton.visibility = GONE
        moreOptions.visibility = GONE
        guideText.visibility = VISIBLE
        pdfButtonListener()
        setupSelectAllCheckBoxListener()
        setupCancelButtonListener()
        setupRearrangeButton()
        setupmoreOptionsListener()
        setupDeleteImageButtonListener()
        setupDeleteDialogListener()
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
            if (emptyFolder) {
                rearrangeButton.visibility = GONE
                moreOptions.visibility = GONE
                guideText.text = getString(R.string.emptyFolderMessage)
                guideText.visibility = VISIBLE
            } else {
                guideText.visibility = GONE
                moreOptions.visibility = VISIBLE
                rearrangeButton.visibility = VISIBLE
            }
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
                        if (dockItem != null && flagForRearrange == 0) {
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
            )
            importedocks.apply {
                layoutManager = GridLayoutManager(requireContext(), 2)
                itemAnimator = DefaultItemAnimator()
                adapter = importedImagesAdapter
            }
        }

    }


    fun setupmoreOptionsListener() {
        moreOptions.setOnClickListener {
            if (imageList.isNotEmpty()) {
                helper.attachToRecyclerView(null)
                mainActivityViewModel.setAddFilesButtonShow(false)
                showActionsTabView()
                flagForSelection = 1
                importedImagesAdapter?.setIsLongClicked(true)
            }
        }
    }

    fun pdfButtonListener() {
        pdfDialog.setOnClickListener {
            if (selectedItemsImage.size > 0) {
                imagePagerViewModel.setImagesToConvert(selectedItemsImage)
                CoroutineScope(Main).launch {
                    requireActivity().window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                    createTempDirectory()
                    importedocks.visibility = GONE
                    waitMessage.text = "Please Wait"
                    loadingData.visibility = VISIBLE
                    waitMessage.visibility = VISIBLE

                }
                val deleteTask = CoroutineScope(IO).launch {
                    val path = "/storage/emulated/0/Android/data/com.pratiksahu.dokify/files/TMP"
                    val directory = File(path)
                    if (!directory.exists())
                        directory.mkdir()
                    else {
                        val del = directory.listFiles()
                        if (del != null)
                            del.forEach {
                                File(it.path).delete()
                            }
                    }
                }
                val task = CoroutineScope(IO).launch {
                    for (i in selectedItemsImage.indices) {
                        createTempFile("_${i}", "TEMP", ".jpg")
                        blackAndWhite(selectedItemsImage[i])
                    }
                }
                deleteTask.invokeOnCompletion {
                    task.invokeOnCompletion {
                        CoroutineScope(Main).launch {
                            importedocks.visibility = VISIBLE
                            waitMessage.visibility = GONE
                            loadingData.visibility = GONE
                            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                            navController.navigate(R.id.action_landingPage_to_convert_to_pdf_dialog)
                        }
                    }
                }
            } else
                ToastMessage("Select atleast one image")
        }
    }

    fun setupRearrangeButton() {
        rearrangeButton.setOnClickListener {
            if (flagForRearrange == 0) {
                mainActivityViewModel.setAddFilesButtonShow(false)
                flagForRearrange = 1
                rearrangeButton.text = "DONE"
                importedocks.layoutManager = LinearLayoutManager(requireContext())
                helper.attachToRecyclerView(importedocks)
                moreOptions.visibility = GONE
            } else if (flagForRearrange == 1) {
                mainActivityViewModel.setAddFilesButtonShow(true)
                flagForRearrange = 0
                rearrangeButton.text = "REARRANGE"
                importedocks.layoutManager = GridLayoutManager(requireContext(), 2)
                helper.attachToRecyclerView(null)
                moreOptions.visibility = VISIBLE
            }
        }
    }

    var deleteFlag = 0

    fun setupDeleteImageButtonListener() {
        deleteFileButton.setOnClickListener {
            if (selectedItemsImage.size > 0) {
                deleteFlag = 1
                imagePagerViewModel.setImageDelete(true, selectedItemsImage)
                navController.navigate(R.id.action_landingPage_to_delete_confirmation_dialog)
            } else
                ToastMessage("No items to delete")

        }
    }


    fun setupDeleteDialogListener() {
        imagePagerViewModel.imageDelete.observe(viewLifecycleOwner, Observer {
            if (!it) {
                if (deleteFlag == 1) {
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
                    deleteFlag = 0
                }
            }
        })
    }

    fun setupCancelButtonListener() {
        cancelSelectionButton.setOnClickListener(null)
        cancelSelectionButton.setOnClickListener {
            mainActivityViewModel.setAddFilesButtonShow(true)
            CoroutineScope(IO).launch {
                val path = "/storage/emulated/0/Android/data/com.pratiksahu.dokify/files/TMP"
                val directory = File(path)
                if (!directory.exists())
                    directory.mkdir()
                else {
                    val del = directory.listFiles()
                    if (del != null)
                        del.forEach {
                            File(it.path).delete()
                        }
                }
            }
            helper.attachToRecyclerView(null)
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
        guideText.visibility = GONE
        rearrangeButton.visibility = VISIBLE
        moreOptions.visibility = VISIBLE
    }

    fun showActionsTabView() {
        //Hide buttons
        selectAllCheckBox.visibility = VISIBLE
        deleteFileButton.visibility = VISIBLE
        pdfDialog.visibility = VISIBLE
        shareMultiple.visibility = GONE
        cancelSelectionButton.visibility = VISIBLE

        //Hide guide text and Rearrange button
        guideText.visibility = GONE
        rearrangeButton.visibility = GONE
        moreOptions.visibility = GONE
    }

    fun createTempFile(fileName: String, prefix: String, suffix: String) {
        val name = prefix + fileName + suffix
        val path = "/storage/emulated/0/Android/data/com.pratiksahu.dokify/files/TMP/"
        File(path).mkdir()
        File("$path$name").apply {
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
        result?.compress(Bitmap.CompressFormat.JPEG, 100, btopstream)
        opstream.write(btopstream.toByteArray())
        opstream.flush()
        opstream.close()
    }


    fun ToastMessage(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
    }
}