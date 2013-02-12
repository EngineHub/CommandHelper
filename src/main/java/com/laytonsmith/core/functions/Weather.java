package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;

/**
 *
 * @author Layton
 */
public class Weather {

	public static String docs() {
		return "Provides functions to control the weather";
	}

	@api(environments=CommandHelperEnvironment.class)
	public static class lightning extends AbstractFunction {

		public String getName() {
			return "lightning";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3, 4};
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.LengthException, ExceptionType.InvalidWorldException, ExceptionType.FormatException};
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			int x;
			int y;
			int z;
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
					w.strikeLightning(StaticLayer.GetLocation(w, x, y + 1, z));
				} else {
					w.strikeLightningEffect(StaticLayer.GetLocation(w, x, y + 1, z));
				}
			} else {
				throw new ConfigRuntimeException("World was not specified", ExceptionType.InvalidWorldException, t);
			}

			return new CVoid(t);
		}

		public String docs() {
			return "void {strikeLocArray, [safe] | x, y, z, [safe]} Makes lightning strike at the x y z coordinates specified in the array(x, y, z). Safe"
					+ " defaults to false, but if true, lightning striking a player will not hurt them.";
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments=CommandHelperEnvironment.class)
	public static class storm extends AbstractFunction {

		public String getName() {
			return "storm";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			boolean b = Static.getBoolean(args[0]);
			MCWorld w = null;
			if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
				w = env.getEnv(CommandHelperEnvironment.class).GetPlayer().getWorld();
			}
			if (args.length == 2) {
				w = Static.getServer().getWorld(args[1].val());
			}
			if (w != null) {
				w.setStorm(b);
			} else {
				throw new ConfigRuntimeException("World was not specified", ExceptionType.InvalidWorldException, t);
			}
			if (args.length == 3) {
				w.setWeatherDuration(Static.getInt32(args[2], t));
			}
			return new CVoid(t);
		}

		public String docs() {
			return "void {isStorming, [world], [int]} Creates a (rain) storm if isStorming is true, stops a storm if"
					+ " isStorming is false. The third argument allows setting how long this weather setting will last.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.InvalidWorldException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		public Boolean runAsync() {
			return false;
		}
	}
	
	@api(environments=CommandHelperEnvironment.class)
	public static class set_thunder extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

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
				throw new ConfigRuntimeException("No existing world specified!", ExceptionType.InvalidWorldException, t);
			}
			if (args.length == 3) {
				w.setThunderDuration(Static.getInt32(args[2], t));
			}
			return new CVoid(t);
		}

		public String getName() {
			return "set_thunder";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		public String docs() {
			return "void {boolean, [world], [int]} Sets whether or not the weather can have thunder. The third argument"
					+ " can specify how long the thunder should last.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api(environments=CommandHelperEnvironment.class)
	public static class has_storm extends AbstractFunction {
		
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InvalidWorldException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return false;
		}

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
				return new CBoolean(w.isStorming(), t);
			} else {
				throw new ConfigRuntimeException("No existing world specified!", ExceptionType.InvalidWorldException, t);
			}
		}

		public String getName() {
			return "has_storm";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "boolean {[world]} Returns whether the world (defaults to player's world) has a storm.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api(environments=CommandHelperEnvironment.class)
	public static class has_thunder extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InvalidWorldException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return false;
		}

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
				return new CBoolean(w.isThundering(), t);
			} else {
				throw new ConfigRuntimeException("No existing world specified!", ExceptionType.InvalidWorldException, t);
			}
		}

		public String getName() {
			return "has_thunder";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "boolean {[world]} Returns whether the world (defaults to player's world) has thunder.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
}
