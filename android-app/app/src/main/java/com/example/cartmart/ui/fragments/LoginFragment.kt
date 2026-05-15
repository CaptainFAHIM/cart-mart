package com.example.cartmart.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.cartmart.core.AppServices
import com.example.cartmart.network.AuthRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment() {
    private lateinit var authMessage: TextView
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var goToRegisterButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(com.example.cartmart.R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authMessage = view.findViewById(com.example.cartmart.R.id.authMessage)
        emailInput = view.findViewById(com.example.cartmart.R.id.emailInput)
        passwordInput = view.findViewById(com.example.cartmart.R.id.passwordInput)
        loginButton = view.findViewById(com.example.cartmart.R.id.loginButton)
        goToRegisterButton = view.findViewById(com.example.cartmart.R.id.goToRegisterButton)

        loginButton.setOnClickListener { authenticate() }
        goToRegisterButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.example.cartmart.R.id.loginContainer, RegisterFragment())
                .commit()
        }
    }

    private fun authenticate() {
        val email = emailInput.text?.toString()?.trim().orEmpty()
        val password = passwordInput.text?.toString().orEmpty()

        if (email.isBlank() || password.isBlank()) {
            showAuthMessage("Email and password are required.", isError = true)
            return
        }

        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                val request = AuthRequest(name = null, email = email, password = password)
                AppServices.api.login(request)
            }

            if (response.isSuccessful) {
                val auth = response.body()
                if (auth != null) {
                    AppServices.sessionManager.saveSession(auth)
                    (activity as? com.example.cartmart.MainActivity)?.showAuthenticatedScreen(auth.user)
                    Toast.makeText(requireContext(), "Logged in", Toast.LENGTH_SHORT).show()
                    return@launch
                }
            }

            val msg = runCatching {
                val errorBody = response.errorBody()?.string().orEmpty()
                if (errorBody.contains("message", ignoreCase = true)) {
                    val regex = Regex("\\\"message\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"")
                    regex.find(errorBody)?.groupValues?.getOrNull(1) ?: errorBody
                } else {
                    errorBody
                }
            }.getOrNull().orEmpty().ifBlank { "Request failed (${response.code()})" }

            showAuthMessage(msg, isError = true)
        }
    }

    private fun showAuthMessage(message: String, isError: Boolean) {
        authMessage.text = message
        authMessage.visibility = View.VISIBLE
        authMessage.setTextColor(
            if (isError) requireContext().getColor(android.R.color.holo_red_light) else requireContext().getColor(android.R.color.holo_green_light)
        )
    }
}
