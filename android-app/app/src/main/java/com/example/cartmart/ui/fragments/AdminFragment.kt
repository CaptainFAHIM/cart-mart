package com.example.cartmart.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cartmart.MainActivity
import com.example.cartmart.R
import com.example.cartmart.core.AppServices
import com.example.cartmart.network.OrderStatusUpdateRequest
import com.example.cartmart.ui.adapters.AdminOrderAdapter
import com.example.cartmart.ui.adapters.ProductAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminFragment : Fragment(R.layout.fragment_admin) {
    private lateinit var statsText: TextView
    private lateinit var productsRecycler: RecyclerView
    private lateinit var ordersRecycler: RecyclerView
    private lateinit var productsAdapter: ProductAdapter
    private lateinit var ordersAdapter: AdminOrderAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        statsText = view.findViewById(R.id.adminStatsText)
        productsRecycler = view.findViewById(R.id.adminProductsRecycler)
        ordersRecycler = view.findViewById(R.id.adminOrdersRecycler)

        productsAdapter = ProductAdapter(showActionButton = false, onAddClick = null)
        ordersAdapter = AdminOrderAdapter { orderId, status ->
            updateOrderStatus(orderId, status)
        }

        productsRecycler.layoutManager = LinearLayoutManager(requireContext())
        ordersRecycler.layoutManager = LinearLayoutManager(requireContext())
        productsRecycler.adapter = productsAdapter
        ordersRecycler.adapter = ordersAdapter

        loadAdminData()
    }

    private fun loadAdminData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val statsResponse = withContext(Dispatchers.IO) { AppServices.api.adminStats() }
            val productsResponse = withContext(Dispatchers.IO) { AppServices.api.products() }
            val ordersResponse = withContext(Dispatchers.IO) { AppServices.api.adminOrders() }

            if (statsResponse.isSuccessful && productsResponse.isSuccessful && ordersResponse.isSuccessful) {
                val stats = statsResponse.body()?.stats
                val products = productsResponse.body()?.products.orEmpty()
                val orders = ordersResponse.body()?.orders.orEmpty()

                statsText.text = buildString {
                    appendLine("Products: ${stats?.productCount ?: 0}")
                    appendLine("Orders: ${stats?.orderCount ?: 0}")
                    appendLine("Users: ${stats?.userCount ?: 0}")
                    appendLine("Pending: ${stats?.pendingOrders ?: 0}")
                }
                productsAdapter.submitList(products)
                ordersAdapter.submitList(orders)
                return@launch
            }

            val unauthorized = listOf(statsResponse, productsResponse, ordersResponse).any { it.code() == 401 }
            if (unauthorized) {
                (activity as? MainActivity)?.showLoginRequired()
                return@launch
            }

            statsText.text = "Failed to load admin data"
        }
    }

    private fun updateOrderStatus(orderId: String, status: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                AppServices.api.updateOrderStatus(orderId, OrderStatusUpdateRequest(status))
            }

            if (response.isSuccessful) {
                loadAdminData()
                return@launch
            }

            if (response.code() == 401) {
                (activity as? MainActivity)?.showLoginRequired()
            }
        }
    }
}
