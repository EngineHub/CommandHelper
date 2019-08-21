package com.laytonsmith.abstraction;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.enums.MCPotionType;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import com.laytonsmith.annotations.convert;
import com.laytonsmith.commandhelper.CommandHelperPlugin;

import java.util.Set;

/**
 * Unfortunately some methods just can't be overridden.
 *
 */
public final class StaticLayer {

	private StaticLayer() {
	}
	//Do not rename this field, it is used reflectively in testing
	private static Convertor convertor = null;

	static {
		InitConvertor();
	}

	private static synchronized void InitConvertor() {
		Set<Class<?>> classes = ClassDiscovery.getDefaultInstance().loadClassesWithAnnotation(convert.class);
		for(Class<?> c : classes) {
			if(!Convertor.class.isAssignableFrom(c)) {
				StreamUtils.GetSystemErr().println("The Convertor " + c.getSimpleName() + " doesn't implement Convertor!");
			}
			convert convert = c.getAnnotation(convert.class);
			if(convert.type() == Implementation.GetServerType()) {
				//This is what we're looking for, instatiate it.
				try {
					if(convertor != null) {
						//Uh... There are more than one implementations for this server type
						System.out.println("More than one Convertor for this server type was detected!");
					}
					convertor = (Convertor) c.newInstance();
					//At this point we are all set
				} catch (Exception e) {
					StreamUtils.GetSystemErr().println("Tried to instantiate the Convertor, but couldn't!");
				}
			}
		}
		if(convertor == null) {
			StreamUtils.GetSystemErr().println("Could not find a suitable convertor! You will experience serious issues with this plugin.");
		}
	}

	public static MCLocation GetLocation(MCWorld w, double x, double y, double z, float yaw, float pitch) {
		return convertor.GetLocation(w, x, y, z, yaw, pitch);
	}

	public static MCLocation GetLocation(MCWorld w, double x, double y, double z) {
		return GetLocation(w, x, y, z, 0, 0);
	}

	public static Class<?> GetServerEventMixin() {
		return convertor.GetServerEventMixin();
	}

	public static MCMaterial GetMaterialFromLegacy(int type, int data) {
		return convertor.GetMaterialFromLegacy(type, data);
	}

	public static MCMaterial GetMaterialFromLegacy(String name, int data) {
		return convertor.GetMaterialFromLegacy(name, data);
	}

	public static MCItemStack GetItemStack(String type, int qty) {
		return convertor.GetItemStack(type, qty);
	}

	public static MCItemStack GetItemStack(MCMaterial type, int qty) {
		return convertor.GetItemStack(type, qty);
	}

	public static MCPotionData GetPotionData(MCPotionType type, boolean extended, boolean upgraded) {
		return convertor.GetPotionData(type, extended, upgraded);
	}

	public static MCServer GetServer() {
		return convertor.GetServer();
	}

	public static MCEnchantment GetEnchantmentByName(String name) {
		return convertor.GetEnchantmentByName(name);
	}

	public static MCMaterial[] GetMaterialValues() {
		return convertor.GetMaterialValues();
	}

	public static MCMaterial GetMaterial(String name) {
		return convertor.GetMaterial(name);
	}

	public static MCEnchantment[] GetEnchantmentValues() {
		return convertor.GetEnchantmentValues();
	}

	public static void Startup(CommandHelperPlugin chp) {
		convertor.Startup(chp);
	}

	public static MCMetadataValue GetMetadataValue(Object value, MCPlugin plugin) {
		return convertor.GetMetadataValue(value, plugin);
	}

	/**
	 * Adds a runnable to the main thread, if required by this platform, if a multithreaded user code would be
	 * dangerous.
	 *
	 * @param ms
	 * @param r
	 * @return
	 */
	public static int SetFutureRunnable(DaemonManager dm, long ms, Runnable r) {
		return convertor.SetFutureRunnable(dm, ms, r);
	}

	public static int SetFutureRepeater(DaemonManager dm, long ms, long initialDelay, Runnable r) {
		return convertor.SetFutureRepeater(dm, ms, initialDelay, r);
	}

	public static void ClearAllRunnables() {
		convertor.ClearAllRunnables();
	}

	public static void ClearFutureRunnable(int id) {
		convertor.ClearFutureRunnable(id);
	}

	/**
	 * Given an entity, returns the more specific entity type, by creating a new more specific type based on the actual
	 * type of the underlying object contained by the more generic type.
	 *
	 * @param e
	 * @return
	 */
	public static MCEntity GetCorrectEntity(MCEntity e) {
		return convertor.GetCorrectEntity(e);
	}

	public static MCRecipe GetNewRecipe(String key, MCRecipeType type, MCItemStack result) {
		return convertor.GetNewRecipe(key, type, result);
	}

	public static String GetPluginName() {
		return convertor.GetPluginName();
	}

	public static MCPlugin GetPlugin() {
		return convertor.GetPlugin();
	}

	public static synchronized Convertor GetConvertor() {
		return convertor;
	}
}
