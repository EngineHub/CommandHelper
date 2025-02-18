package com.laytonsmith.abstraction;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.entities.MCTransformation;
import com.laytonsmith.abstraction.enums.MCAttribute;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;
import com.laytonsmith.abstraction.enums.MCEquipmentSlotGroup;
import com.laytonsmith.abstraction.enums.MCPatternShape;
import com.laytonsmith.abstraction.enums.MCPotionType;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import com.laytonsmith.abstraction.enums.MCTone;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * This should be implemented once for each server type. It mostly wraps static methods, but also provides methods for
 * getting other server specific things. You can get an instance of the current Convertor by looking for the
 * <code>@convert</code> tag. StaticLayer wraps all the functionality for you however.
 */
public interface Convertor {

	MCLocation GetLocation(MCWorld w, double x, double y, double z, float yaw, float pitch);

	Class GetServerEventMixin();

	MCServer GetServer();

	MCItemStack GetItemStack(MCMaterial type, int qty);

	MCItemStack GetItemStack(String type, int qty);

	MCPotionData GetPotionData(MCPotionType type, boolean extended, boolean upgraded);

	MCAttributeModifier GetAttributeModifier(MCAttribute attr, UUID id, String name, double amt, MCAttributeModifier.Operation op, MCEquipmentSlot slot);

	MCAttributeModifier GetAttributeModifier(MCAttribute attr, UUID id, String name, double amt, MCAttributeModifier.Operation op, MCEquipmentSlotGroup slot);

	MCAttributeModifier GetAttributeModifier(MCAttribute attr, MCNamespacedKey key, double amt, MCAttributeModifier.Operation op, MCEquipmentSlot slot);

	MCAttributeModifier GetAttributeModifier(MCAttribute attr, MCNamespacedKey key, double amt, MCAttributeModifier.Operation op, MCEquipmentSlotGroup slot);

	void Startup(CommandHelperPlugin chp);

	MCMaterial[] GetMaterialValues();

	MCMaterial GetMaterialFromLegacy(String name, int data);

	MCMaterial GetMaterialFromLegacy(int id, int data);

	MCMaterial GetMaterial(String name);

	MCMetadataValue GetMetadataValue(Object value, MCPlugin plugin);

	/**
	 * A future runnable is run on a server accessible thread at roughly the time specified in the future. This is no
	 * guarantee however, as the particular server implementation may make this hard to do. The value returned is
	 *
	 * @param dm
	 * @param ms
	 * @param r
	 * @return
	 */
	int SetFutureRunnable(DaemonManager dm, long ms, Runnable r);

	/**
	 * Clears all future runnables, but does not interrupt existing ones.
	 */
	void ClearAllRunnables();

	/**
	 * Clears a future runnable task by id.
	 *
	 * @param id
	 */
	void ClearFutureRunnable(int id);

	/**
	 * Adds a future repeater
	 *
	 * @param dm
	 * @param ms
	 * @param initialDelay
	 * @param r
	 * @return
	 */
	int SetFutureRepeater(DaemonManager dm, long ms, long initialDelay, Runnable r);

	MCEntity GetCorrectEntity(MCEntity e);

	MCItemMeta GetCorrectMeta(MCItemMeta im);

	/**
	 * Returns the entities at the specified location, or null if no entities are in this location.
	 *
	 * @param loc
	 * @return
	 */
	List<MCEntity> GetEntitiesAt(MCLocation loc, double radius);

	/**
	 * Gets the inventory of the specified entity, or null if the entity id is invalid
	 *
	 * @param entity
	 * @return
	 */
	MCInventory GetEntityInventory(MCEntity entity);

	/**
	 * Returns the inventory of the block at the specified location, if it is an inventory type block, or null if
	 * otherwise invalid.
	 *
	 * @param location
	 * @return
	 */
	MCInventory GetLocationInventory(MCLocation location);

	MCInventoryHolder CreateInventoryHolder(String id, String title);

	/**
	 * Runs whenever the server is shutting down (or reloading). There is no guarantee provided as to what thread the
	 * runnables actually run on, so you should ensure that the runnable executes it's actions on the appropriate thread
	 * yourself. Note that this shutdown hook will only run once, so if multiple reload events occur, this will not
	 * be registered for the second run, unless you specifically add it yourself. If you need a shutdown hook to run
	 * every time, and only want to register it once, see {@link #addPersistentShutdownHook}.
	 *
	 * @param r
	 */
	void addShutdownHook(Runnable r);

	/**
	 * Runs whenever the server is shutting down (or reloading). There is no guarantee provided as to what thread the
	 * runnables actually run on, so you should ensure that the runnable executes it's actions on the appropriate thread
	 * yourself. Note that this shutdown hook will never dequeue, so if multiple reload events occur, this will run
	 * every time, and so you should only call this once (i.e. from a static context). If you need a shutdown hook to
	 * dequeue after run, see {@link #addShutdownHook}.
	 * @param r
	 */
	void addPersistentShutdownHook(Runnable r);

