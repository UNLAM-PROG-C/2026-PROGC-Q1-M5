# Validaciones No Alcanzadas

## 1. Coding standard de Google con adaptaciones de la catedra

El codigo no cumple completamente esta validacion. Se revisaron los 74 archivos Java de `src` junto con las reglas locales de `AGENTS.md`.

- Los paquetes no respetan la convencion de Google Java de usar nombres en minuscula. Los 74 archivos declaran paquetes bajo `SheriffsssPackage`, por ejemplo `src/SheriffsssPackage/Game.java:1` y `src/SheriffsssPackage/ui/DisplaySettingsController.java:1`.
- Hay tabs en 39 archivos, aunque `AGENTS.md` pide indentacion de 2 espacios y no agregar tabs. Los casos con mas ocurrencias son:
  - `src/SheriffsssPackage/GameInput.java`: 366 lineas con tabs.
  - `src/SheriffsssPackage/AudioManager.java`: 343 lineas con tabs.
  - `src/SheriffsssPackage/Enemy.java`: 308 lineas con tabs.
  - `src/SheriffsssPackage/DebugOptions.java`: 264 lineas con tabs.
  - `src/SheriffsssPackage/ItemDefinition.java`: 216 lineas con tabs.
  - `src/SheriffsssPackage/GameConfig.java`: 212 lineas con tabs.
- Hay 273 lineas con mas de 100 columnas, limite recomendado por Google Java. Ejemplos:
  - `src/SheriffsssPackage/MenuRenderer.java:103`: 253 caracteres.
  - `src/SheriffsssPackage/MenuRenderer.java:168`: 239 caracteres.
  - `src/SheriffsssPackage/ItemDefinition.java:217`: 228 caracteres.
  - `src/SheriffsssPackage/GameRenderer.java:1253`: 194 caracteres.
  - `src/SheriffsssPackage/GameMap.java:152`: 168 caracteres.
- No se detectaron wildcard imports (`import ...*;`), por lo que esa parte si cumple.
- Hay comentarios en castellano mezclados con codigo que la regla local pide mantener en ingles para codigo nuevo. Ejemplos:
  - `src/SheriffsssPackage/EnemyBehavior.java:6`.
  - `src/SheriffsssPackage/TutorialThread.java:39`.
  - `src/SheriffsssPackage/TutorialThread.java:73`.

Listado completo de archivos con tabs:

- `src/SheriffsssPackage/AssetManager.java`: 65 lineas con tabs.
- `src/SheriffsssPackage/AudioManager.java`: 343 lineas con tabs.
- `src/SheriffsssPackage/Camera.java`: 38 lineas con tabs.
- `src/SheriffsssPackage/CombatFloatingText.java`: 37 lineas con tabs.
- `src/SheriffsssPackage/DayNightCycle.java`: 72 lineas con tabs.
- `src/SheriffsssPackage/DayPhase.java`: 11 lineas con tabs.
- `src/SheriffsssPackage/Debuff.java`: 28 lineas con tabs.
- `src/SheriffsssPackage/DebugBulletTrajectory.java`: 22 lineas con tabs.
- `src/SheriffsssPackage/DebugOptions.java`: 264 lineas con tabs.
- `src/SheriffsssPackage/Enemy.java`: 308 lineas con tabs.
- `src/SheriffsssPackage/EnemyBehavior.java`: 4 lineas con tabs.
- `src/SheriffsssPackage/EnemyDensity.java`: 3 lineas con tabs.
- `src/SheriffsssPackage/EnemyHitSound.java`: 17 lineas con tabs.
- `src/SheriffsssPackage/Equipment.java`: 95 lineas con tabs.
- `src/SheriffsssPackage/FlameBurstEffect.java`: 45 lineas con tabs.
- `src/SheriffsssPackage/GameConfig.java`: 212 lineas con tabs.
- `src/SheriffsssPackage/GameInput.java`: 366 lineas con tabs.
- `src/SheriffsssPackage/GameMap.java`: 132 lineas con tabs.
- `src/SheriffsssPackage/GameTheme.java`: 14 lineas con tabs.
- `src/SheriffsssPackage/ItemDefinition.java`: 216 lineas con tabs.
- `src/SheriffsssPackage/ItemDefinitionDrawConfig.java`: 133 lineas con tabs.
- `src/SheriffsssPackage/Main.java`: 37 lineas con tabs.
- `src/SheriffsssPackage/MapObject.java`: 42 lineas con tabs.
- `src/SheriffsssPackage/MapObjectType.java`: 51 lineas con tabs.
- `src/SheriffsssPackage/Player.java`: 190 lineas con tabs.
- `src/SheriffsssPackage/PlayerRuntimeState.java`: 10 lineas con tabs.
- `src/SheriffsssPackage/Projectile.java`: 151 lineas con tabs.
- `src/SheriffsssPackage/ProjectileStatModifiers.java`: 28 lineas con tabs.
- `src/SheriffsssPackage/ProjectileSystem.java`: 56 lineas con tabs.
- `src/SheriffsssPackage/ProjectileType.java`: 62 lineas con tabs.
- `src/SheriffsssPackage/ProjectileWeaponStats.java`: 38 lineas con tabs.
- `src/SheriffsssPackage/TextRenderer.java`: 59 lineas con tabs.
- `src/SheriffsssPackage/TileType.java`: 26 lineas con tabs.
- `src/SheriffsssPackage/TrainingControls.java`: 35 lineas con tabs.
- `src/SheriffsssPackage/TutorialEventType.java`: 3 lineas con tabs.
- `src/SheriffsssPackage/TutorialStep.java`: 17 lineas con tabs.
- `src/SheriffsssPackage/TutorialThread.java`: 53 lineas con tabs.
- `src/SheriffsssPackage/WeaponType.java`: 2 lineas con tabs.
- `src/SheriffsssPackage/WorldLighting.java`: 121 lineas con tabs.

