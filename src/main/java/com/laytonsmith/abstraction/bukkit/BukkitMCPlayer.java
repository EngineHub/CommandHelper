/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MobEffect;
import net.minecraft.server.ServerConfigurationManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Server;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author layton
 */
public class BukkitMCPlayer extends BukkitMCCommandSender implements MCPlayer {

    Player p;

    public BukkitMCPlayer(Player p) {
        super(p);
        this.p = p;
    }

    @Override
    public String getName() {
        return p.getName();
    }

    @Override
    public String getDisplayName() {
        return p.getDisplayName();
    }

    @Override
    public void chat(String chat) {
        p.chat(chat);
    }

    public boolean isOnline() {
        return p.isOnline();
    }

    public MCItemStack getItemInHand() {
        if (p == null || p.getItemInHand() == null) {
            return null;
        }
        return new BukkitMCItemStack(p.getItemInHand());
    }

    public MCInventory getInventory() {
        if (p == null || p.getInventory() == null) {
            return null;
        }
        return new BukkitMCInventory(p.getInventory());
    }

    public MCWorld getWorld() {
        if (p == null || p.getWorld() == null) {
            return null;
        }
        return new BukkitMCWorld(p.getWorld());
    }

    public void setTempOp(Boolean value) throws ClassNotFoundException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Server server = Bukkit.getServer();

        Class serverClass = Class.forName("org.bukkit.craftbukkit.CraftServer", true, server.getClass().getClassLoader());

        if (!server.getClass().isAssignableFrom(serverClass)) {
            throw new IllegalStateException("Running server isn't CraftBukkit");
        }

        Field opSetField;

        try {
            opSetField = ServerConfigurationManager.class.getDeclaredField("operators");
        } catch (NoSuchFieldException e) {
            opSetField = ServerConfigurationManager.class.getDeclaredField("h");
        }

        opSetField.setAccessible(true); // make field accessible for reflection 

        // Reflection magic
        Set opSet = (Set) opSetField.get((ServerConfigurationManager) serverClass.getMethod("getHandle").invoke(server));

        // since all Java objects pass by reference, we don't need to set field back to object
        if (value) {
            opSet.add(p.getName().toLowerCase());
        } else {
            opSet.remove(p.getName().toLowerCase());
        }
        p.recalculatePermissions();
    }

    public MCLocation getLocation() {
        if(p.getLocation() == null){
            return null;
        }
        return new BukkitMCLocation(p.getLocation());
    }
    
    public MCBlock getTargetBlock(HashSet<Byte> b, int i){
        return new BukkitMCBlock(p.getTargetBlock(b, i));
    }
    
    public InetSocketAddress getAddress(){
        return p.getAddress();
    }

    public boolean teleport(MCLocation l) {
        return p.teleport(((BukkitMCLocation)l).l);
    }

    public void setHealth(int i) {
        p.setHealth(i);
    }

    public void setDisplayName(String name) {
        p.setDisplayName(name);
    }

    public void kickPlayer(String message) {
        p.kickPlayer(message);
    }

    public boolean isSneaking() {
        return p.isSneaking();
    }

    public int getHealth() {
        return p.getHealth();
    }
    
    public Player _Player(){
        return p;
    }

    public void setExp(float i) {
        p.setExp(i);
    }

    public float getExp() {
        return p.getExp();
    }

    public MCGameMode getGameMode() {
        switch(p.getGameMode()){
            case SURVIVAL:
                return MCGameMode.SURVIVAL;
            case CREATIVE:
                return MCGameMode.CREATIVE;
        }
        return null;
    }

    public void setGameMode(MCGameMode mode) {
        switch(mode){
            case SURVIVAL:
                p.setGameMode(GameMode.SURVIVAL);
                break;
            case CREATIVE:
                p.setGameMode(GameMode.CREATIVE);
                break;
        }
    }

    public void setItemInHand(MCItemStack is) {
        p.setItemInHand(((BukkitMCItemStack)is).is);
    }

    public int getLevel() {
        return p.getLevel();
    }

    public void setLevel(int xp) {
        p.setLevel(xp);
    }

    public void giveExp(int xp) {
        p.giveExp(xp);
    }

    public int getTotalExperience() {
        return p.getTotalExperience();
    }

    public void setTotalExperience(int total) {
        p.setTotalExperience(total);
    }

    public int getFoodLevel() {
        return p.getFoodLevel();
    }

    public void setFoodLevel(int f) {
        p.setFoodLevel(f);
    }

    public MCLocation getCompassTarget() {
        return new BukkitMCLocation(p.getCompassTarget());
    }

    public void setCompassTarget(MCLocation l) {
        p.setCompassTarget(((BukkitMCLocation)l).l);
    }

    public boolean isBanned() {
        return p.isBanned();
    }

    public void setBanned(boolean banned) {
        p.setBanned(banned);
    }

    public boolean isWhitelisted() {
        return p.isWhitelisted();
    }

    public void setWhitelisted(boolean value) {
        p.setWhitelisted(value);
    }

    public MCPlayer getPlayer() {
        return new BukkitMCPlayer(p);
    }

    public int getRemainingFireTicks() {
        return p.getFireTicks();
    }
    
    public void setRemainingFireTicks(int i){
        p.setFireTicks(i);
    }

    public void addEffect(int potionID, int strength, int seconds) {
        EntityPlayer ep = ((CraftPlayer) p).getHandle();
        Class epc = EntityLiving.class;
        MobEffect me = new MobEffect(potionID, seconds * 20, strength);
        try {
            Method meth = epc.getDeclaredMethod("d", net.minecraft.server.MobEffect.class);
            //ep.d(new MobEffect(effect, seconds * 20, strength));
            //Call it reflectively, because it's deobfuscated in newer versions of CB
            meth.invoke(ep, me);
        } catch (Exception e) {
            try {
                //Look for the addEffect version                
                Method meth = epc.getDeclaredMethod("addEffect", MobEffect.class);
                //ep.addEffect(me);
                meth.invoke(ep, me);
            } catch (Exception ex) {
                Logger.getLogger(BukkitMCPlayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
