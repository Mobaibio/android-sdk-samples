package com.example.biometricsdkexample.nfc

import android.app.Activity
import com.example.biometricsdkexample.MainActivity
import com.mobai.library.nfc_reading.MBNfcKeyFactory
import kotlinx.coroutines.flow.Flow

interface NfcReader {
    fun read(activity: MainActivity, key: MBNfcKeyFactory)

    fun stopReading(activity: Activity)

    val nfcEventFlow: Flow<NfcEvent>
}