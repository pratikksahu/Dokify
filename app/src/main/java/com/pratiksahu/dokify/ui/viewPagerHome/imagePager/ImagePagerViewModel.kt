package com.pratiksahu.dokify.ui.viewPagerHome.imagePager

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pratiksahu.dokify.model.DocInfo

class ImagePagerViewModel @ViewModelInject constructor() : ViewModel(), LifecycleObserver {

    private val _newImage = MutableLiveData<ArrayList<DocInfo>>()
    val liveNewImage: LiveData<ArrayList<DocInfo>> = _newImage

    private val _selectedImage = MutableLiveData<DocInfo>()
    val selectedImage: LiveData<DocInfo> = _selectedImage

    fun setImage(img: ArrayList<DocInfo>) {
        _newImage.value = img
    }

    fun setSelectedImage(obj: DocInfo) {
        _selectedImage.value = obj
    }

    fun getLiveData() = _newImage as LiveData<ArrayList<DocInfo>>

}