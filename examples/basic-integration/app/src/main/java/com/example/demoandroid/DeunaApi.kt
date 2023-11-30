package com.example.demoandroid
import android.util.Log
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.IOException
import com.deuna.maven.checkout.domain.Environment
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

class DeunaApiService(private val apiKey: String, private val environment: Environment) {
    private val client = OkHttpClient()

    fun fetchOrderToken(merchant: Merchant, onResult: (String?, String?) -> Unit) {
        val url = when (environment) {
            Environment.STAGING -> "https://api.stg.deuna.io/merchants/orders"
            Environment.PRODUCTION -> "https://api.deuna.io/merchants/orders"
            else -> "https://api.dev.deuna.io/merchants/orders"
        }

        Log.d("debug", url)
        Log.d("debug", environment.toString())

        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        Log.d("debug", merchant.toJson())
        val requestBody = merchant.toJson().toRequestBody(jsonMediaType)
        Log.d("api-key", apiKey)
        val request = Request.Builder()
            .url(url)
            .addHeader("x-api-key", apiKey)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("debug", e.toString())
                onResult(null, null)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("response", response.code.toString())
                val responseData = response.body?.string()
                if (responseData != null) {
                    Log.d("response success", responseData)
                } else {
                    Log.d("response success null", "null")
                }

                try {
                    val gson = Gson()
                    val orderResponse = gson.fromJson(responseData, OrderResponse::class.java)
                    val orderToken = orderResponse.token
                    var userToken = extractUserToken(orderResponse.order.payment_link)

                    if (userToken=="") {
                        userToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IlZOUDlOVGM2SWdxVDhhYVRqYlFnOTRPa2wzZHgtLWZfQjdNdXdTNmYzdmMiLCJ0eXAiOiJKV1QifQ.eyJhY3QiOiJyZWFkIiwiYXVkIjpbIjU5YmEwYzgxLTYzNTAtNDUyMy05ZjJmLTU0NTdkNzM3ZmRkNCJdLCJleHAiOjE3MDAyNTMyNjgsImV4dGVybmFsIjp0cnVlLCJpYXQiOjE3MDAxNjY4NjgsImlzcyI6IjU5YmEwYzgxLTYzNTAtNDUyMy05ZjJmLTU0NTdkNzM3ZmRkNCIsInJlZmVyZXIiOiIiLCJzdWIiOiI0MGMzZWMyOS04NTYyLTQxYzYtYmI0Ny05NmNhN2M4NTJlMDEiLCJ1c2VyX3JvbGUiOiJleHRlcm5hbCJ9.c5-0DyAVd8t-4QVmGdAgp2wHbcA5_eh_UwVfT8uMHjIEJBCA5FEpgzLp0Nc1TNWPw5a-IwjsNzxvB4DkUMRfQw4vzd6rPU3zcMT-mc2MBQOKmgYKFcckex0_-M_K6sY7Oa4RL_XJMKkBwiBcqArUpg4AhYxuJbZPizaGPB2LoXVravPEnTvQHf4UrPZyK99x4lRf_c1tNRAX-wJuVatCTIdz-UX9nRHQZjtIa9x3J2K4OXTsDPv6tD4mOU66jXuLeIcLhDFENPsgeJkYdMoRCnY1kQhFq8vV_4u13EUe0ZaFJAC1RAVs5Z41mZALZlCyPpMztjMUyiSQdRnN0PWL5CmuEu6UJuFPmRkL2CDT184Icn4f1FC99AqU0FaMyFfmwKdbn6dguTUgqajQFxiI8kdeZqoC_bBxaF0XABfVjWkqBrqxeBZI_xEvvL6MPPJLp7h5-rXNYj40LA-b53Uvn2HBxnuG-QGJgALurmN23cLq1ZHxWMluPQafad3sJav4ONHQeZxYh8nPKGUQ6_M5zVzZHVtjkxe2gH1hbdjSODZ9RQzaeO7jTOexlDEACy-ai_SDJQDvdCjqPqZDMFR7v7Tjt-eGsmsg733Y8mecE0BSXX5IBdkP_oDv1Neldev_FgMQHCLUd-tjcaa03IOSUJIgFbxDdJ5BZPjxspdjTE4"
                    }

                    onResult(orderToken, userToken)
                } catch (e: Exception) {
                    Log.d("debug", e.toString())
                    onResult(null, null)
                }
            }
        })
    }
}

