# Game refactor

## Seccion 1: Separar Game.java

### Objetivo revisado

Reducir `Game.java` sin reescribir el juego. Esta version vieja debe tratarse como una base Swing/AWT con un flujo principal centrado en menu, settings, training, render, input, audio y sistemas de entidades.

El plan anterior asumia como base estable el secret level, el boss secreto y generadores secretos. Para esta version, eso no debe ser el eje del refactor. Si el branch objetivo no tiene secret level, el refactor base no debe crear ni depender de:

- `SecretSurvivalLevel`;
- `SecretBossLevel`;
- `SecretBossFight`;
- reglas de dianas ashen;
- timers, patrones o transiciones del boss secreto;
- musica o ecos especiales del secreto como requisito arquitectonico.

Nota de estado: el codigo inspeccionado contiene archivos `SecretLevel.java`, `SecretLevelBoss.java`, `SecretLevelGenerator.java` y `SecretLevelBossGenerator.java`, pero `GameLevel` es solo un wrapper con `start(Game, boolean)` y la logica real sigue dentro de `Game.java`. Si esta rama no debe conservar secret level, esos archivos deben tratarse como legado o feature fuera de alcance, no como base para diseniar la arquitectura principal.

La meta sana es que `Game.java` quede como shell: crea managers, recibe input, corre el loop, coordina estado global y delega reglas concretas a objetos pequenos.

### Estado actual util

Ya existen piezas que conviene preservar:

- `AssetManager`: carga y cache de imagenes/cursores.
- `AudioManager`: cache y reproduccion de musica/SFX.
- `GameInput`: captura teclado, mouse y eventos edge-triggered.
- `GameRenderer` y `MenuRenderer`: capa de dibujo separada.
- `EnemySystem`: ownership principal de enemigos y efectos asociados.
- `ProjectileSystem`: ownership de proyectiles.
- `TrainingMode`: reglas concretas del training.
- `WorldGenerator`, `GameMap`, `TileType`, `MapObjectType`, `ItemDefinition`: datos y mundo tipados.
- `EnemyBrain`: Strategy ya existente para comportamiento enemigo.

El problema principal sigue siendo que `Game.java` mezcla demasiadas responsabilidades:

- lifecycle Swing y fullscreen;
- loop principal;
- estados de app;
- arranque de training;
- referencias a secreto/boss si quedan en esta rama;
- movement/update del jugador;
- uso de armas y proyectiles;
- feedback de disparo;
- audio espacial y ecos;
- settings;
- equipamiento;
- getters de render;
- estado de mundo y sesion.

### Cambios que ya no aplican como plan base

Eliminar del camino principal del refactor anterior:

1. No planificar `SecretSurvivalLevel` como fase obligatoria.
2. No planificar `SecretBossFight` como extraccion obligatoria.
3. No convertir la arquitectura alrededor de `SecretLevelGenerator` o `SecretLevelBossGenerator`.
4. No usar `secretLevelActive` como ejemplo de modelo final.
5. No mezclar training y secret bajo el mismo booleano `trainingActive` en el modelo nuevo.
6. No crear un `Game` por nivel.
7. No tocar `bin/`, `jars/`, `out/` ni artefactos generados.
8. No compilar como cierre por defecto; la validacion queda en `compile-and.run.bat`.

Si el codigo secreto queda presente en `src`, hacer una decision explicita antes de mover arquitectura:

- opcion limpia: borrarlo en un cambio dedicado si no pertenece a esta version;
- opcion conservadora: dejarlo fuera del refactor base y aislarlo despues;
- opcion de producto: mantenerlo, pero extraerlo recien cuando `TrainingLevel` ya tenga lifecycle real.

### Arquitectura recomendada para esta version

#### 1. `Game` como shell

Responsabilidades finales de `Game`:

