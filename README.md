# SmartHealth · Wear OS

Aplicación para reloj inteligente (Wear OS) hecha con Kotlin y Jetpack Compose para Wear OS (Material 3). Registra actividad física básica y muestra estadísticas en una interfaz pensada para pantalla circular.

## Qué hace
| Requisito | Dónde está |
|---|---|
| Nombre e icono de la app, título **SmartHealth** | `app/src/main/res` (ícono de corazón con pulso) y encabezado de la pantalla principal |
| Nombre del usuario | `Store.USUARIO` («Victor López»), se muestra en la pantalla principal |
| Contador de pasos | Botones **+100 pasos** y **+1,000 pasos** (acción del usuario) |
| Frecuencia cardiaca (simulada) | `frecuenciaSimulada()`: valor entre 60 y 140 lpm que cambia cada 3 s y con cada acción |
| Botón para registrar actividad | **Registrar actividad**: guarda un registro, lo deja listo para el móvil y lanza una notificación |
| Botón para consultar estadísticas | **Estadísticas**: total de actividades, FC promedio y máxima, últimas 5 actividades |
| Meta de 5,000 pasos | `metaAlcanzada()`: barra de avance y mensaje «¡Meta alcanzada!» |
| Función para organizar el código | `domain/Logica.kt` (`sumarPasos`, `metaAlcanzada`, `porcentajeMeta`, `calcularEstadisticas`, `paqueteJson`…) |
| Estructura condicional | `if` en `metaAlcanzada`, `mensajeMeta` y `calcularEstadisticas` |
| Estructura cíclica | `for` en `calcularEstadisticas`, en la lista de la pantalla de estadísticas y `while(true)` de la FC simulada |

## Vinculación con el móvil (simulada y documentada)
El reloj prepara un paquete JSON por cada actividad registrada y lo guarda en una bandeja de salida. En **Estadísticas → Enviar al móvil** se «entregan» (simulado) y se registran en el log (`SmartHealthLink`). Ver `docs/COMUNICACION.md` para la información que se intercambiaría y cómo se haría con la Data Layer de Wear OS.

## Estructura
```
app/src/main/java/com/smarthealth/watch/
  domain/Logica.kt      lógica pura (probada con JUnit)
  data/Store.kt         estado, almacenamiento local y bandeja de salida
  data/Avisos.kt        notificación al registrar una actividad
  ui/MainActivity.kt    pantallas
app/src/test/.../LogicaTest.kt   pruebas unitarias
```

## Cómo compilar
1. Sube el proyecto a un repositorio de GitHub (rama `main`).
2. En la pestaña **Actions**, el flujo «Compilar APK» ejecuta las pruebas unitarias y genera el APK (artefacto `smarthealth-apk`).
3. Instala con `adb install -r app-debug.apk` en el reloj o en un emulador Wear OS (por ejemplo, Wear OS Large Round API 33 o superior).

Requisitos: compileSdk 36, minSdk 26, AGP 9.1.0, Kotlin 2.3.20.

## Documentación
- `docs/COMUNICACION.md` · flujo y datos que se intercambian con el móvil
- `docs/PRUEBAS.md` · plan de pruebas y registro de resultados y errores
