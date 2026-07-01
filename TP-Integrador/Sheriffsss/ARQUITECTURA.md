# Arquitectura de Sheriffsss — Guía para presentación oral

## 1. Qué hace cada paquete

**`core`** — Infraestructura central del juego
- `Main` — punto de entrada. Crea el `JFrame`, instancia `Game`, registra el shutdown hook y llama `game.startGame()`.
- `Game` (~1500 líneas) — clase orquestadora. Extiende `JPanel` e implementa `Runnable`. Es el game loop, árbitro de estado y coordinador de todos los sistemas.
- `GameConfig` — singleton de constantes estáticas (`TARGET_FPS = 60`, `TILE_SIZE = 32`, resoluciones). Persiste `saves/game.cfg`.
- `GameInput` — implementa `KeyListener`, `MouseListener`, `MouseMotionListener`, `MouseWheelListener`. Almacena el estado de teclado/mouse como flags booleanos.
- `State` — enum con los 4 estados del juego.
- `Facing` — enum con 8 direcciones (DOWN, LEFT, RIGHT, UP, UP_LEFT, ...) con índice de sprite.
- `CursorType`, `DebugOptions` — tipos de cursor y panel de debug solo disponible en training.

**`assets`** — Carga y caché de recursos
- `AssetManager` — carga imágenes y cursores por path, con caché interna.
- `AudioManager` — gestiona `javax.sound.sampled.Clip`. Todos sus métodos son `synchronized` porque los callbacks de audio corren en un hilo separado del sistema.

**`world`** — Mapa y entorno
- `GameMap` — grilla de tiles (`int[][] tiles`) y grilla de objetos (`MapObject[][]`). Métodos de colisión (`isAreaBlockedAtWorld`, `isProjectileBlockedAtWorld`) y conversión mundo↔tile.
- `Camera` — convierte coordenadas mundo a pantalla (`worldToScreenX`, `tileToScreenX`). Calcula tiles visibles.
- `DayNightCycle` / `DayPhase` — ciclo día/noche con alpha de oscuridad y tinte de atardecer.
- `WorldLighting` — calcula alpha de oscuridad por tile con luces dinámicas (proyectiles, revólver, debuffs).
- `MapObject`, `MapObjectType`, `TileType` — definiciones de objetos y tiles (cercas, arbustos, árboles).

**`player`** — Entidad jugador
- `Player` — posición (x, y), HP, facing, knockback. Movimiento con sub-pixel carry (`moveCarryX/Y`). No tiene lógica de disparo — esa vive en `Game`.
- `Equipment` — lista de armas desbloqueadas, arma equipada, apertura/cierre del menú de selección.
- `ItemDefinition` — enum con todos los ítems/armas. Define daño, velocidad de proyectil, cadencia, precisión, sprites.
- `ItemDefinitionDrawConfig` — offsets y ángulos de renderizado del arma según la dirección del jugador.
- `PlayerRuntimeState` — estado transitorio: principalmente el cooldown de disparo (`projectileWeaponCooldownTicks`).

**`enemy`** — Sistema de enemigos
- `Enemy` — entidad enemigo. IA: perseguir jugador (`moveToward`), comportamiento saltarín (`updateJumpingMovement`), estático para dianas de training. Aplica debuffs (BURN, etc.) y ataca al jugador en rango.
- `EnemySystem` — contiene el `ArrayList<Enemy>`. Maneja spawn automático, despawn por distancia, separación física O(n²), sistema de daño con críticos, efectos visuales y sonidos de golpe.
- `EnemyType` — enum con estadísticas base, sprite, radio de colisión, peso de spawn, escala por día.
- `Debuff`, `EnemyBehavior`, `EnemyDensity` — enums de soporte.

