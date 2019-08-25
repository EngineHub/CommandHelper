package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCInstrument;
import com.laytonsmith.abstraction.enums.MCParticle;
import com.laytonsmith.abstraction.enums.MCPlayerStatistic;
import com.laytonsmith.abstraction.enums.MCPotionEffectType;
import com.laytonsmith.abstraction.enums.MCSound;
import com.laytonsmith.abstraction.enums.MCSoundCategory;
import com.laytonsmith.abstraction.enums.MCWeather;
import java.net.InetSocketAddress;

public interface MCPlayer extends MCCommandSender, MCHumanEntity, MCOfflinePlayer {

	boolean canSee(MCPlayer p);

	void chat(String chat);

	InetSocketAddress getAddress();

	boolean getAllowFlight();

	MCLocation getCompassTarget();

	String getDisplayName();

	float getExp();

	float getFlySpeed();

	void setFlySpeed(float speed);

	@Override
	MCPlayerInventory getInventory();

	MCItemStack getItemAt(Integer slot);

	int getLevel();

	String getPlayerListName();

	String getPlayerListHeader();

	String getPlayerListFooter();

	long getPlayerTime();

	MCWeather getPlayerWeather();

	int getRemainingFireTicks();

	MCScoreboard getScoreboard();

	int getTotalExperience();

	int getExpToLevel();

	int getExpAtLevel();

	MCEntity getSpectatorTarget();

	float getWalkSpeed();

	void setWalkSpeed(float speed);

	void giveExp(int xp);

	boolean isSneaking();

	boolean isSprinting();

	void kickPlayer(String message);

	@Override
	boolean removeEffect(MCPotionEffectType type);

	void resetPlayerTime();

	void resetPlayerWeather();

	void sendResourcePack(String url);

	void sendTitle(String title, String subtitle, int fadein, int stay, int fadeout);

	void setAllowFlight(boolean flight);

	void setCompassTarget(MCLocation l);

	void setDisplayName(String name);

	void setExp(float i);

	void setFlying(boolean flight);

	void setLevel(int xp);

	void setPlayerListName(String listName);

	void setPlayerListHeader(String header);

	void setPlayerListFooter(String footer);

	void setPlayerTime(Long time, boolean relative);

	void setPlayerWeather(MCWeather type);

	void setRemainingFireTicks(int i);

	void setScoreboard(MCScoreboard board);

	void setSpectatorTarget(MCEntity entity);

	void setTempOp(Boolean value) throws Exception;

	void setTotalExperience(int total);

	void setVanished(boolean set, MCPlayer to);

	boolean isNewPlayer();

	String getHost();

	void sendBlockChange(MCLocation loc, MCBlockData data);

	void sendSignTextChange(MCLocation loc, String[] lines);

	void playNote(MCLocation loc, MCInstrument instrument, MCNote note);

	void playSound(MCLocation l, MCSound sound, float volume, float pitch);

	void playSound(MCLocation l, String sound, float volume, float pitch);

	void playSound(MCLocation l, MCSound sound, MCSoundCategory category, float volume, float pitch);

	void playSound(MCLocation l, String sound, MCSoundCategory category, float volume, float pitch);

	void stopSound(MCSound sound);

	void stopSound(String sound);

	void stopSound(MCSound sound, MCSoundCategory category);

	void stopSound(String sound, MCSoundCategory category);

	void spawnParticle(MCLocation l, MCParticle pa, int count, double offsetX, double offsetY, double offsetZ, double velocity, Object data);

	int getFoodLevel();

	void setFoodLevel(int f);

	float getSaturation();

	void setSaturation(float s);

	float getExhaustion();

	void setExhaustion(float e);

	void setBedSpawnLocation(MCLocation l, boolean forced);

	void sendPluginMessage(String channel, byte[] message);

	@Override
	boolean isOp();

	void setOp(boolean bln);

	boolean isFlying();

	void updateInventory();

	int getStatistic(MCPlayerStatistic stat);

	int getStatistic(MCPlayerStatistic stat, MCEntityType type);

	int getStatistic(MCPlayerStatistic stat, MCMaterial type);

	void setStatistic(MCPlayerStatistic stat, int amount);

	void setStatistic(MCPlayerStatistic stat, MCEntityType type, int amount);

	void setStatistic(MCPlayerStatistic stat, MCMaterial type, int amount);
}