- inicializar managers globales;
- registrar listeners Swing;
- ejecutar `run()` y `updateGame()`;
- coordinar app state;
- llamar a sistemas concretos;
- pedir `repaint()`;
- hacer shutdown ordenado;
- mantener getters transitorios para `GameRenderer` mientras se migra.

`Game` no deberia contener:

- reglas internas de training;
- formulas de disparo;
- layout de equipamiento;
- logica de sliders/settings;
- audio espacial detallado;
- datos temporales de feedback visual;
- reglas de secreto/boss si esa feature no existe en la version objetivo.

#### 2. `GameContext` para dependencias estables

Crear una clase final con referencias que viven durante todo el juego:

```java
final class GameContext {
    final AssetManager assets;
    final AudioManager audio;
    final GameInput input;
    final DayNightCycle dayNightCycle;
    final EnemySystem enemySystem;
    final ProjectileSystem projectileSystem;
    final DebugOptions debugOptions;
}
```

Reglas:

- se crea una vez en el constructor de `Game`;
- no debe allocar por frame;
- no debe convertirse en una bolsa ilimitada;
- si una feature modifica mundo/jugador, debe pasar por `GameSession` o por un sistema con API clara.

#### 3. `GameSession` para estado mutable de partida

Crear una clase que agrupe la sesion activa:

```java
final class GameSession {
    GameMap map;
    Player player;
    PlayerRuntimeState playerRuntime;
    GameLevel activeLevel;
    TrainingMode trainingMode;
    boolean deathOverlayActive;
    double cameraZoom;
}
```

Primera version aceptable: mutable y package-private. No introducir getters perfectos antes de bajar el acoplamiento real.

#### 4. `GameLevel` real, empezando solo por training

La interfaz actual:

```java
interface GameLevel {
    void start(Game game, boolean resetDebugOptions);
}
```

Eso no es una Strategy completa; solo reenvia a metodos de `Game`. Evolucionarla de forma incremental:

```java
interface GameLevel {
    LevelType type();
    void enter(GameContext context, GameSession session, LevelStartOptions options);
    void update(GameContext context, GameSession session);
    void exit(GameContext context, GameSession session);
}
```

`LevelStartOptions` evita booleanos sueltos:

```java
final class LevelStartOptions {
    final boolean resetDebugOptions;
}
```

Primer nivel concreto:

- `TrainingLevel`.

Niveles futuros:

- `WorldLevel`, solo si vuelve un modo mundo normal.
- `SecretLevel`, solo si el producto decide conservarlo.
- `SecretBossLevel`, solo si el secret boss sigue siendo feature activa.

#### 5. `AppState` separado de `GameLevel`

Menu/settings no son niveles. Son estados de aplicacion.

Mantener al principio el enum existente, pero limpiar el modelo:

- `MENU`;
- `MENU_SETTINGS`;
- `PLAYING`;
- `PAUSE_MENU`;
- `SETTINGS`;
- `DEAD`, solo si realmente se asigna;
- eliminar `TRAINING` si el training pasa a ser `GameLevel`.

Objetivo:

```java
interface AppStateNode {
    void enter(GameContext context, GameSession session);
    void update(GameContext context, GameSession session);
    void exit(GameContext context, GameSession session);
}
```

No hacer esto antes de extraer `GameContext` y `GameSession`.

### Plan de refactor paso a paso

#### Fase 0: Preparacion

1. Leer `AGENTS.md` antes de tocar codigo.
2. No editar `bin/`, `jars/`, `out/` ni builds.
3. No compilar como validacion de cierre por defecto.
4. Confirmar si la rama objetivo conserva o elimina secret level.
5. Hacer cambios chicos: una extraccion por vez.

Resultado esperado: el plan queda alineado con la version vieja sin meter features nuevas.

#### Fase 1: Clasificar y aislar codigo secreto

Si esta version no debe tener secret level:

