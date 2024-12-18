package com.example.biometricsdkexample

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PermissionsViewModel: ViewModel() {
    val cameraPermissionGranted = MutableLiveData<Boolean>()
}