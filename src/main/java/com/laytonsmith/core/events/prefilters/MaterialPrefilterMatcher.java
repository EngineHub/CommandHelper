package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.blocks.MCMaterial.MCVanillaMaterial;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;

/**
 *
 * @param <T>
 */
public abstract class MaterialPrefilterMatcher<T extends BindableEvent> extends StringPrefilterMatcher<T> {

	@api
	public static class MaterialPrefilterDocs implements PrefilterDocs {

		@Override
		public String getName() {
			return "material match";
		}

		@Override
		public String getNameWiki() {
			return "[[Prefilters#material match|Material Match]]";
		}

		@Override
		public String docs() {
			return """
				A material match is a simple string match against the list of materials in the game. For instance,
				SOUL_TORCH or STONE. Unlike a string match though, the compiler is aware of the material types, and
				will be a compile error if the material doesn't exist.

				<%CODE|
					bind('player_interact', null, array(itemname: 'SOUL_TORCH', block: 'STONE'), @event) {
						# This code will run if a player clicks stone while holding a soul torch.
					}
				%>""";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}
	}

	@Override
	public PrefilterDocs getDocsObject() {
		return new MaterialPrefilterDocs();
	}

	@Override
	public void validate(ParseTree node, CClassType nodeType, Environment env)
			throws ConfigCompileException, ConfigCompileGroupException, ConfigRuntimeException {
		if(node.isConst() && !node.getData().equals(CNull.NULL)) {
			try {
				MCVanillaMaterial.valueOf(node.getData().val());
			} catch (IllegalArgumentException ex) {
				env.getEnv(CompilerEnvironment.class).addCompilerWarning(node.getFileOptions(),
						new CompilerWarning("\"" + node.getData() + "\" is not a valid material type, this will never match."
								+ " (This will eventually be a compile error)", node.getTarget(), null));
			}
		}
	}

	@Override
	protected String getProperty(T event) {
		MCMaterial material = getMaterial(event);
		if(material == null) {
			return null;
		}
		return material.getName();
	}

	protected abstract MCMaterial getMaterial(T event);

	@Override
	public int getPriority() {
		return -1;
	}

}
