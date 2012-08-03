
package com.laytonsmith.abstraction;

import com.laytonsmith.core.constructs.Construct;
import java.net.InetSocketAddress;

/**
 * 
 * @author layton
 */
public interface MCPlayer extends MCCommandSender, MCHumanEntity,
        MCOfflinePlayer {
    public void addEffect(int potionID, int strength, int seconds);
    
    public boolean canSee(MCPlayer p);
    
    public void chat(String chat);
    
    public InetSocketAddress getAddress();
    
    public boolean getAllowFlight();
    
    public MCLocation getCompassTarget();
    
    public String getDisplayName();
    
    public float getExp();
    
    public int getFoodLevel();
    
    public MCInventory getInventory();
    
    public MCItemStack getItemAt(Construct construct);
    
    public int getLevel();
    
    public String getPlayerListName();
    
    public long getPlayerTime();
    
    public int getRemainingFireTicks();
    
    public int getTotalExperience();
    
    public void giveExp(int xp);
    
    public boolean isSneaking();
    
    public void kickPlayer(String message);
    
    public boolean removeEffect(int effect);
    
    public void resetPlayerTime();
    
    public void setAllowFlight(boolean flight);
    
    public void setCompassTarget(MCLocation l);
    
    public void setDisplayName(String name);
    
    public void setExp(float i);
    
    public void setFoodLevel(int f);
    
    public void setLevel(int xp);
    
    public void setPlayerListName(String listName);
    
    public void setPlayerTime(Long time);
    
    public void setRemainingFireTicks(int i);
    
    public void setTempOp(Boolean value) throws Exception;
    
    public void setTotalExperience(int total);
    
    public void setVanished(boolean set, MCPlayer to);
    
    public boolean isNewPlayer();
    
    public String getHost();
}
