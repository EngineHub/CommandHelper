package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCPlayer;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayerInventory;
import com.laytonsmith.abstraction.bukkit.BukkitMCScoreboard;
import com.laytonsmith.abstraction.enums.MCInstrument;
import com.laytonsmith.abstraction.enums.MCSound;
import com.laytonsmith.abstraction.enums.MCWeather;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCInstrument;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCSound;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCWeather;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions;
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
import org.bukkit.scoreboard.Scoreboard;

/**
 *
 * @author layton
 */
public class BukkitMCPlayer extends BukkitMCHumanEntity implements MCPlayer, MCCommandSender, MCOfflinePlayer {

	public BukkitMCPlayer(Player player) {
		super(player);
	}

	public BukkitMCPlayer(AbstractionObject ao) {
		this((Player) ao.getHandle());
	}

	@Override
	public Player getHandle() {
		return (Player) metadatable;
	}

    public boolean canSee(MCPlayer p) {
        return getHandle().canSee((Player) p.getHandle());
    }

    public void chat(String chat) {
        getHandle().chat(chat);
    }

    public InetSocketAddress getAddress() {
        return getHandle().getAddress();
    }

    public boolean getAllowFlight() {
        return getHandle().getAllowFlight();
    }

    public MCLocation getCompassTarget() {
        return new BukkitMCLocation(getHandle().getCompassTarget());
    }

    public String getDisplayName() {
        return getHandle().getDisplayName();
    }

    public float getExp() {
        return getHandle().getExp();
    }

    public long getFirstPlayed() {
		return getHandle().getFirstPlayed();
	}

    public int getFoodLevel() {
        return getHandle().getFoodLevel();
    }

	@Override
    public MCPlayerInventory getInventory() {
        if (getHandle() == null || getHandle().getInventory() == null) {
            return null;
        }
        return new BukkitMCPlayerInventory(getHandle().getInventory());
    }

    public MCItemStack getItemAt(Integer slot) {
        if (slot == null) {
            return new BukkitMCItemStack(getHandle().getItemInHand());
        }
        ItemStack is = null;
        //Special slots
        if (slot == 100) {
            is = getHandle().getInventory().getBoots();
        } else if (slot == 101) {
            is = getHandle().getInventory().getLeggings();
        } else if (slot == 102) {
            is = getHandle().getInventory().getChestplate();
        } else if (slot == 103) {
            is = getHandle().getInventory().getHelmet();
        }
        if (slot >= 0 && slot <= 35) {
            is = getHandle().getInventory().getItem(slot);
        }
        if (is == null) {
            return null;
        } else {
            return new BukkitMCItemStack(is);
        }
    }

    public long getLastPlayed() {
		return getHandle().getLastPlayed();
	}

    public int getLevel() {
        return getHandle().getLevel();
    }

    public MCPlayer getPlayer() {
        return new BukkitMCPlayer(getHandle());
    }

    public long getPlayerTime() {
        return getHandle().getPlayerTime();
    }

	public MCWeather getPlayerWeather() {
		return BukkitMCWeather.getConvertor().getAbstractedEnum(getHandle().getPlayerWeather());
	}

    public int getRemainingFireTicks() {
        return getHandle().getFireTicks();
    }

//    public int getTotalExperience() {
//        return p.getTotalExperience();
//    }

    // Method from Essentials plugin:
    // https://raw.github.com/essentials/Essentials/master/Essentials/src/net/ess3/craftbukkit/SetExpFix.java
	//This method is required because the bukkit player.getTotalExperience() method, shows exp that has been 'spent'.
	//Without this people would be able to use exp and then still sell it.
	public int getTotalExperience()
	{
		int exp = (int)Math.round(getExpAtLevel(getHandle()) * getHandle().getExp());
		int currentLevel = getHandle().getLevel();

		while (currentLevel > 0)
		{
			currentLevel--;
			exp += getExpAtLevel(currentLevel);
		}
		return exp;
	}

	private static int getExpAtLevel(final Player player)
	{
		return getExpAtLevel(player.getLevel());
	}

	private static int getExpAtLevel(final int level)
	{
		if (level > 29)
		{
			return 62 + (level - 30) * 7;
		}
		if (level > 15)
		{
			return 17 + (level - 15) * 3;
		}
		return 17;
	}

	public void setFlySpeed(float speed) {
		getHandle().setFlySpeed(speed);
	}
	
	public float getFlySpeed() {
		return getHandle().getFlySpeed();
	}
	
	public void setWalkSpeed(float speed) {
		getHandle().setWalkSpeed(speed);
	}

