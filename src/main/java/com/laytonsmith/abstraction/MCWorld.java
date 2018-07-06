package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.entities.MCFallingBlock;
import com.laytonsmith.abstraction.entities.MCFirework;
import com.laytonsmith.abstraction.enums.MCBiomeType;
import com.laytonsmith.abstraction.enums.MCDifficulty;
import com.laytonsmith.abstraction.enums.MCEffect;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCGameRule;
import com.laytonsmith.abstraction.enums.MCMobs;
import com.laytonsmith.abstraction.enums.MCParticle;
import com.laytonsmith.abstraction.enums.MCSound;
import com.laytonsmith.abstraction.enums.MCSoundCategory;
import com.laytonsmith.abstraction.enums.MCTreeType;
import com.laytonsmith.abstraction.enums.MCWorldEnvironment;
import com.laytonsmith.abstraction.enums.MCWorldType;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.Target;

import java.util.List;

public interface MCWorld extends MCMetadatable {

	List<MCPlayer> getPlayers();

	List<MCEntity> getEntities();

	List<MCLivingEntity> getLivingEntities();

	String getName();

	long getSeed();

	MCWorldEnvironment getEnvironment();

	String getGenerator();

	MCWorldType getWorldType();

	int getSeaLevel();

	int getMaxHeight();

	MCDifficulty getDifficulty();

	void setDifficulty(MCDifficulty difficulty);

	boolean getPVP();

	void setPVP(boolean pvp);

	String[] getGameRules();

	String getGameRuleValue(String gameRule);

	boolean setGameRuleValue(MCGameRule gameRule, String value);

	MCWorldBorder getWorldBorder();

	MCBlock getBlockAt(int x, int y, int z);

	MCChunk getChunkAt(int x, int z);

	MCChunk getChunkAt(MCBlock b);

	MCChunk getChunkAt(MCLocation l);

	MCChunk[] getLoadedChunks();

	boolean regenerateChunk(int x, int y);

	MCEntity spawn(MCLocation l, Class mobType);

	MCEntity spawn(MCLocation l, MCEntityType entType);

	MCEntity spawn(MCLocation l, MCEntityType.MCVanillaEntityType entityType);

	boolean generateTree(MCLocation l, MCTreeType treeType);

	void playEffect(MCLocation l, MCEffect mCEffect, int data, int radius);

	void spawnParticle(MCLocation l, MCParticle pa, int count,
			double offsetX, double offsetY, double offsetZ, double velocity, Object data);

	void playSound(MCLocation l, MCSound sound, float volume, float pitch);

	void playSound(MCLocation l, String sound, float volume, float pitch);

	void playSound(MCLocation l, MCSound sound, MCSoundCategory category, float volume, float pitch);

	void playSound(MCLocation l, String sound, MCSoundCategory category, float volume, float pitch);

	MCItem dropItemNaturally(MCLocation l, MCItemStack is);

	MCItem dropItem(MCLocation l, MCItemStack is);

	MCLightningStrike strikeLightning(MCLocation location);

	MCLightningStrike strikeLightningEffect(MCLocation location);

	void setStorm(boolean b);

	void setThundering(boolean b);

	void setWeatherDuration(int time);

	void setThunderDuration(int time);

	boolean isStorming();

	boolean isThundering();

	MCLocation getSpawnLocation();

	void setSpawnLocation(int x, int y, int z);

	void refreshChunk(int x, int z);

	void loadChunk(int x, int z);

	void unloadChunk(int x, int z);

	void setTime(long time);

	long getTime();

	CArray spawnMob(MCMobs name, String subClass, int qty, MCLocation location, Target t);

	MCFallingBlock spawnFallingBlock(MCLocation loc, int type, byte data);

	MCFirework launchFirework(MCLocation l, int strength, List<MCFireworkEffect> effects);

	MCBiomeType getBiome(int x, int z);

	void setBiome(int x, int z, MCBiomeType type);

	MCBlock getHighestBlockAt(int x, int z);

	void explosion(double x, double y, double z, float size, boolean safe);

	/**
	 * This method performs some check on the world to ensure it exists.
	 *
	 * @return
	 */
	boolean exists();

	void save();
}
