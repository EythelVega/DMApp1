package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity // ¡Clave!
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.Executor

class MainActivity : FragmentActivity() { // ← CAMBIO AQUÍ
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PasalistaScreen()
            }
        }
    }
}

@Composable
fun PasalistaScreen() {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val currentTime = remember { SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date()) }

    var asistenciaRegistrada by remember { mutableStateOf<Boolean?>(null) }
    var autenticacionIntentada by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!autenticacionIntentada) {
            autenticacionIntentada = true
            showBiometricPrompt(
                activity = activity,
                onSuccess = {
                    asistenciaRegistrada = true
                },
                onError = {
                    asistenciaRegistrada = false
                }
            )
        }
    }

    Box( // Fondo ya cambiado aquí
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3F2FD)) // Fondo azul claro
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("📋 Pasalista FIME", fontSize = 40.sp)

            Spacer(modifier = Modifier.height(30.dp))

            Text("Bienvenido👋", fontSize = 20.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Text("Fecha y hora: $currentTime", fontSize = 16.sp)

            Spacer(modifier = Modifier.height(32.dp))

            when (asistenciaRegistrada) {
                true -> {
                    Text("✅ Asistencia registrada exitosamente", fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { activity.finish() }) {
                        Text("Salir")
                    }
                }
                false -> {
                    Text("❌ Falló la autenticación", fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { activity.finish() }) {
                        Text("Salir")
                    }
                }
                null -> {
                    Text("🔒 Verificando identidad...", fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    CircularProgressIndicator(modifier = Modifier.size(40.dp)) // Indicador de carga
                }
            }
        }
    }
}

fun showBiometricPrompt(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onError: () -> Unit
) {
    val executor: Executor = ContextCompat.getMainExecutor(activity)

    val biometricPrompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError()
            }

            override fun onAuthenticationFailed() {
                onError()
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Verificación biométrica")
        .setSubtitle("Usa tu huella o rostro para registrar asistencia")
        .setNegativeButtonText("Cancelar")
        .build()

    biometricPrompt.authenticate(promptInfo)
}
