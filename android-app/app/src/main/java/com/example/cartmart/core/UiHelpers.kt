package com.example.cartmart.core

import com.example.cartmart.network.CartItemDto
import com.example.cartmart.network.OrderDto
import com.example.cartmart.network.OrderItemDto
import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(value: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.US).format(value)
}

fun orderItemsSummary(items: List<OrderItemDto>): String {
    return items.joinToString(separator = "\n") { item ->
        val name = item.product?.name?.takeIf { it.isNotBlank() } ?: item.name
        "$name x${item.quantity} - ${formatCurrency(item.price)} each"
    }
}

fun cartTotal(items: List<CartItemDto>): Double = items.sumOf { it.price * it.quantity }

fun shortOrderId(orderId: String): String = "#${orderId.takeLast(6).uppercase(Locale.US)}"

fun orderCustomerLabel(order: OrderDto): String {
    val user = order.user
    return if (user == null) "Unknown customer" else "${user.name} (${user.email})"
}
