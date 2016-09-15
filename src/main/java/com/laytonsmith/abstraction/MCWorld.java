

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
import com.laytonsmith.abstraction.enums.MCSound;
import com.laytonsmith.abstraction.enums.MCTreeType;
import com.laytonsmith.abstraction.enums.MCWorldEnvironment;
import com.laytonsmith.abstraction.enums.MCWorldType;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.Target;

import java.util.List;

/**
 *
 *
 */
public interface MCWorld extends MCMetadatable {
	public List<MCPlayer> getPlayers();
	public List<MCEntity> getEntities();
    public List<MCLivingEntity> getLivingEntities();

    public String getName();
	public long getSeed();
	public MCWorldEnvironment getEnvironment();
	public String getGenerator();
	public MCWorldType getWorldType();

	int getSeaLevel();

	public MCDifficulty getDifficulty();
	public void setDifficulty(MCDifficulty difficulty);
	public boolean getPVP();
	public void setPVP(boolean pvp);
	public boolean getGameRuleValue(MCGameRule gameRule);
	public void setGameRuleValue(MCGameRule gameRule, boolean value);

    public MCBlock getBlockAt(int x, int y, int z);
	public MCChunk getChunkAt(int x, int z);
	public MCChunk getChunkAt(MCBlock b);
	public MCChunk getChunkAt(MCLocation l);
	public MCChunk[] getLoadedChunks();

	public boolean regenerateChunk(int x, int y);

    public MCEntity spawn(MCLocation l, Class mobType);

	public MCEntity spawn(MCLocation l, MCEntityType entType);

	public MCEntity spawn(MCLocation l, MCEntityType.MCVanillaEntityType entityType);

	public boolean generateTree(MCLocation l, MCTreeType treeType);

	public void playEffect(MCLocation l, MCEffect mCEffect, int data, int radius);

	public void playSound(MCLocation l, MCSound sound, float volume, float pitch);

	public void playSound(MCLocation l, String sound, float volume, float pitch);

    public MCItem dropItemNaturally(MCLocation l, MCItemStack is);

    public MCItem dropItem(MCLocation l, MCItemStack is);

	public MCLightningStrike strikeLightning(MCLocation GetLocation);

	public MCLightningStrike strikeLightningEffect(MCLocation GetLocation);

    public void setStorm(boolean b);
	public void setThundering(boolean b);
	public void setWeatherDuration(int time);
	public void setThunderDuration(int time);
	public boolean isStorming();
	public boolean isThundering();

    public MCLocation getSpawnLocation();
    public void setSpawnLocation(int x, int y, int z);

    public void refreshChunk(int x, int z);
	public void loadChunk(int x, int z);
 	public void unloadChunk(int x, int z);

    public void setTime(long time);

    public long getTime();

    public CArray spawnMob(MCMobs name, String subClass, int qty, MCLocation location, Target t);

	public MCFallingBlock spawnFallingBlock(MCLocation loc, int type, byte data);

	public MCFirework launchFirework(MCLocation l, int strength, List<MCFireworkEffect> effects);

	public MCBiomeType getBiome(int x, int z);

    public void setBiome(int x, int z, MCBiomeType type);

    public MCBlock getHighestBlockAt(int x, int z);

    public void explosion(double x, double y, double z, float size, boolean safe);

	/**
	 * This method performs some check on the world to ensure it exists.
	 * @return
	 */
	public boolean exists();

	public void save();
}
