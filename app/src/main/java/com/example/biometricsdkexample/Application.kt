package com.example.biometricsdkexample

import android.app.Application
import bio.mobai.library.biometrics.capturesession.options.MBCameraOptions
import bio.mobai.library.biometrics.capturesession.options.MBCameraPosition
import bio.mobai.library.biometrics.capturesession.options.MBCaptureConstrains
import bio.mobai.library.biometrics.capturesession.options.MBCaptureSessionOptions
import bio.mobai.library.biometrics.capturesession.options.MBPreviewScaleType
import bio.mobai.library.biometrics.capturesession.options.MBTargetResolution
import bio.mobai.library.biometrics.capturesession.options.MBUIOptions

class SdkExampleApplication:  Application(){
    override fun onCreate() {
        super.onCreate()
        val prefs = applicationContext.getSharedPreferences("CaptureSessionOptions", MODE_PRIVATE)
        val backendServerAddress = prefs.getString("backendAddress", "http://192.168.0.113:8010")!!
        val isDebuggingEnabled = prefs.getBoolean("isDebuggingEnabled", false)
        val isNetworkCallEnabled = prefs.getBoolean("isNetworkCallEnabled", false)
        val isInjectionAttackEnabled = prefs.getBoolean("isInjectionAttackEnabled", false)
        val logLevel = prefs.getInt("logLevel", 0)
        val isSaveDataLocallyEnabled = prefs.getBoolean("isSaveDataLocallyEnabled", false)
        val isFaceStatusTextEnabled = prefs.getBoolean("isFaceStatusTextEnabled", false)
        val isFacePlacementTextEnabled = prefs.getBoolean("isFacePlacementTextEnabled", false)
        val isFaceAngleTextEnabled = prefs.getBoolean("isFaceAngleTextEnabled", false)
        val isTimerTextEnabled = prefs.getBoolean("isTimerTextEnabled", false)
        val isProgressBarEnabled = prefs.getBoolean("isProgressBarEnabled", false)
        val isShowOverlayFaceView = prefs.getBoolean("isShowOverlayFaceView", false)
        val cameraPosition = prefs.getInt("cameraPosition", 0)
        val targetResolution = prefs.getInt("targetResolution", 0)
        val isNewConstraintEnabled = prefs.getBoolean("isNewConstraintEnabled", false)
        val isPayloadOptimizationEnabled = prefs.getBoolean("isPayloadOptimizationEnabled", false)
        val captureType = prefs.getInt("captureType", 0)
        val constraintLevel = prefs.getInt("constraintLevel", 0)
        val numberOfFrames = prefs.getString("numberOfFrames", "3")!!
        val timeBeforeAutomaticCapture = prefs.getString("timeBeforeAutomaticCapture", "1")!!
        val videoCaptureDurationMs = prefs.getString("videoCaptureDurationMs", "1000")!!

        val captureSessionOptionsBuilder = MBCaptureSessionOptions.Builder()
            .automaticCapture(captureType == 0)
            .cameraQuality(
                MBCameraOptions(
                    cameraPosition = if (cameraPosition == 0) {
                        MBCameraPosition.FRONT
                    } else {
                        MBCameraPosition.REAR
                    },
                    targetResolution = when (targetResolution) {
                        0 -> MBTargetResolution.QHD
                        1 -> MBTargetResolution.HD_720
                        2 -> MBTargetResolution.HD_1080
                        3 -> MBTargetResolution.HD_4K
                        else -> MBTargetResolution.QHD
                    },
                    previewScaleType = MBPreviewScaleType.FILL_CENTER
                )
            )
            .captureConstrains(if (isNewConstraintEnabled) MBCaptureConstrains.V2 else MBCaptureConstrains.V1)
            .debugging(isDebuggingEnabled)
            .framesToCollect(Integer.parseInt(numberOfFrames))
            .payloadOptimization(isPayloadOptimizationEnabled)
            .timeBeforeCapture(Integer.parseInt(timeBeforeAutomaticCapture))
            .videoCaptureDuration(Integer.parseInt(videoCaptureDurationMs))

        captureSessionOptionsBuilder.showOval = isShowOverlayFaceView

        val captureSessionUIOptionsBuilder = MBUIOptions.Builder()
            .overlayFaceView(isShowOverlayFaceView)
            .faceStatusLabel(isFaceStatusTextEnabled)
            .progressBar(isProgressBarEnabled)
            .countdownTimerLabel(isTimerTextEnabled)

        AppData.shared.captureSessionOptions = captureSessionOptionsBuilder.build()
        AppData.shared.uiOptions = captureSessionUIOptionsBuilder.build()
        AppData.shared.backendServerAddress = backendServerAddress
    }
}