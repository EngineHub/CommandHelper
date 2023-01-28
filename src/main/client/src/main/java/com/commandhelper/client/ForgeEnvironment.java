package com.commandhelper.client;

import com.commandhelper.client.abstraction.ForgePlayer;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.clientsupport.GenericClientEnv;
import net.minecraft.client.Minecraft;

/**
 *
 */
public class ForgeEnvironment extends GenericClientEnv {

	@Override
	public ForgeEnvironment clone() throws CloneNotSupportedException {
		return (ForgeEnvironment) super.clone();
	}

	@Override
	public MCPlayer getPlayer() {
		return new ForgePlayer(Minecraft.getInstance().player);
	}

}
