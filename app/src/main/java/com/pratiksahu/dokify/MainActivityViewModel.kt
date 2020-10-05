package com.pratiksahu.dokify

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel @ViewModelInject constructor() : ViewModel() {

    private val _addFilesShow = MutableLiveData<Boolean>()
    var addFilesShow: LiveData<Boolean> = _addFilesShow

    private val _compression = MutableLiveData<String>("40")
    var compression: LiveData<String> = _compression

    fun setAddFilesButtonShow(value: Boolean) {
        _addFilesShow.value = value
    }

    fun setCompression(value: String) {
        _compression.value = value
    }
}