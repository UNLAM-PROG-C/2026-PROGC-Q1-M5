# Sheriffsss - Architecture

This document describes the architecture of the Sheriffsss game (package
`SheriffsssPackage`). For every class it states its purpose, key dependencies
and main responsibilities. A dedicated section at the end analyzes the
concurrency and parallelism model.

> Refactoring note: all source files were normalized to the project style
> (Google Java Style: K&R braces, 2-space indentation, no tabs, named constants,
> class-level Javadoc). Several large classes were decomposed into focused
> collaborators (see the "Extracted helper classes" entries below). Three
> classes (`Game`, `GameRenderer`, `TrainingMode`) remain large coordinators;
> the rationale and the partial extractions performed on them are documented in
> their entries.

---

## Entry point and coordination

### Main
Application bootstrap. Creates the `JFrame`, instantiates `Game`, registers the
window-closing and JVM shutdown hooks, and starts the game loop.
- Dependencies: `Game`, `javax.swing.JFrame`.
- Responsibilities: build the window, wire shutdown, make the window visible,
  call `Game.startGame()`.

### Game
Central coordinator. A Swing `JPanel` that is also a `Runnable` driving the
fixed-timestep game loop on its own thread. Owns the high-level state machine
(`MENU`, `MENU_SETTINGS`, `PLAYING`, `SETTINGS`, `DEAD`, training) and wires the
subsystems together.
- Dependencies: `AssetManager`, `AudioManager`, `GameInput`, `MenuRenderer`,
  `GameRenderer`, `DayNightCycle`, `EnemySystem`, `ProjectileSystem`,
  `DebugOptions`, `TrainingMode`, `GameMap`, `Player`, `ShootingSystem`,
  `GameConfig`.
- Responsibilities: run/throttle the game loop, dispatch per-state updates,
  handle settings/menu/equipment input and hit-testing, drive shooting through
  `ShootingSystem`, manage music/cursor, and route window/display changes to the
  Swing thread via `SwingUtilities.invokeLater`.
- Size note: still the largest class. The cohesive, *pure* weapon-aiming math was
  extracted to `ShootingSystem`. The remaining bulk is the menu/settings input
  handling, the game loop, equipment-panel hit-testing and the wide read-only
  getter API consumed by `GameRenderer`; these are tightly bound to `Game`'s
  private state and splitting them further would require exposing that state and
  risk breaking the render/update contract, so they were kept together. The
  `LevelSystem`/`PickupSystem`/`GameLoopState` decomposition does not apply to
  this build: this version of `Game` has no score/level/boss/pickup systems.

### GameConfig
Global configuration and tunable constants (screen/tile sizes, FPS, camera zoom,
key bindings, settings-panel layout, audio gains) plus persistence of display
preferences (resolution, fullscreen, volumes) to `saves/game.cfg`.
- Dependencies: file IO, `java.awt` dimensions.
- Responsibilities: hold compile-time constants, load/save user display
  preferences, expose viewport sizing helpers.

---

## Input

### GameInput
Swing input listener and current-input state holder. Registers as the
key/mouse/wheel/motion listener, stores movement/action/zoom flags and queued
one-shot intents, and exposes a `consume*` API the game loop polls each tick.
- Dependencies: `java.awt.event.*` listeners, `InputActionMapper`, `GameConfig`.
- Responsibilities: receive AWT events, keep input state, hand state to the game
  loop. Key/button mapping is delegated to `InputActionMapper`; state fields are
  package-private so the mapper can mutate them.

### InputActionMapper (extracted from GameInput)
Stateless translation of raw AWT key/mouse events into game intent by mutating
`GameInput` state. Centralizes the key bindings.
- Dependencies: `GameInput`, `GameConfig`, `java.awt.event.*`.
- Responsibilities: edge-trigger queued actions on key-down, clear "held" flags
  on key-up, map mouse buttons/wheel to state.

---

## World, map and entities

