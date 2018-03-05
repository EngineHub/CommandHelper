package com.laytonsmith.core.environments;

import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;

public class CommandHelperEnvironment implements Environment.EnvironmentImpl, Cloneable {

	private MCCommandSender commandSender = null;
	private String command = null;

	/**
	 * Given the environment, this function returns the CommandSender in the environment, which can possibly be null.
	 *
	 * @param env
	 * @return
	 */
	public MCCommandSender GetCommandSender() {
		return commandSender;
	}

	/**
	 * Sets the CommandSender in this environment
	 *
	 * @param env
	 */
	public void SetCommandSender(MCCommandSender cs) {
		commandSender = cs;
	}

	/**
	 * Given the environment, this function returns the Player in the environment, which can possibly be null. It is
	 * also possible the environment contains a CommandSender object instead, which will cause null to be returned.
	 *
	 * @param env
	 * @return
	 */
	public MCPlayer GetPlayer() {
		if(commandSender instanceof MCPlayer) {
			return (MCPlayer) commandSender;
		} else {
			return null;
		}
	}

	/**
	 * Sets the Player in this environment
	 *
	 * @param env
	 */
	public void SetPlayer(MCPlayer p) {
		commandSender = p;
	}

	@Override
	public CommandHelperEnvironment clone() throws CloneNotSupportedException {
		CommandHelperEnvironment clone = (CommandHelperEnvironment) super.clone();
		return clone;
	}

	public void SetCommand(String command) {
		this.command = command;
	}

	public String GetCommand() {
		return this.command;
	}
}
