package com.deuna.maven.web_views.file_downloaders

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.web_views.base.BaseWebView
import com.deuna.maven.web_views.base.*
import java.net.*
import java.util.concurrent.Executors


enum class FileExtension(val extension: String, val mimeType: String) {
    PDF("pdf", "application/pdf"),
    DOC("doc", "application/msword"),
    DOCX("docx", "application/msword"),
    XLS("xls", "application/vnd.ms-excel"),
    XLSX("xlsx", "application/vnd.ms-excel"),
    PPT("ppt", "application/vnd.ms-powerpoint"),
    PPTX("pptx", "application/vnd.ms-powerpoint"),
    ZIP("zip", "application/zip"),
    RAR("rar", "application/x-rar-compressed"),
    TAR("tar", "application/x-tar"),
    GZ("gz", "application/gzip");

    companion object {
        // Find enum case by file extension
        fun fromExtension(extension: String): FileExtension? {
            return entries.find { it.extension.equals(extension, ignoreCase = true) }
        }

        fun fromMime(mime: String): FileExtension? {
            return entries.find { it.mimeType.equals(mime, ignoreCase = true) }
        }

        // Static property to get the list of all file extensions as strings
        val allAsStrings: List<String> = entries.map { it.extension }
    }
}

val String.isFileDownloadUrl: Boolean
    get() {
        // Check if the clean URL ends with a valid file extension
        return getFileExtension() != null
    }

fun String.getFileExtension(): FileExtension? {
    // Extract the part before query parameters (if any)
    val cleanUrl = substringBefore("?")
    for (ext in FileExtension.entries) {
        if (cleanUrl.contains(".${ext.extension}")) {
            return ext
        }
    }
    return null
}

private fun getFileNameFromUrl(url: String): String {
    val fileName = url.substringAfterLast("/")
        .substringBefore("?")
        .substringBefore("#")

    return fileName.ifEmpty { "downloaded_file" }
}

fun getMimeTypeFromUrl(url: String, callback: (String?) -> Unit) {

    val executor = Executors.newSingleThreadExecutor()
    val handler = Handler(Looper.getMainLooper())

    executor.execute {

        var mimeType: String? = null

        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = true
            connection.requestMethod = "GET"
            connection.connect()
            mimeType = connection.getHeaderField("Content-Type")
        } catch (e: Exception) {
            DeunaLogs.info(e.toString())
        }

        handler.post {
            DeunaLogs.info("mime $mimeType")
            // the value could be something like application/pdf;charset=UTF-8
            // so we need to only take the left side of ; => application/pdf
            val parts = mimeType?.split(";") ?: emptyList()
            callback(if (parts.isNotEmpty()) parts.first() else null)
        }
    }

}

fun getProtocolAndDomain(urlString: String): String {
    return try {
        val url = URL(urlString)

        "${url.protocol}://${url.host}"
    } catch (e: Exception) {
        DeunaLogs.info(e.message ?: "")
        ""
    }
}

fun BaseWebView.runOnUiThread(runnable: Runnable) {
    val ctx = context
    if (ctx is Activity) {
        ctx.runOnUiThread(runnable)
    } else {
        Handler(Looper.getMainLooper()).post(runnable)
    }
}

fun WebViewController.runOnUiThread(runnable: Runnable) {
    val ctx = context
    if (ctx is Activity) {
        ctx.runOnUiThread(runnable)
    } else {
        Handler(Looper.getMainLooper()).post(runnable)
    }
}


fun BaseWebView.downloadFile(urlString: String) {
    runOnUiThread {
        if (urlString.isEmpty()) {
            return@runOnUiThread
        }

        var downloadUrl = urlString

        if (!downloadUrl.startsWith("https://") && !downloadUrl.startsWith("http://")) {
            downloadUrl = "${getProtocolAndDomain(webView.url ?: "")}$urlString"
        }

        // Attempt to get the file name and extension
        val fileName = getFileNameFromUrl(downloadUrl)
        val extension = downloadUrl.getFileExtension()

        if (extension != null) {
            startDownloadTask(
                context = context,
                downloadUrl = downloadUrl,
                fileName = fileName,
                mimeType = extension.mimeType
            )
            return@runOnUiThread
        }


        getMimeTypeFromUrl(downloadUrl) { mime ->

            if (mime == null) {
                Toast.makeText(context, "No se pudo descargar el archivo", Toast.LENGTH_SHORT)
                    .show()
                return@getMimeTypeFromUrl
            }

            val ext = FileExtension.fromMime(mime) ?: return@getMimeTypeFromUrl

            startDownloadTask(
                context = context,
                downloadUrl = downloadUrl,
                fileName = "$fileName.${ext.extension}",
                mimeType = mime
            )

        }


    }
}



private fun startDownloadTask(
    context: Context,
    downloadUrl: String,
    fileName: String,
    mimeType: String
) {

    val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
        setTitle(fileName)
        setMimeType(mimeType)
        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
    }

    val manager: DownloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    manager.enqueue(request)

    Toast.makeText(context, "Descarga iniciada", Toast.LENGTH_SHORT).show()
}