1. Quitar secret/boss del plan principal.
2. Marcar `SecretLevel*` como legado si siguen en `src`.
3. Revisar referencias a:
   - `secretLevelActive`;
   - `secretBossLevelActive`;
   - `SecretLevelGenerator`;
   - `SecretLevelBossGenerator`;
   - constantes `SECRET_*`;
   - musica y SFX del secreto;
   - render condicionado por `isSecretLevelActive()`.
4. Borrar o aislar en una fase separada, no mezclado con `GameContext`/`GameSession`.

Si se conserva secret level:

1. No extraerlo primero.
2. Extraer primero training.
3. Recien despues repetir el mismo patron con secret.

Resultado esperado: el refactor no queda bloqueado por una feature vieja o ausente.

#### Fase 2: Crear `GameContext`

Mover referencias estables:

- `AssetManager`;
- `AudioManager`;
- `GameInput`;
- `DayNightCycle`;
- `EnemySystem`;
- `ProjectileSystem`;
- `DebugOptions`.

Mantener temporalmente getters en `Game` para no romper `GameRenderer`.

Resultado esperado: `Game` empieza a delegar dependencias sin cambiar comportamiento.

#### Fase 3: Crear `GameSession`

Mover estado mutable:

- `GameMap map`;
- `Player player`;
- `PlayerRuntimeState localRuntimeState`;
- `GameLevel activeLevel`;
- `TrainingMode trainingMode`;
- `deathOverlayActive`;
- `cameraZoom`;
- flags de runtime que no sean globales.

Mantener getters publicos de `Game` delegando a `session` mientras `GameRenderer` siga leyendo desde `Game`.

Resultado esperado: baja el numero de campos sueltos y queda claro que mundo/jugador pertenecen a la sesion.

#### Fase 4: Convertir `TrainingLevel` en Strategy real

Mover desde `Game` hacia `TrainingLevel` o helpers internos:

- `TRAINING_PLAYER_NAME`;
- `TRAINING_DAY_PROGRESS`;
- `createRandomTrainingWorldSeed`;
- `prepareTrainingSystems`;
- `spawnTrainingPlayer`;
- `resetTrainingTransientState`;
- `clearMenuStateForTraining`, si solo aplica al training;
- `giveTrainingLoadout`;
- `equipTrainingWeapon`;
- `resetTrainingWeapon`;
- `unlockAllTrainingWeapons`;
- `restartTraining` como delegacion al nivel activo;
- `exitTrainingToMenu` como transicion de app state.

`TrainingMode` puede seguir existiendo como objeto interno de `TrainingLevel`.

Resultado esperado: `Game` no sabe como se arma training; solo entra, actualiza y sale del nivel activo.

#### Fase 5: Extraer movimiento/update comun del jugador

Crear un sistema pequeno:

- `PlayerMovementSystem`, o
- `PlayerController` si tambien consume input.

Responsabilidades:

- calcular move vector;
- aplicar colision contra `GameMap`;
- actualizar facing;
- actualizar knockback;
- actualizar velocidad lineal.

No meter render ni input listeners ahi.

Resultado esperado: `updatePlaying()` deja de tener todo el movimiento inline.

#### Fase 6: Extraer armas y proyectiles

Crear `WeaponUseSystem`.

Mover:

- cooldown de arma;
- `attemptFireProjectileWeapon`;
- `configureProjectileHoming`;
- calculo de accuracy;
- calculo de origen del disparo si no lo usa tambien el renderer;
- aviso a `TrainingMode.notifyShotFired()` mediante evento o callback.

Separar `ShotFeedback`:

- muzzle flash;
- hit marker;
- facing lock.

Resultado esperado: disparar deja de depender de metodos privados dispersos en `Game`.

#### Fase 7: Extraer equipamiento UI

Crear:

- `EquipmentMenuLayout`;
- `EquipmentMenuController`.

Mover:

- constantes de panel/lista;
- hit testing;
- scroll;
- seleccion de arma;
- apertura/cierre de selector.

`GameRenderer` y el controller deben usar el mismo layout.

Resultado esperado: layout y clicks de equipamiento dejan de vivir en `Game`.

