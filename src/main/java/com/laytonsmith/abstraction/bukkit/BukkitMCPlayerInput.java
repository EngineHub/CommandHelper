package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCPlayerInput;
import org.bukkit.Input;

public class BukkitMCPlayerInput implements MCPlayerInput {

	private final Input input;

	public BukkitMCPlayerInput(Input input) {
		this.input = input;
	}

	@Override
	public boolean forward() {
		return this.input.isForward();
	}

	@Override
	public boolean backward() {
		return this.input.isBackward();
	}

	@Override
	public boolean left() {
		return this.input.isLeft();
	}

	@Override
	public boolean right() {
		return this.input.isRight();
	}

	@Override
	public boolean jump() {
		return this.input.isJump();
	}

	@Override
	public boolean sneak() {
		return this.input.isSneak();
	}

	@Override
	public boolean sprint() {
		return this.input.isSprint();
	}
}
