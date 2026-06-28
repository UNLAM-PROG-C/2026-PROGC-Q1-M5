# Refactor pendientes actualizado

Este archivo refleja el estado actual del refactor despues de extraer `GameContext`,
`GameSession`, `HealthPickupSystem` y `PlayerMovementSystem`.

## Cumplido

- [x] Sacar el secret level del camino principal del refactor.
- [x] Crear `GameContext` para dependencias estables.
- [x] Crear `GameSession` para estado mutable de partida.
- [x] Mover `map` a `GameSession`.
- [x] Mover `player` a `GameSession`.
- [x] Mover `PlayerRuntimeState` a `GameSession`.
- [x] Mover `trainingMode` a `GameSession`.
- [x] Mover `deathOverlayActive` a `GameSession`.
- [x] Mover `cameraZoom` a `GameSession`.
- [x] Extraer botiquines a `HealthPickupSystem`.
- [x] Extraer movimiento del jugador a `PlayerMovementSystem`.
- [x] Extraer feedback de disparo a `ShotFeedback`.
- [x] Mantener archivos nuevos con una sola clase top-level.
- [x] Crear paquetes por responsabilidad: `context`, `session`, `system`.
- [x] Mantener codigo nuevo sin wildcard imports.
- [x] Mantener codigo nuevo sin tabs.
- [x] Mantener codigo nuevo en ingles.
- [x] Usar constantes descriptivas en sistemas nuevos.
- [x] Reemplazar `facingVector(...)` para no crear `double[]` al disparar.
- [x] Agregar `unitX` y `unitY` a `Facing`.
- [x] Cachear `EnemyType.values()` en rutas frecuentes.
- [x] Separar `State`, `Facing` y `CursorType` en archivos propios.
- [x] Extraer layout de armas a `EquipmentMenuLayout`.
- [x] Extraer interaccion del menu de armas a `EquipmentMenuController`.
- [x] Extraer musica por estado a `MusicController`.
- [x] Extraer seleccion de cursor a `CursorController`.
- [x] Extraer settings de pantalla, volumen y resolucion a `DisplaySettingsController`.
- [x] Revisar `EnemySystem.getEnemies()` y evitar mutaciones externas directas.
- [x] Agregar operaciones explicitas en `EnemySystem`.
- [x] Reemplazar casteos de listas concretas en `TrainingMode`.
- [x] Extraer uso de armas y disparos a `WeaponUseSystem`.
- [x] Crear `EnemyFactory` para enemigos de mundo y dianas de training.
- [x] Revisar arrays temporales en render, especialmente sizes de sprites.
- [x] Cachear colores creados repetidamente durante render.
- [x] Crear `GameView` inicial para que `GameRenderer` no dependa de todo `Game`.
- [x] Migrar `GameRenderer` gradualmente para leer datos de `GameView`.
- [x] Evitar `String.split(" ")` en render de tooltips.
- [x] Crear `GameEvents` simple para disparo y muerte del jugador.
- [x] Crear una interfaz `GameLevel` real.
- [x] Crear `LevelStartOptions`.
- [x] Agregar `activeLevel` a `GameSession`.
- [x] Extraer armado de training hacia `TrainingSessionBuilder`.
- [x] Separar arranque de partida normal hacia `FullGameSessionBuilder`.
- [x] Separar datos de HUD de score, level y boss trigger hacia `ScoreHudView`.
- [x] Reemplazar `trainingActive` por consulta a `activeLevel`.
- [x] Revisar `State.DEAD`: sigue como estado real para pantalla y update post-muerte.
- [x] Eliminar `State.TRAINING` porque training ahora depende de `LevelType.TRAINING`.
- [x] Separar datos de equipment para render hacia `EquipmentHudView`.
- [x] Separar datos de HUD de training hacia `TrainingHudSnapshot`.
- [x] Mover dibujo del HUD de training a `TrainingHudRenderer`.
- [x] Mantener archivos nuevos con una sola clase top-level.
- [x] Mantener paquetes nuevos por responsabilidad.
- [x] No agregar wildcard imports.

## Prioridad 1: seguir reduciendo `Game.java`

- [x] Reducir getters publicos de `Game` con modelos iniciales de render.

## Prioridad 2: flujo de juego y niveles


## Prioridad 3: render y vistas

- [x] Separar datos de HUD de training.
- [x] Separar datos de HUD de score, level y boss trigger.
- [x] Separar datos de equipment para render.
- [x] Evitar que nuevas features de render consulten estado mutable directo.

## Prioridad 4: sistemas existentes


## Prioridad 5: limpiezas de bajo riesgo

- [x] Revisar lineas largas en `Game.java` a medida que se extraigan metodos.

## Prioridad 6: buenas practicas Java

- [x] Mantener indentacion de 2 espacios en todo codigo nuevo o modificado.
- [x] No agregar tabs.
- [x] No agregar wildcard imports.
- [x] Mantener nombres en ingles.
- [x] Mantener constantes descriptivas para cualquier numero fijo nuevo.
- [x] Mantener metodos nuevos cerca de 15 lineas cuando sea razonable.
- [x] Dividir clases grandes cuando se toque una responsabilidad clara.
- [x] Comentar solo contratos, decisiones o comportamiento no obvio.
- [x] Evitar reformateos masivos que mezclen estilo con cambios funcionales.

## Validacion pendiente

- [x] Ejecutar `compile-and.run.bat`.
- [x] Probar menu principal.
- [x] Probar training.
- [x] Probar partida normal.
- [x] Probar disparos.
- [x] Probar botiquines.
- [x] Probar settings de volumen, resolucion y fullscreen.
- [x] Confirmar que no se editaron `bin/`, `jars/`, `dist`, `dist-jar`, `dist-portable` ni builds generados.

## Siguiente corte recomendado

1. Evitar que nuevas features de render consulten estado mutable directo.
2. Ejecutar validacion manual con `compile-and.run.bat`.
3. Probar menu, training, partida normal, disparos, botiquines y settings.
