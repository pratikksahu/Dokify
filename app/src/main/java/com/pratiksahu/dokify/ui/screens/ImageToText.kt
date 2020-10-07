package com.pratiksahu.dokify.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.pratiksahu.dokify.MainActivityViewModel
import com.pratiksahu.dokify.R
import com.pratiksahu.dokify.databinding.FragmentImageToTextBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_image_to_text.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ImageToText : Fragment(R.layout.fragment_image_to_text) {

    val TAG_OCR = "OCR_RESULT"

    @Inject
    lateinit var mainActivityViewModel: MainActivityViewModel

    private val navController by lazy { NavHostFragment.findNavController(this) }

    lateinit var binding: FragmentImageToTextBinding
    var currentPhotoPath: String = ""
    var convertedText: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImageToTextBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtonListener()
        setupAddFilesListener()
        setupObservers()
        helpButtonListener()
        logoAnimation()
    }

    fun logoAnimation() {
        val rotateAnimation = RotateAnimation(
            0F, 360F,
            Animation.RELATIVE_TO_SELF, 0.5F,
            Animation.RELATIVE_TO_SELF, 0.5F
        )

        rotateAnimation.interpolator = LinearInterpolator()
        rotateAnimation.duration = 2000
        rotateAnimation.repeatCount = 2
        logoView.setOnLongClickListener {
            logoView.startAnimation(rotateAnimation)
            true
        }
    }

    fun helpButtonListener() {
        helpButton.setOnClickListener {
            navController.navigate(R.id.action_fragmentImageToText_to_about)
        }
    }

    fun setupObservers() {
        mainActivityViewModel.convertedText.observe(viewLifecycleOwner, Observer {
            convertedText = it
            resultOCR.setText(it)
            if (it.isEmpty() || it.isBlank()) {
                clearText.visibility = GONE
                shareText.visibility = GONE
            } else {
                clearText.visibility = VISIBLE
                shareText.visibility = VISIBLE
            }
        })
    }

    fun setupAddFilesListener() {
        mainActivityViewModel.imageToTextPath.observe(viewLifecycleOwner, Observer {
            val path = it
            val file = File(path)
            if (file.exists()) {
                if (processImage(file.toUri())) {
                    file.delete()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please try again with upright orientation",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }

    fun setupButtonListener() {
        importImage.setOnClickListener {
            mainActivityViewModel.setImageToText(true)
            navController.navigate(R.id.action_fragmentImageToText_to_add_files_popup)
        }
        clearText.setOnClickListener {
            shareText.text = getString(R.string.sharePdfText)
            mainActivityViewModel.setConvertedText("")
        }

        shareText.setOnClickListener {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

            shareText.isClickable = false
            try {
                createFile(timeStamp, "TXT", ".txt")
                val writer = FileWriter(File(currentPhotoPath))
                writer.write(convertedText)
                writer.close()

                val path = currentPhotoPath
                val file = File(path)
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.pratiksahu.android.fileprovider",
                    file
                )
                val intentUrl = Intent(Intent.ACTION_SEND_MULTIPLE)
                intentUrl.type = "application/txt"
                intentUrl.putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayListOf(uri))
                intentUrl.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                shareText.isClickable = true

                requireContext().startActivity(intentUrl)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    requireActivity(),
                    "Unknown Error Occured",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: FileNotFoundException) {
                Log.d("SHARE_FAILED", "Failed , retrying")
                shareText.performClick()
            }

        }
    }

    fun processImage(uri: Uri): Boolean {
        //Processing Offline
        val processImg: InputImage
        try {
            processImg = InputImage.fromFilePath(requireContext(), uri)
            val recognizer = TextRecognition.getClient()
            recognizer.process(processImg)
                .addOnSuccessListener {
                    Log.d(TAG_OCR, it.text)
                    if (it.text.isNotEmpty() && it.text.isNotBlank())
                        mainActivityViewModel.setConvertedText(it.text)
                }
                .addOnFailureListener {
                    Log.d(TAG_OCR, it.message)
                }
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }

        //Processing Online
//        val processImg : FirebaseVisionImage
//        try{
//            processImg = FirebaseVisionImage.fromFilePath(requireContext(),uri)
//            val recognizer = FirebaseVision.getInstance().cloudTextRecognizer
//            val options = FirebaseVisionCloudTextRecognizerOptions.Builder()
//                .setLanguageHints(listOf("en", "hi"))
//                .build()
//            val result = recognizer.processImage(processImg)
//                .addOnSuccessListener {
//                    processResult.text  = it.text
//                }
//                .addOnFailureListener {
//                    ToastMessage(it.toString())
//                }
////            for (block in result.textBlocks) {
////                ToastMessage(block)
////                val blockText = block.text
////                val blockCornerPoints = block.cornerPoints
////                val blockFrame = block.boundingBox
////                for (line in block.lines) {
////                    val lineText = line.text
////                    val lineCornerPoints = line.cornerPoints
////                    val lineFrame = line.boundingBox
////                    for (element in line.elements) {
////                        val elementText = element.text
////                        val elementCornerPoints = element.cornerPoints
////                        val elementFrame = element.boundingBox
////                    }
////                }
////            }
//        }catch (e : IOException){
//            e.printStackTrace()
//        }

    }

    private fun createFile(fileName: String, prefix: String, suffix: String) {

        val name = prefix + fileName + suffix
        val path = "${getString(R.string.textOutputPath)}$name"
        try {
            File(path).apply {
                currentPhotoPath = absolutePath
            }.createNewFile()
        } catch (e: FileNotFoundException) {
            shareText.performClick()
        }

    }
}