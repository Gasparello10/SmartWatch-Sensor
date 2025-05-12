package com.example.testeacelerom.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.testeacelerom.R
import java.io.PrintWriter
import java.net.Socket
import java.util.concurrent.Executors

class SensorService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private val executor = Executors.newSingleThreadExecutor()

    private val serverIP = "192.168.0.37" // Alterar para o IP do seu computador
    private val serverPort = 5000

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        createNotificationChannel()
        startForeground(1, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        executor.shutdown()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val timestamp = System.currentTimeMillis()
            val x = it.values[0]
            val y = it.values[1]
            val z = it.values[2]
            val data = "$timestamp,$x,$y,$z"

            Log.d("Acelerômetro", data)
            sendDataToServer(data)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Ignorado
    }

    private fun sendDataToServer(data: String) {
        executor.execute {
            try {
                Socket(serverIP, serverPort).use { socket ->
                    PrintWriter(socket.getOutputStream(), true).use { writer ->
                        writer.println(data)
                    }
                }
            } catch (e: Exception) {
                Log.e("SocketError", "Falha ao enviar dados", e)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "sensor_channel",
                "Sensor Data Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, "sensor_channel")
            .setContentTitle("Coletando dados do sensor")
            .setContentText("Transmissão ativa para o servidor")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }
}
