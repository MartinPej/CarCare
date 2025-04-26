package com.cse3mad.carcare.ui.mechanic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MechanicViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is mechanic Fragment"
    }
    val text: LiveData<String> = _text
} 