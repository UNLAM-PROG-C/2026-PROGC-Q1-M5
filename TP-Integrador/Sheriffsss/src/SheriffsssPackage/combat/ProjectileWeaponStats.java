package SheriffsssPackage.combat;

public final class ProjectileWeaponStats {
	private final ProjectileType projectileType;
	private final int ammoDefinitionId;
	private final double damage;
	private final double speedPixels;
	private final double knockbackStrengthPixels;
	private final int cooldownTicks;
	private final int lifeTicks;

	public ProjectileWeaponStats(ProjectileType projectileType, int ammoDefinitionId, double damage, double speedPixels,
		double knockbackStrengthPixels, int cooldownTicks, int lifeTicks) {
		this.projectileType = projectileType;
		this.ammoDefinitionId = ammoDefinitionId;
		this.damage = damage;
		this.speedPixels = speedPixels;
		this.knockbackStrengthPixels = knockbackStrengthPixels;
		this.cooldownTicks = cooldownTicks;
		this.lifeTicks = lifeTicks;
	}

	public ProjectileType getProjectileType() {
		return this.projectileType;
	}

	public int getAmmoDefinitionId() {
		return this.ammoDefinitionId;
	}

	public double getDamage(ProjectileStatModifiers modifiers) {
		return Math.max(0.0, this.damage + modifiers.getDamage());
	}

	public double getSpeedPixels(ProjectileStatModifiers modifiers) {
		return Math.max(0.0, this.speedPixels + modifiers.getSpeedPixels());
	}

	public double getKnockbackStrengthPixels(ProjectileStatModifiers modifiers) {
		return Math.max(0.0, this.knockbackStrengthPixels + modifiers.getKnockbackStrengthPixels());
	}

	public int getCooldownTicks(ProjectileStatModifiers modifiers) {
		return Math.max(1, this.cooldownTicks + modifiers.getCooldownTicks());
	}

	public int getLifeTicks(ProjectileStatModifiers modifiers) {
		return Math.max(1, this.lifeTicks + modifiers.getLifeTicks());
	}
}
