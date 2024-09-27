package com.deuna.maven.payment_widget.domain

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Base64
import android.widget.Toast
import com.deuna.maven.shared.DeunaLogs
import java.io.File
import java.io.FileOutputStream
import java.util.Date

fun PaymentWidgetBridge.saveBase64ImageToDevice(base64Image: String) {
    val base64String = base64Image.replace("data:image/png;base64,", "")
    val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size) ?: return

    val filename = Date().time
    val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    val file = File(directory, "$filename.png")

    try {
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()
        Toast.makeText(activity, "La imagen se ha almacenado en el directorio de imágenes.", Toast.LENGTH_LONG).show()

    } catch (e: Exception) {
        DeunaLogs.error(e.message ?: "Unknown error: saveBase64ImageToDevice")
    }

}