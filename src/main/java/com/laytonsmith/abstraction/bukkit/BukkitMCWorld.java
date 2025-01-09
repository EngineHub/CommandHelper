package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCChunk;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCFireworkEffect;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.entities.MCItem;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.entities.MCLightningStrike;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.MCWorldBorder;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCEntity;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCFallingBlock;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCFirework;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCItem;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCLightningStrike;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCLivingEntity;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import com.laytonsmith.abstraction.entities.MCFallingBlock;
import com.laytonsmith.abstraction.entities.MCFirework;
import com.laytonsmith.abstraction.enums.MCBiomeType;
import com.laytonsmith.abstraction.enums.MCDifficulty;
import com.laytonsmith.abstraction.enums.MCEffect;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCGameRule;
import com.laytonsmith.abstraction.enums.MCParticle;
import com.laytonsmith.abstraction.enums.MCSound;
import com.laytonsmith.abstraction.enums.MCSoundCategory;
import com.laytonsmith.abstraction.enums.MCTreeType;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.abstraction.enums.MCWorldEnvironment;
import com.laytonsmith.abstraction.enums.MCWorldType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCBiomeType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDifficulty;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCEntityType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCParticle;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCSound;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCSoundCategory;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCTreeType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCWorldEnvironment;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCWorldType;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CREPluginInternalException;
import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.GameRule;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Consumer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

	public BukkitMCWorld(AbstractionObject a) {
		this((World) null);
		if(a instanceof MCWorld) {
			this.w = ((World) a.getHandle());
		} else {
			throw new ClassCastException();
		}
	}

	@Override
	public World getHandle() {
		return w;
	}

	public World __World() {
		return w;
	}

	@Override
	public List<MCPlayer> getPlayers() {
		List<MCPlayer> list = new ArrayList<>();
		for(Player p : w.getPlayers()) {
			list.add(new BukkitMCPlayer(p));
		}
		return list;
	}

	@Override
	public List<MCEntity> getEntities() {
		List<MCEntity> list = new ArrayList<>();
		for(Entity e : w.getEntities()) {
			list.add(new BukkitMCEntity(e));
		}
		return list;
	}

	@Override
	public List<MCLivingEntity> getLivingEntities() {
		List<MCLivingEntity> list = new ArrayList<>();
		for(LivingEntity e : w.getLivingEntities()) {
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
	public String[] getGameRules() {
		return w.getGameRules();
	}

	@Override
	public Object getGameRuleValue(MCGameRule gameRule) {
		GameRule gr = GameRule.getByName(gameRule.getRuleName());
		if(gr == null) {
			return null;
		}
		return w.getGameRuleValue(gr);
	}

	@Override
	public boolean setGameRuleValue(MCGameRule gameRule, Object value) {
		GameRule gr = GameRule.getByName(gameRule.getRuleName());
		if(gr == null) {
			return false;
		}
		return w.setGameRule(gr, value);
	}

	@Override
	public MCWorldBorder getWorldBorder() {
		return new BukkitMCWorldBorder(w.getWorldBorder());
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
	public int getMaxHeight() {
		return getHandle().getMaxHeight();
	}

	@Override
	public MCBlock getBlockAt(int x, int y, int z) {
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
	public MCEntity spawn(MCLocation l, MCEntityType entType, final CClosure closure) {
		Location location = (Location) l.getHandle();
		Class<? extends Entity> entityClass = ((EntityType) entType.getConcrete()).getEntityClass();
		Entity entity;
		if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_2)) {
			entity = w.spawn(location, entityClass, e -> beforeSpawn(e, closure));
		} else {
			try {
				Method m = w.getClass().getMethod("spawn", Location.class, Class.class, Consumer.class);
				entity = (Entity) m.invoke(w, location, entityClass, (Consumer<Entity>) e -> beforeSpawn(e, closure));
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				throw new CREPluginInternalException(e.getMessage(), Target.UNKNOWN, e);
			}
		}
		return BukkitConvertor.BukkitGetCorrectEntity(entity);
	}

	private void beforeSpawn(Entity entity, CClosure closure) {
		MCEntity temp = BukkitConvertor.BukkitGetCorrectEntity(entity);
		Static.InjectEntity(temp);
		try {
			closure.executeCallable(new CString(entity.getUniqueId().toString(), Target.UNKNOWN));
		} finally {
			Static.UninjectEntity(temp);
		}
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
	public void playEffect(MCLocation l, MCEffect mcEffect, Object data, int radius) {
		Effect effect = Effect.valueOf(mcEffect.name());
		switch(mcEffect) {
			case RECORD_PLAY:
			case STEP_SOUND:
				w.playEffect((Location) l.getHandle(), effect, ((MCMaterial) data).getHandle(), radius);
				return;
			case SMOKE:
			case SHOOT_WHITE_SMOKE:
				w.playEffect((Location) l.getHandle(), effect, BlockFace.valueOf(((MCBlockFace) data).name()), radius);
				return;
			case PARTICLES_AND_SOUND_BRUSH_BLOCK_COMPLETE:
				w.playEffect((Location) l.getHandle(), effect, ((MCBlockData) data).getHandle(), radius);
				return;
		}
		w.playEffect((Location) l.getHandle(), effect, data, radius);
	}

	@Override
	public void spawnParticle(MCLocation l, MCParticle pa, int count, double offsetX, double offsetY, double offsetZ, double velocity, Object data) {
		w.spawnParticle((Particle) pa.getConcrete(), (Location) l.getHandle(), count, offsetX, offsetY, offsetZ,
				velocity, ((BukkitMCParticle) pa).getParticleData(l, data));
	}

	@Override
	public void playSound(MCLocation l, MCSound sound, MCSoundCategory category, float volume, float pitch, Long seed) {
		SoundCategory cat = BukkitMCSoundCategory.getConvertor().getConcreteEnum(category);
		if(cat == null) {
			cat = SoundCategory.MASTER;
		}
		if(seed == null) {
			w.playSound((Location) l.getHandle(), ((BukkitMCSound) sound).getConcrete(), cat, volume, pitch);
		} else {
			w.playSound((Location) l.getHandle(), ((BukkitMCSound) sound).getConcrete(), cat, volume, pitch, seed);
		}
	}

	@Override
	public void playSound(MCEntity ent, MCSound sound, MCSoundCategory category, float volume, float pitch, Long seed) {
		SoundCategory cat = BukkitMCSoundCategory.getConvertor().getConcreteEnum(category);
		if(cat == null) {
			cat = SoundCategory.MASTER;
		}
		if(seed == null) {
			w.playSound((Entity) ent.getHandle(), ((BukkitMCSound) sound).getConcrete(), cat, volume, pitch);
		} else {
			w.playSound((Entity) ent.getHandle(), ((BukkitMCSound) sound).getConcrete(), cat, volume, pitch, seed);
		}
	}

	@Override
	public void playSound(MCEntity ent, String sound, MCSoundCategory category, float volume, float pitch, Long seed) {
		SoundCategory cat = BukkitMCSoundCategory.getConvertor().getConcreteEnum(category);
		if(cat == null) {
			cat = SoundCategory.MASTER;
		}
		if(seed == null) {
			w.playSound((Entity) ent.getHandle(), sound, cat, volume, pitch);
		} else {
			w.playSound((Entity) ent.getHandle(), sound, cat, volume, pitch, seed);
		}
	}

	@Override
	public void playSound(MCLocation l, String sound, MCSoundCategory category, float volume, float pitch, Long seed) {
		SoundCategory cat = BukkitMCSoundCategory.getConvertor().getConcreteEnum(category);
		if(cat == null) {
			cat = SoundCategory.MASTER;
		}
		if(seed == null) {
			w.playSound((Location) l.getHandle(), sound, cat, volume, pitch);
		} else {
			w.playSound((Location) l.getHandle(), sound, cat, volume, pitch, seed);
		}
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
	public MCLightningStrike strikeLightning(MCLocation location) {
		return new BukkitMCLightningStrike(
				w.strikeLightning(((BukkitMCLocation) location).l));
	}

	@Override
	public MCLightningStrike strikeLightningEffect(MCLocation location) {
		return new BukkitMCLightningStrike(
				w.strikeLightningEffect(((BukkitMCLocation) location).l));
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
		// deprecated in 1.8 due to inconsistency
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
	public boolean isChunkForceLoaded(int x, int z) {
		return w.isChunkForceLoaded(x, z);
	}

	@Override
	public void setChunkForceLoaded(int x, int z, boolean forced) {
		w.setChunkForceLoaded(x, z, forced);
	}

	@Override
	public MCChunk[] getForceLoadedChunks() {
		Collection<Chunk> chunks = w.getForceLoadedChunks();
		MCChunk[] mcChunks = new MCChunk[chunks.size()];
		int i = 0;
		for(Chunk c : chunks) {
			mcChunks[i++] = new BukkitMCChunk(c);
		}
		return mcChunks;
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
	public void setFullTime(long time) {
		w.setFullTime(time);
	}

	@Override
	public long getFullTime() {
		return w.getFullTime();
	}

	@Override
	public MCBiomeType getBiome(MCLocation location) {
		return BukkitMCBiomeType.valueOfConcrete(w.getBiome((Location) location.getHandle()));
	}

	@Override
	public void setBiome(int x, int z, MCBiomeType type) {
		w.setBiome(x, z, ((BukkitMCBiomeType) type).getConcrete());
	}

	@Override
	public void setBiome(MCLocation location, MCBiomeType type) {
		w.setBiome(((Location) location.getHandle()), ((BukkitMCBiomeType) type).getConcrete());
	}

	@Override
	public MCBlock getHighestBlockAt(int x, int z) {
		return new BukkitMCBlock(w.getHighestBlockAt(x, z, HeightMap.WORLD_SURFACE));
	}

	@Override
	public boolean explosion(MCLocation location, float size, boolean safe, boolean fire, MCEntity source) {
		Location loc = (Location) location.getHandle();
		Entity src = null;
		if(source != null) {
			src = (Entity) source.getHandle();
		}
		return w.createExplosion(loc, size, fire, !safe, src);
	}

	@Override
	public void setSpawnLocation(int x, int y, int z) {
		w.setSpawnLocation(x, y, z);
	}

	@Override
	public boolean exists() {
		//I dunno how well this will work, but it's worth a shot.
		try {
			w.getName();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean isAutoSave() {
		return w.isAutoSave();
	}

	@Override
	public void setAutoSave(boolean autoSave) {
		w.setAutoSave(autoSave);
	}

	@Override
	public MCFallingBlock spawnFallingBlock(MCLocation loc, MCBlockData data) {
		return new BukkitMCFallingBlock(w.spawnFallingBlock((Location) loc.getHandle(), (BlockData) data.getHandle()));
	}

	@Override
	public MCFirework launchFirework(MCLocation l, int strength, List<MCFireworkEffect> effects) {
		Firework firework = w.spawn(((BukkitMCLocation) l).asLocation(), Firework.class);
		FireworkMeta meta = firework.getFireworkMeta();
		meta.setPower(Math.max(strength, 0));
		for(MCFireworkEffect effect : effects) {
			meta.addEffect((FireworkEffect) effect.getHandle());
		}
		firework.setFireworkMeta(meta);
		if(strength < 0) {
			firework.detonate();
		}
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
		MCChunk[] mcChunks = new MCChunk[chunks.length];
		for(int i = 0; i < chunks.length; i++) {
			mcChunks[i] = new BukkitMCChunk(chunks[i]);
		}
		return mcChunks;
	}

	@Override
	public boolean isChunkLoaded(int x, int z) {
		return w.isChunkLoaded(x, z);
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
	public void setClearWeatherDuration(int time) {
		w.setClearWeatherDuration(time);
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
	public void save() {
		w.save();
	}

	@Override
	public void setKeepSpawnInMemory(boolean keepLoaded) {
		w.setKeepSpawnInMemory(keepLoaded);
	}
}
