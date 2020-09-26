package com.pratiksahu.dokify.ui.viewPagerHome.imagePager

import androidx.core.net.toUri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pratiksahu.dokify.model.DocInfo
import java.io.File
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class ImagePagerViewModel @ViewModelInject constructor() : ViewModel(), LifecycleObserver {

    private val _newImage = MutableLiveData<ArrayList<DocInfo>>()
    val imagesInFolder: LiveData<ArrayList<DocInfo>> = _newImage

    private val _selectedImage = MutableLiveData<DocInfo>()
    val selectedImage: LiveData<DocInfo> = _selectedImage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading


    fun setImage(img: ArrayList<DocInfo>) {
        _newImage.value = img
    }

    fun setSelectedImage(obj: DocInfo) {
        _selectedImage.value = obj
    }

    fun initImages() {
        _loading.value = true
        val path = "/storage/emulated/0/Android/data/com.pratiksahu.dokify/files/Pictures"
        val directory = File(path)
        if (directory.exists()) {
            val files: Array<File> = directory.listFiles()
            if (files.size > 0) {
                Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed())
                val tempDocInfoList = ArrayList<DocInfo>()
                for (i in files.indices) {
                    val tempDocInfo =
                        DocInfo(files[i].toUri(), files[i].name, files[i].length().toString())
                    tempDocInfoList.add(tempDocInfo)
                }
                setImage(tempDocInfoList)
            }
        }
        _loading.value = false
    }

}