#### Fase 8: Extraer settings, musica y cursor

Orden recomendado:

1. `DisplaySettingsController`:
   - fullscreen;
   - resolucion;
   - cambios pendientes;
   - sliders.
2. `MusicController`:
   - musica deseada segun app state y nivel activo;
   - `activeMusicPath`.
3. `CursorController`:
   - cursor segun estado, hover y feedback.

Resultado esperado: `Game` coordina controllers, no implementa cada detalle.

#### Fase 9: Separar datos de render

Crear `GameView` o `RenderContext`.

```java
interface GameView {
    State getState();
    GameMap getMap();
    Player getPlayer();
    List<Enemy> getEnemies();
    List<Projectile> getProjectiles();
    double getCameraZoom();
}
```

Migrar por partes. No intentar cambiar todo `GameRenderer` en un solo diff.

Resultado esperado: el renderer deja de depender de toda la clase `Game`.

#### Fase 10: Limpiar estados y flags

Revisar:

- `State.DEAD`: existe, pero hay que confirmar si realmente se asigna.
- `State.TRAINING`: no debe convivir con `activeLevel.type() == TRAINING`.
- `trainingActive`: debe reemplazarse por consulta al nivel activo cuando el lifecycle exista.
- `secretLevelActive`: no debe quedar en el modelo base si la version no tiene secret level.

Resultado esperado: menos combinaciones invalidas de booleans.

#### Fase 11: Limpiezas de bajo riesgo

Hallazgos actuales utiles:

1. `Game.facingVector(...)` crea `double[]` al disparar.
   - Solucion: agregar `unitX` y `unitY` a `Facing`.
2. `GameRenderer.fitPlayerSpriteSize(...)` crea `int[]` en render.
   - Solucion: calcular ancho/alto localmente o devolver dos helpers simples.
3. `TrainingMode` castea `EnemySystem.getEnemies()` a `ArrayList`.
   - Solucion: agregar API explicita en `EnemySystem` para clear/remove/spawn.
4. `GameRenderer` llama `game.getEnemies()` repetidamente dentro de loops.
   - Solucion: cachear `List<Enemy> enemies = game.getEnemies();` al inicio del metodo.
5. `GameRenderer` usa `String.split(" ")` para tooltip.
   - Solucion: cachear texto wrappeado o escribir wrapper sin regex.
6. `EnemySystem.chooseEnemyType()` llama `EnemyType.values()`.
   - Solucion: `private static final EnemyType[] ENEMY_TYPES = EnemyType.values();`.
7. Hay `new Color(...)` en rutas de render.
   - Solucion: mover colores repetidos a campos `private final` o cache por alpha si aplica.

Resultado esperado: menos ruido y menos allocations evitables sin cambiar arquitectura grande.

### Orden recomendado de implementacion

1. Clasificar secret/boss como fuera de alcance o legado.
2. Crear `GameContext`.
3. Crear `GameSession`.
4. Convertir `TrainingLevel` en Strategy real.
5. Extraer update/movement comun del jugador.
6. Extraer `WeaponUseSystem`.
7. Extraer `ShotFeedback`.
8. Extraer `EquipmentMenuLayout` y `EquipmentMenuController`.
9. Extraer `DisplaySettingsController`.
10. Extraer `MusicController`.
11. Extraer `CursorController`.
12. Crear `GameView`/`RenderContext` para renderer.
13. Limpiar `State.DEAD`, `State.TRAINING`, `trainingActive` y flags de feature vieja.
14. Aplicar micro-limpiezas de allocations.

### Patrones de disenio recomendados

#### 1. State

Uso simple: estados de aplicacion.

Aplicar a:

- menu;
- settings de menu;
- playing;
- pause menu;
- settings dentro del juego;
- death screen si vuelve a ser estado real.

Forma minima:

```java
interface AppStateNode {
    void enter(GameContext context, GameSession session);
    void update(GameContext context, GameSession session);
    void exit(GameContext context, GameSession session);
}
```

