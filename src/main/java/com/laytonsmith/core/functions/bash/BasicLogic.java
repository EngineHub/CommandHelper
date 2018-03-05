package com.laytonsmith.core.functions.bash;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.Target;

/**
 *
 */
public class BasicLogic {

	public static String docs() {
		return "Contains basic logic functions for bash";
	}

	@api(platform = api.Platforms.COMPILER_BASH)
	public static class _if extends BashFunction {

		@Override
		public String getName() {
			return "ifelse";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {condition, ifcode, elsecode} Runs the ifcode if condition is true, otherwise, "
					+ "runs the false code. Note that nothing is ever returned.";
		}

		@Override
		public String compile(Target t, String... args) {
			return new _ifelse().compile(t, args);
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api(platform = api.Platforms.COMPILER_BASH)
	public static class _ifelse extends BashFunction {

		@Override
		public String getName() {
			return "ifelse";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {condition, ifcode, elsecode} Runs the ifcode if condition is true, otherwise, "
					+ "runs the false code. Note that nothing is ever returned.";
		}

		@Override
		public String compile(Target t, String... args) {
			String s = "if [ " + args[0] + " ]; then\n"
					+ args[1] + "\n";

			if(args.length == 3) {
				s += "else\n";
				s += args[2] + "\n";
			}

			s += "fi\n";

			return s;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api(platform = api.Platforms.COMPILER_BASH)
	public static class equals extends BashFunction {

		@Override
		public String getName() {
			return "equals";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {arg1, arg2} Compares two arguments for equality";
		}

		@Override
		public String compile(Target t, String... args) {
			return args[0] + " == " + args[1];
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}
}
