/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.CancelCommandException;
import com.laytonsmith.aliasengine.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Static;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Vehicle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

/**
 *
 * @author Layton
 */
public class Meta {
    public static class runas implements Function{

        public String getName() {
            return "runas";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(args[1].val().charAt(0) != '/'){
                throw new ConfigRuntimeException("The first character of the command must be a forward slash (i.e. '/give')");
            }
            String cmd = args[1].val().substring(1);
            if(args[0] instanceof CArray){
                CArray u = (CArray) args[0];
                for(int i = 0; i < u.size(); i++){
                    exec(line_num, Static.getServer().getPlayer(u.get(i).val()), args[1]);
                }
                return new CVoid(line_num);
            }
            if(args[0].val().equals("~op")){
                Static.getServer().dispatchCommand(new AlwaysOpPlayer(p), cmd);                
            } else{
                Player m = Static.getServer().getPlayer(args[0].val());
                if(m.isOnline()){
                    Static.getServer().dispatchCommand(m, cmd);
                } else {
                    p.sendMessage("The player " + m.getName() + " is not online");
                }
            }
            return new CVoid(line_num);
        }

        public String docs() {
            return "Runs a command as a particular user. The special user '~op' is a user that runs as op. Be careful with this very powerful function."
                    + " Commands cannot be run as an offline player. Returns void. If the first argument is an array of usernames, the command"
                    + " will be run in the context of each user in the array.";
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        
    }

    public static class run implements Function {

        public String getName() {
            return "run";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.getServer().dispatchCommand(p, args[0].val());
            return new CVoid(line_num);
        }

        public String docs() {
            return "Runs a command as the current player. Useful for running commands in a loop.";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        
    }
    
    private static class AlwaysOpPlayer implements Player{
        public Player r;
        
        public AlwaysOpPlayer(Player realPlayer){
            r = realPlayer;
        }
        
        public boolean isOnline() {
            return r.isOnline();
        }

        public String getDisplayName() {
            return r.getDisplayName();
        }

        public void setDisplayName(String string) {
            r.setDisplayName(string);
        }

        public void setCompassTarget(Location lctn) {
            r.setCompassTarget(lctn);
        }

        public Location getCompassTarget() {
            return r.getCompassTarget();
        }

        public InetSocketAddress getAddress() {
            return r.getAddress();
        }

        public void sendRawMessage(String string) {
            r.sendRawMessage(string);
        }

        public void kickPlayer(String string) {
            r.kickPlayer(string);
        }

        public void chat(String string) {
            r.chat(string);
        }

        public boolean performCommand(String string) {
            return r.performCommand(string);
        }

        public boolean isSneaking() {
            return r.isSneaking();
        }

        public void setSneaking(boolean bln) {
            r.setSneaking(bln);
        }

        public void saveData() {
            r.saveData();
        }

        public void loadData() {
            r.loadData();
        }

        @Deprecated
        public void updateInventory() {
            r.updateInventory();
        }

        public String getName() {
            return r.getName();
        }

        public PlayerInventory getInventory() {
            return r.getInventory();
        }

        public ItemStack getItemInHand() {
            return r.getItemInHand();
        }

        public void setItemInHand(ItemStack is) {
            r.setItemInHand(is);
        }

        public boolean isSleeping() {
            return r.isSleeping();
        }

        public int getSleepTicks() {
            return r.getSleepTicks();
        }

        public int getHealth() {
            return r.getHealth();
        }

        public void setHealth(int i) {
            r.setHealth(i);
        }

        public double getEyeHeight() {
            return r.getEyeHeight();
        }

        public double getEyeHeight(boolean bln) {
            return r.getEyeHeight(bln);
        }

        public Location getEyeLocation() {
            return r.getEyeLocation();
        }

        public List<Block> getLineOfSight(HashSet<Byte> hs, int i) {
            return r.getLineOfSight(hs, i);
        }

        public Block getTargetBlock(HashSet<Byte> hs, int i) {
            return r.getTargetBlock(hs, i);
        }

        public List<Block> getLastTwoTargetBlocks(HashSet<Byte> hs, int i) {
            return r.getLastTwoTargetBlocks(hs, i);
        }

        public Egg throwEgg() {
            return r.throwEgg();
        }

        public Snowball throwSnowball() {
            return r.throwSnowball();
        }

        public Arrow shootArrow() {
            return r.shootArrow();
        }

        public boolean isInsideVehicle() {
            return r.isInsideVehicle();
        }

        public boolean leaveVehicle() {
            return r.leaveVehicle();
        }

        public Vehicle getVehicle() {
            return r.getVehicle();
        }

        public int getRemainingAir() {
            return r.getRemainingAir();
        }

        public void setRemainingAir(int i) {
            r.setRemainingAir(i);
        }

        public int getMaximumAir() {
            return r.getMaximumAir();
        }

        public void setMaximumAir(int i) {
            r.setMaximumAir(i);
        }

        public void damage(int i) {
            r.damage(i);
        }

        public void damage(int i, Entity entity) {
            r.damage(i, entity);
        }

        public int getMaximumNoDamageTicks() {
            return r.getMaximumNoDamageTicks();
        }

        public void setMaximumNoDamageTicks(int i) {
            r.setMaximumNoDamageTicks(i);
        }

        public int getLastDamage() {
            return r.getLastDamage();
        }

        public void setLastDamage(int i) {
            r.setLastDamage(i);
        }

        public int getNoDamageTicks() {
            return r.getNoDamageTicks();
        }

        public void setNoDamageTicks(int i) {
            r.setNoDamageTicks(i);
        }

        public Location getLocation() {
            return r.getLocation();
        }

        public void setVelocity(Vector vector) {
            r.setVelocity(vector);
        }

        public Vector getVelocity() {
            return r.getVelocity();
        }

        public World getWorld() {
            return r.getWorld();
        }

        public boolean teleport(Location lctn) {
            return r.teleport(lctn);
        }

        public boolean teleport(Entity entity) {
            return r.teleport(entity);
        }

        @Deprecated
        public void teleportTo(Location lctn) {
            r.teleportTo(lctn);
        }

        @Deprecated
        public void teleportTo(Entity entity) {
            r.teleportTo(entity);
        }

        public List<Entity> getNearbyEntities(double d, double d1, double d2) {
            return r.getNearbyEntities(d, d1, d2);
        }

        public int getEntityId() {
            return r.getEntityId();
        }

        public int getFireTicks() {
            return r.getFireTicks();
        }

        public int getMaxFireTicks() {
            return r.getMaxFireTicks();
        }

        public void setFireTicks(int i) {
            r.setFireTicks(i);
        }

        public void remove() {
            r.remove();
        }

        public boolean isDead() {
            return r.isDead();
        }

        public Server getServer() {
            return r.getServer();
        }

        public Entity getPassenger() {
            return r.getPassenger();
        }

        public boolean setPassenger(Entity entity) {
            return r.setPassenger(entity);
        }

        public boolean isEmpty() {
            return r.isEmpty();
        }

        public boolean eject() {
            return r.eject();
        }

        public float getFallDistance() {
            return r.getFallDistance();
        }

        public void setFallDistance(float f) {
            r.setFallDistance(f);
        }

        public void sendMessage(String string) {
            r.sendMessage(string);
        }

        public boolean isOp() {
            return true;
        }      
    }
}
