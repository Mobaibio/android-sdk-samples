package com.example.biometricsdkexample

import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import bio.mobai.library.biometrics.capturesession.MBCaptureSessionError
import bio.mobai.library.biometrics.capturesession.MBCaptureSessionResult
import bio.mobai.library.biometrics.capturesession.MBCaptureSessionService
import bio.mobai.library.biometrics.capturesession.MBCaptureState
import bio.mobai.library.biometrics.capturesession.MBDistanceError
import bio.mobai.library.biometrics.capturesession.MBFaceBoundingBoxStatus
import bio.mobai.library.biometrics.capturesession.MBFaceGeometryModel
import bio.mobai.library.biometrics.capturesession.MBPoseError
import bio.mobai.library.biometrics.capturesession.MBPositionError
import bio.mobai.library.biometrics.capturesession.listeners.MBBoundingBoxFaceValidatorListener
import bio.mobai.library.biometrics.capturesession.listeners.MBCaptureProgressListener
import bio.mobai.library.biometrics.capturesession.listeners.MBCaptureSessionListener
import bio.mobai.library.biometrics.capturesession.listeners.MBCountDownListener
import bio.mobai.library.biometrics.capturesession.options.MBCameraOptions
import bio.mobai.library.biometrics.capturesession.options.MBCaptureConstrains
import bio.mobai.library.biometrics.capturesession.options.MBCaptureSessionOptions
import bio.mobai.library.biometrics.capturesession.options.MBPreviewScaleType

