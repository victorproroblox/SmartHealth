package com.smarthealth.watch.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.smarthealth.watch.R
import com.smarthealth.watch.data.Pantalla
import com.smarthealth.watch.data.Store
import com.smarthealth.watch.domain.META_PASOS
import com.smarthealth.watch.domain.calcularEstadisticas
import com.smarthealth.watch.domain.formatoPasos
import com.smarthealth.watch.domain.mensajeMeta
import com.smarthealth.watch.domain.metaAlcanzada
import com.smarthealth.watch.domain.porcentajeMeta
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val pedirPermiso = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Store.init(this)
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)

        setContent {
            MaterialTheme {
                AppScaffold { Contenido() }
            }
        }
    }
}

private val Rosa = Color(0xFFFF5A6E)
private val Menta = Color(0xFF78DC96)
private val Gris = Color(0xFFC4C4CC)
private val horaFormato = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

@Composable
private fun Contenido() {
    val ui by Store.state.collectAsStateWithLifecycle()

    // Frecuencia cardiaca simulada: cambia sola cada 3 segundos
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            Store.actualizarFrecuencia()
        }
    }

    BackHandler(enabled = ui.pantalla == Pantalla.ESTADISTICAS) { Store.irA(Pantalla.PRINCIPAL) }

    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 26.dp, vertical = 34.dp),
        ) {
            if (ui.pantalla == Pantalla.PRINCIPAL) Principal() else Estadisticas()
        }
    }
}

@Composable
private fun Principal() {
    val ui by Store.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Nombre e icono + título
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Image(painterResource(R.drawable.ic_heart), contentDescription = "Icono de SmartHealth", modifier = Modifier.size(18.dp))
        Text("SmartHealth", fontSize = 15.sp, color = Rosa, fontWeight = FontWeight.Bold)
    }
    Text("Usuario: ${ui.usuario}", fontSize = 12.sp, color = Gris)

    // Contador de pasos
    Text(formatoPasos(ui.pasos), fontSize = 28.sp, color = Color.White, fontWeight = FontWeight.Bold)
    Text("pasos", fontSize = 11.sp, color = Gris)

    // Meta de 5,000 pasos
    Barra(porcentajeMeta(ui.pasos))
    Text(
        "Meta ${formatoPasos(META_PASOS)} · ${porcentajeMeta(ui.pasos)} %",
        fontSize = 11.sp, color = Gris,
    )
    Text(
        mensajeMeta(ui.pasos),
        fontSize = 12.sp,
        color = if (metaAlcanzada(ui.pasos)) Menta else Gris,
        fontWeight = if (metaAlcanzada(ui.pasos)) FontWeight.Bold else FontWeight.Normal,
    )

    // Frecuencia cardiaca simulada
    Text("♥ ${ui.frecuencia} lpm (simulado)", fontSize = 13.sp, color = Rosa)

    // Botones
    Button(onClick = { Store.agregarPasos(100) }, modifier = Modifier.fillMaxWidth()) { Text("+100 pasos") }
    FilledTonalButton(onClick = { Store.agregarPasos(1000) }, modifier = Modifier.fillMaxWidth()) {
        Text("+1,000 pasos", fontSize = 12.sp)
    }
    Button(onClick = { Store.registrarActividad(context) }, modifier = Modifier.fillMaxWidth()) {
        Text("Registrar actividad")
    }
    FilledTonalButton(onClick = { Store.irA(Pantalla.ESTADISTICAS) }, modifier = Modifier.fillMaxWidth()) {
        Text("Estadísticas")
    }
    Text(ui.mensaje, fontSize = 11.sp, color = Menta, textAlign = TextAlign.Center)
}

@Composable
private fun Estadisticas() {
    val ui by Store.state.collectAsStateWithLifecycle()
    val est = calcularEstadisticas(ui.actividades)

    Text("Estadísticas", fontSize = 15.sp, color = Rosa, fontWeight = FontWeight.Bold)
    Text("Actividades registradas: ${est.totalActividades}", fontSize = 12.sp, color = Color.White)
    Text("FC promedio: ${est.frecuenciaPromedio} lpm · máx: ${est.frecuenciaMaxima} lpm", fontSize = 11.sp, color = Gris, textAlign = TextAlign.Center)
    Text("Pasos hoy: ${formatoPasos(ui.pasos)} (${porcentajeMeta(ui.pasos)} % de la meta)", fontSize = 11.sp, color = Gris, textAlign = TextAlign.Center)

    if (ui.actividades.isEmpty()) {
        Text("Aún no hay actividades. Regresa y toca «Registrar actividad».", fontSize = 11.sp, color = Gris, textAlign = TextAlign.Center)
    }
    // Últimas 5 actividades (recorrido con ciclo)
    for (a in ui.actividades.take(5)) {
        Text(
            "${horaFormato.format(Date(a.horaMs))} · ${a.tipo} · ${formatoPasos(a.pasos)} pasos · ${a.frecuencia} lpm",
            fontSize = 10.sp, color = Color.White, textAlign = TextAlign.Center,
        )
    }

    // Comunicación con el móvil (simulada)
    Text("Móvil (simulado): ${ui.pendientes} pendientes · ${ui.enviados} enviados", fontSize = 11.sp, color = Menta, textAlign = TextAlign.Center)
    if (ui.ultimoPaquete.isNotEmpty()) {
        Text("Último paquete:", fontSize = 10.sp, color = Gris)
        Text(ui.ultimoPaquete, fontSize = 8.sp, color = Gris, textAlign = TextAlign.Center)
    }
    Button(onClick = { Store.enviarAlMovilSimulado() }, modifier = Modifier.fillMaxWidth()) {
        Text("Enviar al móvil", fontSize = 12.sp)
    }
    FilledTonalButton(onClick = { Store.reiniciarDatos() }, modifier = Modifier.fillMaxWidth()) {
        Text("Reiniciar datos", fontSize = 12.sp)
    }
    FilledTonalButton(onClick = { Store.irA(Pantalla.PRINCIPAL) }, modifier = Modifier.fillMaxWidth()) {
        Text("Volver", fontSize = 12.sp)
    }
    Text(ui.mensaje, fontSize = 11.sp, color = Menta, textAlign = TextAlign.Center)
}

@Composable
private fun Barra(porcentaje: Int) {
    Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF333340))) {
        Box(
            Modifier.fillMaxWidth(porcentaje / 100f).height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(if (porcentaje >= 100) Menta else Rosa)
        )
    }
}
