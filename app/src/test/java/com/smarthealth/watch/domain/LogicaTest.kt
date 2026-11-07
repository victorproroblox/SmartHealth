package com.smarthealth.watch.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LogicaTest {

    // ---- Contador de pasos ----
    @Test fun `sumar pasos incrementa el contador`() = assertEquals(1300, sumarPasos(1200, 100))

    @Test fun `un incremento cero o negativo no cambia el contador`() {
        assertEquals(1200, sumarPasos(1200, 0))
        assertEquals(1200, sumarPasos(1200, -50))
    }

    // ---- Meta de 5,000 pasos ----
    @Test fun `4999 pasos no alcanza la meta`() = assertFalse(metaAlcanzada(4999))
    @Test fun `5000 pasos alcanza la meta`() = assertTrue(metaAlcanzada(5000))
    @Test fun `mas de 5000 pasos tambien alcanza la meta`() = assertTrue(metaAlcanzada(5001))

    @Test fun `el porcentaje de la meta va de 0 a 100`() {
        assertEquals(0, porcentajeMeta(0))
        assertEquals(50, porcentajeMeta(2500))
        assertEquals(100, porcentajeMeta(5000))
        assertEquals(100, porcentajeMeta(9000))
    }

    @Test fun `el mensaje de meta cambia al llegar a 5000`() {
        assertEquals("Faltan 3,800 pasos", mensajeMeta(1200))
        assertEquals("¡Meta alcanzada!", mensajeMeta(5000))
    }

    // ---- Frecuencia cardiaca simulada ----
    @Test fun `la frecuencia simulada siempre queda entre 60 y 140`() {
        val azar = Random(42)
        var fc = 72
        for (i in 1..1000) {
            fc = frecuenciaSimulada(fc, azar)
            assertTrue("fc fuera de rango: $fc", fc in FC_MIN..FC_MAX)
        }
    }

    @Test fun `la frecuencia simulada corrige valores fuera de rango`() {
        assertTrue(frecuenciaSimulada(0, Random(1)) >= FC_MIN)
        assertTrue(frecuenciaSimulada(300, Random(1)) <= FC_MAX)
    }

    // ---- Estadísticas ----
    @Test fun `sin actividades las estadisticas son cero y no hay division entre cero`() {
        assertEquals(Estadisticas(0, 0, 0, 0), calcularEstadisticas(emptyList()))
    }

    @Test fun `las estadisticas calculan promedio y maximos`() {
        val lista = listOf(
            crearActividad("Caminata", 1000, 70, 1L),
            crearActividad("Caminata", 3000, 90, 2L),
            crearActividad("Caminata", 5000, 110, 3L),
        )
        val est = calcularEstadisticas(lista)
        assertEquals(3, est.totalActividades)
        assertEquals(90, est.frecuenciaPromedio)
        assertEquals(110, est.frecuenciaMaxima)
        assertEquals(5000, est.pasosMaximos)
    }

    // ---- Paquete para el móvil ----
    @Test fun `el paquete JSON contiene los datos que se enviarian al movil`() {
        val a = crearActividad("Caminata", 5000, 88, 1234L)
        val json = paqueteJson("Victor López", a)
        assertTrue(json.contains("\"id\":\"A1234\""))
        assertTrue(json.contains("\"usuario\":\"Victor López\""))
        assertTrue(json.contains("\"pasos\":5000"))
        assertTrue(json.contains("\"frecuenciaCardiaca\":88"))
        assertTrue(json.contains("\"metaAlcanzada\":true"))
    }

    @Test fun `el paquete JSON escapa las comillas`() {
        val a = crearActividad("Camina \"rápido\"", 10, 80, 1L)
        assertTrue(paqueteJson("x", a).contains("Camina \\\"rápido\\\""))
    }
}
