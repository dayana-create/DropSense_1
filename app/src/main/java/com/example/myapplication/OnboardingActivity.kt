package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {

    private lateinit var imgPage: ImageView
    private lateinit var txtTitle: TextView
    private lateinit var txtDescription: TextView
    private lateinit var txtIndicator: TextView
    private lateinit var btnBack: Button
    private lateinit var btnNext: Button

    private var currentPage = 0

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            completeOnboarding()
        }

    private val titles = arrayOf(
        "Control de tu bomba",
        "Monitorea el nivel de agua",
        "Recibe avisos importantes"
    )

    private val descriptions = arrayOf(
        "Controla el funcionamiento de la bomba y consulta el estado del sistema desde una sola aplicación.",
        "Visualiza el nivel de agua y conoce si la bomba está encendida o apagada.",
        "Permite las notificaciones para recibir avisos del sistema cuando estén disponibles."
    )

    private val images = intArrayOf(
        R.drawable.ic_water_drop,
        R.drawable.ic_water_level,
        R.drawable.ic_notifications
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        imgPage = findViewById(R.id.imgPage)
        txtTitle = findViewById(R.id.txtOnboardingTitle)
        txtDescription = findViewById(R.id.txtOnboardingDescription)
        txtIndicator = findViewById(R.id.txtPageIndicator)
        btnBack = findViewById(R.id.btnBack)
        btnNext = findViewById(R.id.btnNext)

        btnBack.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                updatePage()
            }
        }

        btnNext.setOnClickListener {
            if (currentPage < titles.lastIndex) {
                currentPage++
                updatePage()
            } else {
                requestNotificationPermission()
            }
        }

        updatePage()
    }

    private fun updatePage() {
        imgPage.setImageResource(images[currentPage])
        txtTitle.text = titles[currentPage]
        txtDescription.text = descriptions[currentPage]
        txtIndicator.text = "${currentPage + 1} / ${titles.size}"

        btnBack.isEnabled = currentPage > 0
        btnBack.alpha = if (currentPage > 0) 1f else 0.4f

        btnNext.text =
            if (currentPage == titles.lastIndex) "Permitir y continuar"
            else "Siguiente"
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            completeOnboarding()
        }
    }

    private fun completeOnboarding() {
        getSharedPreferences("app_preferences", MODE_PRIVATE)
            .edit()
            .putBoolean("onboarding_completed", true)
            .apply()

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
