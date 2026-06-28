package SheriffsssPackage;

import java.util.Random;

import SheriffsssPackage.context.GameContext;
import SheriffsssPackage.level.TrainingLevel;
import SheriffsssPackage.session.GameSession;

public final class TrainingSessionBuilder {
  private static final String TRAINING_PLAYER_NAME = "Trainee";
  private static final double TRAINING_DAY_PROGRESS = 1.0;
  private static final int TRAINING_SEED_RADIX = 36;
  private static final int FNV_OFFSET_BASIS = 0x811C9DC5;
  private static final int FNV_PRIME = 0x01000193;

  public void build(Game owner, GameContext context, GameSession session, boolean resetDebugOptions) {
    if (resetDebugOptions) {
      context.debugOptions().resetAll();
    }
    String trainingWorldSeed = createRandomTrainingWorldSeed();
    prepareTrainingSystems(owner, context, session, trainingWorldSeed);
    spawnTrainingPlayer(context, session);
    giveTrainingLoadout(context, session.player());
    clearMenuStateForTraining(context, session);
  }

  public double trainingDayProgress() {
    return TRAINING_DAY_PROGRESS;
  }

  public void applyTrainingDebugLoadout(GameContext context, Player targetPlayer) {
    if (targetPlayer == null) {
      return;
    }
    if (context.debugOptions().shouldUnlockAllWeapons()) {
      unlockAllTrainingWeapons(targetPlayer);
    } else {
      resetTrainingWeapon(targetPlayer);
    }
  }

  private String createRandomTrainingWorldSeed() {
    long seed = System.nanoTime() ^ System.currentTimeMillis();
    return "training-" + Long.toUnsignedString(new Random(seed).nextLong(), TRAINING_SEED_RADIX).toUpperCase();
  }

  private void prepareTrainingSystems(
      Game owner,
      GameContext context,
      GameSession session,
      String trainingWorldSeed) {
    int trainingSeedHash = hashString(trainingWorldSeed);
    context.enemySystem().clear();
    context.enemySystem().reset(trainingSeedHash);
    context.enemySystem().setAutoSpawnEnabled(false);
    session.setActiveLevel(new TrainingLevel());
    session.setTrainingMode(new TrainingMode(owner, context.enemySystem(), trainingSeedHash));
    session.setMap(session.trainingMode().buildArena());
  }

  private void spawnTrainingPlayer(GameContext context, GameSession session) {
    int spawnX = session.trainingMode().getPlayerSpawnWorldX();
    int spawnY = session.trainingMode().getPlayerSpawnWorldY();
    session.setPlayer(new Player(TRAINING_PLAYER_NAME, spawnX, spawnY, context.assets()));
    session.resetPlayerRuntime();
  }

  private void giveTrainingLoadout(GameContext context, Player targetPlayer) {
    if (targetPlayer == null) {
      return;
    }
    applyTrainingDebugLoadout(context, targetPlayer);
  }

  private void resetTrainingWeapon(Player targetPlayer) {
    targetPlayer.getEquipment().resetToWeapon(ItemDefinition.ALTA_PISTOLA_PRIMERA);
  }

  private void unlockAllTrainingWeapons(Player targetPlayer) {
    targetPlayer.getEquipment().unlockWeapons(ItemDefinition.byWeaponType(WeaponType.ARMA_DE_FUEGO));
  }

  private void clearMenuStateForTraining(GameContext context, GameSession session) {
    context.dayNightCycle().reset();
    context.dayNightCycle().setDayProgress(TRAINING_DAY_PROGRESS);
    session.setDeathOverlayActive(false);
  }

  private int hashString(String value) {
    int hash = FNV_OFFSET_BASIS;
    for (char character : value.toCharArray()) {
      hash ^= character;
      hash *= FNV_PRIME;
    }
    return hash;
  }
}
