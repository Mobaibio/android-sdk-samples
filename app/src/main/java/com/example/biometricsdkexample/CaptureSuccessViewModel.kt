package com.example.biometricsdkexample

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CaptureSuccessViewModel: ViewModel() {
    var frameExample = MutableLiveData<ByteArray>()
}