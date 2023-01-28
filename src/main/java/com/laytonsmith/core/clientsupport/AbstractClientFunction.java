package com.laytonsmith.core.clientsupport;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.functions.AbstractFunction;
import com.laytonsmith.core.functions.Function;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 *
 */
public abstract class AbstractClientFunction extends AbstractFunction {

	private Function coreFunction = null;

	protected Function getCoreFunction() {
		if(coreFunction == null) {
			overridefor overridefor = this.getClass().getAnnotation(overridefor.class);
			Class<?> clazz = overridefor.value();
			if(!Function.class.isAssignableFrom(clazz)) {
				throw new Error(clazz + " must extend Function!");
			}
			Function f = (Function) ReflectionUtils.newInstance(clazz);
			coreFunction = f;
		}
		return coreFunction;
	}

	private Boolean restricted = null;

	@Override
	public final boolean isRestricted() {
		if(restricted != null) {
			return restricted;
		}

		permission permission = this.getClass().getAnnotation(permission.class);
		Set<ClientPermission> requiredPermissions = EnumSet.copyOf(Arrays.asList(permission.value()));
		PermissionGrants instance = PermissionGrants.getInstance();
		for(ClientPermission p : requiredPermissions) {
			if(!instance.isGranted(p)) {
				restricted = true;
				return restricted;
			}
		}
		restricted = false;
		return restricted;
	}

	@Override
	public Class<? extends CREThrowable>[] thrown() {
		return getCoreFunction().thrown();
	}

	@Override
	public Boolean runAsync() {
		return getCoreFunction().runAsync();
	}

	@Override
	public String getName() {
		return getCoreFunction().getName();
	}

	@Override
	public Integer[] numArgs() {
		return getCoreFunction().numArgs();
	}

	@Override
	public String docs() {
		return getCoreFunction().docs();
	}
}
