

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCChunk;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCFireworkEffect;
import com.laytonsmith.abstraction.MCItem;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLightningStrike;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCEntity;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCFallingBlock;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCFirework;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCHorse;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCItem;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCLightningStrike;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCLivingEntity;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import com.laytonsmith.abstraction.entities.MCFallingBlock;
import com.laytonsmith.abstraction.entities.MCFirework;
import com.laytonsmith.abstraction.entities.MCHorse;
import com.laytonsmith.abstraction.enums.*;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCBiomeType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDifficulty;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDyeColor;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCEntityType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCOcelotType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCProfession;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCSkeletonType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCSound;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCTreeType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCWorldEnvironment;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCWorldType;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 */
public class BukkitMCWorld extends BukkitMCMetadatable implements MCWorld {

    World w;

    public BukkitMCWorld(World w) {
		super(w);
        this.w = w;
    }

	@Override
	public boolean equals(Object o) {
		return o instanceof MCWorld && this.w.equals(((BukkitMCWorld) o).w);
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

	@Override
    public World getHandle(){
        return w;
    }

    public World __World() {
        return w;
    }

	@Override
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

	@Override
	public List<MCEntity> getEntities() {
		if (w.getEntities() == null) {
			return null;
		}
		List<MCEntity> list = new ArrayList<MCEntity>();
		for (Entity e : w.getEntities()) {
			list.add(new BukkitMCEntity(e));
		}
		return list;
	}

	@Override
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

	@Override
    public String getName() {
        return w.getName();
    }

	@Override
	public long getSeed() {
		return w.getSeed();
	}

	@Override
	public MCDifficulty getDifficulty() {
		return BukkitMCDifficulty.getConvertor().getAbstractedEnum(w.getDifficulty());
	}

	@Override
	public void setDifficulty(MCDifficulty difficulty) {
		w.setDifficulty(BukkitMCDifficulty.getConvertor().getConcreteEnum(difficulty));
	}

	@Override
	public boolean getPVP() {
		return w.getPVP();
	}

	@Override
	public void setPVP(boolean pvp) {
		w.setPVP(pvp);
	}

	@Override
	public boolean getGameRuleValue(MCGameRule gameRule) {
		return Boolean.valueOf(w.getGameRuleValue(gameRule.getGameRule()));
	}

	@Override
	public void setGameRuleValue(MCGameRule gameRule, boolean value) {
		w.setGameRuleValue(gameRule.getGameRule(), String.valueOf(value));
	}

	@Override
	public MCWorldEnvironment getEnvironment() {
		return BukkitMCWorldEnvironment.getConvertor().getAbstractedEnum(w.getEnvironment());
	}

	@Override
	public String getGenerator() {
		try {
			return w.getGenerator().toString();
		} catch (NullPointerException npe) {
			return "default";
		}
	}

	@Override
	public MCWorldType getWorldType() {
		return BukkitMCWorldType.getConvertor().getAbstractedEnum(w.getWorldType());
	}

	@Override
	public int getSeaLevel() {
		return getHandle().getSeaLevel();
	}

	@Override
	public int getMaxHeight(){
		return getHandle().getMaxHeight();
	}

	@Override
    public MCBlock getBlockAt(int x, int y, int z) {
        if (w.getBlockAt(x, y, z) == null) {
            return null;
        }
        return new BukkitMCBlock(w.getBlockAt(x, y, z));
    }

	@Override
    public MCEntity spawn(MCLocation l, Class mobType) {
        return BukkitConvertor.BukkitGetCorrectEntity(w.spawn(((BukkitMCLocation) l).l, mobType));
    }

	@Override
	public MCEntity spawn(MCLocation l, MCEntityType entType) {
		return BukkitConvertor.BukkitGetCorrectEntity(w.spawnEntity(
				((BukkitMCLocation) l).asLocation(),
				((BukkitMCEntityType) entType).getConcrete()));
	}

	@Override
	public MCEntity spawn(MCLocation l, MCEntityType.MCVanillaEntityType entityType) {
		return BukkitConvertor.BukkitGetCorrectEntity(w.spawnEntity(
				((BukkitMCLocation) l).asLocation(),
				(EntityType) MCEntityType.valueOfVanillaType(entityType).getConcrete()));
	}

	@Override
	public boolean generateTree(MCLocation l, MCTreeType treeType) {
		return w.generateTree(((BukkitMCLocation) l).asLocation(), BukkitMCTreeType.getConvertor().getConcreteEnum(treeType));
	}

	@Override
	public void playEffect(MCLocation l, MCEffect mCEffect, int data, int radius) {
		w.playEffect(((BukkitMCLocation) l).l, Effect.valueOf(mCEffect.name()), data, radius);
	}

	@Override
	public void playSound(MCLocation l, MCSound sound, float volume, float pitch) {
		w.playSound(((BukkitMCLocation) l).asLocation(),
				((BukkitMCSound) sound).getConcrete(), volume, pitch);
	}

	@Override
	public void playSound(MCLocation l, String sound, float volume, float pitch) {
		for(Player p: w.getPlayers())
			p.playSound(((BukkitMCLocation)l).asLocation(), sound, volume, pitch);
	}

	@Override
    public MCItem dropItemNaturally(MCLocation l, MCItemStack is) {
        return new BukkitMCItem(w.dropItemNaturally(((BukkitMCLocation) l).l, ((BukkitMCItemStack) is).is));
    }

	@Override
    public MCItem dropItem(MCLocation l, MCItemStack is) {
        return new BukkitMCItem(w.dropItem(((BukkitMCLocation) l).l, ((BukkitMCItemStack) is).is));
    }

	@Override
	public MCLightningStrike strikeLightning(MCLocation GetLocation) {
		return new BukkitMCLightningStrike(
				w.strikeLightning(((BukkitMCLocation) GetLocation).l));
	}

	@Override
	public MCLightningStrike strikeLightningEffect(MCLocation GetLocation) {
		return new BukkitMCLightningStrike(
				w.strikeLightningEffect(((BukkitMCLocation) GetLocation).l));
	}

	@Override
    public void setStorm(boolean b) {
        w.setStorm(b);
    }

	@Override
    public MCLocation getSpawnLocation() {
        return new BukkitMCLocation(w.getSpawnLocation());
    }

	@Override
    public void refreshChunk(int x, int z) {
        w.refreshChunk(x, z);
    }

	@Override
	public void loadChunk(int x, int z) {
 		w.loadChunk(x, z);
 	}

	@Override
 	public void unloadChunk(int x, int z) {
 		w.unloadChunk(x, z);
 	}


	@Override
    public void setTime(long time) {
        w.setTime(time);
    }

	@Override
    public long getTime() {
        return w.getTime();
    }

	@Override
    public MCBiomeType getBiome(int x, int z) {
		return BukkitMCBiomeType.valueOfConcrete(w.getBiome(x, z));
	}

	@Override
    public void setBiome(int x, int z, MCBiomeType type) {
		w.setBiome(x, z, ((BukkitMCBiomeType) type).getConcrete());
	}

	@Override
	public MCBlock getHighestBlockAt(int x, int z) {
		//Workaround for getHighestBlockAt, since it doesn't like transparent
		//blocks.
		Block b = w.getBlockAt(x, w.getMaxHeight() - 1, z);
		while(b.getType() == Material.AIR && b.getY() > 0){
			b = b.getRelative(BlockFace.DOWN);
		}
		return new BukkitMCBlock(b);
	}

	@Override
    public void explosion(double x, double y, double z, float size, boolean safe) {
        w.createExplosion(x, y, z, size, !safe, !safe);
    }

	@Override
    public void setSpawnLocation(int x, int y, int z) {
        w.setSpawnLocation(x, y, z);
    }

	@Override
    public CArray spawnMob(MCMobs name, String subClass, int qty, MCLocation l, Target t) {
        Class mobType = null;
		CArray ids = new CArray(t);
		Location location = (Location) l.getHandle();
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
				case ENDERMITE:
					mobType = Endermite.class;
					break;
				case GUARDIAN:
					mobType = Guardian.class;
					break;
				case RABBIT:
					mobType = Rabbit.class;
					break;
				case SHULKER:
					mobType = Shulker.class;
					break;
				case POLARBEAR:
					mobType = PolarBear.class;
					break;
			}
		} catch (NoClassDefFoundError e) {
			throw new CREFormatException("No mob of type " + name + " exists", t);
		}
		String[] subTypes = subClass.toUpperCase().split("-");
		for (int i = 0; i < qty; i++) {
			Entity e = w.spawn(location, mobType);
			if (name == MCMobs.SPIDERJOCKEY) {
				e.setPassenger(w.spawn(location, Skeleton.class));
			}
			if (!subClass.equals("")) { //if subClass is blank, none of this needs to run at all
				if(e instanceof Sheep){
					Sheep s = (Sheep) e;
					MCDyeColor color = MCDyeColor.WHITE;
					for (String type : subTypes) {
						try {
							color = MCDyeColor.valueOf(type);
							s.setColor(BukkitMCDyeColor.getConvertor().getConcreteEnum(color));
						} catch (IllegalArgumentException ex) {
							throw new CREFormatException(type + " is not a valid color", t);
						}
					}
				} else if(e instanceof Ocelot){
					Ocelot o = (Ocelot) e;
					MCOcelotType otype = MCOcelotType.WILD_OCELOT;
					for (String type : subTypes) {
						try {
							otype = MCOcelotType.valueOf(type);
							o.setCatType(BukkitMCOcelotType.getConvertor().getConcreteEnum(otype));
						} catch (IllegalArgumentException ex){
							throw new CREFormatException(type + " is not an ocelot type", t);
						}
					}
				} else if(e instanceof Creeper){
					Creeper c = (Creeper) e;
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
							throw new CREFormatException(type + " is not a creeper state", t);
						}
					}
				} else if(e instanceof Wolf){
					Wolf w = (Wolf) e;
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
							throw new CREFormatException(type + " is not a wolf state", t);
						}
					}
				} else if(e instanceof Villager){
					Villager v = (Villager) e;
					MCProfession job = MCProfession.FARMER;
					for (String type : subTypes){
						try {
							job = MCProfession.valueOf(type);
							v.setProfession(BukkitMCProfession.getConvertor().getConcreteEnum(job));
						} catch (IllegalArgumentException ex) {
							throw new CREFormatException(type + " is not a valid profession", t);
						}
					}
				} else if(e instanceof Enderman){
					Enderman en = (Enderman) e;
					for (String type : subTypes){
						try {
							MaterialData held = new MaterialData(Material.valueOf(type));
							en.setCarriedMaterial(held);
						} catch (IllegalArgumentException ex) {
							throw new CREFormatException(type + " is not a valid material", t);
						}
					}
				} else if(e instanceof Slime){
					Slime sl = (Slime) e;
					for (String type : subTypes) {
						if(!"".equals(type)){
							try{
								sl.setSize(Integer.parseInt(type));
							} catch (IllegalArgumentException ex){
								throw new CREFormatException(type + " is not a valid size", t);
							}
						}
					}
				} else if(e instanceof Skeleton){
					Skeleton sk = (Skeleton) e;
					MCSkeletonType stype = MCSkeletonType.NORMAL;
					for (String type : subTypes) {
						try {
							stype = MCSkeletonType.valueOf(type);
							sk.setSkeletonType(BukkitMCSkeletonType.getConvertor().getConcreteEnum(stype));
						} catch (IllegalArgumentException ex){
							throw new CREFormatException(type + " is not a skeleton type", t);
						}
					}
				} else if(e instanceof Zombie){
					Zombie z = (Zombie) e;
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
								case VILLAGER_BLACKSMITH:
									if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_9)){
										z.setVillagerProfession(Villager.Profession.BLACKSMITH);
									} else {
										z.setVillager(true);
									}
									break;
								case VILLAGER_BUTCHER:
									if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_9)){
										z.setVillagerProfession(Villager.Profession.BUTCHER);
									} else {
										z.setVillager(true);
									}
									break;
								case VILLAGER_LIBRARIAN:
									if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_9)){
										z.setVillagerProfession(Villager.Profession.LIBRARIAN);
									} else {
										z.setVillager(true);
									}
									break;
								case VILLAGER_PRIEST:
									if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_9)){
										z.setVillagerProfession(Villager.Profession.PRIEST);
									} else {
										z.setVillager(true);
									}
									break;
								case HUSK:
									if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_10)){
										z.setVillagerProfession(Villager.Profession.HUSK);
									}
									break;
							}
						} catch (IllegalArgumentException ex){
							if (z instanceof PigZombie) {
								try {
									((PigZombie) z).setAnger(Integer.valueOf(type));
								} catch (IllegalArgumentException iae) {
									throw new CREFormatException(type + " was neither a zombie state nor a number.", t);
								}
							} else {
								throw new CREFormatException(type + " is not a zombie state", t);
							}
						}
					}
				} else if(e instanceof Pig){
					Pig p = (Pig) e;
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
							throw new CREFormatException(type + " is not a pig state", t);
						}
					}
				} else if(e instanceof Horse) {
					Horse h = (Horse) e;
					for (String type : subTypes) {
						try {
							MCHorse.MCHorseVariant htype = MCHorse.MCHorseVariant.valueOf(type);
							h.setVariant(BukkitMCHorse.BukkitMCHorseVariant.getConvertor().getConcreteEnum(htype));
						} catch (IllegalArgumentException notVar) {
							try {
								MCHorse.MCHorseColor hcolor = MCHorse.MCHorseColor.valueOf(type);
								h.setColor(BukkitMCHorse.BukkitMCHorseColor.getConvertor().getConcreteEnum(hcolor));
							} catch (IllegalArgumentException notColor) {
								try {
									MCHorse.MCHorsePattern hpattern = MCHorse.MCHorsePattern.valueOf(type);
									h.setStyle(BukkitMCHorse.BukkitMCHorsePattern.getConvertor().getConcreteEnum(hpattern));
								} catch (IllegalArgumentException notAnything) {
									throw new CREFormatException("Type " + type + " did not match any horse variants,"
											+ " colors, or patterns.", t);
								}
							}
						}
					}
				}
			}
			ids.push(new CString(e.getUniqueId().toString(), t), t);
		}
        return ids;
    }

	@Override
	public boolean exists() {
		//I dunno how well this will work, but it's worth a shot.
		try{
			w.getName();
			return true;
		} catch(Exception e){
			return false;
		}
	}

	@Override
	public MCFallingBlock spawnFallingBlock(MCLocation loc, int type, byte data) {
		Location mcloc = (Location)((BukkitMCLocation)loc).getHandle();
		return new BukkitMCFallingBlock(w.spawnFallingBlock(mcloc, type, data));
	}

	@Override
	public MCFirework launchFirework(MCLocation l, int strength, List<MCFireworkEffect> effects){
		Firework firework = (Firework) w.spawnEntity(((BukkitMCLocation) l).asLocation(), EntityType.FIREWORK);
		FireworkMeta meta = firework.getFireworkMeta();
		meta.setPower(strength);
		for(MCFireworkEffect effect : effects){
			meta.addEffect((FireworkEffect) effect.getHandle());
		}
		firework.setFireworkMeta(meta);
		return new BukkitMCFirework(firework);
	}

	@Override
	public boolean regenerateChunk(int x, int z) {
		return w.regenerateChunk(x, z);
	}

	@Override
	public MCChunk getChunkAt(int x, int z) {
		return new BukkitMCChunk(w.getChunkAt(x, z));
	}

	@Override
	public MCChunk getChunkAt(MCBlock b) {
		return new BukkitMCChunk(w.getChunkAt(((BukkitMCBlock) b).__Block()));
	}

	@Override
	public MCChunk getChunkAt(MCLocation l) {
		return new BukkitMCChunk(w.getChunkAt(((BukkitMCLocation) l).asLocation()));
	}

	@Override
	public MCChunk[] getLoadedChunks() {
		Chunk[] chunks = w.getLoadedChunks();
		MCChunk[] MCChunks = new MCChunk[chunks.length];
		for (int i = 0; i < chunks.length; i++) {
			MCChunks[i] = new BukkitMCChunk(chunks[i]);
		}
		return MCChunks;
	}

	@Override
	public void setThundering(boolean b) {
		w.setThundering(b);
	}

	@Override
	public void setWeatherDuration(int time) {
		w.setWeatherDuration(time);
	}

	@Override
	public void setThunderDuration(int time) {
		w.setThunderDuration(time);
	}

	@Override
	public boolean isStorming() {
		return w.hasStorm();
	}

	@Override
	public boolean isThundering() {
		return w.isThundering();
	}

	@Override
	public void save(){
		w.save();
	}
}
