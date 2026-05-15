package com.example.cartmart.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cartmart.R
import com.example.cartmart.core.formatCurrency
import com.example.cartmart.core.orderCustomerLabel
import com.example.cartmart.core.orderItemsSummary
import com.example.cartmart.core.shortOrderId
import com.example.cartmart.network.OrderDto

class OrderAdapter : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {
    private val items = mutableListOf<OrderDto>()

    fun submitList(newItems: List<OrderDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val orderNumber: TextView = itemView.findViewById(R.id.orderNumber)
        private val orderStatus: TextView = itemView.findViewById(R.id.orderStatus)
        private val orderTotal: TextView = itemView.findViewById(R.id.orderTotal)
        private val orderCustomer: TextView = itemView.findViewById(R.id.orderCustomer)
        private val orderItems: TextView = itemView.findViewById(R.id.orderItems)
        private val shippingDetails: TextView = itemView.findViewById(R.id.orderShipping)

        fun bind(order: OrderDto) {
            orderNumber.text = shortOrderId(order.id)
            orderStatus.text = order.status
            orderTotal.text = formatCurrency(order.totalPrice)
            orderCustomer.text = orderCustomerLabel(order)
            orderItems.text = orderItemsSummary(order.items)
            shippingDetails.text = "${order.shippingName}\n${order.shippingPhone}\n${order.shippingAddress}"
        }
    }
}

class AdminOrderAdapter(
    private val onSave: (String, String) -> Unit
) : RecyclerView.Adapter<AdminOrderAdapter.AdminOrderViewHolder>() {
    private val items = mutableListOf<OrderDto>()
    private val statuses = listOf("Pending", "Processing", "Shipped", "Delivered", "Cancelled")

    fun submitList(newItems: List<OrderDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminOrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_order, parent, false)
        return AdminOrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminOrderViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class AdminOrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val orderNumber: TextView = itemView.findViewById(R.id.adminOrderNumber)
        private val orderCustomer: TextView = itemView.findViewById(R.id.adminOrderCustomer)
        private val shippingDetails: TextView = itemView.findViewById(R.id.adminOrderShipping)
        private val orderItems: TextView = itemView.findViewById(R.id.adminOrderItems)
        private val orderTotal: TextView = itemView.findViewById(R.id.adminOrderTotal)
        private val statusSpinner: Spinner = itemView.findViewById(R.id.adminOrderStatus)
        private val saveButton: Button = itemView.findViewById(R.id.adminOrderSave)

        fun bind(order: OrderDto) {
            orderNumber.text = shortOrderId(order.id)
            orderCustomer.text = orderCustomerLabel(order)
            shippingDetails.text = "${order.shippingName}\n${order.shippingPhone}\n${order.shippingAddress}"
            orderItems.text = orderItemsSummary(order.items)
            orderTotal.text = formatCurrency(order.totalPrice)

            val adapter = ArrayAdapter(itemView.context, android.R.layout.simple_spinner_item, statuses).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            statusSpinner.adapter = adapter
            val selectedIndex = statuses.indexOf(order.status).takeIf { it >= 0 } ?: 0
            statusSpinner.setSelection(selectedIndex, false)

            saveButton.setOnClickListener {
                onSave(order.id, statusSpinner.selectedItem?.toString() ?: order.status)
            }
        }
    }
}