	/**
	 * Runs all the registered shutdown hooks. This should only be called by the shutdown mechanism. After running, each
	 * Runnable will be removed from the queue.
	 */
	void runShutdownHooks();

	/**
	 * Runs some task on the "main" thread, possibly now, possibly in the future, and possibly even on this thread.
	 * However, if the platform needs some critical action to happen on one thread, (for instance, UI updates on the UI
	 * thread) those actions will occur here.
	 *
	 * @param dm
	 * @param r
	 */
	void runOnMainThreadLater(DaemonManager dm, Runnable r);

	/**
	 * Works like runOnMainThreadLater, but waits for the task to finish.
	 *
	 * @param <T>
	 * @param callable
	 * @return
	 * @throws java.lang.Exception
	 */
	<T> T runOnMainThreadAndWait(Callable<T> callable) throws Exception;

	/**
	 * Returns a MCWorldCreator object for the given world name.
	 *
	 * @param worldName
	 * @return
	 */
	MCWorldCreator getWorldCreator(String worldName);

	/**
	 * Gets a note object, which can be used to play a sound
	 *
	 * @param octave May be 0-2
	 * @param tone
	 * @param sharp If the note should be a sharp (only applies to some tones)
	 * @return
	 */
	MCNote GetNote(int octave, MCTone tone, boolean sharp);

	/**
	 * Returns a color object for this server.
	 *
	 * @param red
	 * @param green
	 * @param blue
	 * @return
	 */
	MCColor GetColor(int red, int green, int blue);

	/**
	 * Returns a transparent color object for this server.
	 *
	 * @param red
	 * @param green
	 * @param blue
	 * @param alpha
	 * @return
	 */
	MCColor GetColor(int red, int green, int blue, int alpha);

	/**
	 * Returns a color object given the color name. The color name must come from the standard color types, or a
	 * FormatException is thrown.
	 *
	 * @param colorName
	 * @param t
	 * @return
	 */
	MCColor GetColor(String colorName, Target t) throws CREFormatException;

	/**
	 * Returns a pattern object
	 *
	 * @param color
	 * @param shape
	 */
	MCPattern GetPattern(MCDyeColor color, MCPatternShape shape);

	/**
	 * Returns an MCFirework which can be built.
	 *
	 * @return
	 */
	MCFireworkBuilder GetFireworkBuilder();

	MCPluginMeta GetPluginMeta();

	/**
	 * Creates a new properly typed recipe instance
	 *
	 * @param type the type of recipe
	 * @param result the itemstack the recipe will result in
	 * @return
	 */
	MCRecipe GetNewRecipe(String key, MCRecipeType type, MCItemStack result);

	/**
	 * Used to convert a generic recipe into the correct type
	 *
	 * @param unspecific type
	 * @return specific type
	 */
	MCRecipe GetRecipe(MCRecipe unspecific);

	/**
	 *
	 * @param name
	 * @return a new MCCommand instance
	 */
	MCCommand getNewCommand(String name);

	/**
	 *
	 * @param unspecific an ambiguous MCCommandSender
	 * @return a properly typed MCCommandSender
	 */
	MCCommandSender GetCorrectSender(MCCommandSender unspecific);

	/**
	 * Returns the name of CommandHelper by parsing the plugin.yml file.
	 *
	 * @return
	 */
	String GetPluginName();

	/**
	 * Returns a MCPlugin instance of CommandHelper.
	 *
	 * @return
	 */
	MCPlugin GetPlugin();

	/**
	 * Returns the name of the current user, or null if this doesn't make sense in the given platform.
	 *
	 * @param env The runtime environment, in case the convertor needs it
	 * @return The username
	 */
	String GetUser(Environment env);

	/**
	 * Returns a Minecraft namespaced key object from a string.
	 * The key can only alphanumeric characters, dots, underscores, and dashes.
	 * A preceding namespace can be delimited with a single colon, which can also have forward slashes.
	 * Example: "path/commandhelper:my_tag".
	 * If no namespace is given, it will default to "commandhelper".
	 *
	 * @param key a string formatted key
	 * @return a key object
	 */
	MCNamespacedKey GetNamespacedKey(String key);

	/**
	 * Returns a new Transformation object.
	 * @param leftRotation
	 * @param rightRotation
	 * @param scale
	 * @param translation
	 * @return
	 */
	public MCTransformation GetTransformation(Quaternionf leftRotation, Quaternionf rightRotation, Vector3f scale, Vector3f translation);

	/**
	 * Returns true if this is the main thread of the application. This is only applicable in some managed environments,
	 * in other environments where this doesn't matter, this will always return false (i.e. all threads are considered
	 * equally important/unimportant). If this returns true, this means that the current thread is for instance the
	 * UI thread, and thus should not be blocked on.
	 * @return
	 */
	public boolean IsMainThread();
}
