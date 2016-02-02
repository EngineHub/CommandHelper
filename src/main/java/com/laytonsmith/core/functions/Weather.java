package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCBlockCommandSender;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREInvalidWorldException;
import com.laytonsmith.core.exceptions.CRE.CRELengthException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;

import java.util.UUID;

/**
 * 
 */
public class Weather {

	public static String docs() {
		return "Provides functions to control the weather";
	}

	@api(environments=CommandHelperEnvironment.class)
	public static class lightning extends AbstractFunction {

		@Override
		public String getName() {
			return "lightning";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3, 4};
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRELengthException.class, CREInvalidWorldException.class, CREFormatException.class};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			int x, y, z;
			UUID ent;
			MCWorld w = null;
			boolean safe = false;
			int safeIndex = 1;
			if (args[0] instanceof CArray) {
				CArray a = (CArray) args[0];
				MCLocation l = ObjectGenerator.GetGenerator().location(a, (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer ? env.getEnv(CommandHelperEnvironment.class).GetPlayer().getWorld() : null), t);
				x = (int) java.lang.Math.floor(l.getX());
				y = (int) java.lang.Math.floor(l.getY());
				z = (int) java.lang.Math.floor(l.getZ());
				w = l.getWorld();
			} else {
				x = (int) java.lang.Math.floor(Static.getNumber(args[0], t));
				y = (int) java.lang.Math.floor(Static.getNumber(args[1], t));
				z = (int) java.lang.Math.floor(Static.getNumber(args[2], t));
				safeIndex = 3;
			}
			if (args.length >= safeIndex + 1) {
				safe = Static.getBoolean(args[safeIndex]);
			}
			if (w != null) {
				if (!safe) {
					ent = w.strikeLightning(StaticLayer.GetLocation(w, x, y + 1, z)).getUniqueId();
				} else {
					ent = w.strikeLightningEffect(StaticLayer.GetLocation(w, x, y + 1, z)).getUniqueId();
				}
			} else {
				throw ConfigRuntimeException.BuildException("World was not specified", CREInvalidWorldException.class, t);
			}

			return new CString(ent.toString(), t);
		}

		@Override
		public String docs() {
			return "int {strikeLocArray, [safe] | x, y, z, [safe]} Makes"
					+ " lightning strike at the x y z coordinates specified"
					+ " in the array(x, y, z). Safe  defaults to false, but"
					+ " if true, lightning striking a player will not hurt"
					+ " them. Returns the entityID of the lightning bolt.";
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments=CommandHelperEnvironment.class)
	public static class storm extends AbstractFunction {

		@Override
		public String getName() {
			return "storm";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			boolean b = Static.getBoolean(args[0]);
			MCWorld w = null;
			int duration = -1;
			if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
				w = env.getEnv(CommandHelperEnvironment.class).GetPlayer().getWorld();
			}
			if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCBlockCommandSender) {
				w = env.getEnv(CommandHelperEnvironment.class).GetBlockCommandSender().getBlock().getWorld();
			}
			if (args.length == 2) {
				if (args[1] instanceof CString) {
					w = Static.getServer().getWorld(args[1].val());
				} else if (args[1] instanceof CInt) {
					duration = Static.getInt32(args[1], t);
				} else {
					throw ConfigRuntimeException.BuildException("", CREFormatException.class, t);
				}
			}
			if (args.length == 3) {
				w = Static.getServer().getWorld(args[1].val());
				duration = Static.getInt32(args[2], t);
			}
			if (w != null) {
				w.setStorm(b);
				if (duration > 0) {
					w.setWeatherDuration(duration);
				}
			} else {
				throw ConfigRuntimeException.BuildException("World was not specified", CREInvalidWorldException.class, t);
			}
			return CVoid.VOID;
		}

		@Override
		public String docs() {
			return "void {isStorming, [world], [int]} Creates a (rain) storm if isStorming is true, stops a storm if"
					+ " isStorming is false. The second argument can be a world name or the duration in ticks of the"
					+ " given weather setting. The third argument allows specifying both a world and a duration."
					+ " The second param is required to be the world if the function is run from console.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class,
					CREInvalidWorldException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}
	
	@api(environments=CommandHelperEnvironment.class)
	public static class set_thunder extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCWorld w = null;
			if (args.length == 1) {
				if (environment.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
					w = environment.getEnv(CommandHelperEnvironment.class).GetPlayer().getWorld();
				}
			} else {
				w = Static.getServer().getWorld(args[1].val());
			}
			if (w != null) {
				w.setThundering(Static.getBoolean(args[0]));
			} else {
				throw ConfigRuntimeException.BuildException("No existing world specified!", CREInvalidWorldException.class, t);
			}
			if (args.length == 3) {
				w.setThunderDuration(Static.getInt32(args[2], t));
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_thunder";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		@Override
		public String docs() {
			return "void {boolean, [world], [int]} Sets whether or not the weather can have thunder. The third argument"
					+ " can specify how long the thunder should last.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api(environments=CommandHelperEnvironment.class)
	public static class has_storm extends AbstractFunction {
		
		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCWorld w = null;
			if (args.length == 1) {
				w = Static.getServer().getWorld(args[0].val());
			} else {
				if (environment.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
					w = environment.getEnv(CommandHelperEnvironment.class).GetPlayer().getWorld();
				}
			}
			if (w != null) {
				return CBoolean.get(w.isStorming());
			} else {
				throw ConfigRuntimeException.BuildException("No existing world specified!", CREInvalidWorldException.class, t);
			}
		}

		@Override
		public String getName() {
			return "has_storm";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "boolean {[world]} Returns whether the world (defaults to player's world) has a storm.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api(environments=CommandHelperEnvironment.class)
	public static class has_thunder extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCWorld w = null;
			if (args.length == 1) {
				w = Static.getServer().getWorld(args[0].val());
			} else {
				if (environment.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
					w = environment.getEnv(CommandHelperEnvironment.class).GetPlayer().getWorld();
				}
			}
			if (w != null) {
				return CBoolean.get(w.isThundering());
			} else {
				throw ConfigRuntimeException.BuildException("No existing world specified!", CREInvalidWorldException.class, t);
			}
		}

		@Override
		public String getName() {
			return "has_thunder";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "boolean {[world]} Returns whether the world (defaults to player's world) has thunder.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
}
