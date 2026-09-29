package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    /*
     * CAMBIAR ESTA IP POR LA IP QUE
     * OBTENGA EL ESP32.
     *
     * Ejemplo:
     * http://192.168.1.50
     */
    private val esp32Url = "http://192.168.1.50"

    private lateinit var txtConnection: TextView
    private lateinit var txtLevel: TextView
    private lateinit var txtPump: TextView
    private lateinit var txtMode: TextView
    private lateinit var txtMessage: TextView
    private lateinit var progressLevel: ProgressBar

    private val executor = Executors.newSingleThreadScheduledExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        txtConnection = findViewById(R.id.txtConnection)
        txtLevel = findViewById(R.id.txtLevel)
        txtPump = findViewById(R.id.txtPump)
        txtMode = findViewById(R.id.txtMode)
        txtMessage = findViewById(R.id.txtMessage)
        progressLevel = findViewById(R.id.progressLevel)

        val btnOn: Button = findViewById(R.id.btnOn)
        val btnOff: Button = findViewById(R.id.btnOff)
        val btnAuto: Button = findViewById(R.id.btnAuto)
        val btnRefresh: Button = findViewById(R.id.btnRefresh)

        btnOn.setOnClickListener {
            sendCommand("/api/pump?state=on")
        }

        btnOff.setOnClickListener {
            sendCommand("/api/pump?state=off")
        }

        btnAuto.setOnClickListener {
            sendCommand("/api/mode?mode=auto")
        }

        btnRefresh.setOnClickListener {
            getStatus()
        }

        executor.scheduleWithFixedDelay(
            {
                getStatus()
            },
            0,
            2,
            TimeUnit.SECONDS
        )
    }

    private fun getStatus() {

        Thread {

            try {

                val url = URL("$esp32Url/api/status")

                val connection =
                    url.openConnection() as HttpURLConnection

                connection.requestMethod = "GET"
                connection.connectTimeout = 3000
                connection.readTimeout = 3000

                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {

                    val reader = BufferedReader(
                        InputStreamReader(connection.inputStream)
                    )

                    val response = reader.readText()

                    reader.close()
                    connection.disconnect()

                    val json = JSONObject(response)

                    val level = json.getInt("level")
                    val pump = json.getBoolean("pump")
                    val mode = json.getString("mode")

                    runOnUiThread {

                        updateInterface(
                            level,
                            pump,
                            mode
                        )
                    }

                } else {

                    connection.disconnect()

                    runOnUiThread {
                        connectionError()
                    }
                }

            } catch (e: Exception) {

                runOnUiThread {
                    connectionError()
                }
            }

        }.start()
    }

    private fun sendCommand(endpoint: String) {

        Thread {

            try {

                val url = URL("$esp32Url$endpoint")

                val connection =
                    url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.connectTimeout = 3000
                connection.readTimeout = 3000

                val responseCode = connection.responseCode

                connection.disconnect()

                runOnUiThread {

                    if (responseCode == HttpURLConnection.HTTP_OK) {

                        txtMessage.text =
                            "Comando enviado correctamente"

                        getStatus()

                    } else {

                        txtMessage.text =
                            "Error al enviar comando: $responseCode"
                    }
                }

            } catch (e: Exception) {

                runOnUiThread {

                    txtMessage.text =
                        "No se pudo conectar con el ESP32"
                }
            }

        }.start()
    }

    private fun updateInterface(
        level: Int,
        pump: Boolean,
        mode: String
    ) {

        txtConnection.text = "Estado: CONECTADO"

        txtLevel.text = "$level%"

        progressLevel.progress =
            level.coerceIn(0, 100)

        txtPump.text =
            if (pump) {
                "Bomba: ENCENDIDA"
            } else {
                "Bomba: APAGADA"
            }

        txtMode.text =
            "Modo: ${mode.uppercase()}"

        txtMessage.text =
            "Sistema actualizado"
    }

    private fun connectionError() {

        txtConnection.text =
            "Estado: DESCONECTADO"

        txtMessage.text =
            "No se pudo establecer comunicación con el ESP32"
    }

    override fun onDestroy() {

        executor.shutdownNow()

        super.onDestroy()
    }
}