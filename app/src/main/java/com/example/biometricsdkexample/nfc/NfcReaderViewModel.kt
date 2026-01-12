package com.example.biometricsdkexample.nfc

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.biometricsdkexample.MainActivity
import com.innovatrics.dot.nfc.reader.NfcTravelDocumentReaderResult
import com.mobai.library.nfc_reading.MBNfcKeyFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


/**
 * Represents the various states of the NFC scanning process.
 */
sealed class ScanningState {
    /** Waiting for a tag to be placed near the device. */
    data object Idle : ScanningState()

    /** Actively reading data groups from the chip. [progress] is 0-100. */
    data class Scanning(val progress: Int) : ScanningState()

    /** Establishing a secure connection (BAC/PACE) with the chip. */
    data object Access : ScanningState()

    /** Performing Passive/Active Authentication to verify document integrity. */
    data object Verifying : ScanningState()

    /** An error occurred during the process. Contains the [error] message. */
    data class Error(val error: String) : ScanningState()

    /** Successfully read the document. Contains the final [doc] data. */
    data class Finished(val doc: NfcTravelDocumentReaderResult) : ScanningState()
}

class NfcReaderViewModel(
    private val nfcReader: NfcReader
) : ViewModel() {

    // Internal mutable state
    private val _uiState = MutableStateFlow<ScanningState>(ScanningState.Idle)

    // External immutable state for the Fragment to observe
    val uiState: StateFlow<ScanningState> = _uiState.asStateFlow()

    init {
        // Observe the hardware events as long as the ViewModel exists
        viewModelScope.launch {
            nfcReader.nfcEventFlow.collect { event ->
                _uiState.value = when (event) {
                    is NfcEvent.AccessStarted -> ScanningState.Access
                    is NfcEvent.DataAuthenticationStarted -> ScanningState.Verifying
                    is NfcEvent.Progress -> ScanningState.Scanning(event.percent)
                    is NfcEvent.Success -> ScanningState.Finished(event.doc)
                    is NfcEvent.Error -> ScanningState.Error(event.e.message ?: "Unknown Error")
                    is NfcEvent.TagFound -> _uiState.value // No state change needed for TagFound
                }
            }
        }
    }

    /**
     * Triggers the NFC Reader Mode on the Activity.
     */
    fun enableNfcReading(activity: Activity, documentKeys: MBNfcKeyFactory) {
        viewModelScope.launch {
            // Re-wrap keys to ensure they match expected internal SDK format if necessary
            val keys = MBNfcKeyFactory(
                documentKeys.nfcKey.documentNumber,
                documentKeys.nfcKey.dateOfExpiry,
                documentKeys.nfcKey.dateOfBirth
            )
            nfcReader.read(activity as MainActivity, keys)
        }
    }

    /**
     * Safely disables the NFC Reader Mode to save battery and release hardware.
     */
    fun disableNfcReading(activity: Activity) {
        nfcReader.stopReading(activity)
    }
}