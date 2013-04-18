package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.bukkit.pluginmessages.BukkitMCMessenger;
import com.laytonsmith.abstraction.enums.MCInventoryType;
import com.laytonsmith.abstraction.pluginmessages.MCMessenger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 *
 * @author layton
 */
public class BukkitMCServer implements MCServer {
    
    Server s;
    public BukkitMCServer(){
        this.s = Bukkit.getServer();
    }
    
    public BukkitMCServer(Server server) {
		this.s = server;
	}

	public Object getHandle(){
        return s;
    }
    
    public Server __Server(){
        return s;
    }

    public String getName() {
        return s.getName();
    }

    public MCPlayer[] getOnlinePlayers() {
        if(s.getOnlinePlayers() == null){
            return null;
        }
        Player[] pa = s.getOnlinePlayers();
        MCPlayer[] mcpa = new MCPlayer[pa.length];
        for(int i = 0; i < pa.length; i++){
            mcpa[i] = new BukkitMCPlayer(pa[i]);
        }
        return mcpa;
    }

    public static MCServer Get() {
        return new BukkitMCServer();
    }
    
    public boolean dispatchCommand(MCCommandSender sender, String command){
        return s.dispatchCommand(new BukkitMCCommandSender(sender).c, command);
    }

    public MCPluginManager getPluginManager() {
        if(s.getPluginManager() == null){
            return null;
        }
        return new BukkitMCPluginManager(s.getPluginManager());
    }

    public MCPlayer getPlayer(String name) {
        if(s.getPlayer(name) == null){
            return null;
        }
        return new BukkitMCPlayer(s.getPlayer(name));
    }

    public MCWorld getWorld(String name) {
        if(s.getWorld(name) == null){
            return null;
        }
        return new BukkitMCWorld(s.getWorld(name));
    }
    
    public List<MCWorld> getWorlds(){
        if(s.getWorlds() == null){
            return null;
        }
        List<MCWorld> list = new ArrayList<MCWorld>();
        for(World w : s.getWorlds()){
            list.add(new BukkitMCWorld(w));
        }
        return list;
    }

    public void broadcastMessage(String message) {
        s.broadcastMessage(message);
    }

	public MCItemFactory getItemFactory() {
		return new BukkitMCItemFactory(s.getItemFactory());
	}

    public MCOfflinePlayer getOfflinePlayer(String player) {
        return new BukkitMCOfflinePlayer(s.getOfflinePlayer(player));
    }

	public MCOfflinePlayer[] getOfflinePlayers() {
		if (s.getOfflinePlayers() == null) {
			return null;
		}
		OfflinePlayer[] offp = s.getOfflinePlayers();
		MCOfflinePlayer[] mcoff = new MCOfflinePlayer[offp.length];
		for (int i = 0; i < offp.length; i++) {
			mcoff[i] = new BukkitMCOfflinePlayer(offp[i]);
		}
		return mcoff;
	}

    /* Boring information get methods -.- */
    public String getModVersion() {
        return s.getBukkitVersion();
    }

    public String getVersion() {
        return s.getVersion();
    }

    public Boolean getAllowEnd() {
        return s.getAllowEnd();
    }

    public Boolean getAllowFlight() {
        return s.getAllowFlight();
    }

    public Boolean getAllowNether() {
        return s.getAllowNether();
    }
    
    public Boolean getOnlineMode() {
    	return s.getOnlineMode();
    }

    public String getWorldContainer() {
        return s.getWorldContainer().getPath();
    }

    public String getServerName() {
        return s.getServerName();
    }

    public int getMaxPlayers() {
        return s.getMaxPlayers();
    }

    public List<MCOfflinePlayer> getBannedPlayers() {
        if(s.getBannedPlayers() == null){
            return null;
        }
        List<MCOfflinePlayer> list = new ArrayList<MCOfflinePlayer>();
        for(OfflinePlayer p : s.getBannedPlayers()){
            list.add(getOfflinePlayer(p.getName()));
        }
        return list;
    }

