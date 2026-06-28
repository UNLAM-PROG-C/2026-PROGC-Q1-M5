# AGENTS.md

Instrucciones para trabajar en este repo.

## REFACTOR

El refactor actual separa responsabilidades que antes estaban concentradas en
`Game.java`. Mantener esta estructura en cambios futuros.

### Estado del refactor

- `Game.java` queda como shell principal del loop, input de alto nivel y
  coordinacion entre sistemas.
- `GameContext` agrupa dependencias estables creadas una vez.
- `GameSession` guarda estado mutable de partida: mapa, jugador,
  `PlayerRuntimeState`, training, nivel activo, overlay de muerte y zoom.
- `GameLevel` modela el nivel activo con `LevelType`; training usa
  `LevelType.TRAINING` y la partida normal usa `LevelType.WORLD`.
- `State.TRAINING` fue eliminado. Training no es estado global de UI.
- `trainingActive` fue reemplazado por `session.activeLevel().type()`.
- El secret level no forma parte del camino principal de esta version.

### Paquetes

- `context`: dependencias estables del juego.
- `session`: estado mutable de la sesion.
- `level`: tipos y lifecycle de niveles.
- `system`: logica de gameplay sin render directo.
- `ui`: controladores y layout de UI.
- `render`: modelos de vista y renderizadores de HUD.

### Sistemas extraidos

- `PlayerMovementSystem`: movimiento del jugador y colisiones.
- `WeaponUseSystem`: uso de armas, disparos y direccion de aim.
- `HealthPickupSystem`: spawn y recoleccion de botiquines.
- `ShotFeedback`: feedback visual temporal de disparos.
- `MusicController`: musica segun estado.
- `CursorController`: cursor segun contexto.
- `DisplaySettingsController`: volumen, resolucion y fullscreen.
- `EquipmentMenuLayout` y `EquipmentMenuController`: layout e interaccion del menu de armas.
- `EnemyFactory`: creacion de enemigos de mundo y dianas de training.
- `GameEvents`: eventos simples como disparo y muerte del jugador.

### Render

- `GameRenderer` debe leer datos de solo lectura desde `GameView`.
- No agregar features nuevas de render que consulten estado mutable directo si
  puede exponerse un view model.
- HUD de score, equipment y training se mantiene separado en:
  `ScoreHudView`, `EquipmentHudView`, `TrainingHudSnapshot`,
  `TrainingHudView` y `TrainingHudRenderer`.

### Reglas de estilo para el refactor

- Codigo nuevo en ingles.
- Paquetes ordenados por responsabilidad.
- Una sola clase top-level por archivo nuevo.
- Indentacion de 2 espacios.
- No agregar tabs.
- No usar wildcard imports.
- No agregar numeros magicos; usar constantes descriptivas.
- Mantener metodos nuevos cerca de 15 lineas cuando sea razonable.
- Dividir clases grandes cuando se toque una responsabilidad clara.
- Comentar solo contratos, decisiones o comportamiento no obvio.

### Validacion

Antes de dar por cerrado un cambio del refactor:

```powershell
javac -encoding UTF-8 -d $env:TEMP\sheriffsss-check (Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
git diff --check
cmd.exe /c "echo. | compile-and.run.bat --packaging-check"
```

Eliminar `out/` si el launcher lo genera durante la validacion.

### Revision de diff

Para revisar funcionalidad ignorando normalizacion de espacios:

```powershell
git diff -w -- src/SheriffsssPackage/Game.java
git diff -w -- src/SheriffsssPackage/GameRenderer.java
git diff -w -- src/SheriffsssPackage/MenuRenderer.java
git diff -w -- src/SheriffsssPackage/TrainingMode.java
git diff -w -- src/SheriffsssPackage/EnemySystem.java
```

Para revisar formato, usar `git diff` normal.

### Documentos de seguimiento

- `GAME_REFACTOR.md`: plan general y patrones aplicables.
- `REFACTOR_PENDIENTES.md`: checklist original.
- `REFACTOR_PENDIENTES_ACTUALIZADO.md`: checklist actualizado.
- `REFACTOR_PENDIENTES_NUEVO.md`: pendientes nuevos derivados de la auditoria.
- `REFACTOR_DIFF_REVIEW.md`: criterio para separar revision funcional y estilo.
