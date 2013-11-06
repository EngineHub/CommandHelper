package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.entities.MCItem;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCLightningStrike;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCItem;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import com.laytonsmith.abstraction.entities.MCLightningStrike;
import com.laytonsmith.abstraction.entities.MCPlayer;
import com.laytonsmith.abstraction.entities.MCLivingEntity;
import com.laytonsmith.abstraction.entities.MCEntity;
import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.entities.MCFallingBlock;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCFallingBlock;
import com.laytonsmith.abstraction.enums.MCBiomeType;
import com.laytonsmith.abstraction.enums.MCCreeperType;
import com.laytonsmith.abstraction.enums.MCDifficulty;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.MCEffect;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCGameRule;
import com.laytonsmith.abstraction.enums.MCHorseColor;
import com.laytonsmith.abstraction.enums.MCHorseStyle;
import com.laytonsmith.abstraction.enums.MCHorseVariant;
import com.laytonsmith.abstraction.enums.MCMobs;
import com.laytonsmith.abstraction.enums.MCOcelotType;
import com.laytonsmith.abstraction.enums.MCPigType;
import com.laytonsmith.abstraction.enums.MCProfession;
import com.laytonsmith.abstraction.enums.MCSkeletonType;
import com.laytonsmith.abstraction.enums.MCSound;
import com.laytonsmith.abstraction.enums.MCTreeType;
import com.laytonsmith.abstraction.enums.MCWolfType;
import com.laytonsmith.abstraction.enums.MCWorldEnvironment;
import com.laytonsmith.abstraction.enums.MCWorldType;
import com.laytonsmith.abstraction.enums.MCZombieType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCBiomeType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDifficulty;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDyeColor;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCEntityType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCHorseColor;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCHorseStyle;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCHorseVariant;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCOcelotType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCProfession;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCSkeletonType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCSound;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCTreeType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCWorldEnvironment;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCWorldType;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.material.MaterialData;

/**
 *
 * @author layton
 */
public class BukkitMCWorld implements MCWorld {

    World w;

    public BukkitMCWorld(World w) {
        this.w = w;
    }

	@Override
	public boolean equals(Object o) {
		return o instanceof MCWorld ? this.w.equals(((BukkitMCWorld)o).w) : false;
	}

	@Override
	public int hashCode() {
		return this.w.hashCode();
	}