### GameMap
The tile/object grid of the world. Converts between world and tile coordinates
and answers walkability/collision queries.
- Dependencies: `TileType`, `MapObject`, `GameConfig`.
- Responsibilities: store tiles and map objects, expose `isWalkableAtWorld` and
  world/tile conversions used by movement, spawning and rendering.

### MapObject
A placed world object instance (position + type + transient durability damage).
- Dependencies: `MapObjectType`.
- Responsibilities: hold an object's location and footprint, expose collision
  bounds, track/reset durability damage.

### Player
The controllable character entity. Holds position, health, facing, knockback and
movement velocity, and owns the player's `Equipment`.
- Dependencies: `Equipment`, `ItemDefinition`, `GameMap`, `AssetManager`,
  `Facing`.
- Responsibilities: movement with collision, knockback, facing updates, taking
  damage/death, attack-speed scaling of cooldowns, equipment access.

### PlayerRuntimeState
Small POJO for per-player transient runtime values (e.g. projectile-weapon
cooldown ticks) kept outside the persistent `Player` entity.
- Responsibilities: hold mutable per-tick combat counters.

### Enemy
A hostile/training entity. Owns mutable position, health, knockback and debuff
state, and advances itself each tick by chasing or jumping toward the nearest
player and attacking in range.
- Dependencies: `EnemyType`, `EnemyBehavior`, `Debuff`, `GameMap`, `Player`,
  `EnemyAI`.
- Responsibilities: per-tick update (movement, debuffs, attack), collision-aware
  movement, damage/knockback application. Pure steering/targeting math is
  delegated to `EnemyAI`.

### EnemyAI (extracted from Enemy)
Stateless steering/targeting math: nearest living player, facing toward a target,
jump velocity vector.
- Dependencies: `Player`, `Facing`.
- Responsibilities: keep the pure AI decisions separate from the entity's mutable
  state, which `Enemy` still owns.

### EnemySystem
Manages the live set of enemies and combat side effects: per-tick updates,
despawning distant enemies, enemy-vs-enemy separation, applying damage with crit
rolls, and tracking transient flame/floating-text/hit-sound effects.
- Dependencies: `Enemy`, `EnemySpawner`, `GameMap`, `Player`, `DayNightCycle`,
  `FlameBurstEffect`, `CombatFloatingText`, `EnemyHitSound`, `ItemDefinition`.
- Responsibilities: own the enemy list and effect lists, update/collide/damage
  enemies, surface hit sounds. Spawning (when/what/where) is delegated to
  `EnemySpawner`.

### EnemySpawner (extracted from EnemySystem)
Owns enemy spawning: cooldown pacing tied to the day/night cycle, weighted random
type selection eligible for the current day, and finding a walkable tile at a
valid distance around an anchor player.
- Dependencies: `Enemy`, `EnemyType`, `GameMap`, `Player`, `DayNightCycle`,
  `java.util.Random`.
- Responsibilities: decide when to spawn, which type, and at which world tile.

### Projectile
A single in-flight projectile (position, velocity, owner, damage, lifetime,
type).
- Dependencies: `ProjectileType`, `Player`, `ItemDefinition`.
- Responsibilities: advance position, expose collision/draw data, expire.

### ProjectileSystem
Manages all live projectiles: spawning, per-tick movement, collision against the
map and enemies, and hit feedback.
- Dependencies: `Projectile`, `ProjectileType`, `GameMap`, `EnemySystem`,
  `ItemDefinition`, `Player`.
- Responsibilities: own the projectile list, update/collide projectiles, report
  whether a target was hit this update.

---

## Combat math (extracted helpers)

### ShootingSystem (extracted from Game)
Stateless shooting math: resolves the world-space muzzle (barrel) origin of a
held weapon for a given facing, maps an aim delta to a `Facing`, and applies
accuracy-based spread to the aim point.
- Dependencies: `Player`, `ItemDefinition`, `ItemDefinitionDrawConfig`, `Facing`,
  `java.util.Random`.
