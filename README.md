# Sheriffsss - Proyecto de Programación Concurrente

Este repositorio contiene tanto la información de la cátedra de Programación Concurrente como el juego **Sheriffsss**, un shooter top-down 2D desarrollado en Java Swing/AWT.

---

## 🎮 De qué trata el juego (Sheriffsss)

**Sheriffsss** es un juego top-down centrado en un modo de entrenamiento en arena cerrada. Te pones en la piel de un sheriff en el Lejano Oeste. El juego cuenta con mecánicas de combate, puntería, uso de distintas armas, un ciclo dinámico de día/noche, y efectos de iluminación. Incluye un tutorial guiado concurrente (en un hilo separado) para que aprendas el movimiento y disparo. 

**Características principales:**
- Control de cantidad de dianas de entrenamiento.
- HUD (Interfaz) con estadísticas: aciertos, fallos, precisión, temporizador y resultado.
- Diferentes tipos de armas seleccionables.
- Opciones avanzadas de debug e interfaz de configuración gráfica in-game.

---

## 🛠️ Requisitos y Dependencias

El juego ha sido construido utilizando las bibliotecas estándar de Java, por lo que **no requiere dependencias externas** adicionales ni motores de juegos complejos para funcionar.

- **Java Development Kit (JDK):** Versión 17 o superior.
- **Sistema Operativo:** Compatible con Windows, macOS y Linux.

Para verificar si tienes la versión correcta de Java, ejecuta en tu terminal:
```bash
java -version
```

---

## 🚀 Instalación y Compilación

El código principal del juego se encuentra en la carpeta `TP-Integrador/Sheriffsss`. Desde ahí, cuentas con scripts automatizados para compilar y lanzar el juego según tu sistema operativo.

### En Linux / macOS
Abre una terminal, dirígete a la carpeta del juego y usa el script `.sh`:

```bash
cd TP-Integrador/Sheriffsss

# Para compilar y ejecutar el juego automáticamente:
./compile-and-run.sh

# Si solo deseas compilar sin ejecutar:
./compile-and-run.sh --compile-only
```

### En Windows
Puedes compilar y correr el juego usando PowerShell o la línea de comandos (CMD). Desde la carpeta `TP-Integrador\Sheriffsss`:

```powershell
# Usando PowerShell
.\compile-and-run.ps1

# Usando CMD tradicional
compile-and.run.bat
```

### Ejecutar el JAR directamente
Si el juego ya ha sido compilado previamente, se generará un archivo `.jar` en la subcarpeta `dist-jar/`. Puedes ejecutarlo de la siguiente manera:

```bash
java -jar dist-jar/Sheriffsss.jar
```

---

## 🕹️ Cómo jugar (Controles)

### Movimiento y Acción
| Tecla / Botón | Acción |
|---------------|--------|
| `W`, `A`, `S`, `D` | Mover al sheriff |
| `Click Izquierdo` | Disparar en dirección al cursor |
| `Click Derecho` | Acción secundaria |
| `E` | Interactuar |
| `TAB` | Abrir / cerrar el inventario (panel de armas) |
| `1` al `9`, `0` | Seleccionar arma rápidamente desde el inventario |
| `Scroll Mouse` | Alternar arma seleccionada |
| `ESC` | Pausa y menú de configuración |

### Cámara y Visualización
| Tecla / Botón | Acción |
|---------------|--------|
| `+`, `=`, `Numpad +` | Acercar la cámara (Zoom In) |
| `-`, `Numpad -` | Alejar la cámara (Zoom Out) |
| `Ctrl + Scroll` | Hacer zoom dinámico con el mouse |
| `F11` | Alternar Pantalla Completa |
| `Alt + Enter` | Pantalla Completa (método alternativo) |

### Modo Entrenamiento
| Tecla | Acción |
|-------|--------|
| `PgUp` | Incrementar la cantidad de dianas |
| `PgDown`| Disminuir la cantidad de dianas |
| `R` | Resetear la arena actual |
| `K` | Saltar el tutorial actual |
| `B` | Volver al menú principal |

*Nota: La configuración de resolución, volumen y pantalla completa se guarda automáticamente en `saves/game.cfg`.*

---

## 🎓 Reglas de la Cátedra (Programación Concurrente)

*Al menos 3 miembros deberán aprobar los code-reviews antes de que se mergeen al Branch destino.*

Al realizar un commit, se realizará la validación del código. Si cumple con los criterios establecidos por la cátedra, será aprobado.

**Validaciones requeridas:**
1. **Coding-Standard de Google:** Ajustado según el lenguaje, con modificaciones y adaptaciones marcadas por la cátedra.
2. **Patrones de diseño:** Es obligatorio el uso de patrones de diseño de la forma adecuada.
3. **No números mágicos:** Utilizar constantes semánticas en lugar de valores duros en el código.
4. **Tamaño de métodos:** Los métodos y funciones **no deben superar las 15 líneas** de código.
