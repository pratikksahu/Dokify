package com.pratiksahu.dokify.ui.dialogs

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.NavHostFragment
import com.pratiksahu.dokify.R
import com.pratiksahu.dokify.model.DocInfo
import com.pratiksahu.dokify.ui.viewPagerHome.imagePager.ImagePagerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.add_files_popup_fragment.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.P)
@AndroidEntryPoint
class add_files_popup : DialogFragment() {


    @Inject
    lateinit var imagePagerViewModel: ImagePagerViewModel
    lateinit var currentPhotoPath: String
    lateinit var photoURI: Uri
    private val res = ArrayList<DocInfo>()
    private var flag = 0
    private val navController by lazy { NavHostFragment.findNavController(this) }


    val permissionRequired = arrayOf(
        permission.CAMERA,
        permission.WRITE_EXTERNAL_STORAGE,
        permission.READ_EXTERNAL_STORAGE
    )

    private val permissionActivityRegister =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.getValue(permission.CAMERA) == true && result.getValue(permission.WRITE_EXTERNAL_STORAGE) == true && result.getValue(
                    permission.READ_EXTERNAL_STORAGE
                ) == true
            ) {
                ToastMessage("Permission Granted")
                if (flag == 1)
                    openCamera()
                if (flag == 0)
                    openGallery()
            } else {
                ToastMessage("Permission Denied")
            }
        }


    private val galleryActivityRegister =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) {
            it?.also {
                it.forEach { obj ->
                    createFile()
                    val bitMap = ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(
                            requireContext().contentResolver,
                            obj
                        )
                    )
                    val opstream = FileOutputStream(currentPhotoPath)

                    //creating byteoutputstream
                    val btopstream = ByteArrayOutputStream(1024)
                    bitMap.compress(Bitmap.CompressFormat.JPEG, 100, btopstream)
                    opstream.write(btopstream.toByteArray())
                    opstream.flush()
                    opstream.close()
                    val sizeStream =
                        requireContext().contentResolver.openInputStream(obj)?.available()
                    res.add(DocInfo(photoURI, null, sizeStream.toString()))
                }
                imagePagerViewModel.setImage(res)
                navController.popBackStack()
            }
        }

    private val cameraActivityRegister =
        registerForActivityResult(ActivityResultContracts.TakePicture()) {
            if (it) {
                ToastMessage("Got it")
                val sizeStream =
                    requireContext().contentResolver.openInputStream(photoURI)?.available()
                imagePagerViewModel.setImage(
                    arrayListOf(
                        DocInfo(
                            photoURI,
                            null,
                            sizeStream.toString()
                        )
                    )
                )
                navController.popBackStack()
            } else {
                File(currentPhotoPath).delete()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_files_popup_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gallerySetup()
        cameraSetup()
    }


    fun gallerySetup() {
        fromGallery.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                flag = 0
                permissionActivityRegister.launch(permissionRequired)
            } else {
                openGallery()
            }
        }
    }

    fun cameraSetup() {
        fromCamera.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                flag = 1
                permissionActivityRegister.launch(permissionRequired)
            } else {
                openCamera()
            }
        }
    }


    fun openGallery() {
        galleryActivityRegister.launch("image/*")
    }


    fun openCamera() {
        createFile()
        cameraActivityRegister.launch(photoURI)
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
            photoURI = FileProvider.getUriForFile(
                requireContext(),
                "com.example.android.fileprovider",
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
        println("TESTING : ${msg}")
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}