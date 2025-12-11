package com.example.biometricsdkexample

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.biometricsdkexample.databinding.FragmentFramesResultFragmentsBinding
import org.json.JSONObject
import java.io.ByteArrayInputStream


class FramesResultFragments : Fragment() {
    private lateinit var successViewModel: CaptureSuccessViewModel
    private var _binding: FragmentFramesResultFragmentsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFramesResultFragmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        successViewModel = ViewModelProvider(requireActivity())[CaptureSuccessViewModel::class.java]

        val responseJson = JSONObject(successViewModel.responseFromServer)
        val recognitionResult = responseJson.getJSONObject("recognition")
        val padResult = responseJson.getJSONObject("pad")
        val authenticityResult = responseJson.getJSONObject("authenticity")
        val integrityResult = responseJson.getJSONObject("integrity")
        val deepfakeResult = responseJson.getJSONObject("deepfake")
        val faceQuality = responseJson.getJSONObject("face_quality_probe").getJSONObject("metrics")

        binding.txtTransactionEnded.text = "Verification Completed"
        binding.txtRecognition.text =
            formatResult("Recognition Result", recognitionResult.getBoolean("passed"))
        binding.txtPad.text = formatResult("Pad Check", padResult.getBoolean("passed"))
        binding.txtAuthenticity.text =
            formatResult("Authenticity Result", authenticityResult.getBoolean("passed"))
        binding.txtIntegrity.text =
            formatResult("Integrity Result", integrityResult.getBoolean("passed"))
        binding.txtDeepfake.text =
            formatResult("Deepfake Result", deepfakeResult.getBoolean("passed"))

        binding.txtIlluminanceUniformity.text =
            "${binding.txtIlluminanceUniformity.text}: ${faceQuality.getString("illumination_uniformity")}"
        binding.txtLuminanceMean.text =
            "${binding.txtLuminanceMean.text}: ${faceQuality.getString("luminance_mean")}"
        binding.txtLuminanceVariance.text =
            "${binding.txtLuminanceVariance.text}: ${faceQuality.getString("luminance_variance")}"
        binding.txtUnderExposure.text =
            "${binding.txtUnderExposure.text}: ${faceQuality.getString("under_exposure_prevention")}"
        binding.txtOverExposure.text =
            "${binding.txtOverExposure.text}: ${faceQuality.getString("over_exposure_prevention")}"
        binding.txtDynamicRange.text =
            "${binding.txtDynamicRange.text}: ${faceQuality.getString("dynamic_range")}"
        binding.txtSharpness.text =
            "${binding.txtSharpness.text}: ${faceQuality.getString("sharpness")}"
        binding.txtNaturalColor.text =
            "${binding.txtNaturalColor.text}: ${faceQuality.getString("natural_color")}"

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }
        /*successViewModel.frameExample.observe(requireActivity() as LifecycleOwner) { frame ->
            frame?.let {
                view.findViewById<ImageView>(R.id.iv_frame_example).setImageBitmap(toBitmap(frame))
            }
        }*/
    }

    private fun formatResult(name: String, passed: Boolean): String {
        val icon = if (passed) "✅" else "❌"
        val status = if (passed) "Passed" else "Failed"
        return "$name: $icon $status"
    }

    private fun toBitmap(frame: ByteArray): Bitmap {
        val arrayInputStream = ByteArrayInputStream(frame)
        return BitmapFactory.decodeStream(arrayInputStream)
    }
}