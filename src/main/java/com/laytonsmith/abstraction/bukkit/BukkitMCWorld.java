/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

/**
 *
 * @author layton
 */
public class BukkitMCWorld implements MCWorld {

    World w;

    public BukkitMCWorld(World w) {
        this.w = w;
    }
    
    public BukkitMCWorld(AbstractionObject a){
        this((World)null);
        if(a instanceof MCWorld){
            this.w = ((World)a.getHandle());
        } else {
            throw new ClassCastException();
        }
    }
    
    public Object getHandle(){
        return w;
    }

    public World __World() {
        return w;
    }

    public List<MCLivingEntity> getLivingEntities() {
        if (w.getLivingEntities() == null) {
            return null;
        }
        List<MCLivingEntity> list = new ArrayList<MCLivingEntity>();
        for (LivingEntity e : w.getLivingEntities()) {
            list.add(new BukkitMCLivingEntity(e));
        }
        return list;
    }

    public String getName() {
        return w.getName();
    }

    public MCBlock getBlockAt(int x, int y, int z) {
        if (w.getBlockAt(x, y, z) == null) {
            return null;
        }
        return new BukkitMCBlock(w.getBlockAt(x, y, z));
    }

    public MCEntity spawn(MCLocation l, Class mobType) {
        return BukkitConvertor.BukkitGetCorrectEntity(w.spawn(((BukkitMCLocation) l).l, mobType));
    }

    public void playEffect(MCLocation l, MCEffect mCEffect, int e, int data) {
        w.playEffect(((BukkitMCLocation) l).l, Effect.valueOf(mCEffect.name()), e, data);
    }

    public void dropItemNaturally(MCLocation l, MCItemStack is) {
        w.dropItemNaturally(((BukkitMCLocation) l).l, ((BukkitMCItemStack) is).is);
    }

    public void dropItem(MCLocation l, MCItemStack is) {
        w.dropItem(((BukkitMCLocation) l).l, ((BukkitMCItemStack) is).is);
    }

    public void strikeLightning(MCLocation GetLocation) {
        w.strikeLightning(((BukkitMCLocation) GetLocation).l);
    }

    public void strikeLightningEffect(MCLocation GetLocation) {
        w.strikeLightningEffect(((BukkitMCLocation) GetLocation).l);
    }

    public void setStorm(boolean b) {
        w.setStorm(b);
    }

    public MCLocation getSpawnLocation() {
        return new BukkitMCLocation(w.getSpawnLocation());
    }

    public void refreshChunk(int x, int z) {
        w.refreshChunk(x, z);
    }

    public void setTime(long time) {
        w.setTime(time);
    }

    public long getTime() {
        return w.getTime();
    }

    public MCBiomeType getBiome(int x, int z) {
        return MCBiomeType.valueOf(w.getBiome(x, z).name());
    }

    public void setBiome(int x, int z, MCBiomeType type) {
        w.setBiome(x, z, Biome.valueOf(type.name()));
    }

    public MCBlock getHighestBlockAt(int x, int z) {
        return new BukkitMCBlock(w.getHighestBlockAt(x, z));
     }

    public void explosion(double x, double y, double z, float size) {
        w.createExplosion(new Location(w, x,y,z), size);
    }

    private enum MOBS {

        CHICKEN, COW, CREEPER, GHAST, PIG, PIGZOMBIE, SHEEP, SKELETON, SLIME,
        SPIDER, SQUID, WOLF, ZOMBIE, CAVESPIDER, ENDERMAN, SILVERFISH, VILLAGER,
        BLAZE, ENDERDRAGON, MAGMACUBE, MOOSHROOM, SPIDERJOCKEY, GIANT, SNOWGOLEM,
        OCELOT, CAT, IRONGOLEM;
    }

