# Sheriffsss

Juego de acción top-down en Java Swing. Sos un sheriff en un pueblo del Lejano Oeste que debe sobrevivir a oleadas de ratas enemigas cada vez más peligrosas. Matá ratas, acumulá puntaje, desbloqueá armas más poderosas y enfrentá al Jefe Rata para avanzar de nivel.

---

## Requisitos

- **Java 17** o superior (JDK)
- Sistema operativo: Linux, Windows o macOS

Para verificar tu instalación:

```bash
java -version
```

---

## Instalación y ejecución

### Linux / macOS

```bash
# Compilar y ejecutar
./compile-and-run.sh

# Solo compilar (sin ejecutar)
./compile-and-run.sh --compile-only
```

### Windows

```powershell
# PowerShell
.\compile-and-run.ps1

# O con el archivo .bat
compile-and.run.bat
```

### Ejecutar el JAR directamente

Si ya tenés el `.jar` compilado en `dist-jar/`:

```bash
java -jar dist-jar/Sheriffsss.jar
```

---

## Modos de juego

### Juego Completo
El modo principal. Sobreviví a oleadas de ratas que escalan de dificultad con cada nivel. El objetivo es llegar al nivel 10.

- Empezás con el **Revolver de Bronce**
- Las ratas aparecen desde el inicio y aumentan en cantidad y fuerza
- Cuando acumulás suficiente puntaje, aparece el **Jefe Rata**
- Al matar al jefe subís de nivel, desbloqueás un arma nueva y aparece una oleada de refuerzo

### Modo Entrenamiento
Arena cerrada para practicar el movimiento, la puntería y conocer las mecánicas del juego sin presión. Incluye tutorial guiado paso a paso.

- Podés controlar cuántos enemigos aparecen (de 1 a 50)
- Los controles del panel de entrenamiento están activos (`PgUp`, `PgDown`, `R`)
- Presioná `B` para volver al menú

---

## Controles

### Movimiento y acción

| Tecla | Acción |
|-------|--------|
| `W` / `A` / `S` / `D` | Moverse (arriba / izquierda / abajo / derecha) |
| `Click izquierdo` | Disparar / atacar hacia el cursor |
| `Click derecho` | Acción secundaria |
| `E` | Interactuar |
| `TAB` | Abrir/cerrar panel de armas |
| `1` – `9`, `0` | Seleccionar arma del inventario |
| `Scroll del mouse` | Cambiar arma seleccionada |
| `ESC` | Pausa / Configuración |

### Cámara

| Tecla | Acción |
|-------|--------|
| `+` / `=` / `Numpad +` | Acercar cámara (zoom in) |
| `-` / `Numpad -` | Alejar cámara (zoom out) |
| `Ctrl + Scroll` | Zoom con la rueda del mouse |

### Pantalla

| Tecla | Acción |
|-------|--------|
| `F11` | Pantalla completa |
| `Alt + Enter` | Pantalla completa (alternativo) |

### Solo en modo Entrenamiento

| Tecla | Acción |
|-------|--------|
| `PgUp` | Aumentar cantidad de enemigos |
| `PgDown` | Disminuir cantidad de enemigos |
| `R` | Resetear arena |
| `K` | Saltar tutorial |
| `B` | Volver al menú principal |

---

## Sistema de progresión

### Puntaje y niveles

Cada enemigo eliminado otorga puntos:

| Enemigo | Puntos |
|---------|--------|
| Rata Chica | 3 |
| Rata | 8 |
| Rata Grande | 20 |
| Jefe Rata | 50 |

Al acumular suficiente puntaje aparece el **Jefe Rata**. Matarlo sube de nivel.

### Umbrales para el Jefe Rata

| Nivel actual | Puntaje necesario |
|--------------|-----------------|
| 1 | 40 |
| 2 | 150 |
| 3 | 350 |
| 4 | 650 |
| 5 | 1100 |
| 6 | 1800 |
| 7 | 2800 |
| 8 | 4200 |
| 9 | 6000 |

### Armas desbloqueables por nivel

| Nivel | Arma desbloqueada |
|-------|-------------------|
| Inicio | Revolver de Bronce |
| 2 | Luger |
| 3 | Revolver Reforzado |
| 4 | Alta Pistola Plateada |
| 5 | La Primera Alta Pistola |
| 6 | Nail Gun |

### Escalado de enemigos

Con cada nivel, todos los enemigos que aparezcan son más poderosos:

- **Vida** ×1.32 por nivel
- **Velocidad** ×1.09 por nivel
- **Daño** ×1.25 por nivel

---

## Enemigos

| Enemigo | Tamaño | Vida base | Daño base | Velocidad |
|---------|--------|-----------|-----------|-----------|
| Rata Chica | 40×40 px | 3 | 4 | Muy alta |
| Rata | 62×62 px | 10 | 14 | Alta |
| Rata Grande | 86×86 px | 30 | 28 | Media |
| Jefe Rata | 148×148 px | 200 | 40 | Lenta |

Todos los enemigos persiguen activamente al jugador.

---

## Botiquines de salud

- Aparecen solos en el mapa, máximo **2 al mismo tiempo**
- Se genera uno cada **60 segundos**
- Al acercarte lo recolectás automáticamente
- Restauran **+25 HP** (hasta el máximo de 100 HP)

---

## HUD (interfaz durante el juego)

- **Esquina inferior izquierda**: barra de vida
- **Esquina superior izquierda**: puntaje actual, nivel y el puntaje necesario para el próximo jefe
- **Esquina superior derecha**: hora del día y fase (mañana / tarde / atardecer / noche)
- **`TAB`**: panel de armas desbloqueadas

---

## Configuración

Las preferencias (resolución, volumen, pantalla completa) se guardan automáticamente en `saves/game.cfg`.

Resoluciones disponibles: 800×704, 1024×768, 1280×720, 1366×768, 1600×900, 1920×1080.

---

## Estructura del proyecto

```
Sheriffsss/
├── src/                   # Código fuente Java
├── resources/
│   ├── sprites/           # Imágenes y sprites
│   └── sounds/            # Efectos de sonido y música
├── dist-jar/              # JAR ejecutable compilado
├── saves/                 # Configuración guardada
├── compile-and-run.sh     # Script Linux/macOS
├── compile-and-run.ps1    # Script Windows (PowerShell)
└── compile-and.run.bat    # Script Windows (CMD)
```