- Responsibilities: keep the heavy trigonometry (rotation/anchor/spread) out of
  `Game`, which still owns the firing state (cooldowns, flash, hit markers).

---

## Items and equipment

### ItemDefinition
Static catalog/definition of items and weapons (sprite, draw config, weapon type,
projectile/ammo stats, accuracy, crit, sounds, animation timing).
- Dependencies: `WeaponType`, `ProjectileType`, `ItemDefinitionDrawConfig`,
  `ProjectileWeaponStats`, `ProjectileStatModifiers`.
- Responsibilities: describe each item; provide weapon/projectile stat lookups
  and helpers like `byWeaponType`.

### ItemDefinitionDrawConfig
Per-facing draw configuration for held items (offsets, base angles, grip/barrel
anchors, recoil, mirroring, behind/front draw order). Builder-style.
- Dependencies: `Facing`, `GameConfig`.
- Responsibilities: provide draw offsets/angles/anchors and recoil swing math used
  by rendering and by `ShootingSystem`.

### Equipment
Per-player weapon inventory: unlocked weapons, currently equipped weapon, and the
open/closed state of the equipment menu and weapon selector.
- Dependencies: `ItemDefinition`, `WeaponType`.
- Responsibilities: unlock/equip weapons, ordering for the selector UI, menu
  toggle state.

### ProjectileWeaponStats
Immutable per-weapon projectile stats (type, ammo id, damage, speed, knockback,
cooldown, lifetime) with modifier application.
- Responsibilities: compute final projectile stats given `ProjectileStatModifiers`.

### ProjectileStatModifiers
Immutable additive stat modifiers for projectiles (damage/speed/knockback/cooldown
/lifetime). `NONE` constant for "no modifier".
- Responsibilities: carry stat deltas.

---

## Day/night, lighting and effects

### DayNightCycle
Tracks the in-game day count and time-of-day progress and derives the current
`DayPhase`.
- Dependencies: `DayPhase`, `GameConfig`.
- Responsibilities: advance time, expose phase/day used for spawning and lighting.

### WorldLighting
Computes lighting/darkness values for the world based on the day/night cycle and
light sources.
- Dependencies: `DayNightCycle`, `GameMap`, `GameConfig`.
- Responsibilities: provide darkness alpha / light contributions for the overlay.

### FlameBurstEffect
Transient visual flame-burst effect (position + lifetime) spawned on certain hits.
- Responsibilities: age itself, report expiry, expose draw data.

### CombatFloatingText
Transient floating combat text (e.g. "CRIT") rising and fading above an enemy.
- Responsibilities: animate position/alpha, report expiry.

### EnemyHitSound
Immutable value object describing a hit sound to play (resource path + world
position) queued by `EnemySystem` for `Game` to play spatially.
- Responsibilities: carry sound path and source location.

---

## Rendering

### GameRenderer
Main world/UI renderer. Draws the world (tiles, objects, players, enemies,
projectiles, held items), lighting/sunset overlays, combat effects, the HUD-ish
overlays, the equipment panel and the full debug-overlay suite.
- Dependencies: `Game` (read-only getters), `AssetManager`, `MenuRenderer`,
  `TextRenderer`, `GameMap`, `Player`, `Enemy`, `Projectile`, `DebugOptions`,
  `Camera`, `WorldLighting`, `java.awt.Graphics2D`.
- Responsibilities: translate game state into pixels each `paintComponent`.
- Size note: remains large. Its debug, HUD, effect and entity drawing routines
  share many private geometry helpers (world↔canvas conversion, held-item anchor
  rotation, facing-from-delta). The prescribed split into
  `HudRenderer`/`EffectRenderer`/`EntityRenderer`/`DebugRenderer` would require
  duplicating or publicly exposing those shared helpers; because the task
  mandates not breaking rendering, the class was style-normalized and kept
  cohesive rather than split blindly. (The debug-overlay rendering that the task
  associated with a `DebugRenderer` physically lives here, not in `DebugOptions`.)

