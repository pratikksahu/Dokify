package com.pratiksahu.dokify.ui.viewPagerHome.pdfPager

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.pratiksahu.dokify.R
import com.pratiksahu.dokify.databinding.CommonViewPagerBinding
import com.pratiksahu.dokify.model.DocInfo
import com.pratiksahu.dokify.ui.recyclerViewAdapter.ImportedPdfsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.common_view_pager.*
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class PdfViewPagerFragment : Fragment(R.layout.common_view_pager) {

    val TAG_PDF_LIST = "PDF LOADING"
    val TAG_DELETE = "PDF DELETED"

    @Inject
    lateinit var pdfViewPagerFragmentViewModel: PdfViewPagerFragmentViewModel
    var importedPdfsAdapter: ImportedPdfsAdapter? = null

    lateinit var binding: CommonViewPagerBinding

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
        binding = CommonViewPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        actionsTab.layoutTransition.setAnimateParentHierarchy(false)
        progressCircle = CircularProgressDrawable(requireContext())
        progressCircle.strokeWidth = 5f
        progressCircle.centerRadius = 30f
        progressCircle.start()

        hideActionsTabView()
        setupSelectAllCheckBoxListener()
        setupCancelButtonListener()
        setupDeleteImageButtonListener()
        setupObservers()
        initAdapter()
    }

    fun setupObservers() {
        pdfViewPagerFragmentViewModel.initPdf()

        pdfViewPagerFragmentViewModel.isEmpty.observe(viewLifecycleOwner, Observer {
            if (it) {
                guideText.text = "No pdf found"
                hideActionsTabView()
            } else {
                guideText.text = "Hold pdf for more options"
            }
        })
        pdfViewPagerFragmentViewModel.loading.observe(viewLifecycleOwner, Observer {
            Log.d(TAG_PDF_LIST, "LOADING PDF STATUS :" + it.toString())
            if (it) {
                importedDocksPdf.visibility = GONE
                loadingData.visibility = View.VISIBLE
            } else {
                loadingData.visibility = GONE
                importedDocksPdf.visibility = View.VISIBLE
            }
        })

        pdfViewPagerFragmentViewModel.pdfInFolder.observe(viewLifecycleOwner, Observer {
            pdfList.clear()
            pdfList.addAll(it)
            importedPdfsAdapter?.items = pdfList
        })
    }

    fun initAdapter() {
        importedPdfsAdapter = ImportedPdfsAdapter(
            pdfList,
            progressCircle,
            { view, pos, dockItem ->

                //PDF CLICKED OR NOT
                if (flagForSelection == 0) {
                    if (dockItem != null) {

                        //TODO open pdf
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

    fun setupDeleteImageButtonListener() {
        deleteFileButton.setOnClickListener {
            if (selectedItemsPdf.size > 0)
                selectedItemsPdf.forEach {
                    File(it.path).delete()
                        .let { result ->
                            Log.d(TAG_DELETE, it.path + " <-- $result")
                        }
                }
            flagForSelection = 0
            hideActionsTabView()
            selectAllCheckBox.isChecked = false
            selectedItemsPdf.clear()
            selectedItems.clear()
            importedPdfsAdapter?.setIsLongClicked(false)
            importedPdfsAdapter?.setSelectedItems(selectedItems)
            importedPdfsAdapter?.items = emptyList()
            pdfViewPagerFragmentViewModel.initPdf()
        }
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
        pdfDialog.visibility = GONE
        deleteFileButton.visibility = GONE
        cancelSelectionButton.visibility = GONE

        //Show guide text
        guideText.visibility = View.VISIBLE
    }

    fun showActionsTabView() {
        //Hide buttons
        selectAllCheckBox.visibility = View.VISIBLE
        pdfDialog.visibility = GONE
        deleteFileButton.visibility = View.VISIBLE
        cancelSelectionButton.visibility = View.VISIBLE

        //Hide guide text
        guideText.visibility = GONE
    }

}