Beneficio: reduce `if/else` largos en `updateGame()`.

No empezar por aca si `GameContext` y `GameSession` todavia no existen.

#### 2. Strategy

Uso simple: niveles, cerebros enemigos y reglas variables.

Ya existe parcialmente en:

- `EnemyBrain`;
- `ChasePlayerBrain`;
- `FleePlayerBrain`;
- `IdleEnemyBrain`.

Aplicar a:

- `GameLevel` real;
- reglas de accuracy si crecen;
- patrones de ataque si vuelven bosses o enemigos especiales;
- seleccion de musica por nivel.

Forma minima para nivel:

```java
interface GameLevel {
    LevelType type();
    void enter(GameContext context, GameSession session, LevelStartOptions options);
    void update(GameContext context, GameSession session);
    void exit(GameContext context, GameSession session);
}
```

Beneficio: `Game` delega reglas sin duplicar loop ni JPanel.

#### 3. Builder

Uso simple: construccion de nivel/sesion.

Aplicar a:

- `TrainingLevelBuilder`;
- `WorldBuildRequest` si vuelve modo mundo;
- loadout inicial del jugador;
- configuracion de arena.

Forma minima:

```java
final class TrainingLevelBuilder {
    GameSession build(GameContext context, LevelStartOptions options) {
        GameSession session = new GameSession();
        // crear seed, map, player, runtime y training mode
        return session;
    }
}
```

Beneficio: evita que `Game` arme mapas, jugador, inventario y sistemas con 20 lineas privadas.

No usar Builder para objetos simples con dos parametros.

#### 4. Factory Method

Uso simple: crear entidades tipadas sin repetir constructores.

Aplicar a:

- enemigos especiales;
- proyectiles;
- loadouts;
- objetos de mapa si crecen variantes.

Ejemplo:

```java
final class EnemyFactory {
    Enemy createDiana(int worldX, int worldY, int dayCount) { ... }
    Enemy createTrainingTarget(int worldX, int worldY) { ... }
}
```

Beneficio: centraliza defaults y evita constructores repetidos en varios sistemas.

#### 5. Command

Uso simple: acciones de UI/input.

Aplicar a:

- botones de menu;
- botones de settings;
- acciones de training como restart/exit;
- debug toggles.

Forma minima:

```java
interface GameCommand {
    void execute(GameContext context, GameSession session);
}
```

Beneficio: separa hit testing de accion ejecutada.

No usarlo para cada tecla de movimiento; ahi `GameInput` ya cumple su funcion.

#### 6. Observer o Event Queue simple

Uso simple: notificar eventos entre sistemas sin acoplarlos directamente.

Aplicar a:

- disparo realizado;
- enemigo muerto;
- jugador muerto;
- item equipado;
- cambio de estado.

Forma minima:

```java
final class GameEvents {
    boolean shotFired;
    boolean playerDied;
    int enemiesKilledThisFrame;

    void clearFrameEvents() { ... }
}
```

Beneficio: `WeaponUseSystem` no necesita conocer internamente `TrainingMode`; puede emitir `shotFired` y el nivel lo consume.

No empezar con un event bus generico complejo.

#### 7. Facade

Ya existe en:

- `AssetManager`;
- `AudioManager`.

Aplicar a:

- `DisplaySettingsController`;
- `EquipmentMenuController`;
- `WeaponUseSystem`.

Beneficio: `Game` llama una API pequena y no conoce detalles internos.

#### 8. Null Object

Uso simple: evitar null checks de nivel activo.

Aplicar a:

- `NoLevel`;
- `NoMusicPolicy` si se extrae musica por estado;
- `NoShotFeedback` solo si hay muchas ramas.

Ejemplo:

```java
final class NoLevel implements GameLevel {
    public LevelType type() { return LevelType.NONE; }
    public void enter(GameContext context, GameSession session, LevelStartOptions options) {}
    public void update(GameContext context, GameSession session) {}
    public void exit(GameContext context, GameSession session) {}
}
```

