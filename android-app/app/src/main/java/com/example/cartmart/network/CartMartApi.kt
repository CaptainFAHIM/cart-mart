package com.example.cartmart.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface CartMartApi {
    @POST("api/auth/register")
    suspend fun register(@Body request: AuthRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun me(): Response<AuthResponse>

    @GET("api/products")
    suspend fun products(
        @Query("search") search: String? = null,
        @Query("category") category: String? = null,
        @Query("featured") featured: String? = null
    ): Response<ProductsResponse>

    @GET("api/products/{id}")
    suspend fun product(@Path("id") id: String): Response<ProductDto>

    @POST("api/products")
    suspend fun createProduct(@Body product: ProductWriteRequest): Response<ProductDto>

    @PUT("api/products/{id}")
    suspend fun updateProduct(@Path("id") id: String, @Body product: ProductWriteRequest): Response<ProductDto>

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(@Path("id") id: String): Response<Unit>

    @POST("api/orders")
    suspend fun placeOrder(@Body request: CreateOrderRequest): Response<OrderResponse>

    @GET("api/orders/mine")
    suspend fun myOrders(): Response<OrdersResponse>

    @GET("api/orders/{id}")
    suspend fun order(@Path("id") id: String): Response<OrderResponse>

    @GET("api/admin/stats")
    suspend fun adminStats(): Response<StatsResponse>

    @GET("api/admin/orders")
    suspend fun adminOrders(): Response<OrdersResponse>

    @PATCH("api/admin/orders/{id}")
    suspend fun updateOrderStatus(
        @Path("id") id: String,
        @Body request: OrderStatusUpdateRequest
    ): Response<OrderResponse>
}
