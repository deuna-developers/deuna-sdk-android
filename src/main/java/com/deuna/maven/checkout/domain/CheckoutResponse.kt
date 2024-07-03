import com.deuna.maven.checkout.domain.CheckoutEvent
import org.json.JSONObject

data class CheckoutResponse(
  val type: CheckoutEvent,
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
                val weight: Weight?,
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
        fun fromJson(jsonObject: JSONObject): CheckoutResponse {
            val data = jsonObject.getJSONObject("data")
            val user = data.getJSONObject("user").let {
                Data.User(it.optString("id"), it.optString("email"), it.optBoolean("is_guest"))
            }
            val items = data.getJSONObject("order").getJSONArray("items").let { itemsArray ->
                List(itemsArray.length()) { i ->
                    val itemJson = itemsArray.getJSONObject(i)
                    val totalAmount = itemJson.optJSONObject("total_amount")?.let {
                        Data.Order.Item.Amount(
                            it.optInt("amount"),
                            it.optInt("original_amount"),
                            it.optString("display_amount"),
                            it.optString("display_original_amount"),
                            it.optString("currency"),
                            it.optString("currency_symbol"),
                            it.optString("display_total_discount"),
                            it.optInt("total_discount")
                        )
                    }
                    val unitPrice = itemJson.optJSONObject("unit_price")?.let {
                        Data.Order.Item.Amount(
                            it.optInt("amount"),
                            it.optInt("original_amount"),
                            it.optString("display_amount"),
                            it.optString("display_original_amount"),
                            it.optString("currency"),
                            it.optString("currency_symbol"),
                            it.optString("display_total_discount"),
                            it.optInt("total_discount")
                        )
                    }
                    val taxAmount = itemJson.optJSONObject("tax_amount")?.let {
                        Data.Order.Item.Amount(
                            it.optInt("amount"),
                            it.optInt("original_amount"),
                            it.optString("display_amount"),
                            it.optString("display_original_amount"),
                            it.optString("currency"),
                            it.optString("currency_symbol"),
                            it.optString("display_total_discount"),
                            it.optInt("total_discount")
                        )
                    }
                    val weight = itemJson.optJSONObject("weight")?.let {
                        Data.Order.Item.Weight(it.optInt("weight"), it.optString("unit"))
                    }
                    Data.Order.Item(
                        itemJson.optString("id"),
                        itemJson.optString("name"),
                        itemJson.optString("description"),
                        itemJson.optString("options"),
                        totalAmount,
                        unitPrice,
                        taxAmount,
                        itemJson.optInt("quantity"),
                        itemJson.optString("uom"),
                        itemJson.optString("upc"),
                        itemJson.optString("sku"),
                        itemJson.optString("isbn"),
                        itemJson.optString("brand"),
                        itemJson.optString("manufacturer"),
                        itemJson.optString("category"),
                        itemJson.optString("color"),
                        itemJson.optString("size"),
                        weight,
                        itemJson.optString("image_url"),
                        itemJson.optString("details_url"),
                        itemJson.optString("type"),
                        itemJson.optBoolean("taxable"),
                        listOf<Any>(), // As discounts is empty in provided JSON
                        itemJson.optBoolean("included_in_subscription"),
                        itemJson.optString("subscription_id")
                    )
                }
            }
            val payment = data.getJSONObject("order").getJSONObject("payment").let { paymentJson ->
                val paymentData = paymentJson.getJSONObject("data").let { paymentDataJson ->
                    val amount = paymentDataJson.getJSONObject("amount").let {
                        Data.PaymentData.Amount(it.optInt("amount"), it.optString("currency"))
                    }
                    val fromCard = paymentDataJson.getJSONObject("from_card").let { fromCardJson ->

                        Data.PaymentData.FromCard(
                            fromCardJson.optString("card_brand"),
                            fromCardJson.optString("first_six"),
                            fromCardJson.optString("last_four"),
                            fromCardJson.optString("bank_name"),
                            fromCardJson.optString("country_iso")
                        )
                    }
                    val merchant = paymentDataJson.getJSONObject("merchant").let {
                        Data.PaymentData.Merchant(it.optString("store_code"), it.optString("id"))
                    }
                    val customer = paymentDataJson.getJSONObject("customer").let {
                        Data.PaymentData.Customer(
                            it.optString("email"),
                            it.optString("id"),
                            it.optString("first_name"),
                            it.optString("last_name")
                        )
                    }
                    Data.PaymentData(
                        amount,
                        Any(), // As metadata is empty in provided JSON
                        fromCard,
                        paymentDataJson.optString("updated_at"),
                        paymentDataJson.optString("method_type"),
                        merchant,
                        paymentDataJson.optString("created_at"),
                        paymentDataJson.optString("id"),
                        paymentDataJson.optString("processor"),
                        customer,
                        paymentDataJson.optString("status"),
                        paymentDataJson.optString("reason"),
                        paymentDataJson.optString("external_transaction_id")
                    )
                }
                Data.Order.Payment(paymentData)
            }
            val merchant = data.getJSONObject("merchant").let {
                Data.Merchant(
                    it.optString("id"),
                    it.optString("name"),
                    it.optString("code"),
                    it.optString("country")
                )
            }
            val schemaRegistry = data.getJSONObject("schemaRegistry").let {
                Data.SchemaRegistry(
                    it.optString("source"),
                    it.optString("schemaId"),
                    it.optString("schema"),
                    it.optString("registryName")
                )
            }
            val order = data.getJSONObject("order").let {
                Data.Order(
                    it.optString("order_id"),
                    it.optString("currency"),
                    it.optDouble("tax_amount"),
                    it.optInt("items_total_amount"),
                    it.optDouble("sub_total"),
                    it.optInt("total_amount"),
                    items,
                    listOf<Any>(), // As discounts is empty in provided JSON
                    Any(), // As metadata is empty in provided JSON
                    it.optString("status"),
                    payment,
                    it.optString("transaction_id")
                )
            }
            val dataOrder =
                Data(user, order, merchant, data.optString("checkoutVersion"), schemaRegistry)
            return CheckoutResponse(CheckoutEvent.valueOf(jsonObject.optString("type")), dataOrder)
        }
    }
}