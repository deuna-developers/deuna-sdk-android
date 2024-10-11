package com.deuna.maven.payment_widget.domain


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Base64
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date

fun PaymentWidgetBridge.saveBase64ImageToDevice(base64Image: String) {
    val base64String = base64Image.replace("data:image/png;base64,", "")
    val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size) ?: return

    // Create a filename using the current timestamp
    val filename = "${Date().time}.png"

    // Save the image temporarily in the cache directory
    val tempFile = File(activity.cacheDir, filename)
    try {
        val fos = FileOutputStream(tempFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(activity, "Error al guardar la imagen", Toast.LENGTH_LONG).show()
        return
    }


    // Create a file in the Downloads directory
    val downloadsDir =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val downloadFile = File(downloadsDir, filename)

    try {
        // Copy the temporary file to the Downloads directory
        tempFile.copyTo(downloadFile, overwrite = true)

        // Notify the user that the image has been downloaded
        Toast.makeText(
            activity, "Imagen descargada: ${downloadFile.absolutePath}", Toast.LENGTH_LONG
        ).show()

    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(activity, "Error al guardar la imagen.", Toast.LENGTH_LONG).show()
    } finally {
        // Delete the temporary file if it exists
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }

}