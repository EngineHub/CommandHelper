package com.laytonsmith.abstraction.enums;

/**
 *
 * @author Hekta
 */
public enum MCDifficulty {
	PEACEFUL(0),
	EASY(1),
	NORMAL(2),
	HARD(3);

	private final int value;

	MCDifficulty(int value) {
		this.value = value;
	}

    public int getValue() {
        return this.value;
    }
}