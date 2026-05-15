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
import com.example.cartmart.ui.adapters.OrderAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrdersFragment : Fragment(R.layout.fragment_orders) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapter: OrderAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.ordersRecycler)
        emptyView = view.findViewById(R.id.ordersEmptyView)

        adapter = OrderAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadOrders()
    }

    private fun loadOrders() {
        viewLifecycleOwner.lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) { AppServices.api.myOrders() }

            if (response.isSuccessful) {
                val orders = response.body()?.orders.orEmpty()
                adapter.submitList(orders)
                emptyView.isVisible = orders.isEmpty()
                recyclerView.isVisible = orders.isNotEmpty()
                return@launch
            }

            if (response.code() == 401) {
                (activity as? MainActivity)?.showLoginRequired()
                return@launch
            }

            adapter.submitList(emptyList())
            emptyView.isVisible = true
        }
    }
}
