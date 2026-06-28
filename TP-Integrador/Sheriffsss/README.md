# Sheriffsss

Juego top-down en Java Swing centrado en un modo de entrenamiento para practicar movimiento, punteria y uso de armas en una arena cerrada.

---

## Requisitos

- **Java 17** o superior (JDK)
- Sistema operativo: Linux, Windows o macOS

Para verificar tu instalacion:

```bash
java -version
```

---

## Instalacion y ejecucion

### Linux / macOS

```bash
# Compilar y ejecutar
./compile-and-run.sh

# Solo compilar
./compile-and-run.sh --compile-only
```

### Windows

```powershell
# PowerShell
.\compile-and-run.ps1

# CMD
compile-and.run.bat
```

### Ejecutar el JAR directamente

Si ya tenes el `.jar` compilado en `dist-jar/`:

```bash
java -jar dist-jar/Sheriffsss.jar
```

---

## Modo Entrenamiento

Arena cerrada para practicar el movimiento, la punteria y las mecanicas principales sin un modo de supervivencia activo. Incluye tutorial guiado paso a paso.

- Control de cantidad de dianas de entrenamiento.
- HUD con aciertos, fallos, precision, temporizador y resultado final.
- Panel de debug opcional desde settings durante training.
- Opcion para volver al menu principal.

---

## Controles

### Movimiento y accion

| Tecla | Accion |
|-------|--------|
| `W` / `A` / `S` / `D` | Moverse |
| `Click izquierdo` | Disparar hacia el cursor |
| `Click derecho` | Accion secundaria |
| `E` | Interactuar |
| `TAB` | Abrir/cerrar panel de armas |
| `1` - `9`, `0` | Seleccionar arma del inventario |
| `Scroll del mouse` | Cambiar arma seleccionada |
| `ESC` | Pausa / Configuracion |

### Camara

| Tecla | Accion |
|-------|--------|
| `+` / `=` / `Numpad +` | Acercar camara |
| `-` / `Numpad -` | Alejar camara |
| `Ctrl + Scroll` | Zoom con la rueda del mouse |

### Pantalla

| Tecla | Accion |
|-------|--------|
| `F11` | Pantalla completa |
| `Alt + Enter` | Pantalla completa alternativa |

### Training

| Tecla | Accion |
|-------|--------|
| `PgUp` | Aumentar cantidad de dianas |
| `PgDown` | Disminuir cantidad de dianas |
| `R` | Resetear arena |
| `K` | Saltar tutorial |
| `B` | Volver al menu principal |

---

## Configuracion

Las preferencias de resolucion, volumen y pantalla completa se guardan automaticamente en `saves/game.cfg`.

Resoluciones disponibles: 800x704, 1024x768, 1280x720, 1366x768, 1600x900, 1920x1080.

---

## Estructura del proyecto

```text
Sheriffsss/
|-- src/                   # Codigo fuente Java
|-- resources/
|   |-- sprites/           # Imagenes y sprites
|   `-- sounds/            # Efectos de sonido y musica
|-- dist-jar/              # JAR ejecutable compilado
|-- saves/                 # Configuracion guardada
|-- compile-and-run.sh     # Script Linux/macOS
|-- compile-and-run.ps1    # Script Windows PowerShell
`-- compile-and.run.bat    # Script Windows CMD
```
