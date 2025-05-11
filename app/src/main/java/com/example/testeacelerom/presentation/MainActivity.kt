/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.testeacelerom.presentation


import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.testeacelerom.R
import com.example.testeacelerom.presentation.theme.TesteAceleromTheme
import java.io.PrintWriter
import java.net.Socket
import java.util.concurrent.Executors

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private val executor = Executors.newSingleThreadExecutor()
    private val serverIP = "192.168.0.97" // Substitua pelo IP do seu servidor
    private val serverPort = 5000

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        // Inicializa SensorManager e registra acelerômetro
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        setContent {
            WearApp("Android")
        }
    }

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

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Ignorado por enquanto
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        executor.shutdown()
    }
}


@Composable
fun WearApp(greetingName: String) {
    TesteAceleromTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            Greeting(greetingName = greetingName)
        }
    }
}

@Composable
fun Greeting(greetingName: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(R.string.hello_world, greetingName)
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}