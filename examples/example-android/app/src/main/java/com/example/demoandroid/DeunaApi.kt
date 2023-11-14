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
                    val userToken = extractUserToken(orderResponse.order.payment_link)
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
                    
                    "payer_info": {
                        "email": "scatalan@deuna.com"
                    },
                    "currency": "MXN",
                    "timezone": "America/Mexico_City",
                    "total_amount": 10900,
                    "total_discount": 0,
                    "total_tax_amount": 0,
                    "tax_amount": 0,
                    "sub_total": 10900,
                    "items_total_amount": 10900,
                    "store_code": "all",
                    "webhook_urls": {
                        "notify_order": "https://internal.deuna.io/api/v1/orders",
                        "apply_coupon": "https://internal.deuna.io/api/v1/orders/{order_token}/coupons",
                        "remove_coupon": "https://internal.deuna.io/api/v1/orders/{order_token}/coupons/{coupon_code}",
                        "get_shipping_methods": "https://internal.deuna.io/api/v1/orders/{order_token}/shipping-methods",
                        "update_shipping_method": "https://internal.deuna.io/api/v1/orders/{order_token}/shipping-method",
                        "shipping_rate": "https://internal.deuna.io/api/v1/orders/{order_token}/shipping"
                    },
                    "items": [
                        {
                            "id": "67166",
                            "name": "Caldo tlalpeño (500 ml)",
                            "description": "Caldo tlalpeño (500 ml)",
                            "quantity": 1,
                            "sku": "Yy-l-LNVyI",
                            "category": "3885",
                            "image_url": "https://d347gjkxx0g7x1.cloudfront.net/menu/img/vips/44031.JPG",
                            "total_amount": {
                                "amount": 10900,
                                "currency": "MXN",
                                "currency_symbol": "${'$'}"
                            },
                            "unit_price": {
                                "amount": 10900,
                                "currency": "MXN",
                                "currency_symbol": "${'$'}"
                            },
                            "tax_amount": {
                                "amount": 0,
                                "currency": "MXN",
                                "currency_symbol": "${'$'}"
                            }
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
                    },
                    "shipping_methods": [
                        {
                            "code": "123",
                            "name": "name",
                            "scheduler": [
                                {
                                    "date": "2023-03-03",
                                    "start_time": "11:11",
                                    "end_time": "12:00",
                                    "steps_minutes": 1
                                }
                            ]
                        }
                    ],
                    "user_instructions": "This item is a gift."
                },
                "custom_fields": {
                    "data": {
                        "external_auth_token": "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vYWxzZWEtYXBpLXN0YWdpbmcub3JkZXJpbmcuY28vdjQwMC9lcy00MTktMS9hbHNlYS1zdGFnaW5nL2F1dGgiLCJpYXQiOjE2OTM0Mzk5MzEsImV4cCI6MTcyNDk3NTkzMSwibmJmIjoxNjkzNDM5OTMxLCJqdGkiOiJ1UGxmNHNpVDJPTkdxR2JpIiwic3ViIjoiMzc3MCIsImxldmVsIjozfQ.GMrUTFLo3PI7k1x5IGeNZzXlUT8Li8_NlI94EqYiXI4",
                        
                        
                        
                        "business_id": 174,
            
                        
                        "brand_id": 8,
            
                        
                        "user_id": 3770,
                        
                        
                        "wow_rewards_user_id": "2bd4e894-4791-11ee-8b97-3a96e2a65f99",
                        
                        "reward": 1000,
                        "cash_rule": {
                            "min_amount": 10,
                            "max_amount": 100
                        },
                        "employee_coupon": "G.O.A.T"
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

