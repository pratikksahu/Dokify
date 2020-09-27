package com.pratiksahu.dokify.ui.dialogs

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.pratiksahu.dokify.`interface`.ToBlackWhite
import com.pratiksahu.dokify.databinding.CropOrConvertDialogBinding
import com.pratiksahu.dokify.model.DocInfo
import com.pratiksahu.dokify.ui.viewPagerHome.imagePager.ImagePagerViewModel
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.crop_or_convert_dialog.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class crop_or_convert_dialog : DialogFragment() {

    @Inject
    lateinit var imagePagerViewModel: ImagePagerViewModel
    lateinit var selectedImage: DocInfo

    private val navController by lazy { NavHostFragment.findNavController(this) }

    lateinit var binding: CropOrConvertDialogBinding

    lateinit var currentPhotoPath: String
    lateinit var photoUri: Uri
    private var converter = ToBlackWhite()
    private val imageList = ArrayList<DocInfo>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initObservers()
        binding = CropOrConvertDialogBinding.inflate(inflater, container, false)
        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }


    private fun initListeners() {
        cropImage.setOnClickListener {
            createFile()
            startCropActivity()
        }
        convertImage.setOnClickListener {
            createFile()
            grayScaledUri(selectedImage.imageUri)
            imagePagerViewModel.initImages()
            navController.popBackStack()
        }
    }

    private fun initObservers() {
        imagePagerViewModel.selectedImage.observe(this, Observer {
            selectedImage = it
        })
        imagePagerViewModel.imagesInFolder.observe(viewLifecycleOwner, Observer {
            imageList.addAll(it)
        })
    }

    private fun startCropActivity() {
        CropImage.activity(selectedImage.imageUri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(requireContext(), this)
    }


    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val result = CropImage.getActivityResult(data)
        if (resultCode == RESULT_OK) {
            val resultUri: Uri = result.uri
            val srcPath: String = resultUri.path.toString()
            val destPath = currentPhotoPath

            //Move from cache to desired location

            moveFiles(Paths.get(srcPath), Paths.get(destPath))
            //Delete the cached image if successfully copied
            File(srcPath).delete()

            //Updating ViewModel
            imagePagerViewModel.initImages()

        } else {
            File(currentPhotoPath).delete()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun moveFiles(srcPath: Path, destPath: Path): Boolean {
        Files.move(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING)?.let { it ->
            File(it.toUri()).exists().let { status ->
                return status
            }
        }
        //if reaches here then file not created
        return false
    }


    fun grayScaledUri(color: Uri) {
        //getting bitmap
        val result = converter.convertToBW(requireContext(), color)
        //creating outputStream to store grayscaled version
        val opstream = FileOutputStream(currentPhotoPath)

        //creating byteoutputstream
        val btopstream = ByteArrayOutputStream(1024)
        result?.compress(Bitmap.CompressFormat.JPEG, 80, btopstream)
        opstream.write(btopstream.toByteArray())
        opstream.flush()
        opstream.close()
    }


    fun createFile() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            // Error occurred while creating the File
            ToastMessage("Error Creating File")
            null
        }
        //Creating Uri for generated path
        photoFile?.also {
            photoUri = FileProvider.getUriForFile(
                requireContext(),
                "com.pratiksahu.android.fileprovider",
                it
            )
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    fun ToastMessage(msg: String) {
        Toast.makeText(requireContext(), "Testing : ${msg}", Toast.LENGTH_SHORT).show()
    }
}