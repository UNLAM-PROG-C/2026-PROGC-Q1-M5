package SheriffsssPackage.combat;

public final class ProjectileStatModifiers {
	public static final ProjectileStatModifiers NONE = new ProjectileStatModifiers(0.0, 0.0, 0.0, 0, 0);

	private final double damage;
	private final double speedPixels;
	private final double knockbackStrengthPixels;
	private final int cooldownTicks;
	private final int lifeTicks;

	public ProjectileStatModifiers(double damage, double speedPixels, double knockbackStrengthPixels, int cooldownTicks, int lifeTicks) {
		this.damage = damage;
		this.speedPixels = speedPixels;
		this.knockbackStrengthPixels = knockbackStrengthPixels;
		this.cooldownTicks = cooldownTicks;
		this.lifeTicks = lifeTicks;
	}

	public double getDamage() {
		return this.damage;
	}

	public double getSpeedPixels() {
		return this.speedPixels;
	}

	public double getKnockbackStrengthPixels() {
		return this.knockbackStrengthPixels;
	}

	public int getCooldownTicks() {
		return this.cooldownTicks;
	}

	public int getLifeTicks() {
		return this.lifeTicks;
	}
}
