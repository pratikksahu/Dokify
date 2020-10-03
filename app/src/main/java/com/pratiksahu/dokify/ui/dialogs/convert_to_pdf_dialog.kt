package com.pratiksahu.dokify.ui.dialogs

import ImageUtils
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.pratiksahu.dokify.R
import com.pratiksahu.dokify.databinding.ConvertToPdfDialogBinding
import com.pratiksahu.dokify.ui.viewPagerHome.imagePager.ImagePagerViewModel
import com.pratiksahu.dokify.ui.viewPagerHome.pdfPager.PdfViewPagerFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.convert_to_pdf_dialog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@SuppressLint("SimpleDateFormat")
@AndroidEntryPoint
class convert_to_pdf_dialog : DialogFragment() {

    @Inject
    lateinit var imagePagerViewModel: ImagePagerViewModel

    @Inject
    lateinit var pdfViewPagerFragmentViewModel: PdfViewPagerFragmentViewModel

    private val navController by lazy { NavHostFragment.findNavController(this) }
    lateinit var binding: ConvertToPdfDialogBinding

    lateinit var currentPhotoPath: String
    lateinit var photoURI: Uri

    val imagesToConvert = ArrayList<Uri>()
    val tempImagesToConvert = ArrayList<Uri>()

    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    var fileName = ""

    var toConvert = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ConvertToPdfDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        makePDF.text = "Create PDF"
        fileNameTextBox.hint = timeStamp
        setupObservers()
        setupListeners()
    }

    fun setupObservers() {
        imagePagerViewModel.initTempImages()

        imagePagerViewModel.imageToConvert.observe(viewLifecycleOwner, Observer {
            imagesToConvert.clear()
            imagesToConvert.addAll(it)

        })
        imagePagerViewModel.tempImageToConvert.observe(viewLifecycleOwner, Observer {
            tempImagesToConvert.clear()
            tempImagesToConvert.addAll(it)

        })
    }

    fun setupListeners() {
        fileNameTextBox.addTextChangedListener {
            fileName = it.toString()
        }

        colorOrNot.setOnCheckedChangeListener { buttonView, isChecked ->
            toConvert = isChecked
        }

        makePDF.setOnClickListener {
            val notify = CoroutineScope(Main).launch {
                makePDF.text = "Please Wait"
                imagePagerViewModel.setIsConverted(false)
                delay(15)
            }
            notify.invokeOnCompletion {

                //Invoke create pdf file method
                if (!fileName.isEmpty() && !fileName.isBlank()) {
                    createFile(fileName, "", ".pdf")
                } else {
                    createFile(timeStamp, "PDF_", ".pdf")
                }

                //Launch pdf creation method
                val convertTask = CoroutineScope(IO).launch {
                    if (toConvert) {
                        ImageUtils.instant?.createPdf(tempImagesToConvert, currentPhotoPath)
                            .let {
                                pdfCreated(it!!)
                            }
                    } else {
                        ImageUtils.instant?.createPdf(imagesToConvert, currentPhotoPath).let {
                            pdfCreated(it!!)
                        }
                    }
                }
                convertTask.invokeOnCompletion {
                    CoroutineScope(Main).launch {
                        navController.popBackStack()
                    }
                }
            }
        }
    }


    fun pdfCreated(success: Boolean) {

        CoroutineScope(Main).launch {
            val name = File(currentPhotoPath).name
            if (success) {
                ToastMessage("$name created successfully")
            } else {
                ToastMessage("$name failed to create , please try again")
                File(currentPhotoPath).delete()
            }
            pdfViewPagerFragmentViewModel.initPdf()
            imagePagerViewModel.setIsConverted(true)
        }
    }

    //Utility function

    fun createFile(fileName: String, prefix: String, suffix: String) {

        val name = prefix + fileName + suffix
        File("${getString(R.string.pdfOutputPath)}$name").apply {
            currentPhotoPath = absolutePath
        }.createNewFile().also {
            photoURI = FileProvider.getUriForFile(
                requireContext(),
                "com.pratiksahu.android.fileprovider",
                File(currentPhotoPath)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        imagePagerViewModel.setIsConverted(true)
    }


    fun ToastMessage(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
    }


}