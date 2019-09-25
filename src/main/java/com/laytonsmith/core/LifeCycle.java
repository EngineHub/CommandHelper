package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.extensions.AbstractExtension;
import com.laytonsmith.core.extensions.MSExtension;
import java.util.Map;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
@MSExtension("Core")
public class LifeCycle extends AbstractExtension {

	@Override
	public Version getVersion() {
		return MSVersion.LATEST;
	}

	@Override
	public Map<String, String> getHelpTopics() {
		return getDocsResourceFolder(LifeCycle.class);
	}

}
