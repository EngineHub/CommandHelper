package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.bukkit.pluginmessages.BukkitMCMessenger;
import com.laytonsmith.abstraction.enums.MCInventoryType;
import com.laytonsmith.abstraction.pluginmessages.MCMessenger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.Recipe;
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

	@Override
	public Object getHandle(){
        return s;
    }
    
    public Server __Server(){
        return s;
    }

	@Override
    public String getName() {
        return s.getName();
    }

	@Override
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
    
	@Override
    public boolean dispatchCommand(MCCommandSender sender, String command){
		CommandSender cs;
		if(sender instanceof BukkitMCPlayer){
			cs = ((BukkitMCPlayer)sender).p;
		} else {
			cs = ((BukkitMCCommandSender)sender).c;
		}
        return s.dispatchCommand(cs, command);
    }

	@Override
    public MCPluginManager getPluginManager() {
        if(s.getPluginManager() == null){
            return null;
        }
        return new BukkitMCPluginManager(s.getPluginManager());
    }

	@Override
    public MCPlayer getPlayer(String name) {
        if(s.getPlayer(name) == null){
            return null;
        }
        return new BukkitMCPlayer(s.getPlayer(name));
    }

	@Override
    public MCWorld getWorld(String name) {
        if(s.getWorld(name) == null){
            return null;
        }
        return new BukkitMCWorld(s.getWorld(name));
    }
    
	@Override
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

	@Override
    public void broadcastMessage(String message) {
        s.broadcastMessage(message);
    }

	@Override
	public void broadcastMessage(String message, String permission) {
		s.broadcast(message, permission);
	}
	
	@Override
	public MCConsoleCommandSender getConsole() {
		return new BukkitMCConsoleCommandSender(s.getConsoleSender());
	}

	@Override
	public MCItemFactory getItemFactory() {
		return new BukkitMCItemFactory(s.getItemFactory());
	}
	
	@Override
	public MCCommandMap getCommandMap() {
		return new BukkitMCCommandMap((SimpleCommandMap) ReflectionUtils.invokeMethod(s.getClass(), s, "getCommandMap"));
	}

	@Override
    public MCOfflinePlayer getOfflinePlayer(String player) {
        return new BukkitMCOfflinePlayer(s.getOfflinePlayer(player));
    }

	@Override
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
	@Override
    public String getModVersion() {
        return s.getBukkitVersion();
    }

	@Override
    public String getVersion() {
        return s.getVersion();
    }

	@Override
    public int getPort() {
        return s.getPort();
    }

	@Override
    public Boolean getAllowEnd() {
        return s.getAllowEnd();
    }

	@Override
    public Boolean getAllowFlight() {
        return s.getAllowFlight();
    }

	@Override
    public Boolean getAllowNether() {
        return s.getAllowNether();
    }
    
	@Override
    public Boolean getOnlineMode() {
    	return s.getOnlineMode();
    }

	@Override
    public String getWorldContainer() {
        return s.getWorldContainer().getPath();
    }

	@Override
    public String getServerName() {
        return s.getServerName();
    }

	@Override
    public int getMaxPlayers() {
        return s.getMaxPlayers();
    }

	@Override
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

	@Override
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

	@Override
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

	@Override
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

	@Override
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

	@Override
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
	
	@Override
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
	
	@Override
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
	
	@Override
	public void banIP(String address) {
		s.banIP(address);
	}
	
	@Override
	public Set<String> getIPBans() {
		return s.getIPBans();
	}
	
	@Override
	public void unbanIP(String address) {
		s.unbanIP(address);
	}
	
	@Override
	public MCMessenger getMessenger() {
		return new BukkitMCMessenger(s.getMessenger());
	}

	@Override
	public MCScoreboard getMainScoreboard() {
		return new BukkitMCScoreboard(s.getScoreboardManager().getMainScoreboard());
	}

	@Override
	public MCScoreboard getNewScoreboard() {
		return new BukkitMCScoreboard(s.getScoreboardManager().getNewScoreboard());
	}
	
	@Override
	public boolean unloadWorld(MCWorld world, boolean save) {
		return s.unloadWorld(((BukkitMCWorld) world).__World(), save);
	}

	@Override
	public void shutdown() {
		s.shutdown();
	}
	
	@Override
	public boolean addRecipe(MCRecipe recipe) {
		return s.addRecipe(((BukkitMCRecipe) recipe).r);
	}
	
	@Override
	public List<MCRecipe> getRecipesFor(MCItemStack result) {
		List<MCRecipe> ret = new ArrayList<MCRecipe>();
		List<Recipe> recipes = s.getRecipesFor(((BukkitMCItemStack) result).__ItemStack());
		for (Recipe recipe : recipes) {
			ret.add(BukkitConvertor.BukkitGetRecipe(recipe));
		}
		return ret;
	}
	
	@Override
	public List<MCRecipe> allRecipes() {
		List<MCRecipe> ret = new ArrayList<MCRecipe>();
		for (Iterator recipes = s.recipeIterator(); recipes.hasNext();) {
			Recipe recipe = (Recipe) recipes.next();
			ret.add(BukkitConvertor.BukkitGetRecipe(recipe));
		}
		return ret;
	}
	
//	public Iterator<MCRecipe> recipe iterator() {
//		Iterator<MCRecipe> ret = //create iterator;
//	}
	
	@Override
	public void clearRecipes() {
		s.clearRecipes();
	}
	
	@Override
	public void resetRecipes() {
		s.resetRecipes();
	}
}
