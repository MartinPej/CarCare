package com.cse3mad.carcare.ui.mycar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyCarViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is my car Fragment"
    }
    val text: LiveData<String> = _text
} 