## 2. Uso de patrones de diseno

El codigo cumple parcialmente, pero no alcanza la validacion de forma consistente.

- Hay intentos de patrones y separacion de responsabilidades:
  - `GameView` funciona como interfaz de lectura para render.
  - `TrainingHudSnapshot`, `TrainingHudView` y `EquipmentHudView` modelan datos de vista.
  - `GameLevel`, `TrainingLevel` y `NoLevel` apuntan a Strategy/State para niveles.
  - `EnemyFactory` encapsula la creacion de dianas de training.
  - Los paquetes `context`, `session`, `system`, `ui` y `render` separan algunas responsabilidades.
- La aplicacion todavia conserva clases tipo "God object" o controladores demasiado grandes, lo que debilita el uso real de patrones:
  - `src/SheriffsssPackage/GameRenderer.java`: 1310 lineas, concentra render de mundo, jugador, enemigos, proyectiles, debug, settings y HUD.
  - `src/SheriffsssPackage/Game.java`: 1105 lineas, mantiene loop, estados, UI, audio, input de alto nivel, actualizaciones de gameplay y transiciones.
  - `src/SheriffsssPackage/TrainingMode.java`: 1012 lineas, mezcla armado de arena, tutorial, spawn, progreso, proyectiles y metricas.
  - `src/SheriffsssPackage/GameInput.java`: 414 lineas, concentra muchos comandos de teclado/mouse en metodos extensos con `switch`.
- `GameLevel` esta definido como abstraccion, pero `TrainingLevel` tiene `enter`, `update` y `exit` vacios. La logica real del training queda en `TrainingSessionBuilder`, `TrainingMode` y `Game`, por lo que el Strategy/State de niveles queda subutilizado.
- `EnemyFactory` no esta inyectada desde `GameContext` ni desde un proveedor comun; `TrainingMode` la instancia directamente en `src/SheriffsssPackage/TrainingMode.java:114`. Esto reduce el beneficio de Factory para testeo, extension e inversion de dependencias.
- `Game` instancia dependencias concretas directamente (`TrainingSessionBuilder`, `MusicController`, `GameRenderer`) en `src/SheriffsssPackage/Game.java:46`, `src/SheriffsssPackage/Game.java:71` y `src/SheriffsssPackage/Game.java:87`, en vez de depender de abstracciones o recibirlas por constructor.

## 3. No numeros magicos

El codigo no cumple esta validacion. Se detectaron 621 candidatos a numeros magicos en 29 archivos, excluyendo literales triviales `0`, `1`, `2` y lineas con `static final`.

