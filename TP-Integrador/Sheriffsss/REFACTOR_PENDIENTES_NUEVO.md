# Refactor pendientes nuevo

Estado generado contra el estado actual del repo.

## Pendientes de codigo

- [x] Revisar nombres nuevos en ingles.
  - Alcance: clases, metodos, atributos, parametros y constantes agregadas por el refactor.
  - Criterio de cierre: no quedan identificadores nuevos en castellano.

- [x] Revisar constantes descriptivas para numeros fijos nuevos.
  - Alcance: sistemas nuevos, render nuevo, builders y enums nuevos.
  - Criterio de cierre: cualquier numero fijo nuevo queda nombrado como constante o queda justificado por contexto obvio.

- [x] Revisar metodos nuevos cercanos a 15 lineas cuando sea razonable.
  - Alcance: clases nuevas y metodos agregados a clases existentes.
  - Criterio de cierre: los metodos largos quedan divididos si mezclan responsabilidades.

- [x] Revisar division de clases grandes tocadas por el refactor.
  - Alcance: `Game`, `GameRenderer`, `MenuRenderer`, `TrainingMode` y sistemas extraidos.
  - Criterio de cierre: cada responsabilidad clara tocada queda movida a una clase o sistema propio.

- [x] Revisar comentarios.
  - Alcance: comentarios y Javadocs agregados.
  - Criterio de cierre: solo quedan comentarios sobre contratos, decisiones o comportamiento no obvio.

- [x] Separar el chequeo de estilo del cambio funcional.
  - Alcance: archivos existentes reformateados por tabs e indentacion.
  - Criterio de cierre: el diff permite distinguir extracciones funcionales de ajustes de formato.

## Validacion pendiente

- [x] Ejecutar `compile-and.run.bat`.
  - Nota: lanza el juego y usa `pause`; no se marco como cumplido con la compilacion temporal.

- [x] Probar menu principal.

- [x] Probar training.

- [x] Probar partida normal.

- [x] Probar disparos.

- [x] Probar botiquines.

- [x] Probar settings de volumen, resolucion y fullscreen.

## Verificado, no pendiente

- [x] No quedan referencias a secret level en `src/SheriffsssPackage`.

- [x] No quedan referencias a `State.TRAINING`.

- [x] No queda `trainingActive`.

- [x] No queda `renderHud(` en `TrainingMode`.

- [x] La compilacion temporal con `javac -encoding UTF-8` pasa.

- [x] `git diff --check` pasa; solo informa warnings de CRLF.

- [x] No hay tabs en archivos nuevos o modificados por este refactor.
