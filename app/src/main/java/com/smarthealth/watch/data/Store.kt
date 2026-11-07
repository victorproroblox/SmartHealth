package com.smarthealth.watch.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.smarthealth.watch.domain.Actividad
import com.smarthealth.watch.domain.crearActividad
import com.smarthealth.watch.domain.frecuenciaSimulada
import com.smarthealth.watch.domain.paqueteJson
import com.smarthealth.watch.domain.sumarPasos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

enum class Pantalla { PRINCIPAL, ESTADISTICAS }

data class UiState(
    val usuario: String = Store.USUARIO,
    val pasos: Int = 0,
    val frecuencia: Int = 72,
    val actividades: List<Actividad> = emptyList(),
    val pendientes: Int = 0,
    val enviados: Int = 0,
    val ultimoPaquete: String = "",
    val mensaje: String = "Listo",
    val pantalla: Pantalla = Pantalla.PRINCIPAL,
)

/**
 * Estado de la app y su almacenamiento local (SharedPreferences).
 * También maneja la "bandeja de salida": los paquetes que se enviarían al móvil.
 */
object Store {
    const val USUARIO = "Victor López"
    private const val MAX_ACTIVIDADES = 50
    private val azar = Random.Default

    private lateinit var prefs: SharedPreferences
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.applicationContext.getSharedPreferences("smarthealth", Context.MODE_PRIVATE)
        val actividades = leerActividades(prefs.getString("actividades", "[]") ?: "[]")
        val pendientes = leerLista(prefs.getString("salida", "[]") ?: "[]").size
        _state.value = UiState(
            pasos = prefs.getInt("pasos", 0),
            actividades = actividades,
            pendientes = pendientes,
            enviados = prefs.getInt("enviados", 0),
            ultimoPaquete = leerLista(prefs.getString("salida", "[]") ?: "[]").lastOrNull() ?: "",
        )
    }

    // ---- Acciones del usuario ----

    fun agregarPasos(cantidad: Int) {
        _state.update {
            val nuevos = sumarPasos(it.pasos, cantidad)
            it.copy(
                pasos = nuevos,
                frecuencia = frecuenciaSimulada(it.frecuencia, azar),
                mensaje = "+$cantidad pasos",
            )
        }
        prefs.edit().putInt("pasos", _state.value.pasos).apply()
    }

    /** Latido simulado: se llama cada pocos segundos para que el dato cambie solo. */
    fun actualizarFrecuencia() {
        _state.update { it.copy(frecuencia = frecuenciaSimulada(it.frecuencia, azar)) }
    }

    /** Registra una actividad con los pasos y la frecuencia actuales, la deja lista para el móvil y avisa. */
    fun registrarActividad(context: Context) {
        val actual = _state.value
        val actividad = crearActividad("Caminata", actual.pasos, actual.frecuencia, System.currentTimeMillis())
        val paquete = paqueteJson(actual.usuario, actividad)

        val lista = (listOf(actividad) + actual.actividades).take(MAX_ACTIVIDADES)
        val salida = leerLista(prefs.getString("salida", "[]") ?: "[]") + paquete
        prefs.edit()
            .putString("actividades", escribirActividades(lista))
            .putString("salida", JSONArray(salida).toString())
            .apply()

        val avisado = Avisos.actividadRegistrada(
            context,
            "Actividad registrada: ${actividad.pasos} pasos, ${actividad.frecuencia} lpm",
            (actividad.horaMs % 100000).toInt(),
        )
        Log.i("SmartHealthLink", "Paquete preparado para el móvil: $paquete")

        _state.update {
            it.copy(
                actividades = lista,
                pendientes = salida.size,
                ultimoPaquete = paquete,
                mensaje = if (avisado) "Actividad registrada. Paquete listo para el móvil."
                else "Actividad registrada (notificaciones desactivadas).",
            )
        }
    }

    /** Simula el envío al móvil: vacía la bandeja de salida y cuenta los paquetes entregados. */
    fun enviarAlMovilSimulado() {
        val salida = leerLista(prefs.getString("salida", "[]") ?: "[]")
        if (salida.isEmpty()) {
            _state.update { it.copy(mensaje = "No hay paquetes pendientes") }
            return
        }
        for (paquete in salida) {
            Log.i("SmartHealthLink", "Enviado (simulado) al móvil: $paquete")
        }
        val enviados = prefs.getInt("enviados", 0) + salida.size
        prefs.edit().putString("salida", "[]").putInt("enviados", enviados).apply()
        _state.update {
            it.copy(pendientes = 0, enviados = enviados, mensaje = "${salida.size} paquete(s) enviados al móvil (simulado)")
        }
    }

    fun reiniciarDatos() {
        prefs.edit().clear().apply()
        _state.update { UiState(pantalla = it.pantalla, mensaje = "Datos reiniciados") }
    }

    fun irA(pantalla: Pantalla) = _state.update { it.copy(pantalla = pantalla) }

    // ---- Almacenamiento ----

    private fun escribirActividades(lista: List<Actividad>): String {
        val arreglo = JSONArray()
        for (a in lista) {
            arreglo.put(
                JSONObject().put("id", a.id).put("tipo", a.tipo).put("pasos", a.pasos)
                    .put("frecuencia", a.frecuencia).put("horaMs", a.horaMs)
            )
        }
        return arreglo.toString()
    }

    private fun leerActividades(json: String): List<Actividad> {
        val arreglo = JSONArray(json)
        val lista = mutableListOf<Actividad>()
        for (i in 0 until arreglo.length()) {
            val o = arreglo.getJSONObject(i)
            lista.add(Actividad(o.getString("id"), o.getString("tipo"), o.getInt("pasos"), o.getInt("frecuencia"), o.getLong("horaMs")))
        }
        return lista
    }

    private fun leerLista(json: String): List<String> {
        val arreglo = JSONArray(json)
        val lista = mutableListOf<String>()
        for (i in 0 until arreglo.length()) lista.add(arreglo.getString(i))
        return lista
    }
}