private fun extractUserToken(paymentLink: String): String {
    val userTokenRegex = "userToken=([^&]+)".toRegex()
    val matchResult = userTokenRegex.find(paymentLink)
    return matchResult?.groups?.get(1)?.value ?: ""
}
class Merchant() {
    fun toJson(): String {
        val orderId = "sdk-android-demo-${UUID.randomUUID()}"
        return """    
            {
                "order_type": "PAYMENT_LINK",
                "order": {
                    "order_id": "$orderId",
                    "currency": "MXN",
                    "timezone": "America/Mexico_City",      
                    "items_total_amount": 2800,                
                    "sub_total": 3000,
                    "total_tax_amount": 500,
                    "shipping_amount": 0,
                    "total_amount": 3500,   
                    "store_code": "all",
                    "payer_info": {
                          "email": "eposada@deuna.com"
                    },
                    "webhook_urls": {
                        "notify_order": "https://internal.deuna.io/api/v1/orders"        
                    },
                    "items": [
                        {
                            "id": "79",
                            "name": "10 ALITAS VOLANTE",
                            "description": "10 alitas picantes",
                            "options": "string option",
                            "total_amount": {
                                "amount": 1200,
                                "currency": "MXN",
                                "currency_symbol": "${'$'}"
                            },
                            "unit_price": {
                                "amount": 850,
                                "currency": "MXN",
                                "currency_symbol": "${'$'}"
                            },
                            "tax_amount": {
                                "amount": 100,
                                "currency": "MXN",
                                "currency_symbol": "${'$'}"
                            },
                            "quantity": 1,
                            "uom": "string",
                            "upc": "string",
                            "sku": "SKU-11021",
                            "isbn": "12-345-678-90123",
                            "brand": "Bolt Swagstore",
                            "manufacturer": "Bolt Factory",
                            "category": "hats",
                            "color": "Red",
                            "size": "XXL",
                            "weight": {
                                "weight": 22,
                                "unit": "kg"
                            },
                            "image_url": "https://boltswagstore.com/inventory/hats/red-hat.png",
                            "details_url": "https://boltswagstore.com/inventory/hats/red-hat.png",
                            "type": "physical",
                            "taxable": true
                        }
                    ],
                    "shipping_options": {
                        "type": "pickup",
                        "details": {
                            "store_name": "Store Name",
                            "address": "6 Rotermanni 11343 Talinn",
                            "address_coordinates": {
                                "lat": 4.721245,
                                "lng": -74.04673
                            },
                            "contact": {
                                "name": "jhon snow",
                                "phone": "972514910"
                            },
                            "additional_details": {
                                "pickup_time": "2021-11-03T22:09:09.086990957Z",
                                "stock_location": ""
                            }
                        }
                    }
                },
                "custom_fields" : {
                    "data" : {
                        "external_auth_token" : "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vYWxzZWEtYXBpLXN0YWdpbmcub3JkZXJpbmcuY28vdjQwMC9lcy00MTktMS9hbHNlYS1zdGFnaW5nL2F1dGgiLCJpYXQiOjE2OTM0Mzk5MzEsImV4cCI6MTcyNDk3NTkzMSwibmJmIjoxNjkzNDM5OTMxLCJqdGkiOiJ1UGxmNHNpVDJPTkdxR2JpIiwic3ViIjoiMzc3MCIsImxldmVsIjozfQ.GMrUTFLo3PI7k1x5IGeNZzXlUT8Li8_NlI94EqYiXI4",
                        "business_id" : 143,
                          "brand_id": 9,
                        "user_id": 3770,
                        "wow_rewards_user_id": "2bd4e894-4791-11ee-8b97-3a96e2a65f99",
                        "employee_coupon": "KDHHDBNCN",
                        "cash_rule" : {
                            "min_amount" : 10,
                            "max_amount" : 100
                        }
                    }
                   
                }
            }
        """.trimIndent()
    }
}
data class OrderResponse(
    val token: String,
    val order: OrderDetails
)

data class OrderDetails(
    val payment_link: String
)

