

package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
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
import com.laytonsmith.abstraction.enums.MCSound;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.abstraction.enums.MCWeather;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCInstrument;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCSound;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCWeather;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.Static;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Note;
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
import java.util.Set;
import java.util.UUID;

/**
 *
 * 
 */
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
        return this.p.canSee(((BukkitMCPlayer)p)._Player());
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
        if (p == null || p.getInventory() == null) {
            return null;
        }
        return new BukkitMCPlayerInventory(p.getInventory());
    }

	@Override
    public MCItemStack getItemAt(Integer slot) {
        if (slot == null) {
            return new BukkitMCItemStack(p.getItemInHand());
        }
        ItemStack is = null;
        //Special slots
        if (slot == 100) {
            is = p.getInventory().getBoots();
        } else if (slot == 101) {
            is = p.getInventory().getLeggings();
        } else if (slot == 102) {
            is = p.getInventory().getChestplate();
        } else if (slot == 103) {
            is = p.getInventory().getHelmet();
        } else if (slot == -106) {
			is = p.getInventory().getItemInOffHand();
		}
        if (slot >= 0 && slot <= 35) {
            is = p.getInventory().getItem(slot);
        }
        if (is == null) {
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
	public int getTotalExperience()
	{
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
			if (level > 30) {
				return (int) (3.5 * Math.pow(level, 2) - 151.5 * level + 2220);
			}
			if(level > 15) {
				return (int) (1.5 * Math.pow(level, 2) - 29.5 * level + 360);
			}
			return 17 * level;
		} else {
			if (level > 30) {
				return (int) (4.5 * Math.pow(level, 2) - 162.5 * level + 2220);
			}
			if (level > 15) {
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
		for (PermissionAttachmentInfo permAttach : p.getEffectivePermissions()) {
			String perm = permAttach.getPermission();
			if (!(perm.startsWith(Static.groupPrefix) && permAttach.getValue())) {
				continue;
			}
			groupNames.add(perm.substring(Static.groupPrefix.length(), perm.length()));
		}
		return groupNames;
	}

	@Override
	public boolean inGroup(String groupName) {
		return getGroups().contains(groupName);
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
			if (pe.getType() == t) {
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
    public void sendTexturePack(String url) {
            p.setTexturePack(url);
    }

        @Override
    public void sendResourcePack(String url) {
            p.setResourcePack(url);
    }

	@Override
    public void setAllowFlight(boolean flight) {
        p.setAllowFlight(flight);
    }

	@Override
    public void setBanned(boolean banned) {
        p.setBanned(banned);
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

    /*public void setHealth(int i) {
        if(i == 0){
            this.fireEntityDamageEvent(MCDamageCause.CUSTOM);
        }
        p.setHealth(i);
    }*/

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
			if(entity == null){
				p.setSpectatorTarget(null);
				return;
			}
			p.setSpectatorTarget((Entity) entity.getHandle());
		} catch(NoSuchMethodError ex){
			// Probably 1.8.6 or prior
		}
	}

	@Override
	public MCEntity getSpectatorTarget() {
		try {
			return BukkitConvertor.BukkitGetCorrectEntity(p.getSpectatorTarget());
		} catch(NoSuchMethodError ex){
			// Probably 1.8.6 or prior
			return null;
		}
	}

	@Override
    public void setTempOp(Boolean value) throws ClassNotFoundException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Server server = Bukkit.getServer();

        Class serverClass = ClassDiscovery.getDefaultInstance().forFuzzyName("org.bukkit.craftbukkit.*", "CraftServer").loadClass();

        if (!server.getClass().isAssignableFrom(serverClass)) {
            throw new IllegalStateException("Running server isn't CraftBukkit");
        }

		try {
			//Probably 1.4.6
			Class nmsMinecraftServerClass = ClassDiscovery.getDefaultInstance().forFuzzyName("net.minecraft.server.*", "MinecraftServer").loadClass();
			/*n.m.s.MinecraftServer*/ Object nmsServer = ReflectionUtils.invokeMethod(nmsMinecraftServerClass, null, "getServer");
			/*n.m.s.PlayerList*/ Object nmsPlayerList = ReflectionUtils.invokeMethod(nmsServer, "getPlayerList");
			Set opSet = (Set)ReflectionUtils.get(ClassDiscovery.getDefaultInstance().forFuzzyName("net.minecraft.server.*", "PlayerList").loadClass(), nmsPlayerList, "operators");

			// since all Java objects pass by reference, we don't need to set field back to object
			if (value) {
				opSet.add(p.getName().toLowerCase());
			} else {
				opSet.remove(p.getName().toLowerCase());
			}
		} catch(ClassCastException ex){
			// Probably 1.7.8
			Class nmsMinecraftServerClass = ClassDiscovery.getDefaultInstance().forFuzzyName("net.minecraft.server.*", "MinecraftServer").loadClass();
			/*n.m.s.MinecraftServer*/ Object nmsServer = ReflectionUtils.invokeMethod(nmsMinecraftServerClass, null, "getServer");
			/*n.m.s.PlayerList*/ Object nmsPlayerList = ReflectionUtils.invokeMethod(nmsServer, "getPlayerList");
			/*n.m.s.OpList*/ Object opSet = ReflectionUtils.get(ClassDiscovery.getDefaultInstance().forFuzzyName("net.minecraft.server.*", "PlayerList").loadClass(), nmsPlayerList, "operators");
			//opSet.getClass().getSuperclass() == n.m.s.JsonList
			Map/*<String, n.m.s.OpListEntry>*/ d = (Map)ReflectionUtils.get(opSet.getClass().getSuperclass(), opSet, "d");
			if(value){
				/*n.m.s.OpListEntry*/ Class nmsOpListEntry = ClassDiscovery.getDefaultInstance().forFuzzyName("net.minecraft.server.*", "OpListEntry").loadClass();
				Class nmsGameProfile;
				try {
					/*net.minecraft.util.com.mojang.authlib.GameProfile*/ nmsGameProfile = Class.forName("net.minecraft.util.com.mojang.authlib.GameProfile");
				} catch (ClassNotFoundException eee){
					// Probably 1.8
					/*com.mojang.authlib.GameProfile*/ nmsGameProfile = Class.forName("com.mojang.authlib.GameProfile");
				}
				Object gameProfile = ReflectionUtils.invokeMethod(p, "getProfile");
				Object opListEntry;
				try {
					opListEntry = ReflectionUtils.newInstance(nmsOpListEntry, new Class[]{nmsGameProfile, int.class}, new Object[]{gameProfile, 4});
				} catch (ReflectionUtils.ReflectionException e) {
					// Probably 1.8.6
					opListEntry = ReflectionUtils.newInstance(nmsOpListEntry, new Class[]{nmsGameProfile, int.class, boolean.class}, new Object[]{gameProfile, 4, false});
				}
				d.put(p.getUniqueId().toString(), opListEntry);
			} else {
				d.remove(p.getUniqueId().toString());
			}
		}
        p.recalculatePermissions();
    }

	@Override
	public void setTotalExperience(int total)
	{
        p.setTotalExperience(total);
	}

	@Override
    public void setVanished(boolean set, MCPlayer to) {
        if (!set) {
            p.showPlayer(((BukkitMCPlayer)to)._Player());
        } else {
            p.hidePlayer(((BukkitMCPlayer)to)._Player());
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
        return !p.getServer().getOfflinePlayer(p.getName()).hasPlayedBefore();
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
		p.playNote((Location) loc.getHandle(), BukkitMCInstrument.getConvertor().getConcreteEnum(instrument), (Note) note.getHandle());
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
		return loc == null ? null : new BukkitMCLocation(loc);
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