    public CArray spawnMob(String name, String subClass, int qty, MCLocation l, Target t) {
        Class mobType = null;
        CArray ids = new CArray(Target.UNKNOWN);
        try {
            switch (MOBS.valueOf(name.toUpperCase().replaceAll(" ", ""))) {
                case CHICKEN:
                    mobType = Chicken.class;
                    break;
                case COW:
                    mobType = Cow.class;
                    break;
                case CREEPER:
                    mobType = Creeper.class;
                    break;
                case GHAST:
                    mobType = Ghast.class;
                    break;
                case PIG:
                    mobType = Pig.class;
                    break;
                case PIGZOMBIE:
                    mobType = PigZombie.class;
                    break;
                case SHEEP:
                    mobType = Sheep.class;
                    break;
                case SKELETON:
                    mobType = Skeleton.class;
                    break;
                case SLIME:
                    mobType = Slime.class;
                    break;
                case SPIDER:
                    mobType = Spider.class;
                    break;
                case SQUID:
                    mobType = Squid.class;
                    break;
                case WOLF:
                    mobType = Wolf.class;
                    break;
                case ZOMBIE:
                    mobType = Zombie.class;
                    break;
                case CAVESPIDER:
                    mobType = CaveSpider.class;
                    break;
                case ENDERMAN:
                    mobType = Enderman.class;
                    break;
                case SILVERFISH:
                    mobType = Silverfish.class;
                    break;
                case BLAZE:
                    mobType = Blaze.class;
                    break;
                case VILLAGER:
                    mobType = Villager.class;
                    break;
                case ENDERDRAGON:
                    mobType = EnderDragon.class;
                    break;
                case MAGMACUBE:
                    mobType = MagmaCube.class;
                    break;
                case MOOSHROOM:
                    mobType = MushroomCow.class;
                    break;
                case SPIDERJOCKEY:
                    mobType = Spider.class;
                    break;
                case GIANT:
                    double x = l.getX();
                    double y = l.getY();
                    double z = l.getZ();
                    float pitch = l.getPitch();
                    float yaw = l.getYaw();
                    net.minecraft.server.Entity giant = new net.minecraft.server.EntityGiantZombie(((CraftWorld) l.getWorld()).getHandle());
                    giant.setLocation(x, y, z, pitch, yaw);
                    ((CraftWorld) ((BukkitMCLocation)l)._Location().getWorld()).getHandle().addEntity(giant, SpawnReason.CUSTOM);
                    break;
                case SNOWGOLEM:
                    mobType = Snowman.class;
                    break;
                case OCELOT:
                    mobType = Ocelot.class;
                    break;
                case IRONGOLEM:
                    mobType = IronGolem.class;
                    break;
            }
        } catch (IllegalArgumentException e) {
            throw new ConfigRuntimeException("No mob of type " + name + " exists",
                    ExceptionType.FormatException, t);
        }
        for (int i = 0; i < qty; i++) {
            MCEntity e = l.getWorld().spawn(l, mobType);
            if (MOBS.valueOf(name.toUpperCase()) == MOBS.SPIDERJOCKEY) {
                Spider s = (Spider) e;
                Skeleton sk = (Skeleton) l.getWorld().spawn(l, Skeleton.class);
                s.setPassenger(sk);
            }
            if (((BukkitMCEntity)e).asEntity() instanceof Sheep) {
                Sheep s = (Sheep) ((BukkitMCEntity)e).asEntity();
                if("".equals(subClass)){
                    subClass = DyeColor.WHITE.name();
                }
                try {
                    s.setColor(DyeColor.valueOf(subClass.toUpperCase()));
                } catch (IllegalArgumentException ex) {
                    throw new ConfigRuntimeException(subClass.toUpperCase() + " is not a valid color",
                            ExceptionType.FormatException, t);
                }
            }
            if(((BukkitMCEntity)e).asEntity() instanceof Ocelot){
                Ocelot o = (Ocelot)((BukkitMCEntity)e).asEntity();
                if("".equals(subClass)){
                    subClass = Ocelot.Type.WILD_OCELOT.name();
                }
                try{
                    o.setCatType(Ocelot.Type.valueOf(subClass.toUpperCase()));
                } catch (IllegalArgumentException ex){
                    throw new ConfigRuntimeException(subClass.toUpperCase() + " is not a ocelot type",
                            ExceptionType.FormatException, t);                    
                }
            }
            ids.push(new CInt(e.getEntityId(), t));
        }
        return ids;
    }
}