- Archivos con mayor cantidad de candidatos:
  - `src/SheriffsssPackage/GameRenderer.java`: 265 candidatos.
  - `src/SheriffsssPackage/ItemDefinition.java`: 164 candidatos.
  - `src/SheriffsssPackage/DebugOptions.java`: 22 candidatos.
  - `src/SheriffsssPackage/GameConfig.java`: 21 candidatos.
  - `src/SheriffsssPackage/MenuRenderer.java`: 20 candidatos.
  - `src/SheriffsssPackage/TrainingMode.java`: 15 candidatos.
  - `src/SheriffsssPackage/Facing.java`: 11 candidatos.
  - `src/SheriffsssPackage/DayNightCycle.java`: 10 candidatos.
  - `src/SheriffsssPackage/EnemyType.java`: 9 candidatos.
  - `src/SheriffsssPackage/ProjectileType.java`: 9 candidatos.
- Ejemplos que deberian reemplazarse por constantes descriptivas o configuracion nombrada:
  - `src/SheriffsssPackage/ItemDefinition.java:7`: configuracion numerica extensa dentro del enum `BRONZE_REVOLVER`.
  - `src/SheriffsssPackage/EnemyType.java:7`: valores de dimensiones, vida, velocidad, cooldown y colision embebidos en `DIANA`.
  - `src/SheriffsssPackage/Game.java:222`: `1000.0 / GameConfig.TARGET_FPS`.
  - `src/SheriffsssPackage/MenuRenderer.java:52`: valores de fuente `Arial`, `BOLD`, `18` repetidos como parametros directos.
  - `src/SheriffsssPackage/TrainingMode.java:328`: valores de milestone y temporizacion embebidos.
  - `src/SheriffsssPackage/DayNightCycle.java:27`: umbrales y progresos de ciclo calculados con literales directos.
- Algunos numeros viven en enums que funcionan como tablas de datos (`ItemDefinition`, `ProjectileType`, `EnemyType`, `MapObjectType`). Aunque el enum les da contexto, la validacion estricta de "NO numeros magicos" no queda alcanzada porque los valores no tienen nombres semanticos propios ni estan documentados por constantes.

Listado completo de archivos con candidatos:

- `src/SheriffsssPackage/AudioManager.java`: 5 candidatos.
- `src/SheriffsssPackage/CombatFloatingText.java`: 2 candidatos.
- `src/SheriffsssPackage/DayNightCycle.java`: 10 candidatos.
- `src/SheriffsssPackage/Debuff.java`: 4 candidatos.
- `src/SheriffsssPackage/DebugOptions.java`: 22 candidatos.
- `src/SheriffsssPackage/Enemy.java`: 4 candidatos.
- `src/SheriffsssPackage/EnemySystem.java`: 8 candidatos.
- `src/SheriffsssPackage/EnemyType.java`: 9 candidatos.
- `src/SheriffsssPackage/Equipment.java`: 1 candidatos.
- `src/SheriffsssPackage/Facing.java`: 11 candidatos.
- `src/SheriffsssPackage/FlameBurstEffect.java`: 6 candidatos.
- `src/SheriffsssPackage/Game.java`: 8 candidatos.
- `src/SheriffsssPackage/GameConfig.java`: 21 candidatos.
- `src/SheriffsssPackage/GameInput.java`: 6 candidatos.
- `src/SheriffsssPackage/GameRenderer.java`: 265 candidatos.
- `src/SheriffsssPackage/ItemDefinition.java`: 164 candidatos.
- `src/SheriffsssPackage/ItemDefinitionDrawConfig.java`: 9 candidatos.
- `src/SheriffsssPackage/MapObjectType.java`: 7 candidatos.
- `src/SheriffsssPackage/MenuRenderer.java`: 20 candidatos.
- `src/SheriffsssPackage/Player.java`: 3 candidatos.
- `src/SheriffsssPackage/PlayerRuntimeState.java`: 1 candidatos.
- `src/SheriffsssPackage/Projectile.java`: 1 candidatos.
- `src/SheriffsssPackage/ProjectileSystem.java`: 1 candidatos.
- `src/SheriffsssPackage/ProjectileType.java`: 9 candidatos.
- `src/SheriffsssPackage/render/TrainingHudRenderer.java`: 2 candidatos.
- `src/SheriffsssPackage/system/WeaponUseSystem.java`: 5 candidatos.
- `src/SheriffsssPackage/TrainingMode.java`: 15 candidatos.
- `src/SheriffsssPackage/TutorialStep.java`: 1 candidatos.
- `src/SheriffsssPackage/ui/EquipmentMenuLayout.java`: 1 candidatos.

## 4. Metodos/funciones de no mas de 15 lineas

El codigo no cumple esta validacion. Se detectaron 84 metodos o constructores de mas de 15 lineas.

