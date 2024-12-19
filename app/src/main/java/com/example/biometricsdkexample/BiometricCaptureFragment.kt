package com.example.biometricsdkexample

import android.os.Bundle
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

class BiometricCaptureFragment : Fragment(R.layout.fragment_biometric_capture),
    MBBoundingBoxFaceValidatorListener,
    MBCaptureProgressListener,
    MBCountDownListener,
    MBCaptureSessionListener
{
    private lateinit var permissionViewModel: PermissionsViewModel
    private var captureSessionOptions = MBCaptureSessionOptions.Builder()
        .cameraQuality(MBCameraOptions(previewScaleType = MBPreviewScaleType.FILL_CENTER))
        .captureConstrains(MBCaptureConstrains.V2)
        .timeBeforeCapture(3)
        .build()

    private lateinit var captureSessionService: MBCaptureSessionService
    private lateinit var overlay: BiometricOverlay

    private lateinit var faceDistanceText: TextView
    private lateinit var facePoseText: TextView
    private lateinit var facePositionText: TextView
    private val notFoundText = "No face detected"


    private lateinit var progressBar: ProgressBar
    private lateinit var timerText: TextView

    private lateinit var successViewModel: CaptureSuccessViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        facePoseText = TextView(requireContext())
        facePoseText.text = notFoundText
        facePoseText.textSize = 18f

        faceDistanceText = TextView(requireContext())
        faceDistanceText .text = notFoundText
        faceDistanceText.textSize = 18f

        facePositionText = TextView(requireContext())
        facePositionText.text = notFoundText
        facePositionText.textSize = 18f
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissionViewModel = ViewModelProvider(requireActivity()).get(PermissionsViewModel::class.java)
        successViewModel = ViewModelProvider(requireActivity()).get(CaptureSuccessViewModel::class.java)

        captureSessionService = MBCaptureSessionService(requireContext(), this, captureSessionOptions)
        captureSessionService.faceBoundingBoxValidatorListener = this
        captureSessionService.captureProgressListener = this
        captureSessionService.countDownListener = this
        captureSessionService.captureSessionListener = this

        overlay = BiometricOverlay(requireContext())

        permissionViewModel.cameraPermissionGranted.observe(requireActivity() as LifecycleOwner) { isGranted ->
            if (isGranted) {
                setOverlay(view)
                captureSessionService.startCamera()
            }
        }
    }

    private fun setOverlay(view: View) {
        view.findViewById<FrameLayout>(R.id.biometric_container).addView(captureSessionService.getCaptureSessionView())
        view.findViewById<FrameLayout>(R.id.biometric_container).addView(overlay)
        view.findViewById<LinearLayout>(R.id.face_distance_status_container).addView(faceDistanceText)
        view.findViewById<LinearLayout>(R.id.face_Pose_status_container).addView(facePoseText)
        view.findViewById<LinearLayout>(R.id.face_position_status_container).addView(facePositionText)
        progressBar = view.findViewById(R.id.progressBar)
        progressBar.max = 100
        timerText = view.findViewById(R.id.timer)
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

        requireActivity().runOnUiThread {
           faceBoxStatus.positionErrors?.let {
               if (it.contains(MBPositionError.NOT_FOUND)) {
                   faceDistanceText.text = notFoundText
                   facePoseText.text = notFoundText
                   facePositionText.text = notFoundText
               } else {
                   if (faceBoxStatus.distanceError != null) {
                       timerText.visibility = View.INVISIBLE
                       when(faceBoxStatus.distanceError) {
                           MBDistanceError.TOO_FAR_AWAY -> { faceDistanceText.text = "Too far away"}
                           MBDistanceError.TOO_CLOSE -> { faceDistanceText.text = "Too close" }
                           else -> {}
                       }
                   } else { faceDistanceText.text = "Valid distance" }

                   if (faceBoxStatus.poseErrors.isNullOrEmpty()) {
                       facePoseText.text = "Valid pose"
                   } else {
                       timerText.visibility = View.INVISIBLE
                       when(faceBoxStatus.poseErrors!!.first()) {
                           MBPoseError.YAW_EXCESSIVE_FACE_ROTATION -> { facePoseText.text = "Excessive yaw rotation" }
                           MBPoseError.PITCH_EXCESSIVE_FACE_ROTATION -> { facePoseText.text = "Excessive pitch rotation" }
                           MBPoseError.ROLL_EXCESSIVE_FACE_ROTATION -> { facePoseText.text = "Excessive roll rotation" }
                       }
                   }


                   if (faceBoxStatus.positionErrors.isNullOrEmpty()) {
                       facePositionText.text = "Valid position"
                   } else {
                       timerText.visibility = View.INVISIBLE
                       when(faceBoxStatus.positionErrors!!.first()) {
                           MBPositionError.TOO_FAR_UP -> { facePositionText.text = "Too far up" }
                           MBPositionError.TOO_FAR_DOWN -> {facePositionText.text = "Too far down" }
                           MBPositionError.TOO_FAR_LEFT -> {facePositionText.text = "Too far left" }
                           MBPositionError.TOO_FAR_RIGHT -> {facePositionText.text = "Too far right" }
                           MBPositionError.NOT_FOUND -> {}
                       }
                   }

               }
           }
        }
    }

    override fun onCaptureProgress(captureProgressCounter: Float) {
        requireActivity().runOnUiThread {
            progressBar.setProgress((captureProgressCounter * 100).toInt(),true)
        }
    }

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

    override fun onFailure(errorEnum: MBCaptureSessionError) {
      Toast.makeText(requireContext(), "Capture session failed", Toast.LENGTH_SHORT).show()
    }

    override fun onStateChanged(stateEnum: MBCaptureState) {

    }

    override fun onSuccess(result: MBCaptureSessionResult?) {
      requireActivity().runOnUiThread {
          successViewModel.frameExample.value = result?.faceImage
          findNavController().navigate(R.id.action_biometricCaptureFragment_to_framesResultFragments)
      }
    }


}