package com.pratiksahu.dokify.ui.dialogs

import ImageUtils
import android.Manifest.permission
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
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
import com.pratiksahu.dokify.MainActivityViewModel
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
import java.io.FileNotFoundException
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

    @Inject
    lateinit var mainActivityViewModel: MainActivityViewModel


    lateinit var currentPhotoPath: String
    lateinit var photoURI: Uri
    private var flag = 0
    private val navController by lazy { NavHostFragment.findNavController(this) }


    var compression = 0
    var forOCR = false


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


    private val galleryActivityRegisterMulti =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) {
            if (it.size != 0) {
                mainActivityViewModel.setAddFilesButtonShow(false)
                Log.d(TAG_IMPORT_GALLERY, "Number of items : ${it.size}")
                CoroutineScope(IO).launch {
                    CoroutineScope(Main).launch {
                        Log.d(TAG_IMPORT_GALLERY, "Loading Image : true")
                        imagePagerViewModel.isLoading(true)
                    }
                    for (i in it.indices) {

                        var name = ""
                        if (it[i].scheme.equals("content")) {
                            val cursor: Cursor? = requireContext().contentResolver.query(
                                it[i],
                                null,
                                null,
                                null,
                                null
                            )
                            cursor.use { cursor ->
                                if (cursor != null && cursor.moveToFirst()) {
                                    name =
                                        cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                                }
                            }
                        }
                        name = name.split('.')[0]
                        var bitMap: Bitmap
//                        val timeStamp =
//                            i.toString() + "_" + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//                        createFile(timeStamp, "JPEG", ".jpeg")
                        createFile(name, "", ".jpeg")
                        if (Build.VERSION.SDK_INT >= 29) {
                            bitMap = ImageDecoder.decodeBitmap(
                                ImageDecoder.createSource(
                                    requireContext().contentResolver,
                                    it[i]
                                )
                            )
                        } else {
                            // Use older version
                            bitMap = MediaStore.Images.Media.getBitmap(
                                requireContext().contentResolver,
                                it[i]
                            )
                        }

                        val opstream = FileOutputStream(currentPhotoPath)

                        //creating byteoutputstream
                        val btopstream = ByteArrayOutputStream()
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
                mainActivityViewModel.setAddFilesButtonShow(true)
                fromGallery.isClickable = true
                fromCamera.isClickable = true
                navController.popBackStack()
            } else {
                CoroutineScope(Main).launch {
                    fromGallery.isClickable = true
                    fromCamera.isClickable = true
                }
            }
        }
    private val galleryActivityRegisterSingle =
        registerForActivityResult(ActivityResultContracts.GetContent()) {

            if (it != null) {
                CoroutineScope(IO).launch {

                    val bitMap: Bitmap
                    val timeStamp =
                        "OCR" + "_" + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                    createFile(timeStamp, "JPEG", ".jpeg")
                    if (Build.VERSION.SDK_INT >= 29) {
                        bitMap = ImageDecoder.decodeBitmap(
                            ImageDecoder.createSource(
                                requireContext().contentResolver,
                                it
                            )
                        )
                    } else {
                        // Use older version
                        bitMap = MediaStore.Images.Media.getBitmap(
                            requireContext().contentResolver,
                            it
                        )
                    }

                    val opstream = FileOutputStream(currentPhotoPath)

                    //creating byteoutputstream
                    val btopstream = ByteArrayOutputStream()
                    bitMap.compress(Bitmap.CompressFormat.JPEG, compression, btopstream)
                    opstream.write(btopstream.toByteArray())
                    opstream.flush()
                    opstream.close()

                }.invokeOnCompletion {
                    CoroutineScope(Main).launch {
                        mainActivityViewModel.setImageToTextPhotoPath(currentPhotoPath)
                        mainActivityViewModel.setImageToText(false)
                        fromGallery.isClickable = true
                        fromCamera.isClickable = true
                        navController.popBackStack()
                    }
                }
            } else {
                CoroutineScope(Main).launch {
                    fromGallery.isClickable = true
                    fromCamera.isClickable = true
                }
            }
        }

    private val cameraActivityRegister =
        registerForActivityResult(ActivityResultContracts.TakePicture()) {
            CoroutineScope(IO).launch {
                if (it) {
                    Log.d(TAG_IMPORT_CAMERA, "Camera result: $it")


                    val oldPath = currentPhotoPath
                    val file = BitmapFactory.decodeFile(oldPath)
                    val width = file.width.toFloat()
                    val height = file.height.toFloat()
                    val bitMap = ImageUtils.instant?.getCompressedBitmap(oldPath, width, height)

                    var timeStamp = ""
                    if (forOCR)
                        timeStamp = "OCR" + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                    else
                        timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())


                    createFile(timeStamp, "JPEG", ".jpeg")
                    val opstream = FileOutputStream(currentPhotoPath)
                    //creating byteoutputstream
                    val btopstream = ByteArrayOutputStream()
                    bitMap?.compress(Bitmap.CompressFormat.JPEG, compression, btopstream)
                    opstream.write(btopstream.toByteArray())
                    opstream.flush()
                    opstream.close()
                    File(oldPath).delete()

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
                    mainActivityViewModel.setImageToTextPhotoPath(currentPhotoPath)
                    mainActivityViewModel.setImageToText(false)
                    fromGallery.isClickable = true
                    fromCamera.isClickable = true
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
        imageToTextListener()
        compressionListener()
        gallerySetup()
        cameraSetup()
    }

    fun compressionListener() {
        mainActivityViewModel.compression.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            compression = it.toInt()
        })
    }

    fun imageToTextListener() {
        mainActivityViewModel.imageToText.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            forOCR = it
        })
    }


    fun gallerySetup() {
        fromGallery.setOnClickListener {
            fromCamera.isClickable = false
            fromGallery.isClickable = false
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
                fromGallery.isClickable = true
                fromCamera.isClickable = true
            } else {
                openGallery()
            }
        }
    }

    fun cameraSetup() {
        fromCamera.setOnClickListener {
            fromCamera.isClickable = false
            fromGallery.isClickable = false
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
                fromGallery.isClickable = true
                fromCamera.isClickable = true
            } else {
                openCamera()
            }
        }
    }


    fun openGallery() {
        if (!forOCR)
            galleryActivityRegisterMulti.launch("image/*")
        else
            galleryActivityRegisterSingle.launch("image/*")

    }


    fun openCamera() {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        createFile(timeStamp, "JPEG", ".jpeg")
        cameraActivityRegister.launch(photoURI)
    }

    fun createFile(fileName: String, prefix: String, suffix: String) {
        val name = prefix + fileName + suffix
        val path = getString(R.string.imageOutputPath)
        File(path).mkdir()
        try {
            File("${path}${name}").apply {
                currentPhotoPath = absolutePath
            }.createNewFile().also {
                photoURI = FileProvider.getUriForFile(
                    requireContext(),
                    "com.pratiksahu.android.fileprovider",
                    File(currentPhotoPath)
                )
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }


    fun ToastMessage(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}