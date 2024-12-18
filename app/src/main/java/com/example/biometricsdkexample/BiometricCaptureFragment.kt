package com.example.biometricsdkexample

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider

class BiometricCaptureFragment : Fragment(R.layout.fragment_biometric_capture) {
    private lateinit var permissionViewModel: PermissionsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissionViewModel = ViewModelProvider(requireActivity()).get(PermissionsViewModel::class.java)

        permissionViewModel.cameraPermissionGranted.observe(requireActivity() as LifecycleOwner) { isGranted ->
            if (isGranted) {
                Toast.makeText(requireContext(), "PermissionGranted", Toast.LENGTH_SHORT).show()
            }
        }
    }
}