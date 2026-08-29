package com.example.data.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImagePickerHelper {

  fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, fileNamePrefix: String = "avatar"): String {
    val dir = File(context.filesDir, "profile_avatars")
    if (!dir.exists()) dir.mkdirs()
    val file = File(dir, "${fileNamePrefix}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")
    FileOutputStream(file).use { out ->
      bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    return file.absolutePath
  }

  fun saveUriToInternalStorage(context: Context, uri: Uri, fileNamePrefix: String = "avatar"): String? {
    return try {
      val inputStream = context.contentResolver.openInputStream(uri) ?: return null
      val dir = File(context.filesDir, "profile_avatars")
      if (!dir.exists()) dir.mkdirs()
      val file = File(dir, "${fileNamePrefix}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")
      FileOutputStream(file).use { out ->
        inputStream.copyTo(out)
      }
      file.absolutePath
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }
}
