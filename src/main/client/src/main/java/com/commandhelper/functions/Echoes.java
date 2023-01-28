package com.commandhelper.functions;

import com.commandhelper.client.AbstractForgeClientFunction;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.clientsupport.overridefor;
import com.laytonsmith.core.clientsupport.ClientPermission;
import com.laytonsmith.core.clientsupport.permission;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;

/**
 *
 */
public class Echoes {

	@api
	@overridefor(com.laytonsmith.core.functions.Echoes.msg.class)
	@permission(ClientPermission.NONE)
	public static class msg extends AbstractForgeClientFunction {

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			Minecraft.getInstance().player.sendSystemMessage(MutableComponent.create(new LiteralContents(
				args[0].toString()
			)));
			return CVoid.VOID;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

	}

}
