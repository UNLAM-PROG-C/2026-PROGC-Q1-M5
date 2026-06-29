package SheriffsssPackage.context;

import SheriffsssPackage.system.enemy.EnemySystem;
import SheriffsssPackage.system.weapon.ProjectileSystem;



/** Groups stable systems created once by the main game shell. */
public final class GameContext {
  private final AssetManager assets;
  private final AudioManager audio;
  private final GameInput input;
  private final DayNightCycle dayNightCycle;
  private final EnemySystem enemySystem;
  private final ProjectileSystem projectileSystem;
  private final DebugOptions debugOptions;

  public GameContext(AssetManager assets, AudioManager audio, GameInput input,
      DayNightCycle dayNightCycle, EnemySystem enemySystem,
      ProjectileSystem projectileSystem, DebugOptions debugOptions)
  {
    this.assets = assets;
    this.audio = audio;
    this.input = input;
    this.dayNightCycle = dayNightCycle;
    this.enemySystem = enemySystem;
    this.projectileSystem = projectileSystem;
    this.debugOptions = debugOptions;
  }

  public AssetManager assets()
  {
    return this.assets;
  }

  public AudioManager audio()
  {
    return this.audio;
  }

  public GameInput input()
  {
    return this.input;
  }

  public DayNightCycle dayNightCycle()
  {
    return this.dayNightCycle;
  }

  public EnemySystem enemySystem()
  {
    return this.enemySystem;
  }

  public ProjectileSystem projectileSystem()
  {
    return this.projectileSystem;
  }

  public DebugOptions debugOptions()
  {
    return this.debugOptions;
  }
}