Beneficio: menos `if (activeLevel != null)`.

#### 9. Type Object / catalogos tipados

Ya existe en:

- `TileType`;
- `MapObjectType`;
- `ItemDefinition`;
- `EnemyType`;
- `ProjectileType`.

Seguir usandolo para:

- armas nuevas;
- items;
- enemigos;
- proyectiles;
- objetos del mapa.

Beneficio: datos cerca del tipo y menos magic numbers dispersos.

#### 10. View Model / Render Context

No es GoF clasico, pero es el patron mas util para este repo.

Aplicar a:

- HUD de training;
- equipamiento;
- settings;
- datos del mundo para render;
- feedback visual.

Forma minima:

```java
final class RenderContext {
    final GameMap map;
    final Player player;
    final List<Enemy> enemies;
    final List<Projectile> projectiles;
    final State state;
}
```

Beneficio: `GameRenderer` deja de consultar toda la clase `Game`.

### Patrones que no conviene meter ahora

- ECS completo: demasiado cambio para esta base.
- Dependency Injection framework: innecesario en un juego Swing chico.
- Visitor: no resuelve el problema principal.
- Abstract Factory grande: solo usar factories pequenas cuando haya duplicacion real.
- Object Pool global: usarlo solo si proyectiles/particulas/enemigos generan presion real de GC.
- Singleton para managers: ya se pueden pasar por `GameContext`.

### Regla practica para nuevas features

- Si cambia una pantalla global, va a `AppStateNode` o controller de UI.
- Si cambia reglas de un nivel, va a `GameLevel`.
- Si cambia training, va a `TrainingLevel` o `TrainingMode`.
- Si cambia disparos, va a `WeaponUseSystem`.
- Si cambia feedback visual, va a `ShotFeedback` o view model.
- Si cambia audio, va a `AudioManager` o `MusicController`.
- Si cambia input, va a `GameInput` y se consume desde update.
- Si cambia render, va a `GameRenderer` sin mutar simulacion.
- Si cambia datos del mundo, pasa por `GameMap`, generadores o catalogos tipados.

`Game.java` solo conecta esas piezas.

## Seccion 2: Buenas Practicas Java

Fuente base: Google Java Style Guide, https://google.github.io/styleguide/javaguide.html.

Esta seccion define como aplicar esas reglas en el refactor sin convertirlo en un cambio cosmetico gigante. La regla operativa es simple: cada clase nueva debe nacer con estas practicas; las clases existentes se ajustan solo cuando se tocan por una razon funcional o de refactor.

### 1. Archivos y estructura

1. Mantener exactamente una clase top-level por archivo `.java`.
2. El nombre del archivo debe coincidir con el nombre de la clase top-level.
3. Mantener el orden de archivo:
   - licencia/copyright si existe;
   - `package SheriffsssPackage;`;
   - imports;
   - declaracion de clase/interfaz/enum.
4. Dejar una sola linea en blanco entre esas secciones.
5. Todo archivo fuente debe estar en UTF-8.
6. No usar tabs. La indentacion debe ser con espacios.

Aplicacion al refactor:

- `GameContext.java`, `GameSession.java`, `WeaponUseSystem.java`, `ShotFeedback.java`, `EquipmentMenuController.java` y clases similares deben crearse como archivos propios.
- No meter varias clases principales en `Game.java` para ahorrar archivos.
- Usar clases package-private cuando solo sean infraestructura interna del paquete.

### 2. Imports

1. No usar wildcard imports como `import java.util.*;`.
2. No usar module imports.
3. Separar imports static y no static en dos grupos si existen ambos.
4. Ordenar imports dentro de cada grupo.
5. No partir imports en varias lineas.
6. No usar static imports para clases nested static; importarlas con import normal.

Aplicacion al refactor:

