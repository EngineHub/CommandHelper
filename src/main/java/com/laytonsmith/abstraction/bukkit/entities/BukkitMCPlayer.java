package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCNote;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCPlayerInventory;
import com.laytonsmith.abstraction.MCScoreboard;
import com.laytonsmith.abstraction.MCWorldBorder;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.blocks.MCSign;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayerInventory;
import com.laytonsmith.abstraction.bukkit.BukkitMCScoreboard;
import com.laytonsmith.abstraction.bukkit.BukkitMCServer;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorldBorder;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;
import com.laytonsmith.abstraction.enums.MCInstrument;
import com.laytonsmith.abstraction.enums.MCParticle;
import com.laytonsmith.abstraction.enums.MCPlayerStatistic;
import com.laytonsmith.abstraction.enums.MCPotionEffectType;
import com.laytonsmith.abstraction.enums.MCSound;
import com.laytonsmith.abstraction.enums.MCSoundCategory;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.abstraction.enums.MCWeather;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCInstrument;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCParticle;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCPlayerStatistic;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCSound;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCSoundCategory;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCWeather;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.Static;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Particle;
import org.bukkit.SoundCategory;
import org.bukkit.WorldBorder;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BukkitMCPlayer extends BukkitMCHumanEntity implements MCPlayer, MCCommandSender, MCOfflinePlayer {

	Player p;

	public BukkitMCPlayer(Entity player) {
		super(player);
		this.p = (Player) player;
	}

	public Player _Player() {
		return p;
	}

	@Override
	public boolean canSee(MCPlayer p) {
		return this.p.canSee(((BukkitMCPlayer) p)._Player());
	}

	@Override
	public void chat(String chat) {
		p.chat(chat);
	}

	@Override
	public InetSocketAddress getAddress() {
		return p.getAddress();
	}

	@Override
	public boolean getAllowFlight() {
		return p.getAllowFlight();
	}

	@Override
	public MCLocation getCompassTarget() {
		return new BukkitMCLocation(p.getCompassTarget());
	}

	@Override
	public String getDisplayName() {
		return p.getDisplayName();
	}

	@Override
	public float getExp() {
		return p.getExp();
	}

	@Override
	public long getFirstPlayed() {
		return p.getFirstPlayed();
	}

	@Override
	public MCPlayerInventory getInventory() {
		return new BukkitMCPlayerInventory(p.getInventory());
	}

	@Override
	public MCItemStack getItemAt(Integer slot) {
		if(slot == null) {
			return new BukkitMCItemStack(p.getInventory().getItemInMainHand());
		}
		ItemStack is = null;
		//Special slots
		if(slot == 100) {
			is = p.getInventory().getBoots();
		} else if(slot == 101) {
			is = p.getInventory().getLeggings();
		} else if(slot == 102) {
			is = p.getInventory().getChestplate();
		} else if(slot == 103) {
			is = p.getInventory().getHelmet();
		} else if(slot == -106) {
			is = p.getInventory().getItemInOffHand();
		}
		if(slot >= 0 && slot <= 35) {
			is = p.getInventory().getItem(slot);
		}
		if(is == null) {
			return null;
		} else {
			return new BukkitMCItemStack(is);
		}
	}

	@Override
	public long getLastPlayed() {
		return p.getLastPlayed();
	}

	@Override
	public int getLevel() {
		return p.getLevel();
	}

	@Override
	public MCPlayer getPlayer() {
		return new BukkitMCPlayer(p);
	}

	@Override
	public long getPlayerTime() {
		return p.getPlayerTime();
	}

	@Override
	public MCWeather getPlayerWeather() {
		return BukkitMCWeather.getConvertor().getAbstractedEnum(p.getPlayerWeather());
	}

	@Override
	public int getRemainingFireTicks() {
		return p.getFireTicks();
	}

	@Override
	public int getTotalExperience() {
		return p.getTotalExperience();
	}

	@Override
	public int getExpToLevel() {
		return p.getExpToLevel();
	}

	@Override
	public int getExpAtLevel() {
		int level = p.getLevel();
		if(level > 30) {
			return (int) (4.5 * Math.pow(level, 2) - 162.5 * level + 2220);
		}
		if(level > 15) {
			return (int) (2.5 * Math.pow(level, 2) - 40.5 * level + 360);
		}
		return (int) (Math.pow(level, 2) + 6 * level);
	}

	@Override
	public void setFlySpeed(float speed) {
		p.setFlySpeed(speed);
	}

	@Override
	public float getFlySpeed() {
		return p.getFlySpeed();
	}

	@Override
	public void setWalkSpeed(float speed) {
		p.setWalkSpeed(speed);
	}

	@Override
	public float getWalkSpeed() {
		return p.getWalkSpeed();
	}

	@Override
	public void giveExp(int xp) {
		p.giveExp(xp);
	}

	@Override
	public boolean hasPlayedBefore() {
		return p.hasPlayedBefore();
	}

	@Override
	public boolean isBanned() {
		return p.isBanned();
	}

	@Override
	public boolean isOnline() {
		return p.isOnline();
	}

	@Override
	public boolean isOp() {
		return p.isOp();
	}

	@Override
	public boolean hasPermission(String perm) {
		return p.hasPermission(perm);
	}

	@Override
	public boolean isPermissionSet(String perm) {
		return p.isPermissionSet(perm);
	}

	@Override
	public List<String> getGroups() {
		// As in https://github.com/sk89q/WorldEdit/blob/master/
		// worldedit-bukkit/src/main/java/com/sk89q/wepif/DinnerPermsResolver.java#L112-L126
		List<String> groupNames = new ArrayList<String>();
		for(PermissionAttachmentInfo permAttach : p.getEffectivePermissions()) {
			String perm = permAttach.getPermission();
			if(!(perm.startsWith(Static.GROUP_PREFIX) && permAttach.getValue())) {
				continue;
			}
			groupNames.add(perm.substring(Static.GROUP_PREFIX.length(), perm.length()));
		}
		return groupNames;
	}

	@Override
	public boolean inGroup(String groupName) {
		return p.hasPermission(Static.GROUP_PREFIX + groupName);
	}

	@Override
	public void setOp(boolean bln) {
		p.setOp(bln);
	}

	@Override
	public boolean isSneaking() {
		return p.isSneaking();
	}

	@Override
	public boolean isSprinting() {
		return p.isSprinting();
	}

	@Override
	public boolean isWhitelisted() {
		return p.isWhitelisted();
	}

	@Override
	public void kickPlayer(String message) {
		p.kickPlayer(message);
	}

	@Override
	public boolean removeEffect(MCPotionEffectType type) {
		PotionEffectType t = (PotionEffectType) type.getConcrete();
		boolean hasIt = false;
		for(PotionEffect pe : p.getActivePotionEffects()) {
			if(pe.getType() == t) {
				hasIt = true;
				break;
			}
		}
		p.removePotionEffect(t);
		return hasIt;
	}

	@Override
	public void resetPlayerTime() {
		p.resetPlayerTime();
	}

	@Override
	public void resetPlayerWeather() {
		p.resetPlayerWeather();
	}

	@Override
	public void sendMessage(String string) {
		//The client doesn't like tabs
		string = string.replaceAll("\t", "    ");
		p.sendMessage(string);
	}

	@Override
	public void sendResourcePack(String url) {
		p.setResourcePack(url);
	}

	@Override
	public void sendTitle(String title, String subtitle, int fadein, int stay, int fadeout) {
		if(title == null || title.isEmpty()) {
			// If the title is null or empty the subtitle won't be displayed. This is unintuitive.
			title = " ";
		}
		p.sendTitle(title, subtitle, fadein, stay, fadeout);
	}

	@Override
	public void setAllowFlight(boolean flight) {
		p.setAllowFlight(flight);
	}

	@Override
	public void setCompassTarget(MCLocation l) {
		p.setCompassTarget(((BukkitMCLocation) l)._Location());
	}

	@Override
	public void setDisplayName(String name) {
		p.setDisplayName(name);
	}

	@Override
	public void setExp(float i) {
		p.setExp(i);
	}

	@Override
	public void setFlying(boolean flight) {
		p.setFlying(flight);
	}

	@Override
	public void setLevel(int xp) {
		p.setLevel(xp);
	}

	@Override
	public void setPlayerTime(Long time, boolean relative) {
		p.setPlayerTime(time, relative);
	}

	@Override
	public void setPlayerWeather(MCWeather type) {
		p.setPlayerWeather(BukkitMCWeather.getConvertor().getConcreteEnum(type));
	}

	@Override
	public void setRemainingFireTicks(int i) {
		p.setFireTicks(i);
	}

	@Override
	public void setSpectatorTarget(MCEntity entity) {
		if(entity == null) {
			p.setSpectatorTarget(null);
			return;
		}
		p.setSpectatorTarget((Entity) entity.getHandle());
	}

	@Override
	public MCEntity getSpectatorTarget() {
		return BukkitConvertor.BukkitGetCorrectEntity(p.getSpectatorTarget());
	}

	private static Class gameProfileClass = null;
	private static Class opListEntryClass = null;
	private static Map<String, Object> opMap = null;

	private static void SetupTempOp() throws ClassNotFoundException {
		if(gameProfileClass != null) {
			return;
		}
		boolean isPaper = ((BukkitMCServer) Static.getServer()).isPaper();
		// Get some version specific mappings
		String nms = "net.minecraft.server";
		String playersPackage = nms + ".players";
		String ops = "p";
		String getPlayerList = isPaper ? "getPlayerList" : "ag";
		MCVersion mcversion = Static.getServer().getMinecraftVersion();
		if(mcversion.lt(MCVersion.MC1_21_3)) {
			getPlayerList = isPaper ? "getPlayerList" : "ah";
			if(mcversion.lt(MCVersion.MC1_20_6)) {
				getPlayerList = "ae";
				if(mcversion.lt(MCVersion.MC1_20_4)) {
					getPlayerList = "ac";
					if(mcversion.lt(MCVersion.MC1_20_2)) {
						ops = "o";
						if(mcversion.equals(MCVersion.MC1_19_3)) {
							getPlayerList = "ab";
						} else if(mcversion.lt(MCVersion.MC1_19_1)) {
							ops = "n";
							if(mcversion.lt(MCVersion.MC1_18)) {
								getPlayerList = "getPlayerList";
								if(mcversion.lt(MCVersion.MC1_17)) {
									String version = ((BukkitMCServer) Static.getServer()).getCraftBukkitPackage().split("\\.")[3];
									nms = "net.minecraft.server." + version;
									playersPackage = nms;
									ops = "operators";
								}
							}
						}
					}
				}
			}
		}

		Class nmsMinecraftServerClass = Class.forName(nms + ".MinecraftServer");
		/*n.m.s.MinecraftServer*/ Object nmsServer = ReflectionUtils.invokeMethod(nmsMinecraftServerClass, null, "getServer");
		/*n.m.s.players.PlayerList*/ Object nmsPlayerList = ReflectionUtils.invokeMethod(nmsServer, getPlayerList);
		/*n.m.s.players.OpList*/ Object opSet = ReflectionUtils.get(Class.forName(playersPackage + ".PlayerList"), nmsPlayerList, ops);
		//opSet.getClass().getSuperclass() == n.m.s.players.JsonList
		/*Map<String, n.m.s.players.OpListEntry>*/ opMap = (Map) ReflectionUtils.get(opSet.getClass().getSuperclass(), opSet, "d");
		/*n.m.s.players.OpListEntry*/ opListEntryClass = Class.forName(playersPackage + ".OpListEntry");
		/*com.mojang.authlib.GameProfile*/ gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
	}

	@Override
	public void setTempOp(Boolean value) throws ClassNotFoundException {
		SetupTempOp();
		if(value) {
			Object gameProfile = ReflectionUtils.invokeMethod(p, "getProfile");
			Object opListEntry = ReflectionUtils.newInstance(opListEntryClass,
					new Class[]{gameProfileClass, int.class, boolean.class},
					new Object[]{gameProfile, 4, false});
			opMap.put(p.getUniqueId().toString(), opListEntry);
		} else {
			opMap.remove(p.getUniqueId().toString());
		}
		p.recalculatePermissions();
	}

	@Override
	public void setTotalExperience(int total) {
		p.setTotalExperience(total);
	}

	@Override
	public void setVanished(boolean set, MCPlayer to) {
		if(!set) {
			p.showPlayer(CommandHelperPlugin.self, ((BukkitMCPlayer) to)._Player());
		} else {
			p.hidePlayer(CommandHelperPlugin.self, ((BukkitMCPlayer) to)._Player());
		}
	}

	@Override
	public void hideEntity(MCEntity entity) {
		try {
			p.hideEntity(CommandHelperPlugin.self, (Entity) entity.getHandle());
		} catch(NoSuchMethodError ex) {
			// probably before 1.18
		}
	}

	@Override
	public void showEntity(MCEntity entity) {
		try {
			p.showEntity(CommandHelperPlugin.self, (Entity) entity.getHandle());
		} catch(NoSuchMethodError ex) {
			// probably before 1.18
		}
	}

	@Override
	public boolean canSeeEntity(MCEntity entity) {
		try {
			return p.canSee((Entity) entity.getHandle());
		} catch(NoSuchMethodError ex) {
			// probably before 1.18
			return true;
		}
	}

	@Override
	public void setWhitelisted(boolean value) {
		p.setWhitelisted(value);
	}

	@Override
	public void setPlayerListName(String listName) {
		p.setPlayerListName(listName);
	}

	@Override
	public String getPlayerListName() {
		return p.getPlayerListName();
	}

	@Override
	public void setPlayerListHeader(String header) {
		p.setPlayerListHeader(header);
	}

	@Override
	public String getPlayerListHeader() {
		return p.getPlayerListHeader();
	}

	@Override
	public void setPlayerListFooter(String footer) {
		p.setPlayerListFooter(footer);
	}

	@Override
	public String getPlayerListFooter() {
		return p.getPlayerListFooter();
	}

	@Override
	public boolean isNewPlayer() {
		//Note the reversed logic here. If they have NOT played before, they are
		//a new player.
		return !p.hasPlayedBefore();
	}

	@Override
	public String getHost() {
		return Static.GetHost(this);
	}

	@Override
	public void sendBlockChange(MCLocation loc, MCBlockData data) {
		p.sendBlockChange(((Location) loc.getHandle()), (BlockData) data.getHandle());
	}

	@Override
	public void sendBlockDamage(MCLocation loc, float progress, MCEntity entity) {
		Location location = (Location) loc.getHandle();
		try {
			if(entity == null) {
				// Using a block position hashCode as the sourceId allows independent control of each block's state.
				int sourceId = (location.getBlockY() + location.getBlockZ() * 31) * 31 + location.getBlockX();
				p.sendBlockDamage(location, progress, sourceId);
			} else {
				p.sendBlockDamage(location, progress, ((Entity) entity.getHandle()).getEntityId());
			}
		} catch (NoSuchMethodError er) {
			// probably prior to 1.19.2 on Paper or 1.19.4 on Spigot
			p.sendBlockDamage(location, progress);
		}
	}

	@Override
	public void sendSignTextChange(MCLocation loc, String[] lines) {
		p.sendSignChange(((Location) loc.getHandle()), lines);
	}

	@Override
	public void sendSignTextChange(MCSign sign) {
		Sign s = (Sign) sign.getHandle();
		try {
			p.sendBlockUpdate(s.getLocation(), s);
		} catch (NoSuchMethodError noBlockUpdate) {
			// probably before 1.20.1
			try {
				p.sendSignChange(s.getLocation(), s.getLines(), s.getColor(), s.isGlowingText());
			} catch (NoSuchMethodError noGlowingText) {
				// probably before 1.17.1
				p.sendSignChange(s.getLocation(), s.getLines(), s.getColor());
			}
		}
	}

	@Override
	public void playNote(MCLocation loc, MCInstrument instrument, MCNote note) {
		p.playNote((Location) loc.getHandle(), BukkitMCInstrument.getConvertor().getConcreteEnum(instrument), (Note) note.getHandle());
	}

	@Override
	public void playSound(MCLocation l, MCSound sound, MCSoundCategory category, float volume, float pitch, Long seed) {
		SoundCategory cat = BukkitMCSoundCategory.getConvertor().getConcreteEnum(category);
		if(cat == null) {
			cat = SoundCategory.MASTER;
		}
		if(seed == null) {
			p.playSound((Location) l.getHandle(), ((BukkitMCSound) sound).getConcrete(), cat, volume, pitch);
		} else {
			p.playSound((Location) l.getHandle(), ((BukkitMCSound) sound).getConcrete(), cat, volume, pitch, seed);
		}
	}

	@Override
	public void playSound(MCEntity ent, MCSound sound, MCSoundCategory category, float volume, float pitch, Long seed) {
		SoundCategory cat = BukkitMCSoundCategory.getConvertor().getConcreteEnum(category);
		if(cat == null) {
			cat = SoundCategory.MASTER;
		}
		if(category == null) {
			p.playSound((Entity) ent.getHandle(), ((BukkitMCSound) sound).getConcrete(), cat, volume, pitch);
		} else {
			p.playSound((Entity) ent.getHandle(), ((BukkitMCSound) sound).getConcrete(), cat, volume, pitch, seed);
		}
	}

	@Override
	public void playSound(MCLocation l, String sound, MCSoundCategory category, float volume, float pitch, Long seed) {
		SoundCategory cat = BukkitMCSoundCategory.getConvertor().getConcreteEnum(category);
		if(cat == null) {
			cat = SoundCategory.MASTER;
		}
		if(seed == null) {
			p.playSound((Location) l.getHandle(), sound, cat, volume, pitch);
		} else {
			p.playSound((Location) l.getHandle(), sound, cat, volume, pitch, seed);
		}
	}

	@Override
	public void playSound(MCEntity ent, String sound, MCSoundCategory category, float volume, float pitch, Long seed) {
		SoundCategory cat = BukkitMCSoundCategory.getConvertor().getConcreteEnum(category);
		if(cat == null) {
			cat = SoundCategory.MASTER;
		}
		if(seed == null) {
			p.playSound((Entity) ent.getHandle(), sound, cat, volume, pitch);
		} else {
			p.playSound((Entity) ent.getHandle(), sound, cat, volume, pitch, seed);
		}
	}

	@Override
	public void stopSound(MCSound sound, MCSoundCategory category) {
		p.stopSound(((BukkitMCSound) sound).getConcrete(),
				BukkitMCSoundCategory.getConvertor().getConcreteEnum(category));
	}

	@Override
	public void stopSound(String sound, MCSoundCategory category) {
		p.stopSound(sound, BukkitMCSoundCategory.getConvertor().getConcreteEnum(category));
	}

	@Override
	public void stopSound(MCSoundCategory category) {
		try {
			p.stopSound(BukkitMCSoundCategory.getConvertor().getConcreteEnum(category));
		} catch (NoSuchMethodError ex) {
			// probably before 1.19.0
		}
	}

	@Override
	public void spawnParticle(MCLocation l, MCParticle pa, int count, double offsetX, double offsetY, double offsetZ, double velocity, Object data) {
		p.spawnParticle((Particle) pa.getConcrete(), (Location) l.getHandle(), count, offsetX, offsetY, offsetZ,
				velocity, ((BukkitMCParticle) pa).getParticleData(l, data));
	}

	@Override
	public int getFoodLevel() {
		return p.getFoodLevel();
	}

	@Override
	public void setFoodLevel(int f) {
		p.setFoodLevel(f);
	}

	@Override
	public float getSaturation() {
		return p.getSaturation();
	}

	@Override
	public void setSaturation(float s) {
		p.setSaturation(s);
	}

	@Override
	public float getExhaustion() {
		return p.getExhaustion();
	}

	@Override
	public void setExhaustion(float e) {
		p.setExhaustion(e);
	}

	@Override
	public MCLocation getBedSpawnLocation() {
		Location loc = p.getBedSpawnLocation();
		if(loc == null) {
			return null;
		}
		return new BukkitMCLocation(loc);
	}

	@Override
	public UUID getUniqueID() {
		return p.getUniqueId();
	}

	@Override
	public void setBedSpawnLocation(MCLocation l, boolean forced) {
		p.setBedSpawnLocation((Location) l.getHandle(), forced);
	}

	@Override
	public MCEntity getVehicle() {
		return new BukkitMCEntity(p.getVehicle());
	}

	@Override
	public void sendPluginMessage(String channel, byte[] message) {
		StaticLayer.GetConvertor().GetPluginMeta().openOutgoingChannel(channel);
		p.sendPluginMessage(CommandHelperPlugin.self, channel, message);
	}

	@Override
	public boolean isFlying() {
		return p.isFlying();
	}

	@Override
	public void updateInventory() {
		p.updateInventory();
	}

	@Override
	public int getStatistic(MCPlayerStatistic stat) {
		return p.getStatistic(BukkitMCPlayerStatistic.getConvertor().getConcreteEnum(stat));
	}

	@Override
	public int getStatistic(MCPlayerStatistic stat, MCEntityType type) {
		EntityType bukkitType = (EntityType) type.getConcrete();
		return p.getStatistic(BukkitMCPlayerStatistic.getConvertor().getConcreteEnum(stat), bukkitType);
	}

	@Override
	public int getStatistic(MCPlayerStatistic stat, MCMaterial type) {
		Material bukkitType = (Material) type.getHandle();
		return p.getStatistic(BukkitMCPlayerStatistic.getConvertor().getConcreteEnum(stat), bukkitType);
	}

	@Override
	public void setStatistic(MCPlayerStatistic stat, int amount) {
		p.setStatistic(BukkitMCPlayerStatistic.getConvertor().getConcreteEnum(stat), amount);
	}

	@Override
	public void setStatistic(MCPlayerStatistic stat, MCEntityType type, int amount) {
		EntityType bukkitType = (EntityType) type.getConcrete();
		p.setStatistic(BukkitMCPlayerStatistic.getConvertor().getConcreteEnum(stat), bukkitType, amount);
	}

	@Override
	public void setStatistic(MCPlayerStatistic stat, MCMaterial type, int amount) {
		Material bukkitType = (Material) type.getHandle();
		p.setStatistic(BukkitMCPlayerStatistic.getConvertor().getConcreteEnum(stat), bukkitType, amount);
	}

	@Override
	public MCWorldBorder getWorldBorder() {
		try {
			WorldBorder wb = p.getWorldBorder();
			if(wb == null) {
				return null;
			}
			return new BukkitMCWorldBorder(wb);
		} catch (NoSuchMethodError ex) {
			// probably before 1.18.2
			return null;
		}
	}

	@Override
	public void setWorldBorder(MCWorldBorder border) {
		try {
			if(border == null) {
				p.setWorldBorder(null);
			} else {
				p.setWorldBorder((WorldBorder) border.getHandle());
			}
		} catch (NoSuchMethodError ex) {
			// probably before 1.18.2
		}
	}

	@Override
	public String getLocale() {
		return p.getLocale();
	}

	@Override
	public MCScoreboard getScoreboard() {
		return new BukkitMCScoreboard(p.getScoreboard());
	}

	@Override
	public void setScoreboard(MCScoreboard board) {
		p.setScoreboard(((BukkitMCScoreboard) board)._scoreboard());
	}

	@Override
	public void respawn() {
		p.spigot().respawn();
	}

	@Override
	public void sendEquipmentChange(MCLivingEntity entity, MCEquipmentSlot slot, MCItemStack item) {
		LivingEntity le = (LivingEntity) entity.getHandle();
		ItemStack is;
		if(item == null) {
			if(Static.getServer().getMinecraftVersion().lt(MCVersion.MC1_19_3)) {
				// null isn't supported prior to 1.19.3
				is = new ItemStack(Material.AIR);
			} else {
				is = null;
			}
		} else {
			is = (ItemStack) item.getHandle();
		}
		try {
			switch(slot) {
				case WEAPON -> p.sendEquipmentChange(le, EquipmentSlot.HAND, is);
				case OFF_HAND -> p.sendEquipmentChange(le, EquipmentSlot.OFF_HAND, is);
				case BOOTS -> p.sendEquipmentChange(le, EquipmentSlot.FEET, is);
				case LEGGINGS -> p.sendEquipmentChange(le, EquipmentSlot.LEGS, is);
				case CHESTPLATE -> p.sendEquipmentChange(le, EquipmentSlot.CHEST, is);
				case HELMET -> p.sendEquipmentChange(le, EquipmentSlot.HEAD, is);
			}
		} catch(NoSuchMethodError ex) {
			// probably before 1.18, which is unsupported
		}
	}

	@Override
	public int getPing() {
		return p.getPing();
	}
}
