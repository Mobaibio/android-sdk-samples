package com.example.biometricsdkexample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {
    private lateinit var permissionViewModel: PermissionsViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        permissionViewModel = ViewModelProvider(this).get(PermissionsViewModel::class.java)
        requestCameraPermission()
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        permissionViewModel.cameraPermissionGranted.value  = it
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                permissionViewModel.cameraPermissionGranted.value  = true
            }
            else -> {
                permissionViewModel.cameraPermissionGranted.value  = false
                cameraPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        }
    }
}