package com.laytonsmith.core.compiler;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.net.URL;

/**
 *
 */
public abstract class EarlyBindingKeyword implements KeywordDocumentation {

	public abstract int process(TokenStream stream, Environment env,
			int keywordPosition) throws ConfigCompileException;

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

	@Override
	public String getKeywordName() {
		return getName();
	}
}
