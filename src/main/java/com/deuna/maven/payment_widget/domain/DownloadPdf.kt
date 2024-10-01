package com.deuna.maven.payment_widget.domain

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast

private fun getFileNameFromUrl(url: String): String {
    return url.substringAfterLast("/")
        .substringBefore("?") // Remove query parameters
        .substringBefore("#") // Remove fragment identifiers
}


// Function to download a PDF from an URL and save it to the device
fun PaymentWidgetBridge.downloadPdf(context: Context, url: String) {

    if (url.isEmpty()) {
        return

    }
    var fileName = getFileNameFromUrl(url)
    fileName = "$fileName${if (fileName.endsWith(".pdf")) "" else ".pdf"}"

    val request: DownloadManager.Request = DownloadManager.Request(Uri.parse(url))
    with(request) {
        setTitle(fileName)
        setMimeType("application/pdf")
        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            fileName
        )
    }
    val manager: DownloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    manager.enqueue(request)


    Toast.makeText(context, "Descarga iniciada", Toast.LENGTH_SHORT).show()
}