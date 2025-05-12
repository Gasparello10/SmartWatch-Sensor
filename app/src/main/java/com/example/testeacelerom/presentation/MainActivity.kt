package com.example.testeacelerom.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.example.testeacelerom.R
import com.example.testeacelerom.presentation.theme.TesteAceleromTheme
import com.example.testeacelerom.service.SensorService


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        // Inicia o serviço de coleta de dados do acelerômetro
        val intent = Intent(this, SensorService::class.java)
        startForegroundService(intent)

        setContent {
            WearApp(context = this)
        }
    }
}

@Composable
fun WearApp(context: Context) {
    var isCapturing by remember { mutableStateOf(false) }

    TesteAceleromTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    TimeText()
                }

                item {
                    Text(
                        text = if (isCapturing) "Status: Capturando" else "Status: Parado",
                        color = MaterialTheme.colors.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                item {
                    Button(
                        onClick = {
                            if (!isCapturing) {
                                val intent = Intent(context, SensorService::class.java)
                                ContextCompat.startForegroundService(context, intent)
                                isCapturing = true
                            }
                        },
                        modifier = Modifier.size(width = 200.dp, height = 60.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "Iniciar Captura")
                    }
                }

                item {
                    Button(
                        onClick = {
                            if (isCapturing) {
                                context.stopService(Intent(context, SensorService::class.java))
                                isCapturing = false
                            }
                        },
                        modifier = Modifier.size(width = 200.dp, height = 60.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Red,
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "Finalizar Captura")
                    }
                }
            }
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