### MenuRenderer
Renders the main menu and the menu-settings screen, and provides button
hit-testing used by `Game`.
- Dependencies: `AssetManager`, `TextRenderer`, `GameConfig`.
- Responsibilities: draw menu/settings, expose button-hover queries.

### TextRenderer
Helper for drawing text (fonts, alignment, shadows).
- Dependencies: `java.awt.Graphics2D`, `AssetManager`.
- Responsibilities: centralize text drawing.

### Camera
Computes the world-to-screen transform from the camera center and zoom.
- Dependencies: `GameConfig`.
- Responsibilities: provide viewport/translation values for rendering.

### AssetManager
Loads and caches images and cursors from the classpath.
- Dependencies: `javax.imageio`, `java.awt` images/cursors.
- Responsibilities: provide cached `BufferedImage`/`Cursor` resources by path.

### DebugBulletTrajectory
Immutable record of a fired bullet's start/end world points for the debug
trajectory overlay.
- Responsibilities: carry trajectory endpoints.

### DebugOptions
Holds all debug toggle flags and the debug panel state (which overlays are on,
bullet-trajectory limit/history, unlock-all-weapons request) and performs the
debug panel hit-testing.
- Dependencies: `DebugBulletTrajectory`.
- Responsibilities: own debug flags and the panel interaction model. (It contains
  no rendering code; the overlays it controls are drawn by `GameRenderer`, so it
  was kept as-is rather than split.)

---

## Audio

### AudioManager
Public audio API: one-shot SFX, looping music, keyed looping SFX, music/SFX
volume. Owns the active `Clip` cache and all playback state.
- Dependencies: `javax.sound.sampled.Clip`/`LineListener`, `AudioLoader`,
  `AudioMixer`.
- Responsibilities: play/stop/loop clips, manage volume and clip lifecycle. All
  methods are `synchronized` because clips are triggered from multiple threads.
  Clip loading is delegated to `AudioLoader`, gain math to `AudioMixer`.

### AudioLoader (extracted from AudioManager)
Loads WAV resources from the classpath and decodes them to playable PCM `Clip`s.
Stateless.
- Dependencies: `javax.sound.sampled.*`.
- Responsibilities: resource lookup, format decoding, `Clip` creation.

### AudioMixer (extracted from AudioManager)
Stateless gain math: clamp a 0..1 volume, convert it to decibel gain, and apply it
to a clip's master gain control.
- Dependencies: `javax.sound.sampled.Clip`/`FloatControl`.
- Responsibilities: gain/volume conversions and application.

---

## Training mode and tutorial

### TrainingMode
Coordinator for the training arena: builds the fixed arena map, configures the
training enemies, renders the training HUD/control panel/tutorial overlay, and
publishes gameplay events to the tutorial thread.
- Dependencies: `Game`, `EnemySystem`, `TutorialThread`, `TrainingControls`,
  `GameMap`, `Player`, `ProjectileSystem`, `MapObjectType`, `EnemyType`.
- Responsibilities: arena construction, training enemy setup, training rendering,
  event publication (`notifyShotFired`, etc.), tutorial thread lifecycle
  (`start`, `skip`, `join` on shutdown).
- Size note: remains large for the same reason as `GameRenderer` (its arena
  builder, HUD renderer and enemy controller share private state and helpers).
  The prescribed split into `TrainingArenaBuilder`/`TrainingHudRenderer`/
  `TrainingEnemyController` was deferred to avoid breaking the tightly-coupled
  training flow; the class was style-normalized.

### TutorialThread
Dedicated daemon thread that paces the training tutorial steps without blocking
the game loop.
- Dependencies: `TutorialStep`, `TutorialEventType`,
  `java.util.concurrent.atomic.AtomicBoolean`.
