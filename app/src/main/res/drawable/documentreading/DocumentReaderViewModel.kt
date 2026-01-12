package com.example.mobainfcinstallation.documentreading

import android.content.Context
import androidx.lifecycle.ViewModel
import bio.mobai.library.document.api.models.MBDocumentCaptureResult
import com.example.mobainfcinstallation.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class DocumentReaderViewModel(
    private val fragmentContainerProvider: FragmentContainerProvider = FragmentContainerProviderImpl()
): ViewModel() {

    private val _document = MutableStateFlow<MBDocumentCaptureResult?>(null)
    val document: StateFlow<MBDocumentCaptureResult?> = _document.asStateFlow()

    fun startCamera(context: Context, containerId: Int) {
       fragmentContainerProvider.setupFragment(context as MainActivity,containerId, onReadingResult = { _document.value = it})
    }

}