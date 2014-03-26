package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCMetadataValue;
import com.laytonsmith.abstraction.MCMetadatable;
import com.laytonsmith.abstraction.MCPlugin;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.util.List;

/**
 *
 * @author Veyyn
 */
public class BukkitMetadata {

	public static String docs() {
		return "Allow to manipulate the Bukkit metadata.";
	}

	public static abstract class MetadataFunction extends AbstractFunction {

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		@Override
		public Exceptions.ExceptionType[] thrown() {
			return new Exceptions.ExceptionType[]{ExceptionType.BadEntityException, ExceptionType.CastException, ExceptionType.FormatException, ExceptionType.InvalidPluginException, ExceptionType.InvalidWorldException, ExceptionType.PlayerOfflineException};
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class get_metadata extends MetadataFunction {

		@Override
		public String getName() {
			return "get_metadata";
		}

		@Override
		public String docs() {
			return "mixed {[object], key | object, key, [plugin]} Returns the metadata values attached to the given object."
					+ " object can be a location array (it will designate a block), an entityID (it will designate an entity)"
					+ " or a string (it will designate a world). If only the key is given, the object is the current player."
					+ " The function returns an associative array where the values are keyed by plugin which have registered"
					+ " the metadata with the given key, and the array values the registered metadata values. If the plugin argument is given"
					+ " (a string that represent the plugin name), the function simply returns the value of the metadata registered"
					+ " by the plugin with this key, or null if no metadata is found. ---"
					+ " The Bukkit metadata allow to attach informations to entities, blocks and worlds, and allow plugins"
					+ " to exchange these informations between them without requiring one to be dependant on each other."
					+ " The metadata are persistent across server reloads, but not across server restarts. The metadata attached"
					+ " to a player are also persistent between logins.";
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			List<MCMetadataValue> metadata;
			if (args.length == 1) {
				metadata = Static.getPlayer(environment, t).getMetadata(args[0].val());
			} else {
				metadata = Static.getMetadatable(args[0], t).getMetadata(args[1].val());
			}
			if (args.length == 3) {
				MCPlugin plugin = Static.getPlugin(args[2], t);
				for (MCMetadataValue value : metadata) {
					if (value.getOwningPlugin().equals(plugin)) {
						return Static.getMSObject(value.value(), t);
					}
				}
				return new CNull(t);
			} else {
				CArray values = new CArray(t);
				for (MCMetadataValue value : metadata) {
					values.set(value.getOwningPlugin().getName(), Static.getMSObject(value.value(), t), t);
				}
				return values;
			}
		}
	}

	@api
	@seealso(get_metadata.class)
	public static class has_metadata extends MetadataFunction {

		@Override
		public String getName() {
			return "has_metadata";
		}

		@Override
		public String docs() {
			return "boolean {[object], key | object, key, [plugin]} Returns if the given object has metadata registered with this key."
					+ " object can be a location array (it will designate a block), an entityID (it will designate an entity)"
					+ " or a string (it will designate a world). If only the key is given, the object is the current player."
					+ " If the plugin is given, the function returns if the plugin have registered a metadata in the object with the given key."
					+ " See get_metadata() for more informations about Bukkit metadata.";
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			String key;
			MCMetadatable metadatable;
			if (args.length == 1) {
				metadatable = Static.getPlayer(environment, t);
				key = args[0].val();
			} else {
				metadatable = Static.getMetadatable(args[0], t);
				key = args[1].val();
			}
			if (metadatable.hasMetadata(key)) {
				if (args.length == 3) {
					MCPlugin plugin = Static.getPlugin(args[2], t);
					for (MCMetadataValue value : metadatable.getMetadata(key)) {
						if (value.getOwningPlugin().equals(plugin)) {
							return new CBoolean(true, t);
						}
					}
					return new CBoolean(false, t);
				} else {
					return new CBoolean(true, t);
				}
			} else {
				return new CBoolean(false, t);
			}
		}
	}

	@api
	@seealso(get_metadata.class)
	public static class set_metadata extends MetadataFunction {

		@Override
		public String getName() {
			return "set_metadata";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3, 4};
		}

		@Override
		public String docs() {
			return "void {[mixed], key, value | mixed, key, value, [plugin]} Registers a metadata value in the given object with the"
					+ " given key. object can be a location array (it will designate a block), an entityID (it will designate an entity)"
					+ " or a string (it will designate a world). If only the key and the value are given, the object is the current player."
					+ " You can specify the plugin that will own the metadata, 'CommandHelper' by default. See get_metadata() for more"
					+ " informations about Bukkit metadata.";
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			String key;
			MCMetadatable metadatable;
			Construct value;
			MCPlugin plugin;
			if (args.length == 2) {
				metadatable = Static.getPlayer(environment, t);
				key = args[0].val();
				value = args[1];
				plugin = Static.getServer().getPluginManager().getPlugin("CommandHelper");
			} else {
				metadatable = Static.getMetadatable(args[0], t);
				key = args[1].val();
				value = args[2];
				plugin = (args.length == 4) ? Static.getPlugin(args[3], t) : Static.getServer().getPluginManager().getPlugin("CommandHelper");
			}
			metadatable.setMetadata(key, ObjectGenerator.GetGenerator().metadataValue(value, plugin));
			return new CVoid(t);
		}
	}

	@api
	@seealso(get_metadata.class)
	public static class remove_metadata extends MetadataFunction {

		@Override
		public String getName() {
			return "remove_metadata";
		}

		@Override
		public String docs() {
			return "void {[mixed], key | mixed, key, [plugin]} Remove the metadata in the given object at the given key."
					+ " object can be a location array (it will designate a block), an entityID (it will designate an entity)"
					+ " or a string (it will designate a world). If only the key is given, the object is the current player."
					+ " If no plugin is given, the function removes all metadata at the given key, otherwise only the value"
					+ " set by the given plugin. See get_metadata() for more informations about Bukkit metadata.";
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			String key;
			MCMetadatable metadatable;
			if (args.length == 1) {
				metadatable = Static.getPlayer(environment, t);
				key = args[0].val();
			} else {
				metadatable = Static.getMetadatable(args[0], t);
				key = args[1].val();
			}
			if (args.length == 3) {
				metadatable.removeMetadata(key, Static.getPlugin(args[2], t));
			} else {
				for (MCMetadataValue value : metadatable.getMetadata(key)) {
					metadatable.removeMetadata(key, value.getOwningPlugin());
				}
			}
			return new CVoid(t);
		}
	}
}