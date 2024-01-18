package com.deuna.maven.checkout.domain

import OrderResponse
import org.json.JSONObject

data class DeunaErrorMessage(var message: String,
                             var type: String,
                             var order: OrderResponse.Data.Order?,
                             var user: OrderResponse.Data.User?)