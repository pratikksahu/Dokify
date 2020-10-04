package com.pratiksahu.dokify

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pratiksahu.dokify.ui.viewPagerHome.imagePager.ImagePagerViewModel
import com.pratiksahu.dokify.ui.viewPagerHome.pdfPager.PdfViewPagerFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var mainActivityViewModel: MainActivityViewModel

    @Inject
    lateinit var imagePagerViewModel: ImagePagerViewModel

    @Inject
    lateinit var pdfViewPagerFragmentViewModel: PdfViewPagerFragmentViewModel

    //All code in landingPage for this activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CoroutineScope(Dispatchers.IO).launch {
            createPdfDirectroy()
            createTempDirectory()
        }
        pdfViewPagerFragmentViewModel.pdfPath = getString(R.string.pdfOutputPath)
        imagePagerViewModel.tempPath = getString(R.string.tempOutputPath)
        imagePagerViewModel.picturePath = getString(R.string.imageOutputPath)
    }

    fun createTempDirectory() {
        //Temporary directory
        val path = getString(R.string.tempOutputPath)
        val directory = File(path)
        if (directory.exists())
            directory.delete()
        directory.mkdir()
    }

    fun createPdfDirectroy() {
        //PDF directory
        val path = getString(R.string.pdfOutputPath)
        val directory = File(path)
        if (directory.exists())
            directory.delete()
        directory.mkdir()
    }

}