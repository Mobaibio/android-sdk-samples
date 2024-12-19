package com.example.biometricsdkexample

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import java.io.ByteArrayInputStream


class FramesResultFragments : Fragment(R.layout.fragment_frames_result_fragments) {
   private lateinit var successViewModel: CaptureSuccessViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        successViewModel = ViewModelProvider(requireActivity()).get(CaptureSuccessViewModel::class.java)

        successViewModel.frameExample.observe(requireActivity() as LifecycleOwner) { frame ->
            frame?.let {
                view.findViewById<ImageView>(R.id.iv_frame_example).setImageBitmap(toBitmap(frame))
            }
        }
    }

    private fun toBitmap(frame: ByteArray): Bitmap {
        val arrayInputStream = ByteArrayInputStream(frame)
        return BitmapFactory.decodeStream(arrayInputStream)
    }
}