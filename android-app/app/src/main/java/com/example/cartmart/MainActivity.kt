package com.example.cartmart

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.cartmart.core.AppServices
import com.example.cartmart.network.ApiUser
import com.example.cartmart.ui.fragments.AdminFragment
import com.example.cartmart.ui.fragments.CartFragment
import com.example.cartmart.ui.fragments.OrdersFragment
import com.example.cartmart.ui.fragments.ProductsFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
 

class MainActivity : AppCompatActivity() {
    private lateinit var loginContainer: View
    private lateinit var appContainer: View
    private lateinit var sessionLabel: TextView
    private lateinit var logoutButton: Button
    private lateinit var productsButton: Button
    private lateinit var cartButton: Button
    private lateinit var ordersButton: Button
    private lateinit var adminButton: Button

    private var currentUser: ApiUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppServices.init(applicationContext)
        setContentView(R.layout.activity_main)

        bindViews()
        bindActions()
        restoreSession()
    }

    private fun bindViews() {
        loginContainer = findViewById(R.id.loginContainer)
        appContainer = findViewById(R.id.appContainer)
        sessionLabel = findViewById(R.id.sessionLabel)
        logoutButton = findViewById(R.id.logoutButton)
        productsButton = findViewById(R.id.productsButton)
        cartButton = findViewById(R.id.cartButton)
        ordersButton = findViewById(R.id.ordersButton)
        adminButton = findViewById(R.id.adminButton)
    }

    private fun bindActions() {
        logoutButton.setOnClickListener { logout() }
        productsButton.setOnClickListener { openFragment(ProductsFragment()) }
        cartButton.setOnClickListener { openFragment(CartFragment()) }
        ordersButton.setOnClickListener { openFragment(OrdersFragment()) }
        adminButton.setOnClickListener { openFragment(AdminFragment()) }
    }

    private fun restoreSession() {
        val token = AppServices.sessionManager.token
        if (token.isNullOrBlank()) {
            showLoginScreen()
            return
        }

        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) { AppServices.api.me() }
            if (response.isSuccessful) {
                response.body()?.user?.let { user ->
                    AppServices.sessionManager.updateUser(user)
                    showAuthenticatedScreen(user)
                    return@launch
                }
            }

            AppServices.sessionManager.clear()
            showLoginScreen()
        }
    }

    

    private fun logout() {
        AppServices.sessionManager.clear()
        currentUser = null
        showLoginScreen()
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
    }

    fun showAuthenticatedScreen(user: ApiUser) {
        currentUser = user
        sessionLabel.text = "${user.name} (${user.role})"
        adminButton.isVisible = user.role == "admin"
        appContainer.isVisible = true
        loginContainer.isVisible = false
        openDefaultFragment(user)
        refreshCartBadge()
    }

    private fun showLoginScreen() {
        currentUser = null
        appContainer.isVisible = false
        loginContainer.isVisible = true
        supportFragmentManager.beginTransaction()
            .replace(R.id.loginContainer, com.example.cartmart.ui.fragments.LoginFragment())
            .commit()
    }

    private fun openDefaultFragment(user: ApiUser) {
        if (user.role == "admin") {
            openFragment(AdminFragment())
        } else {
            openFragment(ProductsFragment())
        }
    }

    fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun refreshCartBadge() {
        val count = AppServices.cartStore.count()
        cartButton.text = if (count > 0) getString(R.string.cart_label_with_count, count) else getString(R.string.cart_label)
    }

    fun requireUser(): ApiUser? = currentUser ?: AppServices.sessionManager.user

    fun showAuthMessage(message: String, isError: Boolean) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun showLoginRequired() {
        Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
        logout()
    }
    
}
