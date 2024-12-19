package com.example.biometricsdkexample

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import bio.mobai.library.biometrics.capturesession.MBCaptureSessionService
import bio.mobai.library.biometrics.capturesession.MBFaceBoundingBoxStatus
import bio.mobai.library.biometrics.capturesession.MBFaceGeometryModel
import bio.mobai.library.biometrics.capturesession.listeners.MBBoundingBoxFaceValidatorListener
import bio.mobai.library.biometrics.capturesession.options.MBCameraOptions
import bio.mobai.library.biometrics.capturesession.options.MBCaptureConstrains
import bio.mobai.library.biometrics.capturesession.options.MBCaptureSessionOptions
import bio.mobai.library.biometrics.capturesession.options.MBPreviewScaleType

class BiometricCaptureFragment : Fragment(R.layout.fragment_biometric_capture),
    MBBoundingBoxFaceValidatorListener
{
    private lateinit var permissionViewModel: PermissionsViewModel
    private var captureSessionOptions = MBCaptureSessionOptions.Builder()
        .cameraQuality(MBCameraOptions(previewScaleType = MBPreviewScaleType.FILL_CENTER))
        .captureConstrains(MBCaptureConstrains.V2)
        .build()

    private lateinit var captureSessionService: MBCaptureSessionService
    private
    lateinit var overlay: BiometricOverlay

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissionViewModel = ViewModelProvider(requireActivity()).get(PermissionsViewModel::class.java)

        captureSessionService = MBCaptureSessionService(requireContext(), this, captureSessionOptions)
        captureSessionService.faceBoundingBoxValidatorListener = this

        overlay = BiometricOverlay(requireContext())

        permissionViewModel.cameraPermissionGranted.observe(requireActivity() as LifecycleOwner) { isGranted ->
            if (isGranted) {
                view.findViewById<FrameLayout>(R.id.biometric_container).addView(captureSessionService.getCaptureSessionView())
                view.findViewById<FrameLayout>(R.id.biometric_container).addView(overlay)

                captureSessionService.startCamera()
            }
        }
    }

    override fun onValidating(
        faceBoxStatus: MBFaceBoundingBoxStatus,
        faceGeometry: MBFaceGeometryModel
    ) {
        if (
            faceBoxStatus.positionErrors == null &&
            faceBoxStatus.poseErrors == null &&
            faceBoxStatus.distanceError == null
            ) {
            overlay.ovalColor = BiometricOverlay.BorderColor.GREEN
        } else {
            overlay.ovalColor = BiometricOverlay.BorderColor.YELLOW
        }
    }
}