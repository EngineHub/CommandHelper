package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.extensions.AbstractExtension;
import com.laytonsmith.core.extensions.MSExtension;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
@MSExtension("Core")
public class LifeCycle extends AbstractExtension {
	static LifeCycle instance;
	
	public static LifeCycle getInstance() {
		return instance;
	}
	
	@Override
	public Version getVersion() {
		return CHVersion.LATEST;
	}

	@Override
	public void onStartup() {
		instance = this;
}

	@Override
	public void onShutdown() {
		instance = null;
	}
}
