package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCBlockCommandSender;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCConsoleCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.PermissionsResolver;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.arguments.ArgumentBuilder;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 */
public class Permissions {

	public static String docs() {
		return "Provides access to the server's underlying permissions system. Permissions functionality is only as good as the management"
				+ " system in place, however, and so not all functions may be supported on a given system.";
	}

	@api(environments={CommandHelperEnvironment.class, GlobalEnv.class})
	public static class has_permission extends AbstractFunction {

		public String getName() {
			return "has_permission";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Using the built in permissions system, checks to see if the player has a particular permission."
					+ " This is simply passed through to the permissions system. This function does not throw a PlayerOfflineException, because"
					+ " it works with offline players, but that means that names must be an exact match. If you notice, this function isn't"
					+ " restricted. However, it IS restricted if the player attempts to check another player's permissions. If run from"
					+ " the console, will always return true.";
		}
		
		public Argument returnType() {
			return new Argument("", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "player").setOptionalDefaultNull(),
						new Argument("", CString.class, "permissionName")
					);
		}

		public Exceptions.ExceptionType[] thrown() {
			return new Exceptions.ExceptionType[]{ExceptionType.InsufficientPermissionException, ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String player = null;
			String permission = null;

			if (args.length == 1) {
				final MCCommandSender mcc = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
				if (mcc instanceof MCConsoleCommandSender || mcc instanceof MCBlockCommandSender) {
					// Console and CommandBlocks always have permission
					return new CBoolean(true, t);
				}

				MCPlayer mcp = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
				if(mcp == null){
					throw new ConfigRuntimeException("No player was specified", Exceptions.ExceptionType.PlayerOfflineException, t);
				}
				player = mcp.getName();
				permission = args[0].val();
			} else {
				player = args[0].val();
				permission = args[1].val();
			}

			if (environment.getEnv(CommandHelperEnvironment.class).GetPlayer() != null && !environment.getEnv(CommandHelperEnvironment.class).GetPlayer().getName().equals(player)) {
				if (!Static.hasCHPermission(getName(), environment)) {
					throw new ConfigRuntimeException("You do not have permission to use the " + getName() + " function.",
							Exceptions.ExceptionType.InsufficientPermissionException, t);
				}
			}

			if (Static.getConsoleName().equals(player.toLowerCase()) || player.startsWith(Static.getBlockPrefix())) {
				// Console and CommandBlocks always have permission
				return new CBoolean(true, t);
			}

			PermissionsResolver perms = environment.getEnv(GlobalEnv.class).GetPermissionsResolver();
			return new CBoolean(perms.hasPermission(player, permission), t);
		}
	}
}
