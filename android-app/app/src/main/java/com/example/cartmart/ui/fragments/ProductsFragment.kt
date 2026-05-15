package com.example.cartmart.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
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
import com.example.cartmart.ui.adapters.ProductAdapter
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductsFragment : Fragment(R.layout.fragment_products) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var messageView: TextView
    private lateinit var searchInput: TextInputEditText
    private lateinit var searchButton: Button
    private lateinit var adapter: ProductAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.productsRecycler)
        emptyView = view.findViewById(R.id.productsEmptyView)
        messageView = view.findViewById(R.id.productsMessage)
        searchInput = view.findViewById(R.id.searchInput)
        searchButton = view.findViewById(R.id.searchButton)

        adapter = ProductAdapter(showActionButton = true) { product ->
            AppServices.cartStore.add(product)
            (activity as? MainActivity)?.refreshCartBadge()
            Toast.makeText(requireContext(), "Added to cart", Toast.LENGTH_SHORT).show()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        searchButton.setOnClickListener {
            loadProducts(searchInput.text?.toString())
        }

        loadProducts(null)
    }

    private fun loadProducts(query: String?) {
        viewLifecycleOwner.lifecycleScope.launch {
            messageView.text = "Loading products..."
            emptyView.isVisible = false

            val response = withContext(Dispatchers.IO) {
                AppServices.api.products(search = query?.trim()?.takeIf { it.isNotBlank() })
            }

            if (response.isSuccessful) {
                val products = response.body()?.products.orEmpty()
                adapter.submitList(products)
                emptyView.isVisible = products.isEmpty()
                messageView.text = if (query.isNullOrBlank()) {
                    "Showing ${products.size} products"
                } else {
                    "Showing ${products.size} results for \"$query\""
                }
            } else {
                adapter.submitList(emptyList())
                emptyView.isVisible = true
                messageView.text = "Unable to load products (${response.code()})"
            }
        }
    }
}
