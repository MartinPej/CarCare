package com.cse3mad.carcare.ui.guides

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GuidesViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is guides Fragment"
    }
    val text: LiveData<String> = _text
} 