package com.example.cartmart.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cartmart.MainActivity
import com.example.cartmart.R
import com.example.cartmart.core.AppServices
import com.example.cartmart.core.cartTotal
import com.example.cartmart.core.formatCurrency
import com.example.cartmart.network.CreateOrderRequest
import com.example.cartmart.ui.adapters.CartAdapter
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CartFragment : Fragment(R.layout.fragment_cart) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var subtotalView: TextView
    private lateinit var shippingView: TextView
    private lateinit var totalView: TextView
    private lateinit var messageView: TextView
    private lateinit var shippingName: TextInputEditText
    private lateinit var shippingPhone: TextInputEditText
    private lateinit var shippingAddress: TextInputEditText
    private lateinit var paymentSpinner: Spinner
    private lateinit var placeOrderButton: Button
    private lateinit var adapter: CartAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.cartRecycler)
        emptyView = view.findViewById(R.id.cartEmptyView)
        subtotalView = view.findViewById(R.id.cartSubtotal)
        shippingView = view.findViewById(R.id.cartShipping)
        totalView = view.findViewById(R.id.cartTotal)
        messageView = view.findViewById(R.id.checkoutMessage)
        shippingName = view.findViewById(R.id.shippingName)
        shippingPhone = view.findViewById(R.id.shippingPhone)
        shippingAddress = view.findViewById(R.id.shippingAddress)
        paymentSpinner = view.findViewById(R.id.paymentMethodSpinner)
        placeOrderButton = view.findViewById(R.id.placeOrderButton)

        adapter = CartAdapter(
            onIncrease = {
                AppServices.cartStore.increase(it)
                renderCart()
                (activity as? MainActivity)?.refreshCartBadge()
            },
            onDecrease = {
                AppServices.cartStore.decrease(it)
                renderCart()
                (activity as? MainActivity)?.refreshCartBadge()
            },
            onRemove = {
                AppServices.cartStore.remove(it)
                renderCart()
                (activity as? MainActivity)?.refreshCartBadge()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        paymentSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listOf("Cash on delivery", "Mobile banking")
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        shippingName.setText(AppServices.sessionManager.user?.name.orEmpty())

        placeOrderButton.setOnClickListener { placeOrder() }

        renderCart()
    }

    private fun renderCart() {
        val items = AppServices.cartStore.getItems()
        adapter.submitList(items)

        val subtotal = cartTotal(items)
        val shipping = if (items.isNotEmpty()) 4.99 else 0.0
        val total = subtotal + shipping

        emptyView.isVisible = items.isEmpty()
        recyclerView.isVisible = items.isNotEmpty()

        subtotalView.text = "Subtotal: ${formatCurrency(subtotal)}"
        shippingView.text = "Shipping: ${formatCurrency(shipping)}"
        totalView.text = "Total: ${formatCurrency(total)}"
        messageView.isVisible = false
    }

    private fun placeOrder() {
        val items = AppServices.cartStore.getItems()
        if (items.isEmpty()) {
            Toast.makeText(requireContext(), "Your cart is empty", Toast.LENGTH_SHORT).show()
            return
        }

        val shippingNameValue = shippingName.text?.toString()?.trim().orEmpty()
        val shippingPhoneValue = shippingPhone.text?.toString()?.trim().orEmpty()
        val shippingAddressValue = shippingAddress.text?.toString()?.trim().orEmpty()
        val paymentMethodValue = paymentSpinner.selectedItem?.toString().orEmpty()

        if (shippingNameValue.isBlank() || shippingPhoneValue.isBlank() || shippingAddressValue.isBlank()) {
            showMessage("Shipping name, phone, and address are required.", isError = true)
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                AppServices.api.placeOrder(
                    CreateOrderRequest(
                        items = AppServices.cartStore.toOrderItems(),
                        shippingName = shippingNameValue,
                        shippingPhone = shippingPhoneValue,
                        shippingAddress = shippingAddressValue,
                        paymentMethod = paymentMethodValue
                    )
                )
            }

            if (response.isSuccessful) {
                AppServices.cartStore.clear()
                (activity as? MainActivity)?.refreshCartBadge()
                renderCart()
                shippingName.text?.clear()
                shippingPhone.text?.clear()
                shippingAddress.text?.clear()
                showMessage("Order placed successfully", isError = false)
                return@launch
            }

            if (response.code() == 401) {
                (activity as? MainActivity)?.showLoginRequired()
                return@launch
            }

            showMessage("Failed to place order (${response.code()})", isError = true)
        }
    }

    private fun showMessage(message: String, isError: Boolean) {
        messageView.isVisible = true
        messageView.text = message
        messageView.setTextColor(
            if (isError) requireContext().getColor(android.R.color.holo_red_light)
            else requireContext().getColor(android.R.color.holo_green_light)
        )
    }
}