- Mayores incumplimientos:
  - `src/SheriffsssPackage/GameInput.java:186`: `keyPressed(...)`, 108 lineas.
  - `src/SheriffsssPackage/Game.java:394`: `updatePlaying(...)`, 62 lineas.
  - `src/SheriffsssPackage/GameInput.java:296`: `keyReleased(...)`, 58 lineas.
  - `src/SheriffsssPackage/GameRenderer.java:104`: `renderWorld(...)`, 56 lineas.
  - `src/SheriffsssPackage/GameRenderer.java:621`: `drawDebugWorldOverlay(...)`, 47 lineas.
  - `src/SheriffsssPackage/DebugOptions.java:258`: `isRowEnabled(...)`, 45 lineas.
  - `src/SheriffsssPackage/Game.java:680`: `updateSettings(...)`, 42 lineas.
  - `src/SheriffsssPackage/AudioManager.java:230`: metodo sincronizado de audio, 37 lineas.
  - `src/SheriffsssPackage/Game.java:158`: `applyFullscreenMode(...)`, 37 lineas.
  - `src/SheriffsssPackage/Game.java:273`: `updateDebugMenu(...)`, 34 lineas.
  - `src/SheriffsssPackage/Game.java:457`: `updateToolUse(...)`, 33 lineas.
  - `src/SheriffsssPackage/GameRenderer.java:339`: `drawEnemies(...)`, 33 lineas.
  - `src/SheriffsssPackage/Game.java:240`: `updateGame()`, 32 lineas.
  - `src/SheriffsssPackage/TrainingMode.java:453`: `updateTutorial...`, 32 lineas.
  - `src/SheriffsssPackage/GameRenderer.java:392`: `drawFlameBurst...`, 31 lineas.
  - `src/SheriffsssPackage/GameRenderer.java:929`: `drawAccuracy...`, 31 lineas.
  - `src/SheriffsssPackage/GameRenderer.java:1050`: `drawDebugMenu...`, 30 lineas.
- Archivos donde se concentran varios metodos largos y conviene priorizar refactor:
  - `GameRenderer.java`.
  - `Game.java`.
  - `TrainingMode.java`.
  - `GameInput.java`.
  - `AudioManager.java`.
  - `DebugOptions.java`.
  - `EnemySystem.java`.
  - `Projectile.java`.
  - `WorldLighting.java`.
- Tambien se detectaron metodos apenas por encima del limite, entre 16 y 19 lineas, en `GameConfig.java`, `MenuRenderer.java`, `TrainingHudRenderer.java`, `DayNightCycle.java`, `TextRenderer.java`, `Equipment.java`, `Player.java`, `GameMap.java`, `Enemy.java` y `TrainingHudSnapshot.java`. Aunque son menos graves, tambien incumplen la regla solicitada.

Listado completo por archivo:

- `src/SheriffsssPackage/AssetManager.java`: 1 metodos.
- `src/SheriffsssPackage/AudioManager.java`: 7 metodos.
- `src/SheriffsssPackage/DayNightCycle.java`: 1 metodos.
- `src/SheriffsssPackage/DebugOptions.java`: 5 metodos.
- `src/SheriffsssPackage/Enemy.java`: 3 metodos.
- `src/SheriffsssPackage/EnemySystem.java`: 3 metodos.
- `src/SheriffsssPackage/Equipment.java`: 1 metodos.
- `src/SheriffsssPackage/Game.java`: 13 metodos.
- `src/SheriffsssPackage/GameConfig.java`: 1 metodos.
- `src/SheriffsssPackage/GameInput.java`: 2 metodos.
- `src/SheriffsssPackage/GameMap.java`: 1 metodos.
- `src/SheriffsssPackage/GameRenderer.java`: 21 metodos.
- `src/SheriffsssPackage/ItemDefinition.java`: 1 metodos.
- `src/SheriffsssPackage/Main.java`: 1 metodos.
- `src/SheriffsssPackage/MenuRenderer.java`: 1 metodos.
- `src/SheriffsssPackage/Player.java`: 2 metodos.
- `src/SheriffsssPackage/Projectile.java`: 2 metodos.
- `src/SheriffsssPackage/render/TrainingHudRenderer.java`: 1 metodos.
- `src/SheriffsssPackage/render/TrainingHudSnapshot.java`: 1 metodos.
- `src/SheriffsssPackage/TextRenderer.java`: 1 metodos.
- `src/SheriffsssPackage/TrainingMode.java`: 12 metodos.
- `src/SheriffsssPackage/WorldLighting.java`: 3 metodos.