**`combat`** — Proyectiles y combate
- `Projectile` — posición, velocidad, vida en ticks. En `update()` avanza en pasos sub-pixel para no saltarse colisiones a alta velocidad. Detecta colisión con tiles y llama a `enemySystem.damageEnemy()` al impactar.
- `ProjectileSystem` — lista de hasta 180 proyectiles activos. Itera en reversa para remoción segura.
- `ShootingSystem` — clase utilitaria stateless con la matemática pura de disparo: calcula el origen del cañón del arma, mapea dirección de apuntado a `Facing`, aplica dispersión de precisión.
- `CombatFloatingText`, `FlameBurstEffect`, `DebugBulletTrajectory` — efectos visuales.
- `ProjectileType`, `WeaponType`, `ProjectileWeaponStats`, `ProjectileStatModifiers` — configuración de armas.

**`render`** — Renderizado
- `GameRenderer` — recibe `Game` como parámetro de solo lectura y dibuja en `Graphics2D`. Renderiza: tiles, objetos, enemigos (con animación por frame sheet), proyectiles (rotados), efectos de llama, jugador, arma sostenida (con rotación de retroceso), overlay de iluminación y HUD.
- `MenuRenderer` — renderiza el menú principal y la pantalla de settings del menú.
- `TextRenderer` — texto con outline, centrado.
- `GameTheme` — colores y strokes de tema.

**`training`** — Modo entrenamiento
- `TrainingMode` — coordinador del modo training. Construye la arena (procedural con semilla aleatoria), gestiona spawn de dianas, lleva el score (aciertos, fallos, precisión), controla el tutorial visible mediante `TutorialPhase` y renderiza su propio HUD. Vive en el hilo del game loop.
- `TrainingTutorialController` — estado de fase del tutorial de entrenamiento.
- `TutorialPhase` — enum del flujo visible del tutorial: apuntado inicial, dianas, aviso de timer y modo normal.
- `TrainingControls` — control del conteo de enemigos por tecla.

---

## 2. Concurrencia — dónde y por qué

El juego usa **2 hilos propios** más el EDT de Swing y un hilo de audio implícito del sistema.

### Hilo 1: `SheriffsssGameLoop`
Creado en `Game.startGame()`:
```java
this.gameThread = new Thread(this, "SheriffsssGameLoop");
this.gameThread.start();
```
Corre el game loop completo: `updateGame()` + `repaint()` a 60 FPS. El campo `gameThread` es **`volatile`** para que el `null` que escribe `shutdown()` sea visible inmediatamente en el loop.

### Hilo 2: `SheriffsssGameShutdown` (shutdown hook)
```java
Runtime.getRuntime().addShutdownHook(new Thread(game::shutdown, "SheriffsssGameShutdown"));
```
Garantiza que `game.shutdown()` se llame aunque el usuario cierre con Ctrl+C o kill.

### EDT (Event Dispatch Thread de Swing)
Recibe todos los eventos de teclado y mouse. `GameInput` escribe flags booleanos. El game loop los lee y "consume" (lee + resetea) cada tick. No hay sincronización explícita — el peor caso es perder un evento por un tick, tolerable en un juego.

### Hilo de audio implícito (`javax.sound.sampled`)
Los `LineListener` callbacks de `AudioManager` corren en un hilo del sistema de audio, concurrente con el game loop que llama `playOnce()`/`stopLoop()`. Por eso **todos los métodos de `AudioManager` son `synchronized`**.

### Campos `volatile`
- `Game.gameThread` — visibilidad del null-set entre shutdown y game loop.
- `Game.shuttingDown` — leído en cada iteración del while por el game loop, escrito por el shutdown hook o el WindowListener. `volatile` evita que el JIT cachee el valor en registro.

### `synchronized Game.shutdown()`
Protege contra doble ejecución si el shutdown hook y el WindowListener llaman al método concurrentemente.

---

## 3. Flujo completo de un clic del mouse (disparo)

