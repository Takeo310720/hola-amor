package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageStorageHelper {
    /**
     * Copies an image from a content Uri to app-internal storage.
     * Returns the file Uri string (e.g. file:///data/user/0/.../couple_photos/xxx.jpg).
     */
    fun saveImageToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val directory = File(context.filesDir, "couple_photos")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = "photo_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val destFile = File(directory, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(destFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
