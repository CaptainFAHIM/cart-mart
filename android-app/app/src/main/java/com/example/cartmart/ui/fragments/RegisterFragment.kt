package com.example.cartmart.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.cartmart.core.AppServices
import com.example.cartmart.network.AuthRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterFragment : Fragment() {
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var registerButton: Button
    private lateinit var goToLoginButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(com.example.cartmart.R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nameInput = view.findViewById(com.example.cartmart.R.id.nameInput)
        emailInput = view.findViewById(com.example.cartmart.R.id.emailInput)
        passwordInput = view.findViewById(com.example.cartmart.R.id.passwordInput)
        registerButton = view.findViewById(com.example.cartmart.R.id.registerButton)
        goToLoginButton = view.findViewById(com.example.cartmart.R.id.goToLoginButton)

        registerButton.setOnClickListener { register() }
        goToLoginButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.example.cartmart.R.id.loginContainer, LoginFragment())
                .commit()
        }
    }

    private fun register() {
        val name = nameInput.text?.toString()?.trim().orEmpty()
        val email = emailInput.text?.toString()?.trim().orEmpty()
        val password = passwordInput.text?.toString().orEmpty()

        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            Toast.makeText(requireContext(), "All fields are required.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                val request = AuthRequest(name = name, email = email, password = password)
                AppServices.api.register(request)
            }

            if (response.isSuccessful) {
                val auth = response.body()
                if (auth != null) {
                    AppServices.sessionManager.saveSession(auth)
                    (activity as? com.example.cartmart.MainActivity)?.showAuthenticatedScreen(auth.user)
                    Toast.makeText(requireContext(), "Account created", Toast.LENGTH_SHORT).show()
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

            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }
    }
}
