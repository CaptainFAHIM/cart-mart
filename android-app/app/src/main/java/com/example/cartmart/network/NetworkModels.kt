package com.example.cartmart.network

import com.google.gson.annotations.SerializedName
import java.util.Locale

data class ApiUser(
    @SerializedName("id") val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "user"
)

data class AuthRequest(
    val name: String? = null,
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String = "",
    val user: ApiUser = ApiUser()
)

data class ProductDto(
    @SerializedName("_id") val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "General",
    val imageUrl: String = "",
    val price: Double = 0.0,
    val stock: Int = 0,
    val featured: Boolean = false
)

data class ProductWriteRequest(
    val name: String,
    val description: String,
    val category: String,
    val imageUrl: String,
    val price: Double,
    val stock: Int
)

data class CartItemDto(
    val productId: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val category: String,
    val stock: Int,
    val quantity: Int = 1
)

data class CreateOrderItemRequest(
    val productId: String,
    val quantity: Int
)

data class CreateOrderRequest(
    val items: List<CreateOrderItemRequest>,
    val shippingName: String,
    val shippingPhone: String,
    val shippingAddress: String,
    val paymentMethod: String
)

data class OrderItemDto(
    val product: ProductDto? = null,
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0
)

data class OrderDto(
    @SerializedName("_id") val id: String = "",
    val user: ApiUser? = null,
    val items: List<OrderItemDto> = emptyList(),
    val shippingName: String = "",
    val shippingPhone: String = "",
    val shippingAddress: String = "",
    val paymentMethod: String = "Cash on delivery",
    val totalPrice: Double = 0.0,
    val status: String = "Pending",
    val createdAt: String? = null
) {
    fun shortId(): String = "#${id.takeLast(6).uppercase(Locale.US)}"
}

data class StatsDto(
    val productCount: Int = 0,
    val orderCount: Int = 0,
    val userCount: Int = 0,
    val pendingOrders: Int = 0
)

data class ProductsResponse(val products: List<ProductDto> = emptyList())
data class OrdersResponse(val orders: List<OrderDto> = emptyList())
data class OrderResponse(val order: OrderDto = OrderDto())
data class StatsResponse(val stats: StatsDto = StatsDto())
data class ErrorResponse(val message: String = "")
data class OrderStatusUpdateRequest(val status: String)
