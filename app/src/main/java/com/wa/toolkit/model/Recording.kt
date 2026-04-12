package com.wa.toolkit.model

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import java.io.File
import java.io.RandomAccessFile
import java.util.regex.Pattern

/**
 * Model class representing a call recording with metadata.
 */
class Recording(val file: File, context: Context?) {

    var phoneNumber: String? = null
        private set
    var contactName: String = "Unknown"
        private set
    var duration: Long = 0 // in milliseconds
        private set
    val date: Long = file.lastModified()
    val size: Long = file.length()

    companion object {
        // Pattern to extract phone number from filename: Call_+1234567890_20261226_164651.wav
        private val PHONE_PATTERN = Pattern.compile("Call_([+\\d]+)_\\d{8}_\\d{6}\\.wav")
    }

    init {
        // Extract phone number from filename
        extractPhoneNumber()

        // Resolve contact name
        if (context != null && phoneNumber != null) {
            resolveContactName(context)
        }

        // Parse duration from WAV header
        parseDuration()
    }

    private fun extractPhoneNumber() {
        val filename = file.name
        val matcher = PHONE_PATTERN.matcher(filename)
        if (matcher.matches()) {
            phoneNumber = matcher.group(1)
        } else {
            // Fallback: try to find any phone number pattern
            val fallbackPattern = Pattern.compile("([+]?\\d{10,15})")
            val fallbackMatcher = fallbackPattern.matcher(filename)
            if (fallbackMatcher.find()) {
                phoneNumber = fallbackMatcher.group(1)
            }
        }

        // Default contact name to phone number
        contactName = phoneNumber ?: "Unknown"
    }

    private fun resolveContactName(context: Context) {
        val phone = phoneNumber ?: return
        if (phone.isEmpty()) return

        try {
            val resolver = context.contentResolver
            val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone))

            resolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val name = cursor.getString(0)
                    if (!name.isNullOrEmpty()) {
                        contactName = name
                    }
                }
            }
        } catch (e: Exception) {
            // Keep phone number as name if lookup fails
        }
    }

    private fun parseDuration() {
        if (!file.exists() || file.length() < 44) {
            duration = 0
            return
        }

        try {
            RandomAccessFile(file, "r").use { raf ->
                // Read WAV header
                val header = ByteArray(44)
                raf.read(header)

                // Verify RIFF header
                if (header[0] != 'R'.toByte() || header[1] != 'I'.toByte() || header[2] != 'F'.toByte() || header[3] != 'F'.toByte()) {
                    duration = estimateDuration()
                    return
                }

                // Get sample rate (bytes 24-27, little endian)
                val sampleRate = (header[24].toInt() and 0xFF) or
                        ((header[25].toInt() and 0xFF) shl 8) or
                        ((header[26].toInt() and 0xFF) shl 16) or
                        ((header[27].toInt() and 0xFF) shl 24)

                // Get byte rate (bytes 28-31, little endian)
                val byteRate = (header[28].toInt() and 0xFF) or
                        ((header[29].toInt() and 0xFF) shl 8) or
                        ((header[30].toInt() and 0xFF) shl 16) or
                        ((header[31].toInt() and 0xFF) shl 24)

                // Get data size (bytes 40-43, little endian)
                val dataSize = (header[40].toInt() and 0xFF).toLong() or
                        ((header[41].toInt() and 0xFF).toLong() shl 8) or
                        ((header[42].toInt() and 0xFF).toLong() shl 16) or
                        ((header[43].toInt() and 0xFF).toLong() shl 24)

                if (byteRate > 0) {
                    duration = (dataSize * 1000L) / byteRate
                } else if (sampleRate > 0) {
                    // Assume 16-bit mono
                    duration = (dataSize * 1000L) / (sampleRate * 2)
                }
            }
        } catch (e: Exception) {
            duration = estimateDuration()
        }
    }

    private fun estimateDuration(): Long {
        // Estimate based on file size (assume 48kHz, 16-bit, mono = 96000 bytes/sec)
        return (file.length() - 44) * 1000L / 96000
    }

    val formattedDuration: String
        get() {
            var seconds = duration / 1000
            var minutes = seconds / 60
            seconds %= 60

            if (minutes >= 60) {
                val hours = minutes / 60
                minutes %= 60
                return String.format("%d:%02d:%02d", hours, minutes, seconds)
            }
            return String.format("%d:%02d", minutes, seconds)
        }

    val formattedSize: String
        get() {
            if (size < 1024) return "$size B"
            if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0)
            return String.format("%.1f MB", size / (1024.0 * 1024.0))
        }

    /**
     * Returns a grouping key for this recording (phone number or "Unknown")
     */
    val groupKey: String
        get() = phoneNumber ?: "unknown"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val recording = other as Recording
        return file == recording.file
    }

    override fun hashCode(): Int {
        return file.hashCode()
    }
}
