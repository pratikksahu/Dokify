package com.pratiksahu.dokify

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel @ViewModelInject constructor() : ViewModel() {

    private val LiveList = MutableLiveData<ArrayList<String>>()
    val LiveData: LiveData<ArrayList<String>>? = LiveList
    fun initList() {
        LiveList.value = arrayListOf("a", "b", "c")
    }
}