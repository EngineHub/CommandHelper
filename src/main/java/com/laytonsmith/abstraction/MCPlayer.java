/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.constructs.Construct;
import java.net.InetSocketAddress;
import java.util.HashSet;

/**
 *
 * @author layton
 */
public interface MCPlayer extends MCCommandSender, MCHumanEntity, MCOfflinePlayer{
    public void addEffect(int potionID, int strength, int seconds);
    public boolean canSee(MCPlayer p);
    public void chat(String chat);
    public InetSocketAddress getAddress();
    public boolean getAllowFlight();

    public MCLocation getCompassTarget();

    public String getDisplayName();
    
    public float getExp();
    public int getFoodLevel();

    public MCGameMode getGameMode();

    public int getHealth();

    public MCInventory getInventory();
    public MCItemStack getItemAt(Construct construct);
    
    public MCItemStack getItemInHand();
    
    public int getLevel();
    
    public MCLocation getLocation();
    
    public long getPlayerTime();
    
    public int getRemainingFireTicks();
    public MCBlock getTargetBlock(HashSet<Byte> b, int i);
    
    public int getTotalExperience();

    public MCVelocity getVelocity();
    public MCWorld getWorld();
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

    public void setGameMode(MCGameMode mode);
    public void setHealth(int i);  
    
    public void setItemInHand(MCItemStack is);

    public void setLevel(int xp);
    
    public void setPlayerTime(Long time);
    public void setRemainingFireTicks(int i);
    
    public void setTempOp(Boolean value) throws Exception;
    
    public void setTotalExperience(int total);
    public void setVanished(boolean set, MCPlayer to);

    public boolean teleport(MCLocation l);
}
