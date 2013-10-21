

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.*;
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

	@Override
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

	public MCWeather getPlayerWeather() {
		return BukkitMCWeather.getConvertor().getAbstractedEnum(p.getPlayerWeather());
	}

    public int getRemainingFireTicks() {
        return p.getFireTicks();
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
		int exp = (int)Math.round(getExpAtLevel(p) * p.getExp());
		int currentLevel = p.getLevel();

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
		p.setFlySpeed(speed);
	}
	
	public float getFlySpeed() {
		return p.getFlySpeed();
	}
	
	public void setWalkSpeed(float speed) {
		p.setWalkSpeed(speed);
	}

	public float getWalkSpeed() {
		return p.getWalkSpeed();
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

	public void setOp(boolean bln) {
		p.setOp(bln);
	}

    public boolean isSneaking() {
        return p.isSneaking();
    }
	
	public boolean isSprinting() {
		return p.isSprinting();
	}

    public boolean isWhitelisted() {
        return p.isWhitelisted();
    }

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

    public void resetPlayerTime() {
        p.resetPlayerTime();
    }

	public void resetPlayerWeather() {
		p.resetPlayerWeather();
	}

    public void sendMessage(String string) {
		//The client doesn't like tabs
		string = string.replaceAll("\t", "    ");
        p.sendMessage(string);
    }

	public void sendTexturePack(String url) {
		p.setTexturePack(url);
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

	public void setFlying(boolean flight) {
		p.setFlying(flight);
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

    public void setPlayerTime(Long time, boolean relative) {
        p.setPlayerTime(time, relative);
    }

	public void setPlayerWeather(MCWeather type) {
		p.setPlayerWeather(BukkitMCWeather.getConvertor().getConcreteEnum(type));
	}

    public void setRemainingFireTicks(int i) {
        p.setFireTicks(i);
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
            opSet.add(p.getName().toLowerCase());
        } else {
            opSet.remove(p.getName().toLowerCase());
        }
        p.recalculatePermissions();
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
	
	public void playSound(MCLocation l, MCSound sound, float volume, float pitch) {
		p.playSound(((BukkitMCLocation) l).asLocation(), 
				BukkitMCSound.getConvertor().getConcreteEnum(sound), volume, pitch);
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

	@Override
	public MCEntity getVehicle() {
		return new BukkitMCEntity(p.getVehicle());
	}

	public void sendPluginMessage(String channel, byte[] message) {
		StaticLayer.GetConvertor().GetPluginMeta().openOutgoingChannel(channel);
		p.sendPluginMessage(CommandHelperPlugin.self, channel, message);
	}

	public boolean isFlying() {
		return p.isFlying();
	}

	public void updateInventory() {
		p.updateInventory();
	}

	public MCScoreboard getScoreboard() {
		return new BukkitMCScoreboard(p.getScoreboard());
	}

	public void setScoreboard(MCScoreboard board) {
		p.setScoreboard(((BukkitMCScoreboard) board).s);
	}
}
