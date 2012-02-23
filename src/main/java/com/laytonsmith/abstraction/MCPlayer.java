/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCBlock;
import java.net.InetSocketAddress;
import java.util.HashSet;

/**
 *
 * @author layton
 */
public interface MCPlayer extends MCCommandSender, MCOfflinePlayer{
    public String getDisplayName();
    public void chat(String chat);
    public MCItemStack getItemInHand();
    public MCInventory getInventory();
    public MCWorld getWorld();

    public void setTempOp(Boolean value) throws Exception;

    public MCLocation getLocation();
    
    public MCBlock getTargetBlock(HashSet<Byte> b, int i);
    public boolean teleport(MCLocation l);

    public void setHealth(int i);

    public void setDisplayName(String name);

    public void kickPlayer(String message);
    public InetSocketAddress getAddress();
    
    public boolean isSneaking();
    
    public int getHealth();
    
    public void setExp(float i);
    
    public float getExp();
    
    public MCGameMode getGameMode();
    public void setGameMode(MCGameMode mode);
    
    public void setItemInHand(MCItemStack is);

    
    public int getLevel();
    public void setLevel(int xp);
    public void giveExp(int xp);
    
    public int getTotalExperience();
    public void setTotalExperience(int total);
    
    
    public int getFoodLevel();
    public void setFoodLevel(int f);

    public MCLocation getCompassTarget();

    public void setCompassTarget(MCLocation l);

    public int getRemainingFireTicks();
    public void setRemainingFireTicks(int i);  
    
    public void addEffect(int potionID, int strength, int seconds);

    public boolean removeEffect(int effect);
    
    public boolean canSee(MCPlayer p);
    public void setVanished(boolean set, MCPlayer to);
}