	public float getWalkSpeed() {
		return getHandle().getWalkSpeed();
	}
	
    public void giveExp(int xp) {
        getHandle().giveExp(xp);
    }

    public boolean hasPlayedBefore() {
		return getHandle().hasPlayedBefore();
	}

    public boolean isBanned() {
        return getHandle().isBanned();
    }

    public boolean isOnline() {
        return getHandle().isOnline();
    }

    public boolean isOp() {
        return getHandle().isOp();
    }

	public void setOp(boolean bln) {
		getHandle().setOp(bln);
	}

    public boolean isSneaking() {
        return getHandle().isSneaking();
    }
	
	public boolean isSprinting() {
		return getHandle().isSprinting();
	}

    public boolean isWhitelisted() {
        return getHandle().isWhitelisted();
    }

    public void kickPlayer(String message) {
        getHandle().kickPlayer(message);
    }

	@Override
    public boolean removeEffect(int potionID) {
		PotionEffectType t = PotionEffectType.getById(potionID);
		boolean hasIt = false;
		for(PotionEffect pe : getHandle().getActivePotionEffects()) {
			if (pe.getType() == t) {
				hasIt = true;
				break;
			}
		}
		getHandle().removePotionEffect(t);
		return hasIt;
    }

    public void resetPlayerTime() {
        getHandle().resetPlayerTime();
    }

	public void resetPlayerWeather() {
		getHandle().resetPlayerWeather();
	}

    public void sendMessage(String string) {
		//The client doesn't like tabs
		string = string.replaceAll("\t", "    ");
        getHandle().sendMessage(string);
    }

	public void sendTexturePack(String url) {
		getHandle().setTexturePack(url);
	}

    public void setAllowFlight(boolean flight) {
        getHandle().setAllowFlight(flight);
    }

    public void setBanned(boolean banned) {
        getHandle().setBanned(banned);
    }

    public void setCompassTarget(MCLocation l) {
        getHandle().setCompassTarget((Location) l.getHandle());
    }

    public void setDisplayName(String name) {
        getHandle().setDisplayName(name);
    }

    public void setExp(float i) {
        getHandle().setExp(i);
    }

	public void setFlying(boolean flight) {
		getHandle().setFlying(flight);
	}

    public void setFoodLevel(int f) {
        getHandle().setFoodLevel(f);
    }

    /*public void setHealth(int i) {
        if(i == 0){
            this.fireEntityDamageEvent(MCDamageCause.CUSTOM);
        }
        p.setHealth(i);
    }*/

    public void setLevel(int xp) {
        getHandle().setLevel(xp);
    }

    public void setPlayerTime(Long time, boolean relative) {
        getHandle().setPlayerTime(time, relative);
    }

	public void setPlayerWeather(MCWeather type) {
		getHandle().setPlayerWeather(BukkitMCWeather.getConvertor().getConcreteEnum(type));
	}

    public void setRemainingFireTicks(int i) {
        getHandle().setFireTicks(i);
    }

    public void setTempOp(Boolean value) throws ClassNotFoundException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Server server = Bukkit.getServer();

        Class serverClass = ClassDiscovery.getDefaultInstance().forFuzzyName("org.bukkit.craftbukkit.*", "CraftServer").loadClass();

        if (!server.getClass().isAssignableFrom(serverClass)) {
            throw new IllegalStateException("Running server isn't CraftBukkit");
        }

        Set opSet = null;
		try{
			//Probably 1.4.5
			/*n.m.s.Server*/ Object nmsServer = ReflectionUtils.invokeMethod(server, "getServer");
			/*o.b.c.ServerConfigurationManagerAbstract*/ Object obcServerConfigurationmanagerAbstract = ReflectionUtils.invokeMethod(nmsServer, "getServerConfigurationManager");
			opSet = (Set) ReflectionUtils.get(ClassDiscovery.getDefaultInstance().forFuzzyName("net.minecraft.server.*", "ServerConfigurationManagerAbstract").loadClass(), obcServerConfigurationmanagerAbstract, "operators");
		} catch(ReflectionUtils.ReflectionException e){
			//Probably 1.4.6
			Class nmsMinecraftServerClass = ClassDiscovery.getDefaultInstance().forFuzzyName("net.minecraft.server.*", "MinecraftServer").loadClass();
			/*n.m.s.MinecraftServer*/ Object nmsServer = ReflectionUtils.invokeMethod(nmsMinecraftServerClass, null, "getServer");
			/*n.m.s.PlayerList*/ Object nmsPlayerList = ReflectionUtils.invokeMethod(nmsServer, "getPlayerList");
			opSet = (Set)ReflectionUtils.get(ClassDiscovery.getDefaultInstance().forFuzzyName("net.minecraft.server.*", "PlayerList").loadClass(), nmsPlayerList, "operators");
		}