- Responsibilities: sleep through per-step minimum durations, finish/advance, and
  support an atomic `skip()` via `interrupt()`. Owns only tutorial state; it
  mutates no world state and shares no locks.

### TutorialStep
Immutable definition of one tutorial step (trigger event, min duration, max wait).
- Dependencies: `TutorialEventType`.
- Responsibilities: carry step timing/trigger data.

### TutorialEventType
Enum of events the game loop publishes to the tutorial thread
(`FIRST_MOVEMENT`, `FIRST_SHOT`, `FIRST_KILL`).

### TrainingControls
State of the training control panel (e.g. configured enemy count) with clamping.
Lives on the game-loop thread; not accessed from the tutorial thread.
- Dependencies: `GameConfig`.
- Responsibilities: hold/clamp/reset training parameters.

---

## Enums and value types

### States (State, Facing, CursorType)
- `State`: high-level game state machine values.
- `Facing`: 8-way facing with sprite index.
- `CursorType`: cursor sprites and hotspots.

### DayPhase
Enum of day phases with Spanish display names (user-facing strings).

### Debuff
Enum of status effects (currently `BURN`) with duration, light radius/intensity
and per-tick `update(Enemy)` behavior.
- Dependencies: `Enemy`, `GameConfig`.

### EnemyBehavior
Enum: `JUMPING`, `CONSTANT_CHASE`, `STATIC`.

### EnemyDensity
Enum: `LOW`, `MEDIUM`, `HIGH`.

### EnemyType
Enum catalog of enemy archetypes with sprite/animation, combat stats (HP/speed/
damage scaled by day), spawn weight/min day, density, hit sound and score reward.
- Dependencies: `EnemyBehavior`, `EnemyDensity`, `Facing`.

### MapObjectType
Enum of placeable object types (sprite, footprint, collision size, minimap color).
- Dependencies: `GameTheme`, `GameConfig`, `Player`.

### TileType
Enum of ground tiles (id, solidity, minimap color, sprite, lighting).
- Dependencies: `AssetManager`, `java.awt.Color`/`BufferedImage`.

### ProjectileType
Enum of projectile kinds (sprite, draw size, piercing, muzzle flash, light).

### WeaponType
Enum of weapon categories (e.g. none / firearm).

### GameTheme
Constants holder for shared colors and strokes.

---

## Concurrency & Parallelism Analysis

### Threads present in the system
1. **Swing Event Dispatch Thread (EDT)** — created by AWT/Swing. Delivers all
   input events (`GameInput` listeners) and runs `paintComponent` →
   `GameRenderer`. All window/display mutations are marshalled here via
   `SwingUtilities.invokeLater` (see `Game.applyFullscreen`,
   `applyPendingDisplaySettings`).
2. **Game-loop thread** (`"SheriffsssGameLoop"`) — `Game implements Runnable`,
   started in `Game.startGame()`. Runs a fixed-timestep loop (`run()`), calls
   `updateGame()` to advance the whole simulation, then `repaint()` (which only
   *schedules* a paint on the EDT). This thread **owns all world state**
   (`Player`, `EnemySystem`, `ProjectileSystem`, `GameMap`, `DayNightCycle`,
   game state machine).
3. **Tutorial thread** (`"SheriffsssTutorial"`) — `TutorialThread extends Thread`,
   a daemon thread used only in training mode. It paces tutorial steps with
   `Thread.sleep` so the game loop never spends frames waiting.
4. **Audio playback threads** — internal to `javax.sound.sampled`. Each `Clip`
   plays on a system-managed thread; `LineListener` callbacks
   (`AudioManager.playOnceUntilFinished`) run on that audio thread, not the game
   loop.
5. **JVM shutdown-hook thread** (`"SheriffsssGameShutdown"`) and the
   window-closing callback (on the EDT) both invoke `Game.shutdown()`.

