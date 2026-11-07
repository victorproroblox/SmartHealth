package com.smarthealth.watch.domain

import java.util.Locale
import kotlin.random.Random

/** Lógica de SmartHealth sin dependencias de Android, para poder probarla con pruebas unitarias. */

const val META_PASOS = 5000
const val FC_MIN = 60
const val FC_MAX = 140

data class Actividad(
    val id: String,
    val tipo: String,
    val pasos: Int,
    val frecuencia: Int,
    val horaMs: Long,
)

data class Estadisticas(
    val totalActividades: Int,
    val frecuenciaPromedio: Int,
    val frecuenciaMaxima: Int,
    val pasosMaximos: Int,
)

/** Suma pasos al contador. Un incremento menor o igual a cero se ignora. */
fun sumarPasos(actuales: Int, incremento: Int): Int =
    if (incremento > 0) actuales + incremento else actuales

/** Condición de la meta: se cumple al llegar a 5,000 pasos o más. */
fun metaAlcanzada(pasos: Int): Boolean = pasos >= META_PASOS

/** Porcentaje de avance hacia la meta, entre 0 y 100. */
fun porcentajeMeta(pasos: Int): Int {
    if (pasos <= 0) return 0
    val porcentaje = (pasos.toLong() * 100 / META_PASOS).toInt()
    return if (porcentaje > 100) 100 else porcentaje
}

fun mensajeMeta(pasos: Int): String =
    if (metaAlcanzada(pasos)) "¡Meta alcanzada!" else "Faltan ${formatoPasos(META_PASOS - pasos)} pasos"

fun formatoPasos(pasos: Int): String = String.format(Locale.US, "%,d", pasos)

/** Frecuencia cardiaca simulada: varía poco respecto a la anterior y siempre queda entre 60 y 140 lpm. */
fun frecuenciaSimulada(anterior: Int, azar: Random): Int {
    val cambio = azar.nextInt(-6, 7)
    return (anterior + cambio).coerceIn(FC_MIN, FC_MAX)
}

/** Calcula las estadísticas recorriendo la lista de actividades (estructura cíclica). */
fun calcularEstadisticas(lista: List<Actividad>): Estadisticas {
    if (lista.isEmpty()) return Estadisticas(0, 0, 0, 0)
    var suma = 0
    var maxima = 0
    var pasosMax = 0
    for (actividad in lista) {
        suma += actividad.frecuencia
        if (actividad.frecuencia > maxima) maxima = actividad.frecuencia
        if (actividad.pasos > pasosMax) pasosMax = actividad.pasos
    }
    val promedio = (suma + lista.size / 2) / lista.size
    return Estadisticas(lista.size, promedio, maxima, pasosMax)
}

fun crearActividad(tipo: String, pasos: Int, frecuencia: Int, horaMs: Long): Actividad =
    Actividad(id = "A$horaMs", tipo = tipo, pasos = pasos, frecuencia = frecuencia, horaMs = horaMs)

private fun esc(texto: String): String = texto.replace("\\", "\\\\").replace("\"", "\\\"")

/** Paquete de datos que el reloj enviaría a la aplicación móvil (JSON). */
fun paqueteJson(usuario: String, a: Actividad): String =
    "{\"tipo\":\"actividad\",\"id\":\"${esc(a.id)}\",\"usuario\":\"${esc(usuario)}\"," +
        "\"actividad\":\"${esc(a.tipo)}\",\"pasos\":${a.pasos},\"frecuenciaCardiaca\":${a.frecuencia}," +
        "\"horaMs\":${a.horaMs},\"metaAlcanzada\":${metaAlcanzada(a.pasos)}}"