        // since all Java objects pass by reference, we don't need to set field back to object
        if (value) {
            opSet.add(getHandle().getName().toLowerCase());
        } else {
            opSet.remove(getHandle().getName().toLowerCase());
        }
        getHandle().recalculatePermissions();
    }

//    public void setTotalExperience(int total) {
//      p.setTotalExperience(0);
//		p.setLevel(0);
//		p.setExp(0);
//		p.giveExp(total);
//    }

	// Method from Essentials plugin:
	// https://raw.github.com/essentials/Essentials/master/Essentials/src/net/ess3/craftbukkit/SetExpFix.java
	//This method is used to update both the recorded total experience and displayed total experience.
	//We reset both types to prevent issues.
	public void setTotalExperience(int total)
	{
		if (total < 0)
		{
			throw new ConfigRuntimeException("Experience can't be negative", Exceptions.ExceptionType.RangeException, null);
		}

		Player p = getHandle();
        p.setExp(0);
        p.setLevel(0);
        p.setTotalExperience(0);

		//This following code is technically redundant now, as bukkit now calulcates levels more or less correctly
		//At larger numbers however... player.getExp(3000), only seems to give 2999, putting the below calculations off.
		int amount = total;
		while (amount > 0)
		{
			final int expToLevel = getExpAtLevel(p);
			amount -= expToLevel;
			if (amount >= 0)
			{
				// give until next level
				p.giveExp(expToLevel);
			}
			else
			{
				// give the rest
				amount += expToLevel;
				p.giveExp(amount);
				amount = 0;
			}
		}
	}

    public void setVanished(boolean set, MCPlayer to) {
        if (!set) {
            getHandle().showPlayer((Player) to.getHandle());
        } else {
            getHandle().hidePlayer((Player) to.getHandle());
        }
    }

    public void setWhitelisted(boolean value) {
        getHandle().setWhitelisted(value);
    }

    public void setPlayerListName(String listName) {
        getHandle().setPlayerListName(listName);
    }

    public String getPlayerListName() {
        return getHandle().getPlayerListName();
    }

    public boolean isNewPlayer() {
        //Note the reversed logic here. If they have NOT played before, they are
        //a new player.
        return !getHandle().getServer().getOfflinePlayer(getHandle().getName()).hasPlayedBefore();
    }

    public String getHost() {
        return Static.GetHost(this);
    }

	public void sendBlockChange(MCLocation loc, int material, byte data) {
		getHandle().sendBlockChange(((Location)loc.getHandle()), material, data);
	}

	public void playNote(MCLocation loc, MCInstrument instrument, MCNote note) {
		getHandle().playNote((Location)loc.getHandle(), BukkitMCInstrument.getConvertor().getConcreteEnum(instrument), (Note)note.getHandle());
	}
	
	public void playSound(MCLocation l, MCSound sound, float volume, float pitch) {
		getHandle().playSound((Location) l.getHandle(), 
				BukkitMCSound.getConvertor().getConcreteEnum(sound), volume, pitch);
	}

	public int getHunger() {
		return getHandle().getFoodLevel();
	}

	public void setHunger(int h) {
		getHandle().setFoodLevel(h);
	}

	public float getSaturation() {
		return getHandle().getSaturation();
	}

	public void setSaturation(float s) {
		getHandle().setSaturation(s);
	}

	public MCLocation getBedSpawnLocation() {
	    return new BukkitMCLocation(getHandle().getBedSpawnLocation());
	}

	public void setBedSpawnLocation(MCLocation l) {
		getHandle().setBedSpawnLocation((Location) l.getHandle(), true);
	}

	public void sendPluginMessage(String channel, byte[] message) {
		StaticLayer.GetConvertor().GetPluginMeta().openOutgoingChannel(channel);
		getHandle().sendPluginMessage(CommandHelperPlugin.self, channel, message);
	}

	public boolean isFlying() {
		return getHandle().isFlying();
	}

	public void updateInventory() {
		getHandle().updateInventory();
	}

	public MCScoreboard getScoreboard() {
		return new BukkitMCScoreboard(getHandle().getScoreboard());
	}

	public void setScoreboard(MCScoreboard board) {
		getHandle().setScoreboard((Scoreboard) board.getHandle());
	}
}