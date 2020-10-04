package com.pratiksahu.dokify.ui.viewPagerHome.imagePager

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pratiksahu.dokify.model.DocInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class ImagePagerViewModel @ViewModelInject constructor() : ViewModel(), LifecycleObserver {

    val TAG_IMAGE_LOADED = "IMAGES_FOUND"
    val TAG_TEMPIMAGE_LOADED = "IMAGES_TEMP_FOUND"

    lateinit var tempPath: String
    lateinit var picturePath: String

    private val _newImage = MutableLiveData<ArrayList<DocInfo>>()
    val imagesInFolder: LiveData<ArrayList<DocInfo>> = _newImage

    private val _selectedImage = MutableLiveData<DocInfo>()
    val selectedImage: LiveData<DocInfo> = _selectedImage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> = _isEmpty

    private val _isConverted = MutableLiveData<Boolean>()
    val isConverted: LiveData<Boolean> = _isConverted

    private val _imageDelete = MutableLiveData<Boolean>()
    val imageDelete: LiveData<Boolean> = _imageDelete

    private val _imagesToDelete = MutableLiveData<ArrayList<Uri>>()
    val imagesToDelete: LiveData<ArrayList<Uri>> = _imagesToDelete

    private val _imageToConvert = MutableLiveData<ArrayList<Uri>>()
    val imageToConvert: LiveData<ArrayList<Uri>> = _imageToConvert

    private val _tempImageToConvert = MutableLiveData<ArrayList<Uri>>()
    val tempImageToConvert: LiveData<ArrayList<Uri>> = _tempImageToConvert

    fun setImageDelete(valueA: Boolean, valueB: ArrayList<Uri>?) {
        _imageDelete.value = valueA
        if (valueA) {
            _imagesToDelete.value = valueB
        } else {
            _imagesToDelete.value?.clear()
        }
    }

    fun setIsConverted(value: Boolean) {
        _isConverted.value = value
    }

    fun setImage(img: ArrayList<DocInfo>) {
        _newImage.value = img
    }

    fun setSelectedImage(obj: DocInfo) {
        _selectedImage.value = obj
    }

    fun isLoading(value: Boolean) {
        _loading.value = value
    }

    fun setImagesToConvert(list: ArrayList<Uri>) {
        _imageToConvert.value = list
    }

    fun setTempImagesToConvert(list: ArrayList<Uri>) {
        _tempImageToConvert.value = list
    }

    fun initTempImages() {
        CoroutineScope(IO).launch {
            CoroutineScope(Main).launch {
                _loading.value = true
            }
            _tempImageToConvert.value?.clear()
            val directory = File(tempPath)
            if (directory.exists()) {
                val files: Array<File> = directory.listFiles()
                Log.d(TAG_TEMPIMAGE_LOADED, files.toString())
                if (files.size > 0) {
                    Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed())
                    val tempDocInfoList = ArrayList<Uri>()
                    files.forEach {
                        tempDocInfoList.add(it.toUri())
                    }
                    CoroutineScope(Main).launch {
                        setTempImagesToConvert(tempDocInfoList)
                    }
                }
            } else {
                directory.mkdir()
            }
            CoroutineScope(Main).launch {
                _loading.value = false
            }
        }
    }


    fun initImages() {
        CoroutineScope(IO).launch {
            CoroutineScope(Main).launch {
                _loading.value = true
            }
            _newImage.value?.clear()
            val directory = File(picturePath)
            if (directory.exists()) {
                val files: Array<File> = directory.listFiles()
                Log.d(TAG_IMAGE_LOADED, files.toString())
                if (files.size > 0) {
                    Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed())
                    val tempDocInfoList = ArrayList<DocInfo>()
                    for (i in files.indices) {
                        val tempDocInfo =
                            DocInfo(files[i].toUri(), files[i].name, files[i].length().toString())
                        tempDocInfoList.add(tempDocInfo)
                    }
                    CoroutineScope(Main).launch {
                        setImage(tempDocInfoList)
                        _isEmpty.value = false
                    }
                } else {
                    CoroutineScope(Main).launch {
                        _isEmpty.value = true
                    }
                }
            } else {
                directory.mkdir()
            }
            CoroutineScope(Main).launch {
                _loading.value = false
            }
        }
    }

}