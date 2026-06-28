# Refactor pendientes

Este archivo lista lo que queda pendiente despues del primer corte aplicado sobre `Game.java`.

## Prioridad 1: reducir `Game.java`

- [x] Extraer movimiento del jugador a `PlayerMovementSystem` o `PlayerController`.
- [x] Extraer uso de armas y disparos a `WeaponUseSystem`.
- [x] Extraer feedback de disparo a `ShotFeedback`.
- [x] Extraer seleccion y layout de armas a `EquipmentMenuLayout`.
- [x] Extraer interaccion del menu de armas a `EquipmentMenuController`.
- [x] Extraer settings de pantalla, volumen y resolucion a `DisplaySettingsController`.
- [x] Extraer musica por estado a `MusicController`.
- [x] Extraer seleccion de cursor a `CursorController`.

## Prioridad 2: separar flujo de juego

- [x] Crear una interfaz `GameLevel` real si el training pasa a lifecycle propio.
- [x] Crear `LevelStartOptions` solo si hay mas opciones de inicio que un booleano.
- [x] Extraer armado de training desde `Game` hacia `TrainingMode` o un builder dedicado.
- [x] Eliminar el camino de partida normal/PLAY del alcance actual del refactor.
- [x] Eliminar `State.DEAD` al quedar sin uso despues de remover el modo normal.
- [x] Revisar si `trainingActive` debe reemplazarse por una consulta de modo o nivel activo.

## Prioridad 3: render y vistas

- [x] Crear `GameView` o `RenderContext` para que `GameRenderer` no dependa de todo `Game`.
- [x] Migrar `GameRenderer` gradualmente para leer datos de solo lectura.
- [x] Separar datos de HUD de training.
- [x] Eliminar HUD de score, level y boss trigger junto con el modo normal.
- [x] Separar datos de equipment para render.
- [x] Evitar que nuevas features de render consulten estado mutable directo.

## Prioridad 4: sistemas existentes

- [x] Revisar `EnemySystem.getEnemies()` y evitar mutaciones externas directas.
- [x] Agregar operaciones explicitas en `EnemySystem` para remover o filtrar enemigos.
- [x] Revisar si `TrainingMode` castea listas concretas y reemplazarlo por APIs del sistema.
- [x] Crear factories pequenas para enemigos especiales si aparecen constructores repetidos.
- [x] Descartar un event queue dedicado porque `GameEvents` quedo sin uso tras remover el modo normal.

## Prioridad 5: buenas practicas Java

- [x] Mantener archivos nuevos con una sola clase top-level.
- [x] Mantener paquetes por responsabilidad: `context`, `session`, `system`, `ui`, `render`, `level`.
- [x] Mantener indentacion de 2 espacios en codigo nuevo o modificado.
- [x] No agregar tabs.
- [x] No agregar wildcard imports.
- [x] Mantener nombres en ingles.
- [x] Mantener constantes descriptivas para cualquier numero fijo nuevo.
- [x] Mantener metodos nuevos cerca de 15 lineas cuando sea razonable.
- [x] Dividir clases grandes cuando se toque una responsabilidad clara.
- [x] Comentar solo contratos, decisiones o comportamiento no obvio.

## Prioridad 6: limpiezas de bajo riesgo

- [x] Reemplazar `facingVector(...)` para no crear `double[]` al disparar.
- [x] Agregar `unitX` y `unitY` a `Facing` si se mantiene ese enum.
- [x] Revisar arrays temporales en render, especialmente sizes de sprites.
- [x] Cachear colores creados repetidamente durante render.
- [x] Evitar `String.split(" ")` en render de tooltips.
- [x] Cachear `EnemyType.values()` en rutas frecuentes si aparece en spawn repetido.
- [x] Revisar lineas largas en `Game.java` cuando se extraigan metodos.

## Validacion pendiente

- [x] Ejecutar `compile-and.run.bat`.
- [x] Probar menu principal.
- [x] Probar training.
- [x] Confirmar que el menu ya no expone partida normal/PLAY.
- [x] Probar disparos.
- [x] Confirmar que `HealthPickupSystem` y `Botiquin.png` quedan conservados pero desconectados.
- [x] Probar settings de volumen, resolucion y fullscreen.
- [x] Confirmar que no se edito `bin/`, `jars/`, `dist`, `dist-jar`, `dist-portable` ni builds generados.