- Al extraer sistemas desde `Game.java`, copiar solo los imports que la nueva clase realmente usa.
- Despues de mover codigo, eliminar imports muertos del archivo original.

### 3. Formato basico

1. Usar llaves en `if`, `else`, `for`, `do` y `while`, aunque haya una sola sentencia.
2. Usar estilo K&R: la llave abre en la misma linea del bloque.
3. Indentar bloques con +2 espacios.
4. Escribir una sentencia por linea.
5. Mantener lineas de Java hasta 100 caracteres cuando sea razonable.
6. Si una linea se parte, indentar la continuacion al menos +4 espacios.
7. Evitar alineaciones manuales con espacios para que columnas coincidan.
8. Usar una sola linea en blanco entre miembros consecutivos.

Aplicacion al refactor:

- No mover metodos solo para reformatear.
- Cuando se extraiga un metodo desde `Game.java`, corregir el formato del metodo extraido en la clase nueva.
- No usar cambios masivos de formato en archivos no relacionados.

### 4. Declaraciones y variables

1. Declarar una sola variable por declaracion.
2. Declarar variables locales cerca de su primer uso.
3. Usar `String[] args`, no `String args[]`.
4. Evitar variables locales que existan solo para sostener estado demasiado lejos de su uso.
5. No usar nombres de una sola letra en APIs publicas.

Aplicacion al refactor:

- En `WeaponUseSystem`, separar variables de direccion, origen, cooldown y accuracy en nombres concretos.
- En `GameSession`, agrupar estado mutable, pero no ocultar estado ambiguo con nombres genericos como `data`, `value` o `manager`.

### 5. Nombres

1. Clases, interfaces y enums: `UpperCamelCase`.
2. Metodos, campos no constantes, parametros y variables locales: `lowerCamelCase`.
3. Constantes reales: `UPPER_SNAKE_CASE`.
4. Una constante real debe ser `static final` y profundamente inmutable.
5. No usar prefijos o sufijos tipo `mField`, `sField`, `name_` o `kName`.
6. Paquetes en minuscula sin guiones bajos.

Aplicacion al refactor:

- Correcto: `GameContext`, `GameSession`, `LevelStartOptions`, `WeaponUseSystem`, `EquipmentMenuLayout`.
- Correcto: `activeLevel`, `playerRuntime`, `cameraZoom`, `shotFeedback`.
- Correcto: `DEFAULT_WEAPON_ATTACK_SOUND`, `REVOLVER_FLASH_TICKS`.
- Incorrecto: `mPlayer`, `sAudio`, `weapon_system`, `Game_Context`.

### 6. Orden interno de clases

1. Usar un orden logico que se pueda explicar.
2. No agregar metodos nuevos siempre al final por fecha.
3. Mantener overloads juntos, sin otros miembros entre medio.
4. Agrupar constantes, campos, constructor, metodos publicos y helpers privados de forma consistente.

Orden recomendado para clases nuevas del refactor:

1. constantes;
2. campos;
3. constructor;
4. API publica/package-private principal;
5. helpers privados;
6. clases internas si son imprescindibles.

Aplicacion al refactor:

- En `WeaponUseSystem`, mantener juntos los overloads de disparo o calculo de origen.
- En `EquipmentMenuController`, mantener juntos los metodos de hit testing.
- En `TrainingLevel`, mantener juntos `enter`, `update` y `exit`.

### 7. Switch, enums y casos exhaustivos

1. Todo `switch` debe ser exhaustivo.
2. Si se usa switch viejo con `:`, documentar cualquier fall-through intencional.
3. Si se usa switch expression, usar sintaxis nueva con `->`.
4. Para enums de estados o niveles, cubrir todos los valores o agregar `default` justificado.

Aplicacion al refactor:

- Si se crea `LevelType`, cualquier switch sobre `TRAINING`, `WORLD`, `NONE` o futuros valores debe quedar exhaustivo.
- Si se reemplazan flags por enums, la exhaustividad es obligatoria para evitar estados invisibles.

