package com.commandhelper.client;

import com.laytonsmith.core.MethodScriptFileLocations;
import java.io.File;
import net.minecraft.client.Minecraft;

/**
 *
 */
public class ForgeClientFileLocations extends MethodScriptFileLocations {


	private static ForgeClientFileLocations defaultInstance = null;

	public static ForgeClientFileLocations getDefault() {
		if(defaultInstance == null) {
			setDefault(new ForgeClientFileLocations(Minecraft.getInstance()));
		}
		return defaultInstance;
	}

	public static void setDefault(ForgeClientFileLocations provider) {
		defaultInstance = provider;
		MethodScriptFileLocations.setDefault(defaultInstance);
	}

	private final Minecraft minecraft;

	public ForgeClientFileLocations(Minecraft minecraft) {
		this.minecraft = minecraft;
		if(minecraft.getSingleplayerServer() != null) {

		}
	}

	public File getGameDirectory() {
		return this.minecraft.gameDirectory;
	}

	public File getRootClientFolder() {
		return new File(getGameDirectory(), "CommandHelperForgeClient");
	}

	/**
	 * Returns the root folder that this server connection should use.
	 * @return
	 */
	public File getServerFolder() {
		if(minecraft.isSingleplayer()) {
			return new File(getRootClientFolder(), "SinglePlayer");
		} else {
			return new File(new File(getRootClientFolder(), "Multiplayer"), this.minecraft.getCurrentServer().ip);
		}
	}

	@Override
	public File getCacheDirectory() {
		return new File(getRootClientFolder(), "cache");
	}

	@Override
	public File getConfigDirectory() {
		return getServerFolder();
	}
}
