package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.*;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.arguments.ArgumentBuilder;
import com.laytonsmith.core.arguments.Signature;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.MLocation;

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
			int x, y, z, ent;
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
				x = (int) java.lang.Math.floor(args[0].primitive(t).castToDouble(t));
				y = (int) java.lang.Math.floor(args[1].primitive(t).castToDouble(t));
				z = (int) java.lang.Math.floor(args[2].primitive(t).castToDouble(t));
				safeIndex = 3;
			}
			if (args.length >= safeIndex + 1) {
				safe = args[safeIndex].primitive(t).castToBoolean();
			}
			if (w != null) {
				if (!safe) {
					ent = w.strikeLightning(StaticLayer.GetLocation(w, x, y + 1, z)).getEntityId();
				} else {
					ent = w.strikeLightningEffect(StaticLayer.GetLocation(w, x, y + 1, z)).getEntityId();
				}
			} else {
				throw new ConfigRuntimeException("World was not specified", ExceptionType.InvalidWorldException, t);
			}

			return new CInt(ent, t);
		}

		public String docs() {
			return "Makes lightning strike at the x y z coordinates specified in the array(x, y, z). Safe"
					+ " defaults to false, but if true, lightning striking a player will not hurt them.";
		}
		
		public Argument returnType() {
			return new Argument("The entityID of the lightning bolt", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Signature(1,
							new Argument("", MLocation.class, "location"),
							new Argument("", CBoolean.class, "safe").setOptionalDefault(false)
						), new Signature(2,
							new Argument("", CDouble.class, "x"),
							new Argument("", CDouble.class, "y"),
							new Argument("", CDouble.class, "z"),
							new Argument("", CBoolean.class, "safe").setOptionalDefault(false)
						)
					);
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
			boolean b = args[0].primitive(t).castToBoolean();
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
			if (args.length == 3 && !args[2].isNull()) {
				w.setWeatherDuration(args[2].primitive(t).castToInt32(t));
			}
			return new CVoid(t);
		}

		public String docs() {
			return "Creates a (rain) storm if isStorming is true, stops a storm if"
					+ " isStorming is false. The third argument allows setting how long this weather setting will last.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CBoolean.class, "isStorming"),
						new Argument("", CString.class, "world").setOptionalDefaultNull(),
						new Argument("", CInt.class, "length").setOptionalDefaultNull()
					);
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
				w.setThundering(args[0].primitive(t).castToBoolean());
			} else {
				throw new ConfigRuntimeException("No existing world specified!", ExceptionType.InvalidWorldException, t);
			}
			if (args.length == 3 && !args[2].isNull()) {
				w.setThunderDuration(args[2].primitive(t).castToInt32(t));
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
			return "Sets whether or not the weather can have thunder. The third argument"
					+ " can specify how long the thunder should last.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CBoolean.class, "isThundering"),
						new Argument("", CString.class, "world").setOptionalDefaultNull(),
						new Argument("", CInt.class, "length").setOptionalDefaultNull()
					);
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
			return "Returns whether the world (defaults to player's world) has a storm.";
		}
		
		public Argument returnType() {
			return new Argument("", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "world").setOptionalDefaultNull()
					);
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
			return "Returns whether the world (defaults to player's world) has thunder.";
		}
		
		public Argument returnType() {
			return new Argument("", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "world").setOptionalDefaultNull()
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
}
