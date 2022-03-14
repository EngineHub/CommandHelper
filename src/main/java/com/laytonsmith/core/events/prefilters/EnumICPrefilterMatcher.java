package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.MEnum;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;

/**
 *
 * @param <T>
 * @param <U>
 */
public abstract class EnumICPrefilterMatcher<T extends BindableEvent, U extends Enum<U>> extends StringICPrefilterMatcher<T> {

	protected final Class<U> enumClass;
	private final MEnum typeof;

	public EnumICPrefilterMatcher(Class<U> enumClass) {
		this.enumClass = enumClass;
		this.typeof = enumClass.getAnnotation(MEnum.class);
		if(typeof == null) {
			throw new Error(enumClass + " must be tagged with MEnum");
		}
	}

	@api
	public static class EnumICPrefilterDocs implements PrefilterMatcher.PrefilterDocs {

		@Override
		public String getNameWiki() {
			return "[[Prefilters#enum ic match|Enum Match]]";
		}

		@Override
		public String getName() {
			return "enum ic match";
		}

		@Override
		public String docs() {
			return "Matches an enum ignoring case. Unlike the normal enum matcher though, this is deprecated, as"
					+ " all enum matches will become case sensitive in the future. This is currently just a"
					+ " warning, however.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

	}

	@Override
	public PrefilterMatcher.PrefilterDocs getDocsObject() {
		return new EnumICPrefilterDocs();
	}

	@Override
	public void validate(ParseTree node, CClassType nodeType, Environment env)
			throws ConfigCompileException, ConfigCompileGroupException, ConfigRuntimeException {
		if(!nodeType.doesExtend(CString.TYPE)) {
			env.getEnv(CompilerEnvironment.class).addCompilerWarning(node.getFileOptions(),
					new CompilerWarning("Unexpected type here.",
							node.getTarget(), null));
		}
		if(node.isConst()) {
			String val = node.getData().val();
			if(!val.startsWith("/") && !val.startsWith("(")) {
				try {
					Enum.valueOf(enumClass, val);
				} catch (IllegalArgumentException ex) {
					env.getEnv(CompilerEnvironment.class).addCompilerWarning(node.getFileOptions(),
							new CompilerWarning("\"" + val + "\" is not a valid enum in " + typeof.value() + " (this"
									+ " field will become case sensitive in the future)",
									node.getTarget(), null));
				}
			}
		}
	}

	@Override
	protected String getProperty(T event) {
		return getEnum(event).name();
	}

	protected abstract Enum<U> getEnum(T event);

}
