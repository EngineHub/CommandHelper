package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CREInsufficientPermissionException;
import com.laytonsmith.core.exceptions.CRE.CREPlayerOfflineException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;

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

		@Override
		public String getName() {
			return "has_permission";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "boolean {[player], permissionName} Using the built in permissions system,"
					+ " checks to see if the player has a particular permission."
					+ " This is simply passed through to the permissions system."
					+ " If you notice, this function isn't restricted. A player can check their own permissions,"
					+ " however it IS restricted if the player attempts to check another player's permissions."
					+ " If run from the console or a CommandBlock, will always return true unless a value has been"
					+ " explicitly set for them.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInsufficientPermissionException.class, CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {

			MCCommandSender sender;
			String permission;

			if (args.length == 1) {
				sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
				permission = args[0].val();
			} else {
				sender = Static.GetCommandSender(args[0].val(), t);
				permission = args[1].val();

				MCPlayer mcp = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
				if (mcp != null && !mcp.getName().equals(args[0].val())) {
					if (!Static.hasCHPermission(getName(), environment)) {
						throw ConfigRuntimeException.BuildException("You do not have permission to use the " + getName() + " function.",
								CREInsufficientPermissionException.class, t);
					}
				}
			}
			
			if (sender == null) {
				return CBoolean.FALSE; // Return false for null command senders.
			}
			
			if ((Static.getConsoleName().equals(sender.getName().toLowerCase())
					|| sender.getName().startsWith(Static.getBlockPrefix()))
					&& !sender.isPermissionSet(permission)) {
				// Console and CommandBlocks always have permission unless specifically set otherwise
				return CBoolean.TRUE;
			}

			return CBoolean.get(sender.hasPermission(permission));
		}
	}
}
