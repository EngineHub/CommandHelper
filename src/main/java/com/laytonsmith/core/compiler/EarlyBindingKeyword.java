package com.laytonsmith.core.compiler;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.core.Documentation;
import java.net.URL;

/**
 *
 */
public abstract class EarlyBindingKeyword implements KeywordDocumentation {

	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends Documentation>[] seeAlso() {
		return new Class[]{};
	}

	@Override
	public URL getSourceJar() {
		return ClassDiscovery.GetClassContainer(this.getClass());
	}

	@Override
	public String getName() {
		return this.getClass().getAnnotation(Keyword.keyword.class).value();
	}

	public String getKeywordName() {
		return getName();
	}
}
