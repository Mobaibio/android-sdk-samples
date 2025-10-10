package com.example.biometricsdkexample

import android.os.NetworkOnMainThreadException
import android.util.Base64
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException

class CaptureSuccessViewModel : ViewModel() {
    var frameExample = MutableLiveData<ByteArray>()
    private val defalultDispatcher: CoroutineDispatcher = Dispatchers.Main
    private val scope = CoroutineScope(defalultDispatcher)
    
    fun sendVideoToLocalhost(
        videoDataBase64: String,
        sessionMetaData: String,
        faceImageDataBase64: String,
        serverIP: String = "",
        serverPort: String = "",
        onSuccess: (() -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    ) {
        scope.launch {
            var connection: HttpURLConnection? = null
            
            try {
                val url = URL("http://$serverIP:$serverPort/videoToDecodeDemo")
                
                connection = withContext(Dispatchers.IO) {
                    url.openConnection() as HttpURLConnection
                }
                
                withContext(Dispatchers.IO) {
                    connection.apply {
                        requestMethod = "POST"
                        setRequestProperty("Content-Type", "application/json")
                        setRequestProperty("Accept", "application/json")
                        doOutput = true
                        connectTimeout = 10000 // 10 seconds
                        readTimeout = 10000 // 10 seconds
                    }
                }
                
                // Generate filename with timestamp
                val timestamp = System.currentTimeMillis()
                val fileName = "android_video_$timestamp.mp4"
                
                // Validate and clean the video data
                val cleanVideoData = videoDataBase64.trim()
                
                // Check if video data is valid base64
                val isValidBase64 = try {
                    Base64.decode(cleanVideoData, Base64.DEFAULT)
                    true
                } catch (e: Exception) {
                    false
                }
                
                if (!isValidBase64) {
                    onError?.invoke(Exception("Invalid base64 video data"))
                    return@launch
                }
                
                if (cleanVideoData.isEmpty()) {
                    onError?.invoke(Exception("Video data is empty"))
                    return@launch
                }
                
                // Decode a small portion to check if it looks like H.264
                try {
                    val videoBytes = Base64.decode(cleanVideoData, Base64.DEFAULT)
                    
                    // Check for H.264 start codes (0x00 0x00 0x00 0x01 or 0x00 0x00 0x01)
                    val hasH264StartCode = videoBytes.size > 4 && (
                            (videoBytes[0] == 0x00.toByte() && videoBytes[1] == 0x00.toByte() &&
                                    videoBytes[2] == 0x00.toByte() && videoBytes[3] == 0x01.toByte()) ||
                                    (videoBytes[0] == 0x00.toByte() && videoBytes[1] == 0x00.toByte() &&
                                            videoBytes[2] == 0x01.toByte())
                            )
                    
                } catch (e: Exception) {
                    // Continue even if H.264 validation fails
                }
                
                // Create JSON payload with proper escaping
                val escapedVideoData = cleanVideoData
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t")
                    .replace("\u0008", "\\b")  // backspace
                    .replace("\u000C", "\\f")  // form feed
                
                val jsonPayload = """
                    {
                        video_base64": "$escapedVideoData",
                        "session_meta_data": "$sessionMetaData",
                        "face_image_base64": "$faceImageDataBase64"
                    }
                """.trimIndent()
                
                withContext(Dispatchers.IO) {
                    OutputStreamWriter(connection.outputStream).use { writer ->
                        writer.write(jsonPayload)
                        writer.flush()
                    }
                }
                
                val responseCode = withContext(Dispatchers.IO) {
                    connection.responseCode
                }
                
                val response = withContext(Dispatchers.IO) {
                    if (responseCode in 200..299) {
                        connection.inputStream.bufferedReader().use { it.readText() }
                    } else {
                        connection.errorStream.bufferedReader().use { it.readText() }
                    }
                }
                
                if (responseCode in 200..299) {
                    // Parse response to get video info
                    try {
                        // Simple JSON parsing for logging (you might want to use a proper JSON library)
                        if (response.contains("\"status\":\"OK\"")) {
                            onSuccess?.invoke()
                        }
                    } catch (e: Exception) {
                        // Continue even if response parsing fails
                    }
                } else {
                    // Handle specific error codes
                    val errorMessage = when (responseCode) {
                        422 -> "422 Unprocessable Content - Server rejected the request"
                        400 -> "400 Bad Request - Invalid request format"
                        500 -> "500 Internal Server Error - Server processing error"
                        else -> "HTTP $responseCode - Unknown server error"
                    }
                    onError?.invoke(Exception(errorMessage))
                }
            } catch (e: Exception) {
                // Provide helpful debugging information
                val errorMessage = when (e) {
                    is ConnectException -> "Connection failed - Check if server is running and IP is correct"
                    is SocketTimeoutException -> "Connection timeout - server might be slow or unreachable"
                    is UnknownHostException -> "Unknown host - check the URL and network connectivity"
                    is NetworkOnMainThreadException -> "Network operation on main thread - this should not happen with coroutines"
                    else -> "Unexpected network error: ${e.message}"
                }
                onError?.invoke(Exception(errorMessage, e))
            } finally {
                withContext(Dispatchers.IO) {
                    connection?.disconnect()
                }
            }
        }
    }
}