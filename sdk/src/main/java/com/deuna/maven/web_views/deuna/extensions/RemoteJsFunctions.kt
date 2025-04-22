package com.deuna.maven.web_views.deuna.extensions

import com.deuna.maven.shared.Json
import com.deuna.maven.web_views.deuna.DeunaWidget
import org.json.JSONObject


fun DeunaWidget.buildResultFunction(requestId: Int, type: String): String {
    return """
    function sendResult(data){
        remoteJs.onRequestResult(JSON.stringify({type:"$type", data: data , requestId: $requestId }));
    }     
    """.trimIndent()
}

fun DeunaWidget.setCustomStyle(data: Json) {
    val dataAsJsonString = JSONObject(data).toString()
    controller?.executeRemoteFunction(
        jsBuilder = { requestId ->
            return@executeRemoteFunction """
                 (function() {
                       if(typeof window.setCustomStyle !== 'function'){
                           return;
                       }    
                       window.setCustomStyle($dataAsJsonString);
                 })();
            """.trimIndent()
        },
        callback = {}
    )
}

fun DeunaWidget.refetchOrder(callback: (Json?) -> Unit) {
    controller?.executeRemoteFunction(
        jsBuilder = { requestId ->
            return@executeRemoteFunction """
                 (function() {
                    ${buildResultFunction(requestId = requestId, type = "refetchOrder")}
                       if(typeof window.deunaRefetchOrder !== 'function'){
                           sendResult({ order:null });
                           return;
                       }
                          
                       window.deunaRefetchOrder()
                       .then(sendResult)
                       .catch(error => sendResult({ order:null }));
                 })();
            """.trimIndent()
        },
        callback = { json ->
            val order = json["order"] as? Json
            callback(order)
        }
    )
}

fun DeunaWidget.isValid(callback: (Boolean) -> Unit) {
    controller?.executeRemoteFunction(
        jsBuilder = { requestId ->
            return@executeRemoteFunction """
                (function() {
                    ${buildResultFunction(requestId = requestId, type = "isValid")}
                    if(typeof window.isValid !== 'function'){
                        sendResult({isValid:false});
                        return;
                    }
                    sendResult( {isValid: window.isValid() });
                })();
            """.trimIndent()
        },
        callback = { json ->
            val isValid = json["isValid"] as? Boolean
            callback(isValid ?: false)
        }
    )
}

fun DeunaWidget.submit(callback: (SubmitResult) -> Unit) {
    controller?.executeRemoteFunction(
        jsBuilder = { requestId ->
            return@executeRemoteFunction """
                (function() {
                    ${buildResultFunction(requestId = requestId, type = "submit")}
                    if(typeof window.submit !== 'function'){
                        sendResult({status:"error", message:"Error al procesar la solicitud." });
                        return;
                    }
                    window.submit()
                    .then(sendResult)
                    .catch(error => sendResult({status:"error", message: error.message ?? "Error al procesar la solicitud." }));
                })();
            """.trimIndent()
        },
        callback = { json ->
            callback(
                SubmitResult(
                    status = json["status"] as? String ?: "error",
                    message = json["message"] as? String
                )
            )
        }
    )
}

fun DeunaWidget.getWidgetState(callback: (Json?) -> Unit) {
    controller?.executeRemoteFunction(
        jsBuilder = { requestId ->
            return@executeRemoteFunction """
            (function() {
                ${buildResultFunction(requestId = requestId, type = "getWidgetState")}
                if(!window.deunaWidgetState){
                    sendResult({ deunaWidgetState: null });
                    return;
                }
                sendResult({ deunaWidgetState: window.deunaWidgetState });
            })();
            """.trimIndent()
        },
        callback = { json ->
            callback(
                json["deunaWidgetState"] as? Json
            )
        }
    )
}

data class SubmitResult(val status: String, val message: String?)