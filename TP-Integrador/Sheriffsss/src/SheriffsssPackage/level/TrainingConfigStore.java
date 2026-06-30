package SheriffsssPackage.level;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Gestiona la persistencia de la configuración de entrenamiento.
 *
 * Responsabilidades:
 *   - Cargar la configuración guardada del archivo de disco
 *   - Guardar la configuración actual al archivo de disco
 *   - Parsear líneas de configuración
 */
public final class TrainingConfigStore
{
  // --- Persistencia ---
  private static final String CONFIG_PATH = "saves/training.cfg";
  private static final String CONFIG_KEY_COUNT = "count";
  private static final char CONFIG_KEY_VALUE_SEPARATOR = '=';

  private final TrainingControls controls;

  public TrainingConfigStore(TrainingControls controls)
  {
    this.controls = controls;
  }

  public void loadControls()
  {
    File file = new File(CONFIG_PATH);
    if (!file.exists())
    {
      return;
    }
    try (BufferedReader reader = new BufferedReader(new FileReader(file)))
    {
      String line;
      while ((line = reader.readLine()) != null)
      {
        applyConfigLine(line);
      }
    }
    catch (IOException ignored)
    {
    }
  }

  public void saveControls()
  {
    File file = new File(CONFIG_PATH);
    ensureParentDirExists(file);
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file)))
    {
      writer.write(CONFIG_KEY_COUNT + CONFIG_KEY_VALUE_SEPARATOR + this.controls.getEnemyCount());
      writer.newLine();
    }
    catch (IOException ignored)
    {
    }
  }

  private void applyConfigLine(String line)
  {
    int separator = line.indexOf(CONFIG_KEY_VALUE_SEPARATOR);
    if (separator < 0)
    {
      return;
    }
    String key = line.substring(0, separator).trim();
    String value = line.substring(separator + 1).trim();
    if (CONFIG_KEY_COUNT.equals(key))
    {
      applyCountValue(value);
    }
  }

  private void applyCountValue(String value)
  {
    try
    {
      this.controls.setEnemyCount(Integer.parseInt(value));
    }
    catch (NumberFormatException ignored)
    {
    }
  }

  private static void ensureParentDirExists(File file)
  {
    File parent = file.getParentFile();
    if (parent != null && !parent.exists())
    {
      parent.mkdirs();
    }
  }
}
