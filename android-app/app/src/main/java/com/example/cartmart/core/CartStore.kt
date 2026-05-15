package com.example.cartmart.core

import android.content.Context
import com.example.cartmart.network.CartItemDto
import com.example.cartmart.network.CreateOrderItemRequest
import com.example.cartmart.network.ProductDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CartStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val itemType = object : TypeToken<MutableList<CartItemDto>>() {}.type

    fun getItems(): MutableList<CartItemDto> {
        val raw = prefs.getString(KEY_CART, null) ?: return mutableListOf()
        return runCatching { gson.fromJson<MutableList<CartItemDto>>(raw, itemType) }
            .getOrElse { mutableListOf() }
            .toMutableList()
    }

    fun count(): Int = getItems().sumOf { it.quantity }

    fun add(product: ProductDto) {
        val items = getItems()
        val existing = items.indexOfFirst { it.productId == product.id }
        if (existing >= 0) {
            val current = items[existing]
            if (current.quantity < current.stock) {
                items[existing] = current.copy(quantity = current.quantity + 1)
            }
        } else if (product.stock > 0) {
            items.add(
                CartItemDto(
                    productId = product.id,
                    name = product.name,
                    price = product.price,
                    imageUrl = product.imageUrl,
                    category = product.category,
                    stock = product.stock,
                    quantity = 1
                )
            )
        }
        save(items)
    }

    fun increase(productId: String) {
        val items = getItems()
        val index = items.indexOfFirst { it.productId == productId }
        if (index >= 0) {
            val current = items[index]
            if (current.quantity < current.stock) {
                items[index] = current.copy(quantity = current.quantity + 1)
                save(items)
            }
        }
    }

    fun decrease(productId: String) {
        val items = getItems()
        val index = items.indexOfFirst { it.productId == productId }
        if (index >= 0) {
            val current = items[index]
            val updated = current.quantity - 1
            if (updated <= 0) {
                items.removeAt(index)
            } else {
                items[index] = current.copy(quantity = updated)
            }
            save(items)
        }
    }

    fun remove(productId: String) {
        val items = getItems().filterNot { it.productId == productId }.toMutableList()
        save(items)
    }

    fun clear() {
        save(emptyList())
    }

    fun toOrderItems(): List<CreateOrderItemRequest> = getItems().map {
        CreateOrderItemRequest(productId = it.productId, quantity = it.quantity)
    }

    private fun save(items: List<CartItemDto>) {
        prefs.edit().putString(KEY_CART, gson.toJson(items)).apply()
    }

    private companion object {
        const val PREFS_NAME = "cartmart_cart"
        const val KEY_CART = "cart"
    }
}
