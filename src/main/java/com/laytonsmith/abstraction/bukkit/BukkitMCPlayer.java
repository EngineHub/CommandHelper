

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.PureUtilities.ClassDiscovery;
import com.laytonsmith.PureUtilities.ReflectionUtils;
import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.enums.MCInstrument;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCInstrument;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.Static;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Note;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author layton
 */
public class BukkitMCPlayer extends BukkitMCHumanEntity implements MCPlayer, MCCommandSender, MCOfflinePlayer {

    Player p;

	public BukkitMCPlayer(Player player) {
        super(player);
        this.p = player;
    }

	public Player _Player() {
        return p;
    }

    public boolean canSee(MCPlayer p) {
        return this.p.canSee(((BukkitMCPlayer)p)._Player());
    }

    public void chat(String chat) {
        p.chat(chat);
    }

    public InetSocketAddress getAddress() {
        return p.getAddress();
    }

    public boolean getAllowFlight() {
        return p.getAllowFlight();
    }

    public MCLocation getCompassTarget() {
        return new BukkitMCLocation(p.getCompassTarget());
    }

    public String getDisplayName() {
        return p.getDisplayName();
    }

    public float getExp() {
        return p.getExp();
    }

    public long getFirstPlayed() {
		return p.getFirstPlayed();
	}

    public int getFoodLevel() {
        return p.getFoodLevel();
    }

    public MCPlayerInventory getInventory() {
        if (p == null || p.getInventory() == null) {
            return null;
        }
        return new BukkitMCPlayerInventory(p.getInventory());
    }

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

    public long getLastPlayed() {
		return p.getLastPlayed();
	}

    public int getLevel() {
        return p.getLevel();
    }

    public MCPlayer getPlayer() {
        return new BukkitMCPlayer(p);
    }

    public long getPlayerTime() {
        return p.getPlayerTime();
    }

    public int getRemainingFireTicks() {
        return p.getFireTicks();
    }

    public int getTotalExperience() {
        return p.getTotalExperience();
    }

    public void giveExp(int xp) {
        p.giveExp(xp);
    }

    public boolean hasPlayedBefore() {
		return p.hasPlayedBefore();
	}

    public boolean isBanned() {
        return p.isBanned();
    }

    public boolean isOnline() {
        return p.isOnline();
    }

    public boolean isOp() {
        return p.isOp();
    }

    public boolean isSneaking() {
        return p.isSneaking();
    }

    public boolean isWhitelisted() {
        return p.isWhitelisted();
    }

    public void kickPlayer(String message) {
        p.kickPlayer(message);
    }

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

    public void resetPlayerTime() {
        p.resetPlayerTime();
    }

    public void sendMessage(String string) {
		//The client doesn't like tabs
		string = string.replaceAll("\t", "    ");
        p.sendMessage(string);
    }

    public void setAllowFlight(boolean flight) {
        p.setAllowFlight(flight);
    }

    public void setBanned(boolean banned) {
        p.setBanned(banned);
    }

    public void setCompassTarget(MCLocation l) {
        p.setCompassTarget(((BukkitMCLocation)l).l);
    }

    public void setDisplayName(String name) {
        p.setDisplayName(name);
    }

    public void setExp(float i) {
        p.setExp(i);
    }

    public void setFoodLevel(int f) {
        p.setFoodLevel(f);
    }

    /*public void setHealth(int i) {
        if(i == 0){
            this.fireEntityDamageEvent(MCDamageCause.CUSTOM);
        }
        p.setHealth(i);
    }*/

    public void setLevel(int xp) {
        p.setLevel(xp);
    }

    public void setPlayerTime(Long time) {
        p.setPlayerTime(time, false);
    }

    public void setRemainingFireTicks(int i) {
        p.setFireTicks(i);
    }

    public void setTempOp(Boolean value) throws ClassNotFoundException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Server server = Bukkit.getServer();

        Class serverClass = ClassDiscovery.forFuzzyName("org.bukkit.craftbukkit.*", "CraftServer");

