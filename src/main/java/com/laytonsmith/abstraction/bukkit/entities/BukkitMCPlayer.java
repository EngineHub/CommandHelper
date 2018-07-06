package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCNote;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCPlayerInventory;
import com.laytonsmith.abstraction.MCScoreboard;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayerInventory;
import com.laytonsmith.abstraction.bukkit.BukkitMCScoreboard;
import com.laytonsmith.abstraction.enums.MCInstrument;
import com.laytonsmith.abstraction.enums.MCParticle;
import com.laytonsmith.abstraction.enums.MCSound;
import com.laytonsmith.abstraction.enums.MCSoundCategory;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.abstraction.enums.MCWeather;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCInstrument;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCSound;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCSoundCategory;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCWeather;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.Static;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Note;
import org.bukkit.Particle;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.InvocationTargetException;
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
		if(p == null || p.getInventory() == null) {
			return null;
		}
		return new BukkitMCPlayerInventory(p.getInventory());
	}

	@Override
	public MCItemStack getItemAt(Integer slot) {
		if(slot == null) {
			return new BukkitMCItemStack(p.getItemInHand());
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
		if(Static.getServer().getMinecraftVersion().lt(MCVersion.MC1_8)) {
			if(level > 30) {
				return (int) (3.5 * Math.pow(level, 2) - 151.5 * level + 2220);
			}
			if(level > 15) {
				return (int) (1.5 * Math.pow(level, 2) - 29.5 * level + 360);
			}
			return 17 * level;
		} else {
			if(level > 30) {
				return (int) (4.5 * Math.pow(level, 2) - 162.5 * level + 2220);
			}
			if(level > 15) {
				return (int) (2.5 * Math.pow(level, 2) - 40.5 * level + 360);
			}
			return (int) (Math.pow(level, 2) + 6 * level);
		}
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
			if(!(perm.startsWith(Static.groupPrefix) && permAttach.getValue())) {
				continue;
			}
			groupNames.add(perm.substring(Static.groupPrefix.length(), perm.length()));
		}
		return groupNames;
	}

	@Override
	public boolean inGroup(String groupName) {
		return p.hasPermission(Static.groupPrefix + groupName);
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
	public boolean removeEffect(int potionID) {
		PotionEffectType t = PotionEffectType.getById(potionID);
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
		if(title == null) {
			// If the title is null the subtitle won't be displayed. This is unintuitive.
			title = "";
		}
		try {
			p.sendTitle(title, subtitle, fadein, stay, fadeout);
		} catch (NoSuchMethodError ex1) {
			// Probably prior to 1.11, try the deprecated method
			try {
				p.sendTitle(title, subtitle);
			} catch (NoSuchMethodError ex2) {
				// Probably prior to 1.8.7, no title API
			}
		}
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
		try {
			if(entity == null) {
				p.setSpectatorTarget(null);
				return;
			}
			p.setSpectatorTarget((Entity) entity.getHandle());
		} catch (NoSuchMethodError ex) {
			// Probably 1.8.6 or prior
		}
	}

	@Override
	public MCEntity getSpectatorTarget() {
		try {
			return BukkitConvertor.BukkitGetCorrectEntity(p.getSpectatorTarget());
		} catch (NoSuchMethodError ex) {
			// Probably 1.8.6 or prior
			return null;
		}
	}

	@Override
	public void setTempOp(Boolean value) throws ClassNotFoundException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		Server server = Bukkit.getServer();
		String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

		Class serverClass = Class.forName("org.bukkit.craftbukkit." + version + ".CraftServer");

		if(!server.getClass().isAssignableFrom(serverClass)) {
			throw new IllegalStateException("Running server isn't CraftBukkit");
		}

		// Since 1.7.8
		Class nmsMinecraftServerClass = Class.forName("net.minecraft.server." + version + ".MinecraftServer");
		/*n.m.s.MinecraftServer*/ Object nmsServer =
				ReflectionUtils.invokeMethod(nmsMinecraftServerClass, null, "getServer");
		/*n.m.s.PlayerList*/ Object nmsPlayerList = ReflectionUtils.invokeMethod(nmsServer, "getPlayerList");
		/*n.m.s.OpList*/ Object opSet = ReflectionUtils.get(
				Class.forName("net.minecraft.server." + version + ".PlayerList"), nmsPlayerList, "operators");
		//opSet.getClass().getSuperclass() == n.m.s.JsonList
		Map/*<String, n.m.s.OpListEntry>*/ d = (Map) ReflectionUtils.get(opSet.getClass().getSuperclass(), opSet, "d");
		if(value) {
			/*n.m.s.OpListEntry*/ Class nmsOpListEntry =
					Class.forName("net.minecraft.server." + version + ".OpListEntry");
			Class nmsGameProfile;
			try {
				/*com.mojang.authlib.GameProfile*/ nmsGameProfile = Class.forName("com.mojang.authlib.GameProfile");
			} catch (ClassNotFoundException eee) {


				// Prior to 1.8
				/*net.minecraft.util.com.mojang.authlib.GameProfile*/ nmsGameProfile =
						Class.forName("net.minecraft.util.com.mojang.authlib.GameProfile");
			}
			Object gameProfile = ReflectionUtils.invokeMethod(p, "getProfile");
			Object opListEntry;
			try {
				opListEntry = ReflectionUtils.newInstance(nmsOpListEntry,
						new Class[]{nmsGameProfile, int.class, boolean.class}, new Object[]{gameProfile, 4, false});
			} catch (ReflectionUtils.ReflectionException e) {
				// Prior to 1.8.6
				opListEntry = ReflectionUtils.newInstance(nmsOpListEntry,
						new Class[]{nmsGameProfile, int.class}, new Object[]{gameProfile, 4});
			}
			d.put(p.getUniqueId().toString(), opListEntry);
		} else {
			d.remove(p.getUniqueId().toString());
		}
		p.recalculatePermissions();
	}

	@Override
	public void setTotalExperience(int total) {
		p.setTotalExperience(total);
	}

	@Override
	public void setVanished(boolean set, MCPlayer to) {
		// show/hide was deprecated in 1.12.2
		// will need to change this to include a plugin argument
		if(!set) {
			p.showPlayer(((BukkitMCPlayer) to)._Player());
		} else {
			p.hidePlayer(((BukkitMCPlayer) to)._Player());
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
	public void sendBlockChange(MCLocation loc, int material, byte data) {
		p.sendBlockChange(((Location) loc.getHandle()), material, data);
	}

	@Override
	public void sendSignTextChange(MCLocation loc, String[] lines) {
		p.sendSignChange(((Location) loc.getHandle()), lines);
	}

	@Override
	public void playNote(MCLocation loc, MCInstrument instrument, MCNote note) {
		p.playNote((Location) loc.getHandle(),
				BukkitMCInstrument.getConvertor().getConcreteEnum(instrument), (Note) note.getHandle());
	}

	@Override
	public void playSound(MCLocation l, MCSound sound, float volume, float pitch) {
		p.playSound(((BukkitMCLocation) l).asLocation(),
				((BukkitMCSound) sound).getConcrete(), volume, pitch);
	}

	@Override
	public void playSound(MCLocation l, String sound, float volume, float pitch) {
		p.playSound(((BukkitMCLocation) l).asLocation(), sound, volume, pitch);
	}

	@Override
	public void playSound(MCLocation l, MCSound sound, MCSoundCategory category, float volume, float pitch) {
		try {
			p.playSound((Location) l.getHandle(), ((BukkitMCSound) sound).getConcrete(),
					BukkitMCSoundCategory.getConvertor().getConcreteEnum(category), volume, pitch);
		} catch (NoClassDefFoundError ex) {
			// probably prior to 1.11, ignore category
			playSound(l, sound, volume, pitch);
		}
	}

	@Override
	public void playSound(MCLocation l, String sound, MCSoundCategory category, float volume, float pitch) {
		try {
			p.playSound((Location) l.getHandle(), sound,
					BukkitMCSoundCategory.getConvertor().getConcreteEnum(category), volume, pitch);
		} catch (NoClassDefFoundError ex) {
			// probably prior to 1.11, ignore category
			playSound(l, sound, volume, pitch);
		}
	}

	@Override
	public void stopSound(MCSound sound) {
		try {
			p.stopSound(((BukkitMCSound) sound).getConcrete());
		} catch (NoSuchMethodError ex) {
			// probably prior to 1.10
		}
	}

	@Override
	public void stopSound(String sound) {
		try {
			p.stopSound(sound);
		} catch (NoSuchMethodError ex) {
			// probably prior to 1.10
		}
	}

	@Override
	public void stopSound(MCSound sound, MCSoundCategory category) {
		try {
			p.stopSound(((BukkitMCSound) sound).getConcrete(),
					BukkitMCSoundCategory.getConvertor().getConcreteEnum(category));
		} catch (NoClassDefFoundError ex) {
			// probably prior to 1.11, ignore category
			stopSound(sound);
		}
	}

	@Override
	public void stopSound(String sound, MCSoundCategory category) {
		try {
			p.stopSound(sound, BukkitMCSoundCategory.getConvertor().getConcreteEnum(category));
		} catch (NoClassDefFoundError ex) {
			// probably prior to 1.11, ignore category
			stopSound(sound);
		}
	}

	@Override
	public void spawnParticle(MCLocation l, MCParticle pa, int count,
			double offsetX, double offsetY, double offsetZ, double velocity, Object data) {
		try {
			Particle type = Particle.valueOf(pa.name());
			Location loc = ((BukkitMCLocation) l).asLocation();
			if(data != null && type.getDataType().equals(ItemStack.class) && data instanceof MCItemStack) {
				p.spawnParticle(type, loc, count,
						offsetX, offsetY, offsetZ, velocity, ((MCItemStack) data).getHandle());
			} else {
				p.spawnParticle(type, loc, count, offsetX, offsetY, offsetZ, velocity);
			}
		} catch (NoClassDefFoundError ex) {
			// probably prior to 1.9
		}
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
	public MCScoreboard getScoreboard() {
		return new BukkitMCScoreboard(p.getScoreboard());
	}

	@Override
	public void setScoreboard(MCScoreboard board) {
		p.setScoreboard(((BukkitMCScoreboard) board)._scoreboard());
	}
}
