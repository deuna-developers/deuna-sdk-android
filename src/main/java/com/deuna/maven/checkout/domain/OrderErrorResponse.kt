package com.deuna.maven.checkout.domain

import org.json.JSONObject
import java.math.BigDecimal

data class OrderErrorResponse(
    val order: Order
) {
    data class Metadata(
        val errorCode: String,
        val errorMessage: String
    )

    data class Order(
        val order_id: String,
        val currency: String,
        val items_total_amount: Int,
        val sub_total: Int,
        val total_amount: Int,
        val items: List<Item>
    )

    data class Item(
        val id: String,
        val name: String,
        val description: String,
        val options: String,
        val total_amount: TotalAmount,
        val unit_price: UnitPrice,
        val tax_amount: TaxAmount,
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
        val taxable: Boolean
    )

    data class TotalAmount(
        val amount: Int,
        val original_amount: Int,
        val display_amount: String,
        val display_original_amount: String,
        val currency: String,
        val currency_symbol: String,
        val total_discount: Int,
        val display_total_discount: String
    )

    data class UnitPrice(
        val amount: Int,
        val display_amount: String,
        val currency: String,
        val currency_symbol: String
    )

    data class TaxAmount(
        val amount: Int,
        val display_amount: String,
        val currency: String,
        val currency_symbol: String
    )

    data class Weight(
        val weight: Int,
        val unit: String
    )


    companion object {
        fun fromJson(json: JSONObject): OrderErrorResponse {
            val orderData = json.getJSONObject("order")
            val itemsJson = orderData.getJSONArray("items")
            val items = mutableListOf<Item>()
            for (i in 0 until itemsJson.length()) {
                val itemJson = itemsJson.getJSONObject(i)

                val total_amount = TotalAmount(
                    itemJson.getJSONObject("total_amount").getInt("amount"),
                    itemJson.getJSONObject("total_amount").getInt("original_amount"),
                    itemJson.getJSONObject("total_amount").getString("display_amount"),
                    itemJson.getJSONObject("total_amount").getString("display_original_amount"),
                    itemJson.getJSONObject("total_amount").getString("currency"),
                    itemJson.getJSONObject("total_amount").getString("currency_symbol"),
                    itemJson.getJSONObject("total_amount").getInt("total_discount"),
                    itemJson.getJSONObject("total_amount").getString("display_total_discount"),
                )

                val unit_price = UnitPrice(
                    itemJson.getJSONObject("unit_price").getInt("amount"),
                    itemJson.getJSONObject("unit_price").getString("display_amount"),
                    itemJson.getJSONObject("unit_price").getString("currency"),
                    itemJson.getJSONObject("unit_price").getString("currency_symbol"),
                )

                val tax_amount = TaxAmount(
                    itemJson.getJSONObject("tax_amount").getInt("amount"),
                    itemJson.getJSONObject("tax_amount").getString("display_amount"),
                    itemJson.getJSONObject("tax_amount").getString("currency"),
                    itemJson.getJSONObject("tax_amount").getString("currency_symbol"),
                )

                val weight = Weight(
                    itemJson.getJSONObject("weight").getInt("weight"),
                    itemJson.getJSONObject("weight").getString("unit"),
                )

                val item = Item(
                    itemJson.getString("id"),
                    itemJson.getString("name"),
                    itemJson.getString("description"),
                    itemJson.getString("options"),
                    total_amount,
                    unit_price,
                    tax_amount,
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
                )
                items.add(item)
            }



            val order = Order(
                orderData.getString("order_id"),
                orderData.getString("currency"),
                orderData.getInt("items_total_amount"),
                orderData.getInt("sub_total"),
                orderData.getInt("total_amount"),
                items,
            )
            return OrderErrorResponse(order)
        }
    }
}
