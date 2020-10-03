package com.pratiksahu.dokify.ui.dialogs

import android.Manifest.permission
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.NavHostFragment
import com.pratiksahu.dokify.R
import com.pratiksahu.dokify.ui.viewPagerHome.imagePager.ImagePagerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.add_files_popup_fragment.*
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

@RequiresApi(Build.VERSION_CODES.P)
@AndroidEntryPoint
class add_files_popup : DialogFragment() {

    private val TAG_IMPORT_GALLERY = "GALLERY_IMPORT"
    private val TAG_IMPORT_CAMERA = "CAMERA_IMPORT"
    private val TAG_PERMISSION = "PERMISSION_RESULT"

    @Inject
    lateinit var imagePagerViewModel: ImagePagerViewModel
    lateinit var currentPhotoPath: String
    lateinit var photoURI: Uri
    private var flag = 0
    private val navController by lazy { NavHostFragment.findNavController(this) }
    var compression = 100


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
                Log.d(TAG_PERMISSION, "Permission Granted")
                if (flag == 1)
                    openCamera()
                if (flag == 0)
                    openGallery()
            } else {
                ToastMessage("Permission Denied")
                Log.d(TAG_PERMISSION, "Permission Denied")
            }
        }


    private val galleryActivityRegister =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) {
            if (it.size != 0)
                it?.also {
                    Log.d(TAG_IMPORT_GALLERY, "Number of items : ${it.size}")
                    CoroutineScope(IO).launch {
                        CoroutineScope(Main).launch {
                            Log.d(TAG_IMPORT_GALLERY, "Loading Image : true")
                            imagePagerViewModel.isLoading(true)
                        }
                        for (i in it.indices) {
                            val timeStamp =
                                i.toString() + "_" + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                            createFile(timeStamp, "JPEG", ".jpeg")
                            val bitMap = ImageDecoder.decodeBitmap(
                                ImageDecoder.createSource(
                                    requireContext().contentResolver,
                                    it[i]
                                )
                            )
                            val opstream = FileOutputStream(currentPhotoPath)

                            //creating byteoutputstream
                            val btopstream = ByteArrayOutputStream(1024)
                            bitMap.compress(Bitmap.CompressFormat.JPEG, compression, btopstream)
                            opstream.write(btopstream.toByteArray())
                            opstream.flush()
                            opstream.close()
                        }
                        imagePagerViewModel.initImages()
                        CoroutineScope(Main).launch {
                            Log.d(TAG_IMPORT_GALLERY, "Loading Image : false")
                            imagePagerViewModel.isLoading(false)
                        }
                    }
                    navController.popBackStack()
                }
            else
                ToastMessage("Error importing image from gallery")
        }

    private val cameraActivityRegister =
        registerForActivityResult(ActivityResultContracts.TakePicture()) {
            CoroutineScope(IO).launch {
                if (it) {
                    Log.d(TAG_IMPORT_CAMERA, "Camera result: $it")
                    imagePagerViewModel.initImages()
                } else {
                    Log.d(TAG_IMPORT_CAMERA, "Camera result: $it")
                    File(currentPhotoPath).delete().let {
                        Log.d(
                            TAG_IMPORT_CAMERA,
                            "Temp Photo Deleted <--- $it  Path <-- $currentPhotoPath"
                        )
                    }
                }
            }.invokeOnCompletion {
                CoroutineScope(Main).launch {
                    navController.popBackStack()
                }
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
        compressionSetup()
    }

    fun compressionSetup() {
        compressValue.setText("40")
        compressValue.addTextChangedListener {
            val text = it.toString()
            if (text.isNotBlank() && text.isNotEmpty()) {
                val value = text.toInt()
                if (value > 100) {
                    compression = 100
                    ToastMessage("Maximum compression value is 100")
                } else if (value < 20) {
                    compression = 20
                    ToastMessage("Minimum compression value is 20")
                } else {
                    compression = value
                }
            }
        }
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
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        createFile(timeStamp, "JPEG", ".jpeg")
        cameraActivityRegister.launch(photoURI)
    }

    fun createFile(fileName: String, prefix: String, suffix: String) {
        val name = prefix + fileName + suffix
        val path = "/storage/emulated/0/Android/data/com.pratiksahu.dokify/files/Pictures/"
        File(path).mkdir()
        File("${path}${name}").apply {
            currentPhotoPath = absolutePath
        }.createNewFile().also {
            photoURI = FileProvider.getUriForFile(
                requireContext(),
                "com.pratiksahu.android.fileprovider",
                File(currentPhotoPath)
            )
        }
    }


    fun ToastMessage(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}