package SheriffsssPackage;

public enum EnemyType {
	DIANA(
		"sprites/Diana.png",
		EnemyBehavior.STATIC,
		64, 64, 1, 1, 36, 36, 12,
		1.0, 0.0, 0.0, 0.0, 0, 1, 0, 1,
		EnemyDensity.LOW, "sounds/diana_clang.wav",
		1
	),
	BANDIDO(
		"sprites/Diana.png",
		EnemyBehavior.CONSTANT_CHASE,
		64, 64, 1, 1, 36, 36, 14,
		5.0, 1.2, 8.0, 6.0, 20, 60, 0, 1,
		EnemyDensity.LOW, "sounds/diana_clang.wav",
		5
	),
	RATA_CHICA(
		"sprites/RataMalvada.png",
		EnemyBehavior.CONSTANT_CHASE,
		512, 512, 1, 1, 40, 40, 14,
		3.0, 2.2, 4.0, 4.0, 20, 45, 4, 1,
		EnemyDensity.LOW, "",
		3
	),
	RATA(
		"sprites/RataMalvada.png",
		EnemyBehavior.CONSTANT_CHASE,
		512, 512, 1, 1, 62, 62, 24,
		10.0, 1.5, 14.0, 7.0, 28, 55, 3, 1,
		EnemyDensity.LOW, "",
		8
	),
	RATA_GRANDE(
		"sprites/RataMalvada.png",
		EnemyBehavior.CONSTANT_CHASE,
		512, 512, 1, 1, 86, 86, 36,
		30.0, 0.9, 28.0, 12.0, 38, 70, 1, 1,
		EnemyDensity.LOW, "",
		20
	),
	JEFE_RATA(
		"sprites/RataMalvada.png",
		EnemyBehavior.CONSTANT_CHASE,
		512, 512, 1, 1, 148, 148, 60,
		200.0, 0.6, 40.0, 18.0, 56, 80, 0, 1,
		EnemyDensity.LOW, "",
		50
	);

	private final String spritePath;
	private final EnemyBehavior behavior;
	private final int frameWidth;
	private final int frameHeight;
	private final int framesPerRow;
	private final int rowCount;
	private final int drawWidth;
	private final int drawHeight;
	private final int collisionRadius;
	private final double baseMaxHP;
	private final double baseSpeed;
	private final double baseDamage;
	private final double attackKnockbackStrengthPixels;
	private final int attackRangePixels;
	private final int attackCooldownTicks;
	private final int spawnWeight;
	private final int minimumSpawnDay;
	private final EnemyDensity density;
	private final String hitSoundPath;
	private final int scoreReward;

	EnemyType(String spritePath, EnemyBehavior behavior, int frameWidth, int frameHeight, int framesPerRow, int rowCount,
		int drawWidth, int drawHeight, int collisionRadius, double baseMaxHP, double baseSpeed, double baseDamage,
		double attackKnockbackStrengthPixels, int attackRangePixels,
		int attackCooldownTicks, int spawnWeight, int minimumSpawnDay, EnemyDensity density, String hitSoundPath,
		int scoreReward) {
		this.spritePath = spritePath;
		this.behavior = behavior;
		this.frameWidth = frameWidth;
		this.frameHeight = frameHeight;
		this.framesPerRow = framesPerRow;
		this.rowCount = rowCount;
		this.drawWidth = drawWidth;
		this.drawHeight = drawHeight;
		this.collisionRadius = collisionRadius;
		this.baseMaxHP = baseMaxHP;
		this.baseSpeed = baseSpeed;
		this.baseDamage = baseDamage;
		this.attackKnockbackStrengthPixels = attackKnockbackStrengthPixels;
		this.attackRangePixels = attackRangePixels;
		this.attackCooldownTicks = attackCooldownTicks;
		this.spawnWeight = spawnWeight;
		this.minimumSpawnDay = minimumSpawnDay;
		this.density = density;
		this.hitSoundPath = hitSoundPath == null ? "" : hitSoundPath;
		this.scoreReward = scoreReward;
	}

	public String getSpritePath() {
		return this.spritePath;
	}

	public EnemyBehavior getBehavior() {
		return this.behavior;
	}

	public int getFrameWidth() {
		return this.frameWidth;
	}

	public int getFrameHeight() {
		return this.frameHeight;
	}

	public int getFramesPerRow() {
		return this.framesPerRow;
	}

	public int getDrawWidth() {
		return this.drawWidth;
	}

	public int getDrawHeight() {
		return this.drawHeight;
	}

	public int getCollisionRadius() {
		return this.collisionRadius;
	}

	public double getScaledMaxHP(int dayCount) {
		return this.baseMaxHP * Math.pow(1.32, Math.max(0, dayCount - 1));
	}

	public double getScaledSpeed(int dayCount) {
		return this.baseSpeed * Math.pow(1.09, Math.max(0, dayCount - 1));
	}

	public double getScaledDamage(int dayCount) {
		return this.baseDamage * Math.pow(1.25, Math.max(0, dayCount - 1));
	}

	public double getAttackKnockbackStrengthPixels() {
		return this.attackKnockbackStrengthPixels;
	}

	public int getAttackRangePixels() {
		return this.attackRangePixels;
	}

	public int getAttackCooldownTicks() {
		return this.attackCooldownTicks;
	}

	public int getSpawnWeight() {
		return this.spawnWeight;
	}

	public int getMinimumSpawnDay() {
		return this.minimumSpawnDay;
	}

	public EnemyDensity getDensity() {
		return this.density;
	}

	public String getHitSoundPath() {
		return this.hitSoundPath;
	}

	public int getScoreReward() {
		return this.scoreReward;
	}

	public int getAnimationRow(Facing facing) {
		if (this.rowCount <= 1) {
			return 0;
		}
		if (facing == Facing.DOWN) {
			return 0;
		}
		if (facing == Facing.LEFT) {
			return 1;
		}
		if (facing == Facing.RIGHT) {
			return 2;
		}
		return 3;
	}
}