```
Usuario hace clic
      ↓
EDT → GameInput.mousePressed()
      • primaryClickQueued = true
      • primaryHeld = true
      • guarda mouseX, mouseY
      ↓
Game loop tick → Game.updateGame() → updatePlaying()
      • consumePrimaryGameplayClick() → lee y resetea primaryClickQueued
      • si menú de equipo abierto y clic en él → consume clic, NO dispara
      • si no → primaryGameplayPressedThisFrame = true
      ↓
updateToolUse()
      • verifica arma equipada y cooldown
      • llama attemptFireProjectileWeapon(player, runtimeState, weapon, mouseWorldX, mouseWorldY)
      ↓
attemptFireProjectileWeapon()  [Game.java línea 705]
      • verifica cooldown (runtime.projectileWeaponCooldownTicks > 0 → aborta)
      • calcula Facing desde (targetX - player.x, targetY - player.y)
      • calcula origen del cañón (heldItemOriginWorldX/Y) con rotación del arma
      • aplica dispersión de precisión (offset perpendicular aleatorio)
      • projectileSystem.spawn(tipo, origen, destino, velocidad, daño, ...)
      • setea cooldown para el próximo disparo
      • audio.playOnceUntilFinished(sonido del disparo)
      • si revólver → triggerRevolverFlash() (luz dinámica)
      • trainingMode.notifyShotFired() (incrementa contador de disparos)
      ↓
Próximo tick → updateProjectiles() → projectileSystem.update(map, enemySystem)
      • Projectile.update() avanza posición en pasos sub-pixel
      • detecta colisión con tile → proyectil muere
      • detecta colisión con enemigo → enemySystem.damageEnemy()
            • reduce HP del enemigo
            • si HP <= 0 → enemigo eliminado de la lista
            • crea CombatFloatingText si hay crítico
      ↓
trainingMode.awardScoreForDestroyedTargets()
      • detecta reducción en cantidad de enemigos
      • suma aciertos al score
```

---

## 4. Máquina de estados

```
MENU ──(click Play)──────────────────────→ PLAYING
MENU ──(click Settings)──────────────────→ MENU_SETTINGS
MENU_SETTINGS ──(Escape / Back)──────────→ MENU
PLAYING ──(Escape)───────────────────────→ SETTINGS
PLAYING ──(HP <= 0)──────────────────────→ MENU
SETTINGS ──(Resume)──────────────────────→ PLAYING
SETTINGS ──(Main Menu)───────────────────→ MENU
```

El modo training corre dentro de `PLAYING` como `LevelType.TRAINING`.

| Estado | Qué actualiza | Qué renderiza |
|---|---|---|
| `MENU` | `updateMenu()` — clics en botones | Solo `menuRenderer.draw()` |
| `MENU_SETTINGS` | `updateMenuSettings()` — sliders | `menuRenderer.draw()` + panel settings |
| `PLAYING` | `updatePlaying()` — movimiento, enemigos, proyectiles | Mundo completo + HUD training |
| `SETTINGS` | `updateSettings()` — sliders y botones de pausa | Mundo + overlay semitransparente |

**Transición por muerte** (`updatePlaying()`):
```java
if (this.session.player().getCurrentHP() <= 0.0) {
    this.session.player().die();
    returnToMenu();
    return;
}
```

---

## 5. Relaciones entre clases

```
Main
 └── Game (JPanel + Runnable)
      ├── AssetManager
      ├── AudioManager
      ├── GameInput  ←──── eventos del EDT
      ├── MenuRenderer(AssetManager)
      ├── GameRenderer(AssetManager, MenuRenderer)
      │    ├── Camera
      │    └── WorldLighting
      ├── DayNightCycle
      ├── EnemySystem
      │    └── Enemy[] (creados en spawn)
      ├── ProjectileSystem
      │    └── Projectile[] (creados en spawn)
      └── TrainingMode  (creado al iniciar training)
```

**Dependencias clave:**
- `Projectile.update()` → llama `EnemySystem.damageEnemy()` — el proyectil daña al enemigo.
- `Enemy.update()` → llama `Player.damageEnemyAttack()` — el enemigo daña al jugador.
- `GameRenderer.render(Game game)` — lee estado via getters, **nunca muta** el estado del juego.
- `TrainingMode.update(Player, GameInput, ProjectileSystem)` — coordina training usando los sistemas del game loop.

