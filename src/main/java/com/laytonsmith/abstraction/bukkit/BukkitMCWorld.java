

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.enums.MCMobs;
import com.laytonsmith.abstraction.enums.MCBiomeType;
import com.laytonsmith.abstraction.enums.MCEffect;
import com.laytonsmith.abstraction.enums.MCProfession;
import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCBiomeType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDyeColor;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCProfession;
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
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.material.MaterialData;
import org.bukkit.Material;

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

    public MCItem dropItemNaturally(MCLocation l, MCItemStack is) {
        return new BukkitMCItem(w.dropItemNaturally(((BukkitMCLocation) l).l, ((BukkitMCItemStack) is).is));
    }

    public MCItem dropItem(MCLocation l, MCItemStack is) {
        return new BukkitMCItem(w.dropItem(((BukkitMCLocation) l).l, ((BukkitMCItemStack) is).is));
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
		return BukkitMCBiomeType.getConvertor().getAbstractedEnum(w.getBiome(x, z));
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

    public void setSpawnLocation(int x, int y, int z) {
        w.setSpawnLocation(x, y, z);
    }

    public CArray spawnMob(MCMobs name, String subClass, int qty, MCLocation l, Target t) {
        Class mobType = null;
        CArray ids = new CArray(Target.UNKNOWN);
        try {
            switch (name) {
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
                    net.minecraft.server.Entity giant = new net.minecraft.server.EntityGiantZombie(((CraftWorld) (l.getWorld().getHandle())).getHandle());
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
				case BAT:
					mobType = Bat.class;
					break;
				case WITHER:
					mobType = Wither.class;
					break;
				case WITHER_SKULL:
					mobType = WitherSkull.class;
					break;
				case WITCH:
					mobType = Witch.class;
					break;
            }
        } catch (IllegalArgumentException e) {
            throw new ConfigRuntimeException("No mob of type " + name + " exists",
                    ExceptionType.FormatException, t);
        }
        for (int i = 0; i < qty; i++) {
            MCEntity e = l.getWorld().spawn(l, mobType);
            if (name == MCMobs.SPIDERJOCKEY) {
                Spider s = (Spider) e;
                Skeleton sk = (Skeleton) l.getWorld().spawn(l, Skeleton.class);
                s.setPassenger(sk);
            }
            if (((BukkitMCEntity)e).asEntity() instanceof Sheep) {
                Sheep s = (Sheep) ((BukkitMCEntity)e).asEntity();
				MCDyeColor color = MCDyeColor.WHITE;
                if(!"".equals(subClass)){
                    color = MCDyeColor.valueOf(subClass.toUpperCase());
                }
                try {
                    s.setColor(BukkitMCDyeColor.getConvertor().getConcreteEnum(color));
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
            if(((BukkitMCEntity)e).asEntity() instanceof Creeper){
                Creeper c = (Creeper)((BukkitMCEntity)e).asEntity();
                if("POWERED".equals(subClass.toUpperCase())){
                    c.setPowered(true);
                }
            }
            if(((BukkitMCEntity)e).asEntity() instanceof Wolf){
                Wolf w = (Wolf)((BukkitMCEntity)e).asEntity();
                if("ANGRY".equals(subClass.toUpperCase())){
                    w.setAngry(true);
                }
            }
            if(((BukkitMCEntity)e).asEntity() instanceof PigZombie){
                PigZombie pz = (PigZombie)((BukkitMCEntity)e).asEntity();
                if("".equals(subClass)){
                    pz.setAngry(false);
                }
                else{
                    try{
                        pz.setAnger(java.lang.Integer.parseInt(subClass));
                    } catch (IllegalArgumentException ex){
                           throw new ConfigRuntimeException(subClass + " is not a valid anger level",
                                   ExceptionType.FormatException, t);
                    }
                }
            }
            if (((BukkitMCEntity)e).asEntity() instanceof Villager) {
                Villager v = (Villager) ((BukkitMCEntity)e).asEntity();
                MCProfession job = MCProfession.FARMER;
                if(!"".equals(subClass)){
                    job = MCProfession.valueOf(subClass.toUpperCase());
                }
                try {
                    v.setProfession(BukkitMCProfession.getConvertor().getConcreteEnum(job));
                } catch (IllegalArgumentException ex) {
                    throw new ConfigRuntimeException(subClass.toUpperCase() + " is not a valid profession",
                        ExceptionType.FormatException, t);
                }
            }
            if (((BukkitMCEntity)e).asEntity() instanceof Enderman) {
                Enderman en = (Enderman) ((BukkitMCEntity)e).asEntity();
                if(!"".equals(subClass)){
                	MaterialData held = new MaterialData(Material.valueOf(subClass.toUpperCase()));
                    try {
                        en.setCarriedMaterial(held);
                    } catch (IllegalArgumentException ex) {
                        throw new ConfigRuntimeException(subClass.toUpperCase() + " cannot be held",
                            ExceptionType.FormatException, t);
                    }
                }
            }
            if (((BukkitMCEntity)e).asEntity() instanceof Slime){
            	Slime sl = (Slime) ((BukkitMCEntity)e).asEntity();
            	if(!"".equals(subClass)){
            		try {
            			sl.setSize(java.lang.Integer.parseInt(subClass));
            		} catch (IllegalArgumentException ex) {
            			throw new ConfigRuntimeException(subClass + " is not a valid size",
            					ExceptionType.FormatException, t);
            		}
            	}
            }
            if (((BukkitMCEntity)e).asEntity() instanceof Zombie){
            	Zombie z = (Zombie) ((BukkitMCEntity)e).asEntity();
            	if(subClass.contains("baby")){
            		z.setBaby(true);
            	}
            	if(subClass.contains("villager")){
            		z.setVillager(true);
            	}
            }
            if (((BukkitMCEntity)e).asEntity() instanceof Skeleton){
            	Skeleton sk = (Skeleton) ((BukkitMCEntity)e).asEntity();
            	sk.setSkeletonType(SkeletonType.NORMAL);
            	if("WITHER".equals(subClass.toUpperCase())){
            		sk.setSkeletonType(SkeletonType.WITHER);
            	}
            }
            ids.push(new CInt(e.getEntityId(), t));
        }
        return ids;
    }
}
