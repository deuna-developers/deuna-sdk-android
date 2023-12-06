import android.util.Log
import com.deuna.maven.checkout.CheckoutEvents
import org.json.JSONObject

data class OrderResponse(
    val type: CheckoutEvents,
    val data: Data
) {
    data class Data(
        val user: User,
        val order: Order,
        val merchant: Merchant,
        val checkoutVersion: String,
        val schemaRegistry: SchemaRegistry
    ) {
        data class User(
            val id: String,
            val email: String,
            val is_guest: Boolean
        )

        data class Order(
            val order_id: String,
            val currency: String,
            val tax_amount: Double,
            val items_total_amount: Int,
            val sub_total: Double,
            val total_amount: Int,
            val items: List<Item>,
            val discounts: List<Any>,
            val metadata: Any,
            val status: String,
            val payment: Payment,
            val transaction_id: String
        ) {
            data class Item(
                val id: String,
                val name: String,
                val description: String,
                val options: String,
                val total_amount: Amount?,
                val unit_price: Amount?,
                val tax_amount: Amount?,
                val quantity: Int,
                val uom: String,
                val upc: String,
                val sku: String,
                val isbn: String,
                val brand: String,
                val manufacturer: String,
                val category: String,
                val color: String,
                val size: String,
                val weight: Weight,
                val image_url: String,
                val details_url: String,
                val type: String,
                val taxable: Boolean,
                val discounts: List<Any>,
                val included_in_subscription: Boolean,
                val subscription_id: String
            ) {
                data class Amount(
                    val amount: Int,
                    val original_amount: Int,
                    val display_amount: String,
                    val display_original_amount: String,
                    val currency: String,
                    val currency_symbol: String,
                    val display_total_discount: String,
                    val total_discount: Int

                )

                data class Weight(
                    val weight: Int,
                    val unit: String
                )
            }

            data class Payment(
                val data: PaymentData
            )
        }

        data class PaymentData(
            val amount: Amount,
            val metadata: Any,
            val from_card: FromCard,
            val updated_at: String,
            val method_type: String,
            val merchant: Merchant,
            val created_at: String,
            val id: String,
            val processor: String,
            val customer: Customer,
            val status: String,
            val reason: String,
            val external_transaction_id: String
        ) {
            data class Amount(
                val amount: Int,
                val currency: String
            )

            data class FromCard(
                val card_brand: String,
                val first_six: String,
                val last_four: String,
                val bank_name: String,
                val country_iso: String
            ) {
            }

            data class Merchant(
                val store_code: String,
                val id: String
            )

            data class Customer(
                val email: String,
                val id: String,
                val first_name: String,
                val last_name: String
            )
        }

        data class Merchant(
            val id: String,
            val name: String,
            val code: String,
            val country: String
        )

        data class SchemaRegistry(
            val source: String,
            val schemaId: String,
            val schema: String,
            val registryName: String
        )
    }

    companion object {
        fun fromJson(jsonObject: JSONObject): OrderResponse {
            val data = jsonObject.getJSONObject("data")
            val user = data.getJSONObject("user").let {
                Data.User(it.getString("id"), it.getString("email"), it.getBoolean("is_guest"))
            }
            val items = data.getJSONObject("order").getJSONArray("items").let { itemsArray ->
                List(itemsArray.length()) { i ->
                    val itemJson = itemsArray.getJSONObject(i)
                    val totalAmount = itemJson.optJSONObject("total_amount")?.let {
                        Data.Order.Item.Amount(
                            it.getInt("amount"),
                            it.optInt("original_amount"),
                            it.getString("display_amount"),
                            it.optString("display_original_amount"),
                            it.getString("currency"),
                            it.getString("currency_symbol"),
                            it.optString("display_total_discount"),
                            it.optInt("total_discount")
                        )
                    }
                    val unitPrice = itemJson.optJSONObject("unit_price")?.let {
                        Data.Order.Item.Amount(
                            it.getInt("amount"),
                            it.optInt("original_amount"),
                            it.getString("display_amount"),
                            it.optString("display_original_amount"),
                            it.getString("currency"),
                            it.getString("currency_symbol"),
                            it.optString("display_total_discount"),
                            it.optInt("total_discount")
                        )
                    }
                    val taxAmount = itemJson.optJSONObject("tax_amount")?.let {
                        Data.Order.Item.Amount(
                            it.getInt("amount"),
                            it.optInt("original_amount"),
                            it.getString("display_amount"),
                            it.optString("display_original_amount"),
                            it.getString("currency"),
                            it.getString("currency_symbol"),
                            it.optString("display_total_discount"),
                            it.optInt("total_discount")
                        )
                    }
                    val weight = itemJson.optJSONObject("weight").let {
                        Data.Order.Item.Weight(it.getInt("weight"), it.getString("unit"))
                    }
                    Data.Order.Item(
                        itemJson.getString("id"),
                        itemJson.getString("name"),
                        itemJson.getString("description"),
                        itemJson.getString("options"),
                        totalAmount,
                        unitPrice,
                        taxAmount,
                        itemJson.getInt("quantity"),
                        itemJson.getString("uom"),
                        itemJson.getString("upc"),
                        itemJson.getString("sku"),
                        itemJson.getString("isbn"),
                        itemJson.getString("brand"),
                        itemJson.getString("manufacturer"),
                        itemJson.getString("category"),
                        itemJson.getString("color"),
                        itemJson.getString("size"),
                        weight,
                        itemJson.getString("image_url"),
                        itemJson.getString("details_url"),
                        itemJson.getString("type"),
                        itemJson.getBoolean("taxable"),
                        listOf<Any>(), // As discounts is empty in provided JSON
                        itemJson.getBoolean("included_in_subscription"),
                        itemJson.getString("subscription_id")
                    )
                }
            }
            val payment = data.getJSONObject("order").getJSONObject("payment").let { paymentJson ->
                val paymentData = paymentJson.getJSONObject("data").let { paymentDataJson ->
                    val amount = paymentDataJson.getJSONObject("amount").let {
                        Data.PaymentData.Amount(it.getInt("amount"), it.getString("currency"))
                    }
                    val fromCard = paymentDataJson.getJSONObject("from_card").let { fromCardJson ->

                        Data.PaymentData.FromCard(
                            fromCardJson.getString("card_brand"),
                            fromCardJson.getString("first_six"),
                            fromCardJson.getString("last_four"),
                            fromCardJson.getString("bank_name"),
                            fromCardJson.getString("country_iso")
                        )
                    }
                    val merchant = paymentDataJson.getJSONObject("merchant").let {
                        Data.PaymentData.Merchant(it.getString("store_code"), it.getString("id"))
                    }
                    val customer = paymentDataJson.getJSONObject("customer").let {
                        Data.PaymentData.Customer(
                            it.getString("email"),
                            it.getString("id"),
                            it.getString("first_name"),
                            it.getString("last_name")
                        )
                    }
                    Data.PaymentData(
                        amount,
                        Any(), // As metadata is empty in provided JSON
                        fromCard,
                        paymentDataJson.getString("updated_at"),
                        paymentDataJson.getString("method_type"),
                        merchant,
                        paymentDataJson.getString("created_at"),
                        paymentDataJson.getString("id"),
                        paymentDataJson.getString("processor"),
                        customer,
                        paymentDataJson.getString("status"),
                        paymentDataJson.getString("reason"),
                        paymentDataJson.getString("external_transaction_id")
                    )
                }
                Data.Order.Payment(paymentData)
            }
            val merchant = data.getJSONObject("merchant").let {
                Data.Merchant(
                    it.getString("id"),
                    it.getString("name"),
                    it.getString("code"),
                    it.getString("country")
                )
            }
            val schemaRegistry = data.getJSONObject("schemaRegistry").let {
                Data.SchemaRegistry(
                    it.getString("source"),
                    it.getString("schemaId"),
                    it.getString("schema"),
                    it.getString("registryName")
                )
            }
            val order = data.getJSONObject("order").let {
                Data.Order(
                    it.getString("order_id"),
                    it.getString("currency"),
                    it.optDouble("tax_amount"),
                    it.getInt("items_total_amount"),
                    it.getDouble("sub_total"),
                    it.getInt("total_amount"),
                    items,
                    listOf<Any>(), // As discounts is empty in provided JSON
                    Any(), // As metadata is empty in provided JSON
                    it.getString("status"),
                    payment,
                    it.getString("transaction_id")
                )
            }
            val dataOrder =
                Data(user, order, merchant, data.getString("checkoutVersion"), schemaRegistry)
            return OrderResponse(CheckoutEvents.valueOf(jsonObject.getString("type")), dataOrder)
        }
    }
}