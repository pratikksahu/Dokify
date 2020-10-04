package com.pratiksahu.dokify.ui.viewPagerHome.pdfPager

import android.content.ActivityNotFoundException
import android.content.Intent
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.pratiksahu.dokify.R
import com.pratiksahu.dokify.databinding.ViewPdfFragmentBinding
import com.pratiksahu.dokify.model.DocInfo
import com.pratiksahu.dokify.ui.recyclerViewAdapter.ImportedPdfsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.view_pdf_fragment.*
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class PdfViewPagerFragment : Fragment(R.layout.view_pdf_fragment) {

    val TAG_PDF_LIST = "PDF_LOADING"
    val TAG_SHARE = "PDF_SHARED"
    val TAG_OPEN = "PDF_OPEN"

    @Inject
    lateinit var pdfViewPagerFragmentViewModel: PdfViewPagerFragmentViewModel
    var importedPdfsAdapter: ImportedPdfsAdapter? = null

    lateinit var binding: ViewPdfFragmentBinding

    private val navController by lazy { NavHostFragment.findNavController(this) }

    val pdfList = ArrayList<DocInfo>()

    lateinit var progressCircle: CircularProgressDrawable
    var flagForSelection = 0
    val selectedItems = ArrayList<Int>()
    val selectedItemsPdf = ArrayList<Uri>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ViewPdfFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        actionsTab.layoutTransition.setAnimateParentHierarchy(false)
        progressCircle = CircularProgressDrawable(requireContext())
        progressCircle.strokeWidth = 5f
        progressCircle.centerRadius = 30f
        progressCircle.start()

        hideActionsTabView()
        setupSelectAllCheckBoxListener()
        setupShareMultiple()
        setupCancelButtonListener()
        setupDeletePdfButtonListener()
        setupDeleteDialogResponseListener()
        setupObservers()
        initAdapter()
    }

    fun setupObservers() {
        pdfViewPagerFragmentViewModel.initPdf()

        pdfViewPagerFragmentViewModel.isEmpty.observe(viewLifecycleOwner, Observer {
            if (it) {
                guideText.text = "No pdf found"
            } else {
                guideText.text = "Hold pdf for more options"
            }
        })
        pdfViewPagerFragmentViewModel.loading.observe(viewLifecycleOwner, Observer {
            Log.d(TAG_PDF_LIST, "LOADING PDF STATUS :$it")
            if (it) {
                importedDocksPdf.visibility = GONE
                loadingData.visibility = VISIBLE
                waitMessage.visibility = VISIBLE
            } else {
                loadingData.visibility = GONE
                importedDocksPdf.visibility = View.VISIBLE
                waitMessage.visibility = GONE
            }
        })

        pdfViewPagerFragmentViewModel.pdfInFolder.observe(viewLifecycleOwner, Observer {
            pdfList.clear()
            pdfList.addAll(it)
            Log.d(TAG_PDF_LIST, it.toString())
            importedPdfsAdapter?.items = pdfList
        })
    }

    fun setupShareMultiple() {
        shareMultiple.setOnClickListener {
            if (selectedItemsPdf.size > 0) {
                val uriArrayList = ArrayList<Uri>()
                selectedItemsPdf.forEach {
                    val path = it.path
                    val file = File(path)
                    val uri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.pratiksahu.android.fileprovider",
                        file
                    )
                    uriArrayList.add(uri)
                }
                val multipleShareIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
                multipleShareIntent.type = "application/pdf"
                multipleShareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriArrayList)
                requireContext().startActivity(multipleShareIntent)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please select atleast one pdf",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun initAdapter() {
        importedPdfsAdapter = ImportedPdfsAdapter(
            emptyList(),
            { view, pos, dockItem ->
                Log.d(TAG_SHARE, dockItem.toString())
                try {
                    val path = dockItem!!.imageUri.path
                    val file = File(path)
                    val uri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.pratiksahu.android.fileprovider",
                        file
                    )
                    val intentUrl = Intent(Intent.ACTION_SEND_MULTIPLE)
                    intentUrl.type = "application/pdf"
                    intentUrl.putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayListOf(uri))
                    intentUrl.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    requireContext().startActivity(intentUrl)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        requireActivity(),
                        "Unknown Error Occured",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            { view, pos, dockItem ->
                //PDF CLICKED OR NOT
                if (flagForSelection == 0) {
                    Log.d(TAG_OPEN, dockItem.toString())
                    try {
                        val path = dockItem!!.imageUri.path
                        val file = File(path)
                        val uri = FileProvider.getUriForFile(
                            requireContext(),
                            "com.pratiksahu.android.fileprovider",
                            file
                        )
                        val intentUrl = Intent(Intent.ACTION_VIEW)
                        intentUrl.setDataAndType(uri, "application/pdf")
                        intentUrl.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        requireContext().startActivity(intentUrl)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(
                            requireActivity(),
                            "No PDF Viewer Installed",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                }
                //PDF CLICKED FOR CHECKBOX ACTION OR NOT
                if (flagForSelection == 1) {
                    if (!selectedItems.contains(pos)) {
                        selectedItems.add(pos)
                        if (!selectedItemsPdf.contains(dockItem!!.imageUri)) {
                            selectedItemsPdf.add(dockItem.imageUri)
                        }
                    } else {
                        if (selectedItems.contains(pos))
                            selectedItems.remove(pos)
                        if (selectedItemsPdf.contains(dockItem!!.imageUri))
                            selectedItemsPdf.remove(dockItem.imageUri)
                    }
                    importedPdfsAdapter?.setSelectedItems(selectedItems)
                }

            },
            { item, pos, isChecked ->
                //Item Checkbox click invoke
                if (isChecked) {
                    selectedItems.add(pos)
                    selectedItemsPdf.add(item!!.imageUri)
                } else {
                    if (selectAllCheckBox.isChecked)
                        selectAllCheckBox.isChecked = false
                    if (selectedItems.contains(pos))
                        selectedItems.remove(pos)
                    if (selectedItemsPdf.contains(item!!.imageUri))
                        selectedItemsPdf.remove(item.imageUri)
                }
                importedPdfsAdapter?.setSelectedItems(selectedItems)
            }
        ) { _, _, _ ->
            //Item long press invoke
            flagForSelection = 1
            showActionsTabView()
            importedPdfsAdapter?.setIsLongClicked(true)
        }
        importedDocksPdf.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            itemAnimator = DefaultItemAnimator()
            adapter = importedPdfsAdapter
        }
    }

    fun setupCancelButtonListener() {
        cancelSelectionButton.setOnClickListener(null)
        cancelSelectionButton.setOnClickListener {
            flagForSelection = 0
            importedPdfsAdapter?.setIsLongClicked(false)
            selectedItems.clear()
            selectedItemsPdf.clear()
            importedPdfsAdapter?.setSelectedItems(selectedItems)
            hideActionsTabView()
            selectAllCheckBox.isChecked = false
        }
    }

    var deleteFlag = 0

    fun setupDeletePdfButtonListener() {
        deleteFileButton.setOnClickListener {
            if (selectedItemsPdf.size > 0) {
                deleteFlag = 1
                pdfViewPagerFragmentViewModel.setPdfDelete(true, selectedItemsPdf)
                navController.navigate(R.id.action_viewPdfFragment_to_delete_confirmation_dialog)
            } else
                ToastMessage("Select atleast one item to delete")

        }
    }

    fun setupDeleteDialogResponseListener() {
        pdfViewPagerFragmentViewModel.pdfDelete.observe(viewLifecycleOwner, Observer {
            if (!it) {
                if (deleteFlag == 1) {
                    importedPdfsAdapter?.items = emptyList()
                    pdfViewPagerFragmentViewModel.initPdf()
                    flagForSelection = 0
                    hideActionsTabView()
                    selectAllCheckBox.isChecked = false
                    selectedItemsPdf.clear()
                    selectedItems.clear()
                    importedPdfsAdapter?.setIsLongClicked(false)
                    importedPdfsAdapter?.setSelectedItems(selectedItems)
                    deleteFlag == 0
                }
            }
        })
    }

    fun setupSelectAllCheckBoxListener() {
        selectAllCheckBox.setOnCheckedChangeListener(null)
        selectAllCheckBox.isChecked = false
        selectAllCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                selectAllCheckBox.text = "Unselect All"
                //Storing Uri
                pdfList.forEach {
                    selectedItemsPdf.add(it.imageUri)
                }
                //Storing index
                selectedItems.clear()
                pdfList.forEachIndexed { index, _ ->
                    selectedItems.add(index)
                }
                importedPdfsAdapter?.setSelectedItems(selectedItems)
            } else {
                selectAllCheckBox.text = "Select All"
                selectedItemsPdf.clear()
                selectedItems.clear()
                importedPdfsAdapter?.setSelectedItems(selectedItems)
            }
        }
        selectAllCheckBox.isChecked = false
    }


    fun hideActionsTabView() {
        //Hide buttons
        selectAllCheckBox.visibility = GONE
        deleteFileButton.visibility = GONE
        cancelSelectionButton.visibility = GONE
        shareMultiple.visibility = GONE
        //Show guide text
        guideText.visibility = View.VISIBLE
        //Hide rearrange and moreOptions button
    }

    fun showActionsTabView() {
        //Hide buttons
        selectAllCheckBox.visibility = View.VISIBLE
        deleteFileButton.visibility = View.VISIBLE
        cancelSelectionButton.visibility = View.VISIBLE
        shareMultiple.visibility = VISIBLE

        //Hide guide text and rearrange button
        guideText.visibility = GONE

    }

    fun ToastMessage(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
    }

}