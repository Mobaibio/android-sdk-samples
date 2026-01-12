package com.example.biometricsdkexample.documentreading

import androidx.appcompat.app.AppCompatActivity
import bio.mobai.library.document.api.models.MBDocumentCaptureResult
import com.example.biometricsdkexample.MainActivity

interface FragmentContainerProvider {
    fun setupFragment(
        activity: AppCompatActivity,
        containerId: Int,
        onReadingResult: (MBDocumentCaptureResult) -> Unit
    )
    fun removeFragment(
        activity: MainActivity,
        id: Int
    )
}

class FragmentContainerProviderImpl(
    private val documentDataSource: DocumentDataSource = DocumentDataSourceImpl()
): FragmentContainerProvider {

    override fun setupFragment(
        activity: AppCompatActivity,
        containerId: Int,
        onReadingResult: (MBDocumentCaptureResult) -> Unit
    ) {
        val fragmentManager = activity.supportFragmentManager

        val fragment = documentDataSource.getCaptureFragment(activity as MainActivity, onResult = {onReadingResult(it)})

        if (!fragmentManager.isStateSaved) {
            val existingFragment = fragmentManager.findFragmentById(containerId)
            if (existingFragment == null || existingFragment::class != fragment::class) {
                fragmentManager
                    .beginTransaction()
                    .replace(containerId, fragment)
                    .commitAllowingStateLoss()
            }
        }
    }

    override fun removeFragment(
        activity: MainActivity,
        id: Int
    ) {
        val fragmentManager = activity.supportFragmentManager
        val fragment = fragmentManager.findFragmentById(id)

        if (fragment != null && !fragmentManager.isStateSaved) {
            fragmentManager
                .beginTransaction()
                .remove(fragment)
                .commitNow()
            fragmentManager.executePendingTransactions()
        }
    }

}
