package com.example.biometricsdkexample

import bio.mobai.library.biometrics.capturesession.options.MBCaptureSessionOptions
import bio.mobai.library.biometrics.capturesession.options.MBUIOptions

class AppData {
    lateinit var backendServerAddress: String
    lateinit var captureSessionOptions: MBCaptureSessionOptions
    lateinit var uiOptions: MBUIOptions

    companion object{
        var shared = AppData()
    }
}