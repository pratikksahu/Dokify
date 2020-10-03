package com.pratiksahu.dokify.ui.dialogs

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.pratiksahu.dokify.MainActivityViewModel
import com.pratiksahu.dokify.databinding.DeleteConfirmationDialogBinding
import com.pratiksahu.dokify.ui.viewPagerHome.imagePager.ImagePagerViewModel
import com.pratiksahu.dokify.ui.viewPagerHome.pdfPager.PdfViewPagerFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.delete_confirmation_dialog.*
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class delete_confirmation_dialog : DialogFragment() {

    lateinit var binding: DeleteConfirmationDialogBinding

    @Inject
    lateinit var imagePagerViewModel: ImagePagerViewModel

    @Inject
    lateinit var pdfViewPagerFragmentViewModel: PdfViewPagerFragmentViewModel

    @Inject
    lateinit var mainActivityViewModel: MainActivityViewModel

    private val navController by lazy { NavHostFragment.findNavController(this) }

    private val TAG_IMAGE_DELETE = "IMAGE_DELETE"
    private val TAG_PDF_DELETE = "PDF_DELETE"

    var imgDelete = false
    val imgList = ArrayList<Uri>()
    var pdfDelete = false
    val pdfList = ArrayList<Uri>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DeleteConfirmationDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
    }

    fun setupObservers() {
        imagePagerViewModel.imageDelete.observe(viewLifecycleOwner, Observer { toDelete ->
            imagePagerViewModel.imagesToDelete.observe(viewLifecycleOwner, Observer {
                imgDelete = toDelete
                imgList.addAll(it)
            })
        })
        pdfViewPagerFragmentViewModel.pdfDelete.observe(viewLifecycleOwner, Observer { toDelete ->
            pdfViewPagerFragmentViewModel.pdfToDelete.observe(viewLifecycleOwner, Observer {
                pdfDelete = toDelete
                pdfList.addAll(it)
            })
        })
    }

    fun setupListeners() {
        yesButton.setOnClickListener {
            if (imgDelete) {
                imgList.forEach {
                    File(it.path).delete()
                        .let { result ->
                            Log.d(TAG_IMAGE_DELETE, it.path + " <-- $result")
                        }
                }
                imagePagerViewModel.setImageDelete(false, null)
            }
            if (pdfDelete) {
                pdfList.forEach {
                    File(it.path).delete()
                        .let { result ->
                            Log.d(TAG_PDF_DELETE, it.path + " <-- $result")
                        }
                }
                pdfViewPagerFragmentViewModel.setPdfDelete(false, null)
            }
            mainActivityViewModel.setAddFilesButtonShow(true)
            navController.popBackStack()
        }
        noButton.setOnClickListener {
            imagePagerViewModel.setImageDelete(false, null)
            imagePagerViewModel.setImageDelete(false, null)
            mainActivityViewModel.setAddFilesButtonShow(true)
            navController.popBackStack()
        }
    }
}