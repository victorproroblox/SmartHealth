# Comunicación reloj ↔ móvil (simulación documentada)

No se dispone de un teléfono Android emparejado con el reloj, así que el mecanismo se demuestra con una simulación. El reloj hace todo lo que haría en la comunicación real; solo la entrega final al móvil se simula.

## Flujo
```
Usuario toca «Registrar actividad»
   → el reloj crea la Actividad (pasos, frecuencia, hora)
   → genera el paquete JSON y lo guarda en la bandeja de salida
   → muestra una notificación: «Actividad registrada: N pasos, X lpm»
   → «Estadísticas → Enviar al móvil» entrega los pendientes (simulado, queda en el log)
   → (en la versión real) el móvil recibe el paquete y responde con una confirmación
```

## Información que se intercambia
Del reloj al móvil, por cada actividad (campo → tipo → ejemplo):

| Campo | Tipo | Ejemplo |
|---|---|---|
| tipo | texto | `"actividad"` |
| id | texto único | `"A1730935200000"` |
| usuario | texto | `"Victor López"` |
| actividad | texto | `"Caminata"` |
| pasos | entero | `5000` |
| frecuenciaCardiaca | entero (lpm) | `88` |
| horaMs | entero (milisegundos) | `1730935200000` |
| metaAlcanzada | booleano | `true` |

Del móvil al reloj (versión real): confirmación de recepción con el `id` recibido, para quitar el paquete de la bandeja de salida.

## Cómo sería con Wear OS real
- Dependencia: `play-services-wearable`.
- Reloj y móvil con el mismo `applicationId` y la misma firma.
- Envío con `MessageClient.sendMessage(nodeId, "/smarthealth/actividad", paquete)` y, si no hay conexión, se conserva en la bandeja de salida y se reintenta al reconectar (`DataClient` para datos que deben sincronizarse).
- En el móvil, un `WearableListenerService` recibe `onMessageReceived` y guarda el registro.
