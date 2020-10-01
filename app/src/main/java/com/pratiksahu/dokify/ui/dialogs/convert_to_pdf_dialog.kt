package com.pratiksahu.dokify.ui.dialogs

import ImageUtils
import android.annotation.SuppressLint
import android.graphics.Bitmap
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
import com.pratiksahu.dokify.`interface`.ToBlackWhite
import com.pratiksahu.dokify.databinding.ConvertToPdfDialogBinding
import com.pratiksahu.dokify.ui.viewPagerHome.imagePager.ImagePagerViewModel
import com.pratiksahu.dokify.ui.viewPagerHome.pdfPager.PdfViewPagerFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.convert_to_pdf_dialog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
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
    private var toBlackWhite = ToBlackWhite()

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
        makePDF.text = "Create PDF"
        fileNameTextBox.hint = timeStamp
        setupObservers()
        setupListeners()
    }

    fun setupObservers() {

        imagePagerViewModel.imageToConvert.observe(viewLifecycleOwner, Observer {
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
            }
            notify.invokeOnCompletion {
                val task = CoroutineScope(IO).launch()
                {
                    if (toConvert) {
                        for (i in imagesToConvert.indices) {
                            createTempFile("_${i}", "TEMP", ".jpg")
                            blackAndWhite(imagesToConvert[i])
                        }
                    }
                }
                task.invokeOnCompletion {
                    imagePagerViewModel.initTempImages()

                    //Invoke create pdf file method
                    if (!fileName.isEmpty() && !fileName.isBlank()) {
                        createFile(fileName, "", ".pdf")
                    } else {
                        createFile(timeStamp, "PDF_", ".pdf")
                    }

                    //Launch pdf creation method
                    CoroutineScope(IO).launch {
                        if (toConvert) {
                            ImageUtils.instant?.createPdf(tempImagesToConvert, currentPhotoPath)
                                .let {
                                    pdfCreated(it!!)
                                }
                            CoroutineScope(Main).launch {
                                tempImagesToConvert.forEach {
                                    File(it.path).delete()
                                }
                            }
                        } else {
                            ImageUtils.instant?.createPdf(imagesToConvert, currentPhotoPath).let {
                                pdfCreated(it!!)
                            }
                        }
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
                ToastMessage("$name failed to create , Try again")
            }
            pdfViewPagerFragmentViewModel.initPdf()
            navController.popBackStack()
        }
    }

    //Utility function

    fun createFile(fileName: String, prefix: String, suffix: String) {

        val name = prefix + fileName + suffix
        File("/storage/emulated/0/Android/data/com.pratiksahu.dokify/files/PDF/$name").apply {
            currentPhotoPath = absolutePath
        }.createNewFile().also {
            photoURI = FileProvider.getUriForFile(
                requireContext(),
                "com.pratiksahu.android.fileprovider",
                File(currentPhotoPath)
            )
        }
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