### 8. Comentarios, TODOs y Javadoc

1. Los comentarios de implementacion deben explicar decisiones, no repetir el codigo.
2. Los TODOs deben tener formato rastreable: `TODO: recurso - motivo`.
3. No usar TODOs con nombres de personas como contexto.
4. Usar Javadoc en clases o miembros visibles cuando una persona necesite entender contrato, side effects o lifecycle.
5. No escribir Javadoc obvio para getters triviales.

Aplicacion al refactor:

- `GameContext` y `GameSession` pueden no necesitar Javadoc si quedan package-private y claros.
- `GameLevel` si necesita Javadoc breve porque define lifecycle.
- `LevelStartOptions` necesita explicar si sus flags afectan debug, seed o estado persistente.
- Cualquier workaround temporal por codigo legado debe quedar con TODO rastreable o con una fase del documento.

### 9. Practicas de programacion

1. Usar `@Override` siempre que sea legal.
2. No ignorar excepciones capturadas; loguear, relanzar o comentar por que ignorarlas es correcto.
3. Calificar miembros static con el nombre de la clase, no con una instancia.
4. No usar `finalize()`.
5. No usar excepciones como control de flujo dentro del game loop.

Aplicacion al refactor:

- `TrainingLevel implements GameLevel` debe anotar `enter`, `update` y `exit` con `@Override`.
- Si `AudioManager` o sistemas de SFX capturan errores, deben registrar el recurso fallido o documentar la razon del fallback.
- Constantes de `GameConfig` deben leerse como `GameConfig.TARGET_FPS`, no desde una instancia.

### 10. Compatibilidad con rendimiento del juego

Google Style no reemplaza las reglas de rendimiento de `AGENTS.md`. En este proyecto, ambas reglas conviven asi:

1. No crear objetos temporales evitables en `update` o render.
2. No reformatear hot paths enteros en el mismo cambio que mueve logica.
3. No introducir streams, lambdas o colecciones temporales en loops por frame solo por estilo.
4. Preferir metodos pequenos y nombres claros antes que wrappers que asignan memoria.
5. Si el estilo pide claridad y el rendimiento pide evitar allocation, resolver con APIs explicitas.

Ejemplos aplicables:

- Reemplazar `double[] facingVector(...)` por `Facing.unitX()` y `Facing.unitY()`.
- Reemplazar `int[] fitPlayerSpriteSize(...)` por calculo local o helpers de ancho/alto.
- Evitar `String.split(" ")` en render de tooltips; cachear lineas o hacer wrapper sin regex.

### 11. Checklist por cada clase nueva

Antes de cerrar una clase nueva del refactor:

1. Archivo con una sola clase top-level.
2. `package` correcto.
3. Imports sin wildcard y sin imports muertos.
4. Indentacion con espacios.
5. Llaves obligatorias en condicionales y loops.
6. Lineas largas revisadas.
7. Nombres en `UpperCamelCase`, `lowerCamelCase` o `UPPER_SNAKE_CASE` segun corresponda.
8. Overloads juntos.
9. `switch` exhaustivos.
10. `@Override` en implementaciones.
11. Excepciones capturadas con accion o comentario.
12. Sin allocations evitables en rutas calientes.

### 12. Checklist para tocar clases existentes

Antes de cerrar un cambio sobre `Game.java`, `GameRenderer.java`, `TrainingMode.java` o sistemas existentes:

1. El cambio funcional queda separado de limpiezas cosmeticas grandes.
2. No se reordena todo el archivo sin necesidad.
3. El codigo movido queda formateado con las reglas nuevas en su destino.
4. El archivo origen queda sin imports muertos.
5. No se agregan wildcard imports.
6. No se agregan TODOs sin contexto rastreable.
7. No se cambian nombres publicos salvo que el refactor lo requiera.
8. La validacion de cierre sigue siendo `compile-and.run.bat`, sin crear builds temporales extra.