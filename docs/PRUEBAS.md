# Pruebas de SmartHealth

## Pruebas automáticas (se ejecutan en GitHub Actions)
`app/src/test/.../LogicaTest.kt`, 13 pruebas JUnit sobre la lógica: incremento del contador, meta en 4999 / 5000 / 5001, porcentaje, mensaje de meta, rango de la frecuencia simulada (1,000 valores), estadísticas con lista vacía y con datos, y paquete JSON.
El resultado se ve en Actions → «Pruebas unitarias» y en el artefacto `reporte-pruebas`.

## Pruebas en el reloj (llenar con lo observado)
| ID | Prueba | Pasos | Resultado esperado | Resultado real | ¿Pasó? |
|---|---|---|---|---|---|
| P1 | Botones responden | Tocar +100, +1,000, Registrar y Estadísticas | Cada botón ejecuta su acción | | |
| P2 | El contador incrementa | Tocar +100 tres veces | El contador sube 300 | | |
| P3 | Meta de 5,000 | Subir pasos hasta 4,900 y luego 5,000 | A 4,900 dice «Faltan 100 pasos»; a 5,000 dice «¡Meta alcanzada!» y la barra se completa | | |
| P4 | Frecuencia simulada | Observar la pantalla 10 s | El valor cambia solo y se mantiene entre 60 y 140 | | |
| P5 | Registrar actividad | Tocar «Registrar actividad» | Mensaje de confirmación y notificación | | |
| P6 | Estadísticas | Registrar 2 actividades y abrir Estadísticas | Total = 2, FC promedio y máxima, lista de actividades | | |
| P7 | Envío simulado | «Enviar al móvil» | Pendientes pasa a 0 y Enviados aumenta | | |
| P8 | Persistencia | Cerrar la app y abrirla | Pasos y actividades siguen ahí | | |

## Error identificado y corregido (llenar con un error real que aparezca)
| Campo | Detalle |
|---|---|
| Qué se observó | |
| Cómo se reprodujo | |
| Causa | |
| Corrección | |
| Prueba después de corregir | |
