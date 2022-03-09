package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.MEnum;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
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
public abstract class EnumPrefilterMatcher<T extends BindableEvent, U extends Enum<U>> extends MacroPrefilterMatcher<T> {

	protected final Class<U> enumClass;
	private final MEnum typeof;

	public EnumPrefilterMatcher(Class<U> enumClass) {
		this.enumClass = enumClass;
		this.typeof = enumClass.getAnnotation(MEnum.class);
		if(typeof == null) {
			throw new Error(enumClass + " must be tagged with MEnum");
		}
	}

	@api
	public static class EnumPrefilterDocs implements PrefilterDocs {

		@Override
		public String getNameWiki() {
			return "[[Prefilters#enum match|Enum Match]]";
		}

		@Override
		public String getName() {
			return "enum match";
		}

		@Override
		public String docs() {
			return "Matches an enum. This is a string comparison, and is case sensitive. Unlike string matches though,"
					+ " the compiler will detect if a hardcoded enum value is in the list of valid enums or not.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

	}

	@Override
	public PrefilterDocs getDocsObject() {
		return new EnumPrefilterDocs();
	}

	@Override
	public void validate(ParseTree node, Environment env) throws ConfigCompileException, ConfigCompileGroupException, ConfigRuntimeException {
		if(!node.getDeclaredType(env).doesExtend(CString.TYPE)) {
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
							new CompilerWarning("\"" + val + "\" is not a valid enum in " + typeof.value(),
									node.getTarget(), null));
				}
			}
		}
	}

	@Override
	protected Object getProperty(T event) {
		return getEnum(event).name();
	}

	protected abstract Enum<U> getEnum(T event);

}
