
package com.laytonsmith.abstraction;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import com.laytonsmith.abstraction.enums.MCTone;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * This should be implemented once for each server type. It mostly wraps
 * static methods, but also provides methods for getting other server specific
 * things. You can get an instance of the current Convertor by looking for the
 * <code>@convert</code> tag. StaticLayer wraps all the functionality for you
 * however.
 * @author layton
 */
public interface Convertor {

    public MCLocation GetLocation(MCWorld w, double x, double y, double z, float yaw, float pitch);
    public Class GetServerEventMixin();

    public MCEnchantment[] GetEnchantmentValues();

	/**
	 * Returns the enchantment, given an enchantment name (or a string'd number).
	 * Returns null if no such enchantment exists.
	 * @param name
	 * @return 
	 */
    public MCEnchantment GetEnchantmentByName(String name);

    public MCServer GetServer();

    public MCItemStack GetItemStack(int type, int qty);

    public void Startup(CommandHelperPlugin chp);
    
    public int LookupItemId(String materialName);

    public String LookupMaterialName(int id);

    public MCItemStack GetItemStack(int type, int data, int qty);
    
	public MCMaterial getMaterial(int id);
	
    /**
     * A future runnable is run on a server accessible thread at roughly the time specified in the future.
     * This is no guarantee however, as the particular server implementation may make this hard to do. The
     * value returned is 
     * @param r
     * @return 
     */
    public int SetFutureRunnable(DaemonManager dm, long ms, Runnable r);

    public void ClearAllRunnables();

    public void ClearFutureRunnable(int id);

    public int SetFutureRepeater(DaemonManager dm, long ms, long initialDelay, Runnable r);

    public MCEntity GetCorrectEntity(MCEntity e);

	public MCItemMeta GetCorrectMeta(MCItemMeta im);
	
	/**
	 * Returns the entities at the specified location, or null
	 * if no entities are in this location.
	 * @param loc
	 * @return 
	 */
	public List<MCEntity> GetEntitiesAt(MCLocation loc, double radius);
	
	/**
	 * Gets the inventory of the specified entity, or null if the entity id
	 * is invalid
	 * @param entityID
	 * @return 
	 */
	public MCInventory GetEntityInventory(int entityID);
	
	/**
	 * Returns the inventory of the block at the specified location, if it is
	 * an inventory type block, or null if otherwise invalid.
	 * @param location
	 * @return 
	 */
	public MCInventory GetLocationInventory(MCLocation location);
	
	/**
	 * Run whenever the server is shutting down (or restarting). There is no
	 * guarantee provided as to what thread the runnables actually run on, so you should
	 * ensure that the runnable executes it's actions on the appropriate thread
	 * yourself.
	 * @param r 
	 */
	public void addShutdownHook(Runnable r);
	
	/**
	 * Runs all the registered shutdown hooks. This should only be called by the shutdown mechanism.
	 * After running, each Runnable will be removed from the queue.
	 */
	public void runShutdownHooks();
	
	/**
	 * Runs some task on the "main" thread, possibly now, possibly in the future, and
	 * possibly even on this thread. However, if the platform needs some critical action
	 * to happen on one thread, (for instance, UI updates on the UI thread) those actions
	 * will occur here.
	 * @param r 
	 */
	public void runOnMainThreadLater(DaemonManager dm, Runnable r);
	
	/**
	 * Works like runOnMainThreadLater, but waits for the task to finish.
	 * @param r 
	 */
	public <T> T runOnMainThreadAndWait(Callable<T> callable) throws Exception;
	
	/**
	 * Returns a MCWorldCreator object for the given world name.
	 * @param worldName
	 * @return 
	 */
	public MCWorldCreator getWorldCreator(String worldName);

	/**
	 * Gets a note object, which can be used to play a sound
	 * @param octave May be 0-2
	 * @param tone
	 * @param sharp If the note should be a sharp (only applies to some tones)
	 * @return 
	 */
	public MCNote GetNote(int octave, MCTone tone, boolean sharp);
    
	/**
	 * Returns the max block ID number supported by this server.
	 * @return 
	 */
	public int getMaxBlockID();
	
	/**
	 * Returns the max item ID number supported by this server.
	 * @return 
	 */
	public int getMaxItemID();
	
	/**
	 * Returns the max record ID number supported by this server.
	 * @return 
	 */
	public int getMaxRecordID();

	/**
	 * Returns a color object for this server.
	 * @param red
	 * @param green
	 * @param blue
	 * @return 
	 */
	public MCColor GetColor(int red, int green, int blue);
	
	/**
	 * Returns an MCFirework which can be built.
	 * @return 
	 */
	public MCFireworkBuilder GetFireworkBuilder();
	
	public MCPluginMeta GetPluginMeta();
	
	/**
	 * Creates a new properly typed recipe instance
	 * 
	 * @param type the type of recipe
	 * @param result the itemstack the recipe will result in
	 * @return
	 */
	public MCRecipe GetNewRecipe(MCRecipeType type, MCItemStack result);
	
	/**
	 * Used to convert a generic recipe into the correct type
	 * 
	 * @param unspecific type
	 * @return specific type
	 */
	public MCRecipe GetRecipe(MCRecipe unspecific);
	
	/**
	 * 
	 * @param name
	 * @return a new MCCommand instance
	 */
	public MCCommand getNewCommand(String name);
	
	/**
	 * 
	 * @param an ambiguous MCCommandSender
	 * @return a properly typed MCCommandSender
	 */
	public MCCommandSender GetCorrectSender(MCCommandSender unspecific);
}