	@Override
	public String toString() {
		return this.w.toString();
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

	public List<MCPlayer> getPlayers() {
		if (w.getPlayers() == null) {
			return null;
		}
		List<MCPlayer> list = new ArrayList<MCPlayer>();
		for (Player p : w.getPlayers()) {
			list.add(new BukkitMCPlayer(p));
		}
		return list;
	}

	public List<MCEntity> getEntities() {
		if (w.getEntities() == null) {
			return null;
		}
		List<MCEntity> list = new ArrayList<MCEntity>();
		for (Entity e : w.getEntities()) {
			list.add(BukkitConvertor.BukkitGetCorrectEntity(e));
		}
		return list;
	}

    public List<MCLivingEntity> getLivingEntities() {
        if (w.getLivingEntities() == null) {
            return null;
        }
        List<MCLivingEntity> list = new ArrayList<MCLivingEntity>();
        for (LivingEntity e : w.getLivingEntities()) {
            list.add((MCLivingEntity) BukkitConvertor.BukkitGetCorrectEntity(e));
        }
        return list;
    }

    public String getName() {
        return w.getName();
    }

	public long getSeed() {
		return w.getSeed();
	}

	public MCDifficulty getDifficulty() {
		return BukkitMCDifficulty.getConvertor().getAbstractedEnum(w.getDifficulty());
	}

	public void setDifficulty(MCDifficulty difficulty) {
		w.setDifficulty(BukkitMCDifficulty.getConvertor().getConcreteEnum(difficulty));
	}

	public boolean getPVP() {
		return w.getPVP();
	}

	public void setPVP(boolean pvp) {
		w.setPVP(pvp);
	}

	public boolean getGameRuleValue(MCGameRule gameRule) {
		return Boolean.valueOf(w.getGameRuleValue(gameRule.getGameRule()));
	}

	public void setGameRuleValue(MCGameRule gameRule, boolean value) {
		w.setGameRuleValue(gameRule.getGameRule(), String.valueOf(value));
	}

	public MCWorldEnvironment getEnvironment() {
		return BukkitMCWorldEnvironment.getConvertor().getAbstractedEnum(w.getEnvironment());
	}

	public String getGenerator() {
		try {
			return w.getGenerator().toString();
		} catch (NullPointerException npe) {
			return "default";
		}
	}

	public MCWorldType getWorldType() {
		return BukkitMCWorldType.getConvertor().getAbstractedEnum(w.getWorldType());
	}

    public MCBlock getBlockAt(int x, int y, int z) {
        if (w.getBlockAt(x, y, z) == null) {
            return null;
        }
        return new BukkitMCBlock(w.getBlockAt(x, y, z));
    }

    public MCEntity spawn(MCLocation l, Class mobType) {
        return BukkitConvertor.BukkitGetCorrectEntity(w.spawn((Location) l.getHandle(), mobType));
    }

	public MCEntity spawn(MCLocation l, MCEntityType entType) {
		return BukkitConvertor.BukkitGetCorrectEntity(w.spawnEntity(
				(Location) l.getHandle(),
				BukkitMCEntityType.getConvertor().getConcreteEnum(MCEntityType.valueOf(entType.name()))));
	}

	public boolean generateTree(MCLocation l, MCTreeType treeType) {
		return w.generateTree((Location) l.getHandle(), BukkitMCTreeType.getConvertor().getConcreteEnum(treeType));
	}

    public void playEffect(MCLocation l, MCEffect mCEffect, int e, int data) {
        w.playEffect((Location) l.getHandle(), Effect.valueOf(mCEffect.name()), e, data);
    }

	public void playSound(MCLocation l, MCSound sound, float volume, float pitch) {
		w.playSound((Location) l.getHandle(), 
				BukkitMCSound.getConvertor().getConcreteEnum(sound), volume, pitch);
	}

    public MCItem dropItemNaturally(MCLocation l, MCItemStack is) {
        return new BukkitMCItem(w.dropItemNaturally((Location) l.getHandle(), ((BukkitMCItemStack) is).is));
    }

    public MCItem dropItem(MCLocation l, MCItemStack is) {
        return new BukkitMCItem(w.dropItem((Location) l.getHandle(), ((BukkitMCItemStack) is).is));
    }

	public MCLightningStrike strikeLightning(MCLocation l) {
		return new BukkitMCLightningStrike(
				w.strikeLightning((Location) l.getHandle()));
	}

	public MCLightningStrike strikeLightningEffect(MCLocation l) {
		return new BukkitMCLightningStrike(
				w.strikeLightningEffect((Location) l.getHandle()));
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
		//Workaround for getHighestBlockAt, since it doesn't like transparent
		//blocks.
		Block b = w.getBlockAt(x, w.getMaxHeight() - 1, z);
		while(b.getType() == Material.AIR && b.getY() > 0){
			b = b.getRelative(BlockFace.DOWN);
		}
		return new BukkitMCBlock(b);
	}

    public void explosion(double x, double y, double z, float size, boolean safe) {
        w.createExplosion(x, y, z, size, !safe, !safe);
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
					mobType = Giant.class;
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
				case HORSE:
					mobType = Horse.class;
					break;
			}
        } catch (IllegalArgumentException e) {
            throw new ConfigRuntimeException("No mob of type " + name + " exists",
                    ExceptionType.FormatException, t);
        }
        for (int i = 0; i < qty; i++) {
            MCEntity e = l.getWorld().spawn(l, mobType);
            String[] subTypes = subClass.toUpperCase().split("-");
            if (name == MCMobs.SPIDERJOCKEY) {
                e.setPassenger(l.getWorld().spawn(l, Skeleton.class));
            }
			if (!subClass.equals("")) { //if subClass is blank, none of this needs to run at all 
				if ((Entity) e.getHandle() instanceof Sheep) {
					Sheep s = (Sheep) e.getHandle();
					MCDyeColor color;
					for (String type : subTypes) {
						try {
							color = MCDyeColor.valueOf(type);
							s.setColor(BukkitMCDyeColor.getConvertor().getConcreteEnum(color));
						} catch (IllegalArgumentException ex) {
							throw new ConfigRuntimeException(type + " is not a valid color",
									ExceptionType.FormatException, t);
						}
					}
				}
				if((Entity) e.getHandle() instanceof Ocelot){
					Ocelot o = (Ocelot) e.getHandle();
					MCOcelotType otype;
					for (String type : subTypes) {
						try {
							otype = MCOcelotType.valueOf(type);
							o.setCatType(BukkitMCOcelotType.getConvertor().getConcreteEnum(otype));
						} catch (IllegalArgumentException ex){
							throw new ConfigRuntimeException(type + " is not an ocelot type",
									ExceptionType.FormatException, t);					
						}
					}
				}
				if((Entity) e.getHandle() instanceof Creeper){
					Creeper c = (Creeper) e.getHandle();
					for (String type : subTypes) {
						try {
							MCCreeperType ctype = MCCreeperType.valueOf(type);
							switch (ctype) {
							case POWERED:
								c.setPowered(true);
								break;
							default:
								break;
							}
						} catch (IllegalArgumentException ex){
							throw new ConfigRuntimeException(type + " is not a creeper state",
									ExceptionType.FormatException, t);
						}
					}
				}
				if((Entity) e.getHandle() instanceof Wolf){
					Wolf w = (Wolf) e.getHandle();
					for (String type : subTypes) {
						try {
							MCWolfType wtype = MCWolfType.valueOf(type);
							switch (wtype) {
							case ANGRY:
								w.setAngry(true);
								break;
							case TAMED:
								w.setTamed(true);
								break;
							default:
								break;
							}
						} catch (IllegalArgumentException ex){
							throw new ConfigRuntimeException(type + " is not a wolf state",
									ExceptionType.FormatException, t);
						}
					}
				}
				if ((Entity) e.getHandle() instanceof Villager) {
					Villager v = (Villager) e.getHandle();
					MCProfession job;
					for (String type : subTypes){
						try {
							job = MCProfession.valueOf(type);
							v.setProfession(BukkitMCProfession.getConvertor().getConcreteEnum(job));
						} catch (IllegalArgumentException ex) {
							throw new ConfigRuntimeException(type + " is not a valid profession",
									ExceptionType.FormatException, t);
						}
					}
				}
				if ((Entity) e.getHandle() instanceof Enderman) {
					Enderman en = (Enderman) e.getHandle();
					for (String type : subTypes){
						try {
							MaterialData held = new MaterialData(Material.valueOf(type));
							en.setCarriedMaterial(held);
						} catch (IllegalArgumentException ex) {
							throw new ConfigRuntimeException(type + " is not a valid material",
									ExceptionType.FormatException, t);
						}
					}
				}
				if((Entity) e.getHandle() instanceof Slime){
					Slime sl = (Slime) e.getHandle();
					for (String type : subTypes) {
						if(!"".equals(type)){
							try{
								sl.setSize(Integer.parseInt(type));
							} catch (IllegalArgumentException ex){
								throw new ConfigRuntimeException(type + " is not a valid size",
										ExceptionType.FormatException, t);
							}
						}
					}
				}
				if((Entity) e.getHandle() instanceof Skeleton){
					Skeleton sk = (Skeleton) e.getHandle();
					MCSkeletonType stype = MCSkeletonType.NORMAL;
					for (String type : subTypes) {
						try {
							stype = MCSkeletonType.valueOf(type);
							sk.setSkeletonType(BukkitMCSkeletonType.getConvertor().getConcreteEnum(stype));
						} catch (IllegalArgumentException ex){
							throw new ConfigRuntimeException(type + " is not a skeleton type",
									ExceptionType.FormatException, t);					
						}
					}
				}
				if((Entity) e.getHandle() instanceof Zombie){
					Zombie z = (Zombie) e.getHandle();
					for (String type : subTypes) {
						try {
							MCZombieType ztype = MCZombieType.valueOf(type);
							switch (ztype) {
								case BABY:
									z.setBaby(true);
									break;
								case VILLAGER:
									z.setVillager(true);
									break;
							}
						} catch (IllegalArgumentException ex){
							if (z instanceof PigZombie) {
								try {
									((PigZombie) z).setAnger(Integer.valueOf(type));
								} catch (IllegalArgumentException iae) {
									throw new ConfigRuntimeException(type + " was neither a zombie state nor a number.",
											ExceptionType.FormatException, t);
								}
							} else {
								throw new ConfigRuntimeException(type + " is not a zombie state",
										ExceptionType.FormatException, t);
							}
						}
					}
				}
				if((Entity) e.getHandle() instanceof Pig){
					Pig p = (Pig) e.getHandle();
					for (String type : subTypes) {
						try {
							MCPigType ptype = MCPigType.valueOf(type);
							switch (ptype) {
							case SADDLED:
								p.setSaddle(true);
								break;
							default:
								break;
							}
						} catch (IllegalArgumentException ex){
							throw new ConfigRuntimeException(type + " is not a pig state",
									ExceptionType.FormatException, t);
						}
					}
				}
				if((Entity) e.getHandle() instanceof Horse) {
					Horse h = (Horse) e.getHandle();
					for (String type : subTypes) {
						try {
							MCHorseVariant htype = MCHorseVariant.valueOf(type);
							h.setVariant(BukkitMCHorseVariant.getConvertor().getConcreteEnum(htype));
						} catch (IllegalArgumentException notVar) {
							try {
								MCHorseColor hcolor = MCHorseColor.valueOf(type);
								h.setColor(BukkitMCHorseColor.getConvertor().getConcreteEnum(hcolor));
							} catch (IllegalArgumentException notColor) {
								try {
									MCHorseStyle hpattern = MCHorseStyle.valueOf(type);
									h.setStyle(BukkitMCHorseStyle.getConvertor().getConcreteEnum(hpattern));
								} catch (IllegalArgumentException notAnything) {
									throw new ConfigRuntimeException("Type " + type + " did not match any horse variants,"
											+ " colors, or patterns.", ExceptionType.FormatException, t);
								}
							}
						}
					}
				}
			}
            ids.push(new CInt(e.getEntityId(), t));
        }
        return ids;
    }

	public boolean exists() {
		//I dunno how well this will work, but it's worth a shot.
		try{
			w.getName();
			return true;
		} catch(Exception e){
			return false;
		}
	}

	public MCFallingBlock spawnFallingBlock(MCLocation loc, int type, byte data) {
		Location mcloc = (Location)((BukkitMCLocation)loc).getHandle();
		return new BukkitMCFallingBlock(w.spawnFallingBlock(mcloc, type, data));
	}

	public boolean regenerateChunk(int x, int z) {
		return w.regenerateChunk(x, z);
	}

	public MCChunk getChunkAt(int x, int z) {
		return new BukkitMCChunk(w.getChunkAt(x, z));
	}

	public MCChunk getChunkAt(MCBlock b) {
		return new BukkitMCChunk(w.getChunkAt(((BukkitMCBlock) b).__Block()));
	}

	public MCChunk getChunkAt(MCLocation l) {
		return new BukkitMCChunk(w.getChunkAt((Location) l.getHandle()));
	}

	public void setThundering(boolean b) {
		w.setThundering(b);
	}

	public void setWeatherDuration(int time) {
		w.setWeatherDuration(time);
	}

	public void setThunderDuration(int time) {
		w.setThunderDuration(time);
	}

	public boolean isStorming() {
		return w.hasStorm();
	}

	public boolean isThundering() {
		return w.isThundering();
	}
}