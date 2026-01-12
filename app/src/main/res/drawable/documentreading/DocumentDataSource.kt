package com.example.mobainfcinstallation.documentreading

import android.content.Context
import bio.mobai.library.document.api.MBDocumentCaptureFragment
import bio.mobai.library.document.api.MBDocumentCaptureFragmentListener
import bio.mobai.library.document.api.MBDocumentCaptureOptions
import bio.mobai.library.document.api.models.MBDocumentCaptureResult
import bio.mobai.library.document.internal.sdk.DotSdkInitializer
import com.example.mobainfcinstallation.R
import com.example.mobainfcinstallation.MainActivity

interface DocumentDataSource {
    fun getCaptureFragment(
        activity: MainActivity,
        onResult: (MBDocumentCaptureResult) -> Unit
    ): MBDocumentCaptureFragment
}

/**
 * DocumentDataSourceImpl dedicated class that binds the document capture sdk
 */
class DocumentDataSourceImpl: com.example.biometricsdkexample.documentreading.DocumentDataSource {
    override fun getCaptureFragment(
        activity: MainActivity,
        onResult: (MBDocumentCaptureResult) -> Unit
    ): MBDocumentCaptureFragment {
     
        val lic = activity.resources.openRawResource(R.raw.iengine).use { it.readBytes() }

        if (DotSdkInitializer.isInitialized()) {
            DotSdkInitializer.deinitialize()
        }
        DotSdkInitializer.initialize(activity as Context, lic)

        val options = MBDocumentCaptureOptions
                .Builder()
                .enableMrzReading(true)
                .build()

        return MBDocumentCaptureFragment.newInstance(
            options = options,
            license = lic,
            listener = MBDocumentCaptureFragmentListener { result -> onResult(result) },
        ) as MBDocumentCaptureFragment
    }
}