        if (!server.getClass().isAssignableFrom(serverClass)) {
            throw new IllegalStateException("Running server isn't CraftBukkit");
        }

        Set opSet = null;
		try{
			//Probably 1.4.5
			/*n.m.s.Server*/ Object nmsServer = ReflectionUtils.invokeMethod(server, "getServer");
			/*o.b.c.ServerConfigurationManagerAbstract*/ Object obcServerConfigurationmanagerAbstract = ReflectionUtils.invokeMethod(nmsServer, "getServerConfigurationManager");
			opSet = (Set) ReflectionUtils.get(ClassDiscovery.forFuzzyName("net.minecraft.server.*", "ServerConfigurationManagerAbstract"), obcServerConfigurationmanagerAbstract, "operators");
		} catch(ReflectionUtils.ReflectionException e){
			//Probably 1.4.6
			Class nmsMinecraftServerClass = ClassDiscovery.forFuzzyName("net.minecraft.server.*", "MinecraftServer");
			/*n.m.s.MinecraftServer*/ Object nmsServer = ReflectionUtils.invokeMethod(nmsMinecraftServerClass, null, "getServer");
			/*n.m.s.PlayerList*/ Object nmsPlayerList = ReflectionUtils.invokeMethod(nmsServer, "getPlayerList");
			opSet = (Set)ReflectionUtils.get(ClassDiscovery.forFuzzyName("net.minecraft.server.*", "PlayerList"), nmsPlayerList, "operators");
		}

        // since all Java objects pass by reference, we don't need to set field back to object
        if (value) {
            opSet.add(p.getName().toLowerCase());
        } else {
            opSet.remove(p.getName().toLowerCase());
        }
        p.recalculatePermissions();
    }

    public void setTotalExperience(int total) {
        p.setTotalExperience(0);
		p.setLevel(0);
		p.setExp(0);
		p.giveExp(total);
    }

    public void setVanished(boolean set, MCPlayer to) {
        if (!set) {
            p.showPlayer(((BukkitMCPlayer)to)._Player());
        } else {
            p.hidePlayer(((BukkitMCPlayer)to)._Player());
        }
    }

    public void setWhitelisted(boolean value) {
        p.setWhitelisted(value);
    }

    public void setPlayerListName(String listName) {
        p.setPlayerListName(listName);
    }

    public String getPlayerListName() {
        return p.getPlayerListName();
    }

    public boolean isNewPlayer() {
        //Note the reversed logic here. If they have NOT played before, they are
        //a new player.
        return !p.getServer().getOfflinePlayer(p.getName()).hasPlayedBefore();
    }

    public String getHost() {
        return Static.GetHost(this);
    }

	public void sendBlockChange(MCLocation loc, int material, byte data) {
		p.sendBlockChange(((Location)loc.getHandle()), material, data);
	}

	public void playNote(MCLocation loc, MCInstrument instrument, MCNote note) {
		p.playNote((Location)loc.getHandle(), BukkitMCInstrument.getConvertor().getConcreteEnum(instrument), (Note)note.getHandle());
	}

	public int getHunger() {
		return p.getFoodLevel();
	}

	public void setHunger(int h) {
		p.setFoodLevel(h);
	}

	public float getSaturation() {
		return p.getSaturation();
	}

	public void setSaturation(float s) {
		p.setSaturation(s);
	}

	public MCLocation getBedSpawnLocation() {
	    return new BukkitMCLocation(p.getBedSpawnLocation());
	}

	public void setBedSpawnLocation(MCLocation l) {
		p.setBedSpawnLocation((Location)l.getHandle(), true);
	}

	public MCEntity getVehicle() {
		return new BukkitMCEntity(p.getVehicle());
	}

	public void sendPluginMessage(String channel, byte[] message) {
		StaticLayer.GetConvertor().GetPluginMeta().openOutgoingChannel(channel);
		p.sendPluginMessage(CommandHelperPlugin.self, channel, message);
	}
}