    public List<MCOfflinePlayer> getWhitelistedPlayers() {
        if(s.getBannedPlayers() == null){
            return null;
        }
        List<MCOfflinePlayer> list = new ArrayList<MCOfflinePlayer>();
        for(OfflinePlayer p : s.getWhitelistedPlayers()){
            list.add(getOfflinePlayer(p.getName()));
        }
        return list;
    }

    public List<MCOfflinePlayer> getOperators() {
        if(s.getOperators() == null){
            return null;
        }
        List<MCOfflinePlayer> list = new ArrayList<MCOfflinePlayer>();
        for(OfflinePlayer p : s.getOperators()){
            list.add(getOfflinePlayer(p.getName()));
        }
        return list;
    }

    public Economy getEconomy() {
        try{
            @SuppressWarnings("unchecked")
			RegisteredServiceProvider<Economy> economyProvider = (RegisteredServiceProvider<Economy>)
                    s.getServicesManager().getRegistration(Class.forName("net.milkbowl.vault.economy.Economy"));
            if (economyProvider != null) {
                return economyProvider.getProvider();
            }
        } catch(ClassNotFoundException e){
            //Ignored, it means they don't have Vault installed.
        }
        return null;            
    }

    public void runasConsole(String cmd) {
        s.dispatchCommand(s.getConsoleSender(), cmd);
    }
	
	@Override
	public String toString() {
		return s.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BukkitMCServer?s.equals(((BukkitMCServer)obj).s):false);
	}

	@Override
	public int hashCode() {
		return s.hashCode();
	}

	public MCInventory createInventory(MCInventoryHolder holder, MCInventoryType type) {
		InventoryHolder ih = null;
		
		if (holder instanceof MCPlayer) {
			ih = ((BukkitMCPlayer)holder)._Player();
		} else if (holder instanceof MCHumanEntity) {
			ih = ((BukkitMCHumanEntity)holder).asHumanEntity();
		} else if (holder.getHandle() instanceof InventoryHolder) {
			ih = (InventoryHolder)holder.getHandle();
		}
		
		return new BukkitMCInventory(Bukkit.createInventory(ih, InventoryType.valueOf(type.name())));
	}
	
	public MCInventory createInventory(MCInventoryHolder holder, int size) {
		InventoryHolder ih = null;
		
		if (holder instanceof MCPlayer) {
			ih = ((BukkitMCPlayer)holder)._Player();
		} else if (holder instanceof MCHumanEntity) {
			ih = ((BukkitMCHumanEntity)holder).asHumanEntity();
		} else if (holder.getHandle() instanceof InventoryHolder) {
			ih = (InventoryHolder)holder.getHandle();
		}
		
		return new BukkitMCInventory(Bukkit.createInventory(ih, size));
	}
	
	public MCInventory createInventory(MCInventoryHolder holder, int size, String title) {
		InventoryHolder ih = null;
		
		if (holder instanceof MCPlayer) {
			ih = ((BukkitMCPlayer)holder)._Player();
		} else if (holder instanceof MCHumanEntity) {
			ih = ((BukkitMCHumanEntity)holder).asHumanEntity();
		} else if (holder.getHandle() instanceof InventoryHolder) {
			ih = (InventoryHolder)holder.getHandle();
		}
		
		return new BukkitMCInventory(Bukkit.createInventory(ih, size, title));
	}
	
	public void banIP(String address) {
		s.banIP(address);
	}
	
	public Set<String> getIPBans() {
		return s.getIPBans();
	}
	
	public void unbanIP(String address) {
		s.unbanIP(address);
	}
	
	public MCMessenger getMessenger() {
		return new BukkitMCMessenger(s.getMessenger());
}

	public MCScoreboard getMainScoreboard() {
		return new BukkitMCScoreboard(s.getScoreboardManager().getMainScoreboard());
	}

	public MCScoreboard getNewScoreboard() {
		return new BukkitMCScoreboard(s.getScoreboardManager().getNewScoreboard());
	}
}
