package com.laytonsmith.core.asm;

import com.laytonsmith.core.environments.Environment;

/**
 * This class provides a collection of standard includes, which are intended to be kept in a single, standardized
 * place, for easy upgrade and adjustments. For templates that are part of the standard C library, please note this
 * in the comment. If it's in the standard library, this means that it doesn't need to have platform dependent versions.
 * For other templates, it may be necessary to have multiple templates for each OS/arch.
 *
 * Note that all templates in this file are automatically included in the AsmCommonLib includes.
 */
public class AsmCommonLibTemplates {

	public static interface Generator {
		void include(Environment env);
	}

	private static void register(String code, Environment env) {
		LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);
		llvmenv.addGlobalDeclaration(code);
	}

	/**
	 * C Standard
	 */
	public static final Generator PUTS = (env) -> {
		register("declare dso_local i32 @puts(i8*)", env);
	};

	/**
	 * C Standard (requires stdio.h on Windows)
	 */
	public static final Generator SPRINTF = (env) -> {
		register("declare dso_local i32 @sprintf(i8*, i8*, ...)", env);
	};

	/**
	 * C Standard
	 */
	public static final Generator EXIT = (env) -> {
		register("declare dso_local void @exit(i32) noreturn nounwind", env);
	};

	/**
	 * C Standard
	 */
	public static final Generator RAND = (env) -> {
		register("declare dso_local i32 @rand()", env);
	};

	/**
	 * C Standard
	 */
	public static final Generator SRAND = (env) -> {
		register("declare dso_local void @srand(i32)", env);
	};

	/**
	 * C Standard
	 */
	public static final Generator TIME = (env) -> {
		register("declare dso_local i32 @time(...)", env);
	};

	public static final Generator LLVM_MEMCPY_P0I8_P0I8_I64 = (env) -> {
		register("declare void @llvm.memcpy.p0i8.p0i8.i64(i8* noalias nocapture writeonly, i8* noalias nocapture readonly, i64, i1 immarg) nounwind", env);
	};
}