**Patrones de diseño:**
- **Game Loop** — `Game.run()` con delta accumulator. Update y render en cada tick.
- **Input buffering / consume pattern** — `GameInput` acumula eventos como flags; el game loop los consume (lee + resetea) cada tick. Evita perder eventos de un solo frame.
- **Facade** — `Game` expone solo getters de lectura a `GameRenderer` y `TrainingMode`.
- **Strategy** — `EnemyBehavior` (STATIC, JUMPING, CHASING): `Enemy.effectiveBehavior()` despacha al código correcto.
- **Object Pool** — `AudioManager` reutiliza `Clip` objects (hasta `MAX_OVERLAPPING_SFX_CLIPS = 12` por sonido).
- **Immutable data objects** — `ItemDefinition` (enum), `ProjectileType` (enum): datos de configuración que no mutan.
- **Separación update / render** — `updateGame()` nunca pinta; `GameRenderer.render()` nunca escribe estado del mundo.

---

## 6. Game Loop

**Ubicación**: `Game.run()`, línea 252.

```java
double drawInterval = 1000000000.0 / GameConfig.TARGET_FPS;  // 16.666ms
double delta = 0.0;
long lastTime = System.nanoTime();

while (!this.shuttingDown && Thread.currentThread() == this.gameThread) {
    long currentTime = System.nanoTime();
    delta += (currentTime - lastTime) / drawInterval;
    lastTime = currentTime;

    while (delta >= 1.0) {
        updateGame();
        repaint();
        this.frameCount++;
        delta--;
    }
}
```

- **60 FPS** (`TARGET_FPS = 60`), tick = 16.666ms.
- **Delta accumulator**: si el loop está atrasado, ejecuta múltiples `updateGame()` para recuperar tiempo (catch-up).
- **Sin interpolación**: el render siempre muestra el estado del último update completo.
- `repaint()` encola una solicitud al EDT. El EDT llama `paintComponent()` → `renderer.render(graphics, game)`.

**Orden de `updateGame()` en cada ciclo:**
1. Detecta cambio de tamaño de ventana (`syncViewportSize`)
2. Procesa F11 (fullscreen), debug menu
3. Decrementa ticks de muzzle flash, hit marker, facing lock
4. Despacha al método del estado actual: `updateMenu()` / `updatePlaying()` / etc.
5. Si training activo: `trainingMode.update(input, projectileSystem)`
6. Actualiza mensajes de HUD, música, cursor

**Orden de `updatePlaying()` en cada ciclo:**
1. Procesa Escape y toggle de equipo
2. Procesa clic (equipment o disparo)
3. Lee WASD, mueve jugador, verifica colisión con mapa
4. Aplica knockback al jugador
5. `enemySystem.update()` — mueve enemigos, spawn, colisiones entre enemigos
6. Verifica HP del jugador → si <= 0, vuelve al menú
7. `updateToolUse()` → disparo si hay clic/hold
8. `updateProjectiles()` → mueve proyectiles, detecta impactos

---

## Puntos clave para el tribunal

| Pregunta probable | Respuesta corta |
|---|---|
| ¿Dónde está el game loop? | `Game.run()` línea 252, delta accumulator a 60 FPS |
| ¿Cuántos hilos hay? | 2 propios + EDT de Swing + hilo de audio del sistema |
| ¿Por qué `synchronized` en `AudioManager`? | Los callbacks de `LineListener` corren en un hilo de audio del sistema, concurrente con el game loop |
| ¿Por qué `volatile` en `shuttingDown`? | Para que el valor escrito por el shutdown hook sea visible inmediatamente en el game loop sin sincronización completa |
| ¿Dónde vive el tutorial de training? | En el game loop, dentro de `TrainingMode` mediante `TutorialPhase` |
| ¿Qué pasa cuando el jugador hace clic? | EDT → flag en `GameInput` → game loop lo consume en `updatePlaying()` → `attemptFireProjectileWeapon()` → `ProjectileSystem.spawn()` |
| ¿Cómo funciona la colisión de proyectiles? | `Projectile.update()` avanza en pasos sub-pixel y consulta `GameMap.isProjectileBlockedAtWorld()` |
| ¿Cómo se separan los enemigos entre sí? | `EnemySystem` hace O(n²) separación física por radio de colisión en cada tick |
