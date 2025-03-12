package com.deuna.compose_demo.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import com.deuna.compose_demo.*
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.toMap
import org.json.JSONObject

@Suppress("UNCHECKED_CAST")
class Order(val id: String, val items: List<OrderItem>) {
    companion object {
        fun fromJson(json: Json): Order {
            return Order(
                id = json["order_id"] as String,
                items = (json["items"] as List<Json>).map {
                    OrderItem.fromJson(it)
                }
            )
        }
    }
}


@Suppress("UNCHECKED_CAST")
class OrderItem(val id: String, val name: String, val options: String?, val displayAmount: String) {
    companion object {
        fun fromJson(json: Json): OrderItem {
            val totalAmount = json["total_amount"] as Json
            return OrderItem(
                id = json["id"] as String,
                name = json["name"] as String,
                options = json["options"] as? String,
                displayAmount = totalAmount["display_amount"] as String,
            )
        }
    }
}


@Composable
fun PaymentSuccessfulScreen(json: Json) {
    val navController = LocalNavController.current

    val order = Order.fromJson(json)
    val items = order.items

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.height(20.dp))

            Text(text = "Payment Successful")
            Box(modifier = Modifier.height(20.dp))
            Text(text = "Order ID: ${order.id}")
            Box(modifier = Modifier.height(20.dp))
            LazyColumn {
                items(items.count()) { index ->
                    Text(
                        text = items[index].name,
                        modifier = Modifier.padding(8.dp)
                    )
                    Text(
                        text = items[index].options ?: "",
                        modifier = Modifier.padding(8.dp)
                    )
                    Text(
                        text = "Total: ${items[index].displayAmount}",
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Box(modifier = Modifier.height(20.dp))

            ElevatedButton(
                onClick = {
                    // pop the current screen
                    navController.popBackStack()
                },
            ) {
                Text(text = "Go back!")
            }
        }
    }
}


@Preview
@Composable
private fun Preview() {
    Navigator {
        PaymentSuccessfulScreen(
            json = JSONObject(
                """
                {
                  "currency": "USD",
                  "discounts": [
                    {
                      "amount": 10,
                      "code": "SUMMER10",
                      "description": "Summer Sale Discount",
                      "details_url": "https://example.com/discount-details",
                      "discount_category": "Seasonal",
                      "display_amount": "${'$'}10.00",
                      "free_shipping": {
                        "is_free_shipping": true,
                        "maximum_cost_allowed": 20
                      },
                      "reference": "REF12345",
                      "target_type": "Order",
                      "type": "Percentage"
                    }
                  ],
                  "items": [
                    {
                      "brand": "ExampleBrand",
                      "category": "Clothing",
                      "color": "Red",
                      "description": "A stylish red shirt",
                      "details_url": "https://example.com/item-details",
                      "id": "ITEM123",
                      "image_url": "https://example.com/image.png",
                      "item_details": [
                        {
                          "priority": 1,
                          "label": "Size",
                          "value": "M"
                        },
                        {
                          "priority": 2,
                          "label": "Material",
                          "value": "Cotton"
                        }
                      ],
                      "manufacturer": "ExampleManufacturer",
                      "name": "Red Shirt",
                      "options": "Size: M, Color: Red",
                      "quantity": 2,
                      "size": "M",
                      "sku": "SKU123",
                      "subscription_id": "SUB123",
                      "tax_amount": {
                        "amount": 5,
                        "currency": "USD",
                        "currency_symbol": "${'$'}",
                        "display_amount": "${'$'}5.00"
                      },
                      "taxable": true,
                      "total_amount": {
                        "amount": 50,
                        "currency": "USD",
                        "currency_symbol": "${'$'}",
                        "display_amount": "${'$'}50.00",
                        "display_original_amount": "${'$'}55.00",
                        "display_total_discount": "${'$'}5.00",
                        "original_amount": 55,
                        "total_discount": 5
                      },
                      "type": "Physical",
                      "unit_price": {
                        "amount": 25,
                        "currency": "USD",
                        "currency_symbol": "${'$'}",
                        "display_amount": "${'$'}25.00"
                      },
                      "weight": {
                        "unit": "kg",
                        "weight": 1
                      }
                    }
                  ],
                  "items_total_amount": 50,
                  "order_id": "ORDER12345",
                  "payment": {
                    "data": {
                      "amount": {
                        "amount": 50,
                        "currency": "USD"
                      },
                      "authorization_code": "AUTH123",
                      "created_at": "2023-07-09T12:34:56Z",
                      "customer": {
                        "email": "customer@example.com",
                        "first_name": "John",
                        "id": "CUST123",
                        "last_name": "Doe"
                      },
                      "external_transaction_id": "EXT123",
                      "from_card": {
                        "bank_name": "ExampleBank",
                        "card_brand": "Visa",
                        "country_iso": "US",
                        "first_six": "123456",
                        "installment": {
                          "installment_amount": 10,
                          "installment_rate": 5,
                          "installment_type": "Fixed",
                          "installments": 5,
                          "plan_id": "PLAN123",
                          "plan_option_id": "OPTION123"
                        },
                        "last_four": "7890"
                      },
                      "id": "PAYMENT123",
                      "merchant": {
                        "id": "MERCHANT123",
                        "store_code": "STORE123"
                      },
                      "merchant_payment_processor_name": "ExampleProcessor",
                      "method_type": "CreditCard",
                      "processor": "ExampleProcessor",
                      "reason": "Purchase",
                      "status": "Completed",
                      "updated_at": "2023-07-09T12:45:00Z"
                    }
                  },
                  "shipping_amount": 5,
                  "status": "Pending",
                  "sub_total": 55,
                  "total_amount": 60,
                  "transaction_id": "TRANS123"
                }
            """.trimIndent()
            ).toMap()
        )
    }
}