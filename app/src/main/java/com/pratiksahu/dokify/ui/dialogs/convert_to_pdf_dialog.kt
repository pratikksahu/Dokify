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
import com.pratiksahu.dokify.databinding.ConvertToPdfDialogBinding
import com.pratiksahu.dokify.ui.viewPagerHome.imagePager.ImagePagerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.convert_to_pdf_dialog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@SuppressLint("SimpleDateFormat")
@AndroidEntryPoint
class convert_to_pdf_dialog : DialogFragment() {

    @Inject
    lateinit var imagePagerViewModel: ImagePagerViewModel

    private val navController by lazy { NavHostFragment.findNavController(this) }
    lateinit var binding: ConvertToPdfDialogBinding

    lateinit var currentPhotoPath: String
    lateinit var photoURI: Uri

    val imagesToConvert = ArrayList<Uri>()
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
        setupObservers()
        setupListeners()
    }

    fun setupObservers() {
        imagePagerViewModel.imageToConvert.observe(viewLifecycleOwner, Observer {
            imagesToConvert.addAll(it)
        })
    }

    fun setupListeners() {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        fileNameTextBox.setText(timeStamp)
        fileNameTextBox.addTextChangedListener {
            fileName = it.toString()
        }

        colorOrNot.setOnCheckedChangeListener { buttonView, isChecked ->
            toConvert = isChecked
            if (isChecked)
                buttonView.text = "Black And White PDF "
            else
                buttonView.text = "Color PDF "
        }

        makePDF.setOnClickListener {
            createFile(fileName, "PDF/", "PDF", ".pdf")
            CoroutineScope(IO).launch {
                ImageUtils.instant?.createPdf(imagesToConvert, currentPhotoPath)
            }
        }
    }

    //Utility function

    fun createFile(fileName: String, dir: String, prefix: String, suffix: String) {
        val photoFile: File? = try {
            createImageFile(fileName, dir, prefix, suffix)
        } catch (ex: IOException) {
            // Error occurred while creating the File
            ToastMessage("Error Creating File")
            null
        }
        //Creating Uri for generated path
        photoFile?.also {
            photoURI = FileProvider.getUriForFile(
                requireContext(),
                "com.pratiksahu.android.fileprovider",
                it
            )
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(
        fileName: String,
        dir: String,
        prefix: String,
        suffix: String
    ): File {
        val storageDir: File? = context?.getExternalFilesDir(dir)
        return File.createTempFile(
            "${prefix}_${fileName}", /* prefix */
            "$suffix", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    fun ToastMessage(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
    }

}