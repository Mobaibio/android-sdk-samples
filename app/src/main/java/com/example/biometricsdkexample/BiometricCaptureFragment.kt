package com.example.biometricsdkexample

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import bio.mobai.library.biometrics.capturesession.MBCaptureSessionService
import bio.mobai.library.biometrics.capturesession.options.MBCaptureSessionOptions

class BiometricCaptureFragment : Fragment(R.layout.fragment_biometric_capture) {
    private lateinit var permissionViewModel: PermissionsViewModel
    private var captureSessionOptions = MBCaptureSessionOptions.Builder().build()

    private lateinit var captureSessionService: MBCaptureSessionService

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissionViewModel = ViewModelProvider(requireActivity()).get(PermissionsViewModel::class.java)

        captureSessionService = MBCaptureSessionService(requireContext(), this, captureSessionOptions)

        permissionViewModel.cameraPermissionGranted.observe(requireActivity() as LifecycleOwner) { isGranted ->
            if (isGranted) {
                view.findViewById<FrameLayout>(R.id.biometric_container).addView(captureSessionService.getCaptureSessionView())
                captureSessionService.startCamera()
            }
        }
    }
}