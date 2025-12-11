package com.example.biometricsdkexample

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bio.mobai.library.biometrics.capturesession.options.MBCameraOptions
import bio.mobai.library.biometrics.capturesession.options.MBCameraPosition
import bio.mobai.library.biometrics.capturesession.options.MBCaptureConstrains
import bio.mobai.library.biometrics.capturesession.options.MBCaptureSessionOptions
import bio.mobai.library.biometrics.capturesession.options.MBPreviewScaleType
import bio.mobai.library.biometrics.capturesession.options.MBTargetResolution
import bio.mobai.library.biometrics.capturesession.options.MBUIOptions
import com.example.biometricsdkexample.databinding.SheetSettingsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SettingsSheet : BottomSheetDialogFragment() {
    private var isDebuggingEnabled: Boolean = false
    private var isNetworkCallEnabled: Boolean = false
    private var isInjectionAttackEnabled: Boolean = false
    private var logLevel: Int = 0
    private var isSaveDataLocallyEnabled: Boolean = false
    private var isFaceStatusTextEnabled: Boolean = false
    private var isFacePlacementTextEnabled: Boolean = false
    private var isFaceAngleTextEnabled: Boolean = false
    private var isTimerTextEnabled: Boolean = false
    private var isProgressBarEnabled: Boolean = false
    private var isShowOverlayFaceView: Boolean = false
    private var cameraPosition: Int = 0
    private var targetResolution: Int = 0
    private var isNewConstraintEnabled: Boolean = false
    private var isPayloadOptimizationEnabled: Boolean = false
    private var captureType: Int = 0
    private var constraintLevel: Int = 0
    private var numberOfFrames: String = "3"
    private var timeBeforeAutomaticCapture: String = "1"
    private var videoCaptureDurationMs: String = "1000"
    private var _binding: SheetSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SheetSettingsBinding.inflate(inflater, container, false)
        prefs =
            requireActivity().getSharedPreferences("CaptureSessionOptions", Context.MODE_PRIVATE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSave.setOnClickListener {
            with(prefs.edit()) {
                putBoolean("isDebuggingEnabled", isDebuggingEnabled)
                putBoolean("isNetworkCallEnabled", isNetworkCallEnabled)
                putBoolean("isInjectionAttackEnabled", isInjectionAttackEnabled)
                putInt("logLevel", logLevel)
                putBoolean("isSaveDataLocallyEnabled", isSaveDataLocallyEnabled)
                putBoolean("isFaceStatusTextEnabled", isFaceStatusTextEnabled)
                putBoolean("isFacePlacementTextEnabled", isFacePlacementTextEnabled)
                putBoolean("isFaceAngleTextEnabled", isFaceAngleTextEnabled)
                putBoolean("isTimerTextEnabled", isTimerTextEnabled)
                putBoolean("isProgressBarEnabled", isProgressBarEnabled)
                putBoolean("isShowOverlayFaceView", isShowOverlayFaceView)
                putInt("cameraPosition", cameraPosition)
                putInt("targetResolution", targetResolution)
                putBoolean("isNewConstraintEnabled", isNewConstraintEnabled)
                putBoolean("isPayloadOptimizationEnabled", isPayloadOptimizationEnabled)
                putInt("captureType", captureType)
                putInt("constraintLevel", constraintLevel)
                putString("numberOfFrames", binding.edtNumberOfFrames.text.toString())
                putString(
                    "timeBeforeAutomaticCapture",
                    binding.edtTimeBeforeInterval.text.toString()
                )
                putString("videoCaptureDurationMs", binding.edtDurationMs.text.toString())
                apply()
                commit()
            }
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
            this.dismiss()
        }

        binding.btnExit.setOnClickListener {
            this.dismiss()
        }

        isDebuggingEnabled = prefs.getBoolean("isDebuggingEnabled", false)
        isNetworkCallEnabled = prefs.getBoolean("isNetworkCallEnabled", false)
        isInjectionAttackEnabled = prefs.getBoolean("isInjectionAttackEnabled", false)
        logLevel = prefs.getInt("logLevel", 0)
        isSaveDataLocallyEnabled = prefs.getBoolean("isSaveDataLocallyEnabled", false)
        isFaceStatusTextEnabled = prefs.getBoolean("isFaceStatusTextEnabled", false)
        isFacePlacementTextEnabled = prefs.getBoolean("isFacePlacementTextEnabled", false)
        isFaceAngleTextEnabled = prefs.getBoolean("isFaceAngleTextEnabled", false)
        isTimerTextEnabled = prefs.getBoolean("isTimerTextEnabled", false)
        isProgressBarEnabled = prefs.getBoolean("isProgressBarEnabled", false)
        isShowOverlayFaceView = prefs.getBoolean("isShowOverlayFaceView", false)
        cameraPosition = prefs.getInt("cameraPosition", 0)
        targetResolution = prefs.getInt("targetResolution", 0)
        isNewConstraintEnabled = prefs.getBoolean("isNewConstraintEnabled", false)
        isPayloadOptimizationEnabled = prefs.getBoolean("isPayloadOptimizationEnabled", false)
        captureType = prefs.getInt("captureType", 0)
        constraintLevel = prefs.getInt("constraintLevel", 0)
        numberOfFrames = prefs.getString("numberOfFrames", "3")!!
        timeBeforeAutomaticCapture = prefs.getString("timeBeforeAutomaticCapture", "1")!!
        videoCaptureDurationMs = prefs.getString("videoCaptureDurationMs", "1000")!!

        binding.btnGroupDebug.check(if (isDebuggingEnabled) R.id.debug_enabled else R.id.debug_disabled)
        binding.btnGroupNetwork.check(if (isNetworkCallEnabled) R.id.network_enabled else R.id.network_disabled)
        binding.btnGroupInjection.check(if (isInjectionAttackEnabled) R.id.injection_enabled else R.id.injection_disabled)
        binding.btnGroupLogLevel.check(
            when (logLevel) {
                0 -> R.id.log_none
                1 -> R.id.log_error
                2 -> R.id.log_warning
                3 -> R.id.log_info
                4 -> R.id.log_debug
                else -> R.id.log_none
            }
        )
        binding.btnGroupSaveData.check(if (isSaveDataLocallyEnabled) R.id.save_local_data_enabled else R.id.save_local_data_disabled)
        binding.btnGroupFaceStatusText.check(if (isFaceStatusTextEnabled) R.id.face_status_text_enabled else R.id.face_status_text_disabled)
        binding.btnGroupFacePlacementText.check(if (isFacePlacementTextEnabled) R.id.face_placement_text_enabled else R.id.face_placement_text_disabled)
        binding.btnGroupFaceAngleText.check(if (isFaceAngleTextEnabled) R.id.face_angle_text_enabled else R.id.face_angle_text_disabled)
        binding.btnGroupTimerText.check(if (isTimerTextEnabled) R.id.face_timer_text_enabled else R.id.face_timer_text_disabled)
        binding.btnGroupProgressBarEnabled.check(if (isProgressBarEnabled) R.id.progress_bar_enabled else R.id.progress_bar_disabled)
        binding.btnGroupShouldShowOverlay.check(if (isShowOverlayFaceView) R.id.show_overlay_enabled else R.id.show_overlay_disabled)
        binding.btnGroupNewConstraintsEnabled.check(if (isNewConstraintEnabled) R.id.new_constraints_enabled else R.id.new_constraints_disabled)
        binding.btnGroupCameraPosition.check(if (cameraPosition == 0) R.id.camera_position_front else R.id.camera_position_rear)
        binding.btnGroupCameraResolution.check(
            when (targetResolution) {
                0 -> R.id.camera_resolution_qhd
                1 -> R.id.camera_resolution_720p
                2 -> R.id.camera_resolution_1080p
                3 -> R.id.camera_resolution_4k
                else -> R.id.camera_position_rear
            }
        )
        binding.btnGroupCaptureType.check(if (captureType == 0) R.id.capture_type_automatic else R.id.capture_type_manual)
        binding.btnGroupConstraintLevel.check(
            when (constraintLevel) {
                0 -> R.id.constraint_level_easy
                1 -> R.id.constraint_level_medium
                2 -> R.id.constraint_level_strict
                else -> R.id.constraint_level_easy
            }
        )
        binding.edtNumberOfFrames.setText(numberOfFrames)
        binding.edtTimeBeforeInterval.setText(timeBeforeAutomaticCapture)
        binding.edtDurationMs.setText(videoCaptureDurationMs)

        binding.btnGroupDebug.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked) {
                isDebuggingEnabled = when (checkedId) {
                    R.id.debug_enabled -> true
                    R.id.debug_disabled -> false
                    else -> false
                }
            }
        }
        binding.btnGroupNetwork.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked)
                isNetworkCallEnabled = when (checkedId) {
                    R.id.network_enabled -> true
                    R.id.network_disabled -> false
                    else -> false
                }
        }
        binding.btnGroupInjection.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked)
                isInjectionAttackEnabled = when (checkedId) {
                    R.id.injection_enabled -> true
                    R.id.injection_disabled -> false
                    else -> false
                }
        }
        binding.btnGroupLogLevel.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked)
                logLevel = when (checkedId) {
                    R.id.log_none -> 0
                    R.id.log_error -> 1
                    R.id.log_warning -> 2
                    R.id.log_info -> 3
                    R.id.log_debug -> 4
                    else -> 0
                }
        }
        binding.btnGroupSaveData.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked)
                isSaveDataLocallyEnabled = when (checkedId) {
                    R.id.save_local_data_enabled -> true
                    R.id.save_local_data_disabled -> false
                    else -> false
                }
        }
        binding.btnGroupFaceStatusText.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked)
                isFaceStatusTextEnabled = when (checkedId) {
                    R.id.face_status_text_enabled -> true
                    R.id.face_status_text_disabled -> false
                    else -> false
                }
        }
        binding.btnGroupFacePlacementText.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked)
                isFacePlacementTextEnabled = when (checkedId) {
                    R.id.face_placement_text_enabled -> true
                    R.id.face_placement_text_disabled -> false
                    else -> false
                }
        }
        binding.btnGroupFaceAngleText.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked)
                isFaceAngleTextEnabled = when (checkedId) {
                    R.id.face_angle_text_enabled -> true
                    R.id.face_angle_text_disabled -> false
                    else -> false
                }
        }
        binding.btnGroupTimerText.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked)
                isTimerTextEnabled = when (checkedId) {
                    R.id.face_timer_text_enabled -> true
                    R.id.face_timer_text_disabled -> false
                    else -> false
                }
        }
        binding.btnGroupProgressBarEnabled.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked)
                isProgressBarEnabled = when (checkedId) {
                    R.id.show_overlay_enabled -> true
                    R.id.show_overlay_disabled -> false
                    else -> false
                }
        }
        binding.btnGroupNewConstraintsEnabled.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked)
                isNewConstraintEnabled = when (checkedId) {
                    R.id.new_constraints_enabled -> true
                    R.id.new_constraints_disabled -> false
                    else -> false
                }
        }
        binding.btnGroupCameraPosition.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked)
                cameraPosition = when (checkedId) {
                    R.id.camera_position_front -> 0
                    R.id.camera_position_rear -> 1
                    else -> 0
                }
        }
        binding.btnGroupCameraResolution.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked)
                targetResolution = when (checkedId) {
                    R.id.camera_resolution_qhd -> 0
                    R.id.camera_resolution_720p -> 1
                    R.id.camera_resolution_1080p -> 2
                    R.id.camera_resolution_4k -> 3
                    else -> 0
                }
        }
        binding.btnGroupCaptureType.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked)
                captureType = when (checkedId) {
                    R.id.capture_type_automatic -> 0
                    R.id.capture_type_manual -> 1
                    else -> 0
                }
        }
        binding.btnGroupConstraintLevel.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked)
                constraintLevel = when (checkedId) {
                    R.id.constraint_level_easy -> 0
                    R.id.constraint_level_medium -> 1
                    R.id.constraint_level_strict -> 2
                    else -> 0
                }
        }

    }
}