
package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCInstrument;
import com.laytonsmith.abstraction.enums.MCSound;
import com.laytonsmith.abstraction.enums.MCWeather;
import java.net.InetSocketAddress;

/**
 *
 * @author layton
 */
public interface MCPlayer extends MCCommandSender, MCHumanEntity,
        MCOfflinePlayer {


    public boolean canSee(MCPlayer p);

    public void chat(String chat);

    public InetSocketAddress getAddress();

    public boolean getAllowFlight();

    public MCLocation getCompassTarget();

    public String getDisplayName();

    public float getExp();

	public float getFlySpeed();
	
	public void setFlySpeed(float speed);

    public int getFoodLevel();

    public MCPlayerInventory getInventory();

    public MCItemStack getItemAt(Integer slot);

    public int getLevel();

    public String getPlayerListName();

    public long getPlayerTime();

	public MCWeather getPlayerWeather();

    public int getRemainingFireTicks();

	public MCScoreboard getScoreboard();

    public int getTotalExperience();

	public float getWalkSpeed();
	
	public void setWalkSpeed(float speed);

    public void giveExp(int xp);

    public boolean isSneaking();
	
	public boolean isSprinting();

    public void kickPlayer(String message);

    public boolean removeEffect(int effect);

    public void resetPlayerTime();

	public void resetPlayerWeather();

	public void sendTexturePack(String url);

    public void setAllowFlight(boolean flight);

    public void setCompassTarget(MCLocation l);

    public void setDisplayName(String name);

    public void setExp(float i);

	public void setFlying(boolean flight);

    public void setFoodLevel(int f);

    public void setLevel(int xp);

    public void setPlayerListName(String listName);

    public void setPlayerTime(Long time, boolean relative);

	public void setPlayerWeather(MCWeather type);

    public void setRemainingFireTicks(int i);

	public void setScoreboard(MCScoreboard board);

    public void setTempOp(Boolean value) throws Exception;

    public void setTotalExperience(int total);

    public void setVanished(boolean set, MCPlayer to);

    public boolean isNewPlayer();

    public String getHost();

	public void sendBlockChange(MCLocation loc, int material, byte data);

	/**
	 * Unlike {@see MCEntity#getLocation}, this will work when not run on the server
	 * thread, but this does mean that the data recieved may be slightly outdated.
	 * @return
	 */
	public MCLocation asyncGetLocation();

	public void playNote(MCLocation loc, MCInstrument instrument, MCNote note);
	
	public void playSound(MCLocation l, MCSound sound, float volume, float pitch);

	public int getHunger();

	public void setHunger(int h);

	public float getSaturation();

	public void setSaturation(float s);

	public void setBedSpawnLocation(MCLocation l);

	public void sendPluginMessage(String channel, byte[] message);

	public boolean isOp();

	public void setOp(boolean bln);

	public boolean isFlying();

	public void updateInventory();
}
