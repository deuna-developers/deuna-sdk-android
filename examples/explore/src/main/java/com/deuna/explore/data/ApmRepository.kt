package com.deuna.explore.data

import com.deuna.explore.domain.ApmOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL

object ApmRepository {
    private const val GIST_URL =
        "https://gist.githubusercontent.com/darwinmorocho-deuna/16d9c3b60cae611bb0027fe82e4b9bcb/raw/mobile_apms_config.json"

    suspend fun fetchApmOptions(): List<ApmOption> = withContext(Dispatchers.IO) {
        val json = URL(GIST_URL).readText()
        val array = JSONArray(json)
        (0 until array.length()).mapNotNull { i ->
            val obj = array.getJSONObject(i)
            val androidCompatible = obj.optBoolean("androidCompatible", true)
            if (!androidCompatible) return@mapNotNull null
            ApmOption(
                paymentMethod = obj.getString("paymentMethod"),
                processor = obj.getString("processor"),
                logo = obj.getString("logo"),
                iosCompatible = obj.optBoolean("iosCompatible", true),
                androidCompatible = true,
            )
        }
    }
}
