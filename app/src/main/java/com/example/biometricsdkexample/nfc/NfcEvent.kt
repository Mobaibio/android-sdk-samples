package com.example.biometricsdkexample.nfc

import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import android.os.Bundle
import com.example.biometricsdkexample.MainActivity
import com.innovatrics.dot.nfc.reader.NfcTravelDocumentReaderResult
import com.mobai.library.nfc_reading.MBNfcKeyFactory
import com.mobai.library.nfc_reading.MBNfcTravelDocumentReader
import com.mobai.library.nfc_reading.MBNfcTravelDocumentReaderInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Defines all possible signals originating from the NFC Hardware and SDK.
 * This class acts as the bridge between raw hardware callbacks and the ViewModel.
 */
sealed class NfcEvent {

    /** Emitted the instant an NFC tag enters the phone's electromagnetic field. */
    object TagFound : NfcEvent()

    /** Emitted when the secure communication channel is being established. */
    object AccessStarted : NfcEvent()

    /** Emitted when the reader begins validating the chip's security certificates. */
    object DataAuthenticationStarted : NfcEvent()

    /** * Emitted during the heavy data transfer phase.
     * @param percent The completion percentage (0 to 100).
     */
    data class Progress(val percent: Int) : NfcEvent()

    /** * Emitted when the document has been fully read and verified.
     * @param doc The complete data set extracted from the NFC chip.
     */
    data class Success(val doc: NfcTravelDocumentReaderResult) : NfcEvent()

    /** * Emitted if the process is interrupted or fails.
     * @param e The exception detailing what went wrong (e.g., connection lost).
     */
    data class Error(val e: Exception) : NfcEvent()
}


/**
 * Concrete implementation of the NfcReader interface.
 * Coordinates between Android's NfcAdapter and the Mobai SDK reader.
 */
class NfcReaderImpl(
    private val context: Context,
    private val nfcTravelDocumentReader: MBNfcTravelDocumentReader,
): NfcReader {

    private val nfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(context)
    }

    // SharedFlow allows multiple collectors and handles events asynchronously
    private val _nfcEventFlow = MutableSharedFlow<NfcEvent>(extraBufferCapacity = 1)
    override val nfcEventFlow: Flow<NfcEvent> = _nfcEventFlow.asSharedFlow()

    /**
     * Interface listener that converts SDK callbacks into Flow events.
     */
    private val listener = object : MBNfcTravelDocumentReaderInterface {
        override fun onAccessEstablishmentStarted() {
            _nfcEventFlow.tryEmit(NfcEvent.AccessStarted)
        }

        override fun onReadingError(exception: Exception) {
            _nfcEventFlow.tryEmit(NfcEvent.Error(exception))
        }

        override fun onReadingFinished(document: NfcTravelDocumentReaderResult) {
            _nfcEventFlow.tryEmit(NfcEvent.Success(document))
        }

        override fun onProgress(progress: Int) {
            _nfcEventFlow.tryEmit(NfcEvent.Progress(progress))
        }

        override fun onDataAuthenticationStarted() {
            _nfcEventFlow.tryEmit(NfcEvent.DataAuthenticationStarted)
        }
    }

    init {
        // Attach the listener to the Mobai SDK reader instance
        nfcTravelDocumentReader.setListener(listener)
    }

    /**
     * Enables Android Reader Mode.
     * When a tag is discovered, it is passed directly to the Mobai SDK for reading.
     */
    override fun read(activity: MainActivity, key: MBNfcKeyFactory) {
        val options = Bundle().apply {
            // Delay check to maintain connection stability during high data transfer
            putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
        }

        nfcAdapter?.enableReaderMode(
            activity,
            { tag ->
                _nfcEventFlow.tryEmit(NfcEvent.TagFound)
                // The actual decryption and reading happens here
                nfcTravelDocumentReader.read(tag, key)
            },
            NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            options,
        )
    }

    /**
     * Disables Reader Mode to release the NFC hardware controller.
     */
    override fun stopReading(activity: Activity) {
        nfcAdapter?.disableReaderMode(activity)
    }
}