package com.pratiksahu.dokify.ui.viewPagerHome.pdfPager

import android.net.Uri
import androidx.core.net.toUri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pratiksahu.dokify.model.DocInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class PdfViewPagerFragmentViewModel @ViewModelInject constructor() : ViewModel(),
    LifecycleObserver {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> = _isEmpty

    private val _pdfInFolder = MutableLiveData<ArrayList<DocInfo>>()
    val pdfInFolder: LiveData<ArrayList<DocInfo>> = _pdfInFolder

    private val _pdfDelete = MutableLiveData<Boolean>()
    val pdfDelete: LiveData<Boolean> = _pdfDelete

    private val _pdfToDelete = MutableLiveData<ArrayList<Uri>>()
    val pdfToDelete: LiveData<ArrayList<Uri>> = _pdfToDelete


    fun setPdfDelete(valueA: Boolean, valueB: ArrayList<Uri>?) {
        _pdfDelete.value = valueA
        if (valueA) {
            _pdfToDelete.value = valueB
        } else {
            _pdfToDelete.value?.clear()
        }
    }

    fun setPdf(list: ArrayList<DocInfo>) {
        _pdfInFolder.value = list
    }

    fun initPdf() {
        CoroutineScope(Dispatchers.IO).launch {
            CoroutineScope(Dispatchers.Main).launch {
                _loading.value = true
            }
            val path = "/storage/emulated/0/Android/data/com.pratiksahu.dokify/files/PDF"
            val directory = File(path)
            if (directory.exists()) {
                val files: Array<File> = directory.listFiles()
                if (files.size > 0) {
                    Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed())
                    val tempDocInfoList = ArrayList<DocInfo>()
                    for (i in files.indices) {
                        val size = files[i].length().toBigDecimal().divide(BigDecimal(1000000))
                            .setScale(3, RoundingMode.FLOOR).toString() + "MB"
                        val tempDocInfo =
                            DocInfo(files[i].toUri(), files[i].name, size)
                        tempDocInfoList.add(tempDocInfo)
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        setPdf(tempDocInfoList)
                        _isEmpty.value = false
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        _isEmpty.value = true
                    }
                }
            }
            CoroutineScope(Dispatchers.Main).launch {
                _loading.value = false
            }
        }
    }
}