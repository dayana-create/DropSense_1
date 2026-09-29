package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Punto de entrada para la inicialización de recursos/verificación de sesión.
        // Actualmente la aplicación no utiliza tokens, por lo que no se realiza
        // una verificación de autenticación aquí.
        initializeApp()
    }

    private fun initializeApp() {
        handler.postDelayed({
            val preferences = getSharedPreferences("app_preferences", MODE_PRIVATE)
            val onboardingCompleted =
                preferences.getBoolean("onboarding_completed", false)

            val nextActivity = if (onboardingCompleted) {
                MainActivity::class.java
            } else {
                OnboardingActivity::class.java
            }

            startActivity(Intent(this, nextActivity))
            finish()
        }, 800)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
