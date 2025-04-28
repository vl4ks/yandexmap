package com.denisova.yandexmap

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class AuthActivity : AppCompatActivity() {
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)
        sharedPref = getSharedPreferences("AuthPref", MODE_PRIVATE)

        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val registerButton: Button = findViewById(R.id.registerButton)
        val loginButton: Button = findViewById(R.id.loginButton)

        if (sharedPref.getBoolean("isLoggedIn", false)) {
            val email = sharedPref.getString("email", "")
            Toast.makeText(this, "Добро пожаловать, $email", Toast.LENGTH_SHORT).show()
            finishWithResult(true)
        }

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                saveAuthData(email, true)
                Toast.makeText(this, "Регистрация успешна: $email", Toast.LENGTH_SHORT).show()
                finishWithResult(true)
            } else {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                saveAuthData(email, true)
                Toast.makeText(this, "Вход выполнен: $email", Toast.LENGTH_SHORT).show()
                finishWithResult(true)
            } else {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveAuthData(email: String, isLoggedIn: Boolean) {
        sharedPref.edit().apply {
            putString("email", email)
            putBoolean("isLoggedIn", isLoggedIn)
            apply()
        }
    }

    private fun finishWithResult(success: Boolean) {
        val resultIntent = Intent().apply {
            putExtra("AUTH_RESULT", success)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    companion object {
        fun logout(context: Context) {
            context.getSharedPreferences("AuthPref", MODE_PRIVATE).edit().apply {
                putBoolean("isLoggedIn", false)
                apply()
            }
        }
    }
}