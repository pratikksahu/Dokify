package com.pratiksahu.dokify.ui.screens

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class ImageToText : Fragment(R.layout.fragment_image_to_text) {

    val TAG_OCR = "OCR_RESULT"

    @Inject
    lateinit var mainActivityViewModel: MainActivityViewModel

    private val navController by lazy { NavHostFragment.findNavController(this) }

    lateinit var binding: FragmentImageToTextBinding
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
        importButtonListener()
        setupAddFilesListener()
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

    fun importButtonListener() {
        importImage.setOnClickListener {
            mainActivityViewModel.setImageToText(true)
            navController.navigate(R.id.action_fragmentImageToText_to_add_files_popup)
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
                    resultOCR.setText(it.text)
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
}