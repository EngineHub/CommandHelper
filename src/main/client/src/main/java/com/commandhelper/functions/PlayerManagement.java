package com.commandhelper.functions;

import com.commandhelper.client.AbstractForgeClientFunction;
import com.commandhelper.client.ForgeEnvironment;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.clientsupport.ClientPermission;
import com.laytonsmith.core.clientsupport.overridefor;
import com.laytonsmith.core.clientsupport.permission;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 */
public class PlayerManagement {

	@api
	@permission(ClientPermission.NONE)
	@overridefor(com.laytonsmith.core.functions.PlayerManagement.player.class)
	public static class player extends AbstractForgeClientFunction {

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer player = env.getEnv(ForgeEnvironment.class).getPlayer();
			return new CString(player.getName(), t);
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

	}
}
