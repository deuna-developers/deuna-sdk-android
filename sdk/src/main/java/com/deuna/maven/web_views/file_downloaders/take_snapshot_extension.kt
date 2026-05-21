package com.deuna.maven.web_views.file_downloaders

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.deuna.maven.shared.DeunaLogs


// Function to check if the interface is registered in the WebView
private fun WebView.isInterfaceRegistered(interfaceName: String, callback: (Boolean) -> Unit) {
    val js = """
        (function() {
            if (typeof window.$interfaceName !== 'undefined') {
                return true;
            } else {
                return false;
            }
        })();
    """.trimIndent()

    this.evaluateJavascript(js) { result ->
        // The result will be "true" or "false"
        callback(result == "true")
    }
}

fun WebView.takeSnapshot(
    bridge: TakeSnapshotBridge,
    target: String = "document.body",
    completion: (base64Image: String?) -> Unit
) {

    isInterfaceRegistered(bridge.name){ registered ->

        if(!registered){
            DeunaLogs.error("Interface ${bridge.name} is not registered")
            completion(null)
            return@isInterfaceRegistered
        }

        bridge.completion?.invoke(null) // cancel any previous task
        bridge.completion = {
            completion(it)
            bridge.completion = null
        }
        val js = """
            (function() {
                function takeScreenshot() {
                    html2canvas($target, { allowTaint: true, useCORS: true }).then((canvas) => {
                        // Convert the canvas to a base64 image
                        var imgData = canvas.toDataURL("image/png");
                        console.log(imgData);
                        if (!window.${bridge.name}) {
                            console.log('Interface not found');
                            return;
                        }
                        window.${bridge.name}.onResult(imgData);
                    }).catch((error) => {
                        window.${bridge.name}.onResult(null);
                    });
                }

                // If html2canvas is not added
                if (typeof html2canvas === "undefined") {
                    var script = document.createElement("script");
                    script.src = "https://html2canvas.hertzen.com/dist/html2canvas.min.js";
                    script.onload = function () {
                        takeScreenshot();
                    };
                    document.head.appendChild(script);
                } else {
                    takeScreenshot();
                }
            })();
        """.trimIndent()
        evaluateJavascript(js, null)

    }
}

/**
 * Intercepts take snapshot results
 */
class TakeSnapshotBridge(
    val name: String,
    var completion: ((base64Image: String?) -> Unit)? = null
) {
    @JavascriptInterface
    fun onResult(base64Image: String?) {
        completion?.invoke(base64Image)
    }
}