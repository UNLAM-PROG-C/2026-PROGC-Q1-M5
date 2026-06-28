package SheriffsssPackage.core;

public enum Facing {
	DOWN(0),
	LEFT(1),
	UP(2),
	RIGHT(3),
	DOWN_LEFT(4),
	UP_RIGHT(5),
	UP_LEFT(6),
	DOWN_RIGHT(7);

	private final int spriteIndex;

	Facing(int spriteIndex) {
		this.spriteIndex = spriteIndex;
	}

	public int getSpriteIndex() {
		return this.spriteIndex;
	}
}