// BiometricCaptureFragment handles biometric capture using the Mobai SDK.
class BiometricCaptureFragment : Fragment(R.layout.fragment_biometric_capture),
    MBBoundingBoxFaceValidatorListener, // Listens for face bounding box validation events
    MBCaptureProgressListener,          // Tracks capture progress
    MBCountDownListener,                // Monitors countdown events during capture
    MBCaptureSessionListener            // Listens for capture session state changes
{
    
    private lateinit var permissionViewModel: PermissionsViewModel
    private var captureSessionOptions = MBCaptureSessionOptions.Builder()
        .cameraQuality(MBCameraOptions(previewScaleType = MBPreviewScaleType.FILL_CENTER))
        .captureConstrains(MBCaptureConstrains.V2)
        .timeBeforeCapture(3)
        .build()
    
    // Capture session service to manage the biometric capture process
    private lateinit var captureSessionService: MBCaptureSessionService
    private lateinit var biometricOverlay: BiometricOverlay
    
    // UI elements for displaying capture status
    private lateinit var faceDistanceText: TextView
    private lateinit var facePoseText: TextView
    private lateinit var facePositionText: TextView
    private val notFoundText = "No face detected"
    
    private var sessionId: String? = null
    
    private lateinit var progressBar: ProgressBar
    private lateinit var timerText: TextView
    
    private lateinit var successViewModel: CaptureSuccessViewModel
    
    // Initialize TextViews to display status information
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        facePoseText = TextView(requireContext())
        facePoseText.text = notFoundText
        facePoseText.textSize = 18f
        
        faceDistanceText = TextView(requireContext())
        faceDistanceText.text = notFoundText
        faceDistanceText.textSize = 18f
        
        facePositionText = TextView(requireContext())
        facePositionText.text = notFoundText
        facePositionText.textSize = 18f
    }
    
    // Set up ViewModels to observe and manage state
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissionViewModel =
            ViewModelProvider(requireActivity()).get(PermissionsViewModel::class.java)
        successViewModel =
            ViewModelProvider(requireActivity()).get(CaptureSuccessViewModel::class.java)
        
        captureSessionService =
            MBCaptureSessionService(requireContext(), this, captureSessionOptions)
        captureSessionService.faceBoundingBoxValidatorListener = this
        captureSessionService.captureProgressListener = this
        captureSessionService.countDownListener = this
        captureSessionService.captureSessionListener = this
        
        // Initialize overlay for guiding user during capture
        biometricOverlay = BiometricOverlay(requireContext())
        sessionId = getString(R.string.sessionId).ifEmpty {
            null
        }
        // Observe camera permissions and start camera if granted
        permissionViewModel.cameraPermissionGranted.observe(requireActivity() as LifecycleOwner) { isGranted ->
            if (isGranted) {
                setOverlay(view)
                captureSessionService.startCamera(sessionId)
            }
        }
    }
    
    // Sets up overlay and status indicators
    private fun setOverlay(view: View) {
        view.findViewById<FrameLayout>(R.id.biometric_container).apply {
            addView(captureSessionService.getCaptureSessionView())
            addView(biometricOverlay)
        }
        
        view.findViewById<LinearLayout>(R.id.face_distance_status_container)
            .addView(faceDistanceText)
        view.findViewById<LinearLayout>(R.id.face_Pose_status_container).addView(facePoseText)
        view.findViewById<LinearLayout>(R.id.face_position_status_container)
            .addView(facePositionText)
        
        progressBar = view.findViewById<ProgressBar>(R.id.progressBar).apply { max = 100 }
        timerText = view.findViewById(R.id.timer)
    }
    
    
    // Callback for validating face bounding box during capture
    override fun onValidating(
        faceBoxStatus: MBFaceBoundingBoxStatus,
        faceGeometry: MBFaceGeometryModel
    ) {
        biometricOverlay.ovalColor = when {
            faceBoxStatus.positionErrors == null &&
                    faceBoxStatus.poseErrors == null &&
                    faceBoxStatus.distanceError == null -> BiometricOverlay.BorderColor.GREEN
            
            else -> BiometricOverlay.BorderColor.YELLOW
        }
        
        // Update UI with face validation results
        requireActivity().runOnUiThread {
            faceBoxStatus.positionErrors?.let {
                if (it.contains(MBPositionError.NOT_FOUND)) {
                    faceDistanceText.text = notFoundText
                    facePoseText.text = notFoundText
                    facePositionText.text = notFoundText
                } else {
                    updateValidationUI(faceBoxStatus)
                }
            }?:run {
                updateValidationUI(faceBoxStatus)
            }
        }
    }
    
    // Updates UI elements based on validation results
    private fun updateValidationUI(faceBoxStatus: MBFaceBoundingBoxStatus) {
        faceBoxStatus.distanceError?.let {
            timerText.visibility = View.INVISIBLE
            faceDistanceText.text = when (it) {
                MBDistanceError.TOO_FAR_AWAY -> "Too far away"
                MBDistanceError.TOO_CLOSE -> "Too close"
                else -> "Valid distance"
            }
        } ?: run { faceDistanceText.text = "Valid distance" }
        
        faceBoxStatus.poseErrors?.firstOrNull()?.let {
            timerText.visibility = View.INVISIBLE
            facePoseText.text = when (it) {
                MBPoseError.YAW_EXCESSIVE_FACE_ROTATION -> "Excessive yaw rotation"
                MBPoseError.PITCH_EXCESSIVE_FACE_ROTATION -> "Excessive pitch rotation"
                MBPoseError.ROLL_EXCESSIVE_FACE_ROTATION -> "Excessive roll rotation"
                else -> "Valid pose"
            }
        } ?: run { facePoseText.text = "Valid pose" }
        
        faceBoxStatus.positionErrors?.firstOrNull()?.let {
            timerText.visibility = View.INVISIBLE
            facePositionText.text = when (it) {
                MBPositionError.TOO_FAR_UP -> "Too far up"
                MBPositionError.TOO_FAR_DOWN -> "Too far down"
                MBPositionError.TOO_FAR_LEFT -> "Too far left"
                MBPositionError.TOO_FAR_RIGHT -> "Too far right"
                else -> "Valid position"
            }
        } ?: run { facePositionText.text = "Valid position" }
    }
    
    // Callback for updating capture progress
    override fun onCaptureProgress(captureProgressCounter: Float) {
        requireActivity().runOnUiThread {
            progressBar.setProgress((captureProgressCounter * 100).toInt(), true)
        }
    }
    
    // Callback for countdown timer during capture
    override fun onCountdown(timeCounter: Int) {
        requireActivity().runOnUiThread {
            if (timeCounter != 0) {
                timerText.visibility = View.VISIBLE
                timerText.text = "$timeCounter"
            } else {
                timerText.visibility = View.INVISIBLE
            }
        }
    }
    
    // Callback for handling capture session failure
    override fun onFailure(errorEnum: MBCaptureSessionError) {
        Toast.makeText(requireContext(), "Capture session failed", Toast.LENGTH_SHORT).show()
    }
    
    // Callback for changes in capture session state
    override fun onStateChanged(stateEnum: MBCaptureState) {
        // Handle state changes if necessary
    }
    
    // Callback for handling successful capture session results
    override fun onSuccess(result: MBCaptureSessionResult?) {
        result?.let {
            var endPoint = getString(R.string.endPoint)
            if(!endPoint.startsWith("/")){
                endPoint = "/$endPoint"
            }
            successViewModel.sendVideoToLocalhost(
                Base64.encodeToString(result.capturedVideoData, Base64.NO_WRAP),
                result.sessionVideoMetadata!!,
                Base64.encodeToString(result.faceImage, Base64.NO_WRAP),
                serverIP = getString(R.string.serverIp),
                serverPort = getString(R.string.serverPort),
                endPoint = endPoint,
                onSuccess = {
                    requireActivity().runOnUiThread {
                        successViewModel.frameExample.value = result?.faceImage
                        findNavController().navigate(R.id.action_biometricCaptureFragment_to_framesResultFragments)
                    }
                },
                onError = {
                    
                }
            )
        }
    }
}