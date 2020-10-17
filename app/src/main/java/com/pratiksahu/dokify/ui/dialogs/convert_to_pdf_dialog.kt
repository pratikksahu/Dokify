package com.pratiksahu.dokify.ui.dialogs

import ImageUtils
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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

    private val TEMP_TAG = "TEMP_IMAGES"
    private val ORIGINAL_TAG = "ORIGINAL_TAG"

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
        makePDF.text = getString(R.string.createPdf)
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
            if (makePDF.text == getString(R.string.createPdf)) {
                val notify = CoroutineScope(Main).launch {
                    makePDF.isClickable = false
                    makePDF.text = getString(R.string.pleaseWait)
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
                            Log.d(TEMP_TAG, tempImagesToConvert.toString())
                            ImageUtils.instant?.createPdf(tempImagesToConvert, currentPhotoPath)
                                .let {
                                    pdfCreated(it!!)
                                }
                        } else {
                            Log.d(ORIGINAL_TAG, imagesToConvert.toString())
                            ImageUtils.instant?.createPdf(imagesToConvert, currentPhotoPath).let {
                                pdfCreated(it!!)
                            }
                        }
                    }
                    convertTask.invokeOnCompletion {
                        CoroutineScope(Main).launch {
                            makePDF.text = getString(R.string.sharePdfText)
                            makePDF.isClickable = true
                            colorOrNot.isChecked = false
                            colorOrNot.isClickable = false
                            fileNameTextBox.isClickable = false
//                            navController.popBackStack()
                        }
                    }
                }
            }
            if (makePDF.text == getString(R.string.sharePdfText)) {
                try {
                    val path = currentPhotoPath
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

    //Utility function for pdf

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