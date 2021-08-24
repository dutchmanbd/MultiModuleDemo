package com.ticonsys.smsverification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SmsVerificationViewModel @Inject constructor(
) : ViewModel() {


    private val _message = MutableLiveData<String>()
    val message: LiveData<String>
        get() = _message

    fun updateMessage(message: String?) {
        _message.postValue(message ?: "")
    }
}