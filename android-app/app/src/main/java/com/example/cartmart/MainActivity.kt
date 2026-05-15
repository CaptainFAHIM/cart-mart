package com.example.cartmart

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.cartmart.core.AppServices
import com.example.cartmart.network.ApiUser
import com.example.cartmart.network.AuthRequest
import com.example.cartmart.ui.fragments.AdminFragment
import com.example.cartmart.ui.fragments.CartFragment
import com.example.cartmart.ui.fragments.OrdersFragment
import com.example.cartmart.ui.fragments.ProductsFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var loginContainer: View
    private lateinit var appContainer: View
    private lateinit var sessionLabel: TextView
    private lateinit var authMessage: TextView
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
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
        authMessage = findViewById(R.id.authMessage)
        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        logoutButton = findViewById(R.id.logoutButton)
        productsButton = findViewById(R.id.productsButton)
        cartButton = findViewById(R.id.cartButton)
        ordersButton = findViewById(R.id.ordersButton)
        adminButton = findViewById(R.id.adminButton)
    }

    private fun bindActions() {
        loginButton.setOnClickListener { authenticate(isRegister = false) }
        registerButton.setOnClickListener { authenticate(isRegister = true) }
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

    private fun authenticate(isRegister: Boolean) {
        val name = nameInput.text?.toString()?.trim().orEmpty()
        val email = emailInput.text?.toString()?.trim().orEmpty()
        val password = passwordInput.text?.toString().orEmpty()

        if (email.isBlank() || password.isBlank()) {
            showAuthMessage("Email and password are required.", isError = true)
            return
        }

        if (isRegister && name.isBlank()) {
            showAuthMessage("Full name is required for registration.", isError = true)
            return
        }

        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                val request = AuthRequest(
                    name = if (isRegister) name else null,
                    email = email,
                    password = password
                )

                if (isRegister) {
                    AppServices.api.register(request)
                } else {
                    AppServices.api.login(request)
                }
            }

            if (response.isSuccessful) {
                val auth = response.body()
                if (auth != null) {
                    AppServices.sessionManager.saveSession(auth)
                    showAuthenticatedScreen(auth.user)
                    Toast.makeText(
                        this@MainActivity,
                        if (isRegister) "Account created" else "Logged in",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }
            }

            showAuthMessage(extractError(response), isError = true)
        }
    }

    private fun logout() {
        AppServices.sessionManager.clear()
        currentUser = null
        showLoginScreen()
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
    }

    private fun showAuthenticatedScreen(user: ApiUser) {
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
        authMessage.text = ""
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
        authMessage.text = message
        authMessage.visibility = View.VISIBLE
        authMessage.setTextColor(
            if (isError) getColor(android.R.color.holo_red_light) else getColor(android.R.color.holo_green_light)
        )
    }

    fun showLoginRequired() {
        Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
        logout()
    }

    private fun extractError(response: Response<*>): String {
        return runCatching {
            val errorBody = response.errorBody()?.string().orEmpty()
            if (errorBody.contains("message", ignoreCase = true)) {
                val regex = Regex("\\\"message\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"")
                regex.find(errorBody)?.groupValues?.getOrNull(1) ?: errorBody
            } else {
                errorBody
            }
        }.getOrNull().orEmpty().ifBlank { "Request failed (${response.code()})" }
    }
}
