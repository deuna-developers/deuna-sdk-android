package com.deuna.maven

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.Environment
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.value
import com.deuna.maven.web_views.base.WebViewController
import org.json.JSONObject

/* <<<<<<<<<<<<<<  ✨ Windsurf Command ⭐ >>>>>>>>>>>>>>>> */
/**
 * Generates a fraud ID for the given parameters.
 *
 * @param context The application or activity context
 * @param params Optional JSON object containing additional parameters for the fraud ID generation.
 * @param callback A callback function to be invoked when the fraud ID generation completes. The callback receives a `String?` object containing the fraud ID or `null` if the request fails.
 */
/* <<<<<<<<<<  b83a2374-da3c-4fcb-b677-6197827fd300  >>>>>>>>>>> */
fun DeunaSDK.generateFraudId(
    context: Context,
    params: Json? = null,
    callback: (fraudId: String?) -> Unit
) {
    GenerateFraudId(context, publicApiKey, environment, callback, params)
}


class GenerateFraudId(
    val context: Context,
    private val publicApiKey: String,
    val environment: Environment,
    val callback: (fraudId: String?) -> Unit,
    private val params: Json?
) {
    private var controller: WebViewController? = null

    init {
        initializeWebView()
    }

    private fun initializeWebView() {
        controller = WebViewController(context = context, webView = WebView(context))
        controller!!.listener = object : WebViewController.Listener {

            override fun onWebViewLoaded() {
                generateFraudId()
            }

            override fun onWebViewError() {
                controller?.destroy()
                callback.invoke(null)
            }

            override fun onOpenExternalUrl(url: String) {}

            override fun onDownloadFile(url: String) {}

        }

        controller?.webView?.addJavascriptInterface(ConsoleLogBridge(), "android")

        controller?.loadUrl("https://cdn.stg.deuna.io/mobile-sdks/get_fraud_id.html") {
            return@loadUrl """
            console.log = function(message) {
                android.consoleLog(message);
            };
            console.error = function(message) {
                android.consoleError(message);
            };
            """.trimIndent()
        }
    }


    private fun buildResultFunction(requestId: Int, type: String): String {
        return """
    function sendResult(data){
        remoteJs.onRequestResult(JSON.stringify({type:"$type", data: data , requestId: $requestId }));
    }     
    """.trimIndent()
    }


    fun generateFraudId() {
        controller?.executeRemoteFunction(
            jsBuilder = { requestId ->
                return@executeRemoteFunction """
                 (function() {
                    ${buildResultFunction(requestId = requestId, type = "generateFraudId")}
                   
                    
                       if(typeof window.generateFraudId !== 'function'){
                           sendResult({ fraudId: null });
                           return;
                       }
                       
                        
                       window.generateFraudId(
                         {
                            publicApiKey: "$publicApiKey",
                            env: "${environment.value()}",
                            ${params?.let { "params: ${JSONObject(it)}" } ?: ""}
                         }
                       )
                       .then((fraudId) => sendResult({ fraudId: fraudId }))
                       .catch((error) => {
                            console.error(error.message || error.toString());
                            sendResult({ fraudId:null })
                         }
                       );
                 })();
            """.trimIndent()
            },
            callback = { json ->
                val fraudId = json["fraudId"] as? String
                callback.invoke(fraudId)
                controller?.destroy()
            }
        )
    }


    inner class ConsoleLogBridge {
        @JavascriptInterface
        fun consoleLog(message: String) {
            DeunaLogs.info("ConsoleLogBridge Log: $message")
        }

        @JavascriptInterface
        fun consoleError(message: String) {
            DeunaLogs.error("ConsoleLogBridge error: $message")
        }
    }
}