### Synchronization primitives used and why
- **`volatile`** on `Game.gameThread` and `Game.shuttingDown`: these flags are
  written by the EDT/shutdown thread and read by the game-loop thread; `volatile`
  guarantees visibility of the stop signal so the loop terminates promptly.
- **`synchronized`** on `Game.shutdown()` and on **every** `AudioManager` method:
  `shutdown()` may be called concurrently from the window listener (EDT) and the
  JVM shutdown hook, so it is made idempotent under a lock. `AudioManager` is
  reached from the game-loop thread (gameplay SFX/music), from the EDT (volume
  sliders in settings) and from audio `LineListener` callbacks (clip cleanup);
  the lock serializes access to the shared `Clip` caches and looping state.
- **`AtomicBoolean`** in `TutorialThread` (`skipRequested`, `finished`): lock-free
  flags coordinating the game-loop thread (which calls `skip()`) and the tutorial
  thread (which checks/sets them). `skip()` uses `compareAndSet` + `interrupt()`
  so a sleeping tutorial wakes immediately with no polling.
- **Thread interruption** as a control-flow signal: `TutorialThread.skip()`
  interrupts the sleeping tutorial; `TrainingMode` shutdown calls `skip()` then
  `join(timeout)` to reap it.

### Thread-confinement design (why this model)
The system deliberately avoids shared mutable world state across threads:
- The **game loop thread is the single writer/owner of the simulation**. The EDT
  only *reads* that state during paint (`GameRenderer` uses `Game`'s getters) and
  *schedules* repaints; because the writer and the reader are interleaved at frame
  boundaries and the data is plain references, this keeps the design simple
  without per-field locking. Window changes that must run on the EDT are pushed
  there explicitly with `invokeLater`.
- The **tutorial thread owns only tutorial state** and never mutates the world; it
  communicates one-way (game loop → tutorial) through `TutorialThread`'s
  thread-safe API (atomics + interrupt). This is the textbook reason to use a
  separate thread: a step needs to *wait* a minimum real-time duration, and doing
  that inside the 60 FPS loop would either stall the simulation or force a
  time-based state machine into `update()`. Offloading it keeps the loop running
  at full frame rate.
- **Audio** is concurrent by construction: `javax.sound.sampled` runs playback on
  its own threads, so the game thread can fire a sound and return immediately;
  `AudioManager`'s lock is the only place cross-thread shared state (the clip
  caches) is touched.

### Concurrency vs. parallelism in this game
- **Concurrency** (multiple independent tasks making progress, not necessarily
  simultaneously) is what this game uses: the game loop, the EDT, the tutorial
  pacer and the audio engine are separate threads with separate responsibilities,
  coordinated through visibility flags and one-way messaging rather than shared
  locks. They *may* run on different cores, but correctness does not depend on it.
- **Parallelism** (splitting one computation across cores to go faster) is **not**
  used. There are no parallel streams, `ExecutorService`s, `ForkJoinPool`s or
  data-parallel loops. The simulation step (`updateGame`) is intentionally
  single-threaded: entity updates, collisions and spawning all mutate shared world
  state, so running them in parallel would require locking that would cost more
  than it saves at this scale, and would complicate determinism.

### Java APIs/libraries involved
- `java.lang.Thread` / `java.lang.Runnable` — game loop and tutorial threads.
- `java.util.concurrent.atomic.AtomicBoolean` — lock-free tutorial coordination.
- `synchronized` / `volatile` — visibility and mutual exclusion for shutdown and
  audio.
- `javax.swing.SwingUtilities.invokeLater` — marshalling work onto the EDT.
- `javax.sound.sampled` (`Clip`, `LineListener`, `FloatControl`) — audio with its
  own internal playback threads.

In short: the game runs the **simulation on one thread**, **rendering/input on the
Swing EDT**, **tutorial pacing on a third (daemon) thread**, and lets the **audio
subsystem manage its own threads** — a concurrency model chosen for clear
ownership and a non-blocking 60 FPS loop, rather than a parallelism model aimed at
raw throughput.
