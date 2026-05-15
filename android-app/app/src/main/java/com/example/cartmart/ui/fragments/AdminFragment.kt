package com.example.cartmart.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
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
import com.example.cartmart.network.OrderStatusUpdateRequest
import com.example.cartmart.network.ProductDto
import com.example.cartmart.network.ProductWriteRequest
import com.example.cartmart.ui.adapters.AdminOrderAdapter
import com.example.cartmart.ui.adapters.ProductAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminFragment : Fragment(R.layout.fragment_admin) {
    private lateinit var statsText: TextView
    private lateinit var productFormTitle: TextView
    private lateinit var productFormMessage: TextView
    private lateinit var productNameInput: EditText
    private lateinit var productDescriptionInput: EditText
    private lateinit var productCategoryInput: EditText
    private lateinit var productImageUrlInput: EditText
    private lateinit var productPriceInput: EditText
    private lateinit var productStockInput: EditText
    private lateinit var productFeaturedSwitch: com.google.android.material.switchmaterial.SwitchMaterial
    private lateinit var productSubmit: Button
    private lateinit var resetProductButton: Button
    private lateinit var productsRecycler: RecyclerView
    private lateinit var ordersRecycler: RecyclerView
    private lateinit var productsAdapter: ProductAdapter
    private lateinit var ordersAdapter: AdminOrderAdapter
    private var editingProductId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        statsText = view.findViewById(R.id.adminStatsText)
        productFormTitle = view.findViewById(R.id.productFormTitle)
        productFormMessage = view.findViewById(R.id.productFormMessage)
        productNameInput = view.findViewById(R.id.productNameInput)
        productDescriptionInput = view.findViewById(R.id.productDescriptionInput)
        productCategoryInput = view.findViewById(R.id.productCategoryInput)
        productImageUrlInput = view.findViewById(R.id.productImageUrlInput)
        productPriceInput = view.findViewById(R.id.productPriceInput)
        productStockInput = view.findViewById(R.id.productStockInput)
        productFeaturedSwitch = view.findViewById(R.id.productFeaturedSwitch)
        productSubmit = view.findViewById(R.id.productSubmit)
        resetProductButton = view.findViewById(R.id.resetProductButton)
        productsRecycler = view.findViewById(R.id.adminProductsRecycler)
        ordersRecycler = view.findViewById(R.id.adminOrdersRecycler)

        productsAdapter = ProductAdapter(
            showActionButton = true,
            actionButtonText = "Edit",
            allowOutOfStock = true
        ) { product ->
            fillProductForm(product)
        }
        ordersAdapter = AdminOrderAdapter { orderId, status ->
            updateOrderStatus(orderId, status)
        }

        productsRecycler.layoutManager = LinearLayoutManager(requireContext())
        ordersRecycler.layoutManager = LinearLayoutManager(requireContext())
        productsRecycler.adapter = productsAdapter
        ordersRecycler.adapter = ordersAdapter

        resetProductButton.setOnClickListener { clearProductForm() }
        productSubmit.setOnClickListener { saveProduct() }

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
                if (editingProductId == null) {
                    productFormMessage.text = "Tap Edit on a product to update it, or fill the form to add a new one."
                }
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

    private fun fillProductForm(product: ProductDto) {
        editingProductId = product.id
        productFormTitle.text = "Edit product"
        productSubmit.text = "Update product"
        productFormMessage.text = "Editing ${product.name}"

        productNameInput.setText(product.name)
        productDescriptionInput.setText(product.description)
        productCategoryInput.setText(product.category)
        productImageUrlInput.setText(product.imageUrl)
        productPriceInput.setText(product.price.toString())
        productStockInput.setText(product.stock.toString())
        productFeaturedSwitch.isChecked = product.featured
    }

    private fun clearProductForm() {
        editingProductId = null
        productFormTitle.text = "Add product"
        productSubmit.text = "Save product"
        productFormMessage.text = ""

        productNameInput.setText("")
        productDescriptionInput.setText("")
        productCategoryInput.setText("")
        productImageUrlInput.setText("")
        productPriceInput.setText("")
        productStockInput.setText("")
        productFeaturedSwitch.isChecked = false
    }

    private fun saveProduct() {
        val name = productNameInput.text?.toString()?.trim().orEmpty()
        val description = productDescriptionInput.text?.toString()?.trim().orEmpty()
        val category = productCategoryInput.text?.toString()?.trim().orEmpty()
        val imageUrl = productImageUrlInput.text?.toString()?.trim().orEmpty()
        val price = productPriceInput.text?.toString()?.trim()?.toDoubleOrNull()
        val stock = productStockInput.text?.toString()?.trim()?.toIntOrNull()

        if (name.isBlank() || description.isBlank() || category.isBlank() || imageUrl.isBlank() || price == null || stock == null) {
            Toast.makeText(requireContext(), "Please complete all product fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val payload = ProductWriteRequest(
            name = name,
            description = description,
            category = category,
            imageUrl = imageUrl,
            price = price,
            stock = stock,
            featured = productFeaturedSwitch.isChecked
        )

        viewLifecycleOwner.lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                if (editingProductId.isNullOrBlank()) {
                    AppServices.api.createProduct(payload)
                } else {
                    AppServices.api.updateProduct(editingProductId!!, payload)
                }
            }

            if (response.isSuccessful) {
                Toast.makeText(
                    requireContext(),
                    if (editingProductId.isNullOrBlank()) "Product added" else "Product updated",
                    Toast.LENGTH_SHORT
                ).show()
                clearProductForm()
                loadAdminData()
                return@launch
            }

            if (response.code() == 401) {
                (activity as? MainActivity)?.showLoginRequired()
                return@launch
            }

            Toast.makeText(requireContext(), "Unable to save product (${response.code()})", Toast.LENGTH_SHORT).show()
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
