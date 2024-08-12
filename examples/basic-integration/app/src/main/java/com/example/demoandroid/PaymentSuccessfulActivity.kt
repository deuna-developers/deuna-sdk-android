package com.example.demoandroid

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.toMap
import org.json.JSONObject

class PaymentSuccessfulActivity : Activity() {

    companion object {
        val EXTRA_JSON_ORDER = "EXTRA_JSON_ORDER"
    }

    private lateinit var order: Order

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val orderString = intent.getStringExtra(EXTRA_JSON_ORDER)!!
        order = Order.fromJson(
            json = JSONObject(orderString).toMap()
        )

        setContentView(R.layout.activity_payment_successful)

        findViewById<TextView>(R.id.payment_successful_order_id_text_view).text =
            "ORDER ID: ${order.id}"

        val listView = findViewById<ListView>(R.id.payment_successful_order_items_list_view)
        listView.adapter = OrderItemsAdapter(context = this, dataSource = order.items)
    }
}

class OrderItemsAdapter(private val context: Context, private val dataSource: List<OrderItem>) :
    BaseAdapter() {
    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): OrderItem {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = convertView ?: inflater.inflate(R.layout.product_row, parent, false)

        val textViewId = rowView.findViewById<TextView>(R.id.order_item_id)
        val textViewName = rowView.findViewById<TextView>(R.id.order_item_name)
        val textViewOptions = rowView.findViewById<TextView>(R.id.order_item_options)
        val textViewTotal = rowView.findViewById<TextView>(R.id.order_item_total)
        val item = getItem(position)
        textViewId.text = "Item ID: ${item.id}"
        textViewName.text = "Item name: ${item.name}"
        textViewOptions.text = item.options ?: ""
        textViewTotal.text = "Total: ${item.displayAmount}"

        return rowView
    }
}

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