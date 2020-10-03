package com.pratiksahu.dokify

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel @ViewModelInject constructor() : ViewModel() {

    private val _addFilesShow = MutableLiveData<Boolean>()
    var addFilesShow: LiveData<Boolean> = _addFilesShow

    fun setAddFilesButtonShow(value: Boolean) {
        _addFilesShow.value = value
    }
}