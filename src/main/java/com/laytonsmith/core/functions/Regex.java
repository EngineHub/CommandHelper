package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 */
@core
public class Regex {

	public static String docs() {
		return "This class provides regular expression functions. For more details, please see the page on "
				+ "[[Regex|regular expressions]]. Note that all the functions are just passthroughs"
				+ " to the Java regex mechanism. If you need to set a flag on the regex, where the api calls"
				+ " for a pattern, instead send array('pattern', 'flags') where flags is any of i, m, or s."
				+ " Alternatively, using the embedded flag system that Java provides is also valid. Named captures are"
				+ " also supported if you are using Java 7, otherwise they are not supported.";
	}

	@api
	public static class reg_match extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "reg_match";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "array {pattern, subject} Searches for the given pattern, and returns an array with the results. Captures are supported."
					+ " If the pattern is not found anywhere in the subject, an empty array is returned. The indexes of the array"
					+ " follow typical regex fashion; the 0th element is the whole match, and 1-n are the captures specified in"
					+ " the regex.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_2_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			Pattern pattern = getPattern(args[0], t);
			String subject = args[1].val();
			CArray ret = CArray.GetAssociativeArray(t);
			Matcher m = pattern.matcher(subject);
			if(m.find()) {
				ret.set(0, new CString(m.group(0), t), t);
				for(int i = 1; i <= m.groupCount(); i++) {
					if(m.group(i) == null) {
						ret.set(i, CNull.NULL, t);
					} else {
						ret.set(i, new CString(m.group(i), t), t);
					}
				}
				//Named groups are only supported in Java 7, but we can
				//dynamically enable this feature if they have it.
				Set<String> namedGroups = getNamedGroups(pattern.pattern());
				try {
					for(String key : namedGroups) {
						ret.set(key, (String) ReflectionUtils.invokeMethod(Matcher.class, m, "group", new Class[]{String.class}, new Object[]{key}), t);
					}
				} catch (ReflectionUtils.ReflectionException ex) {
					throw new CREFormatException("Named captures are only supported with Java 7.", t);
				}
			}
			return ret;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(!Construct.IsDynamicHelper(children.get(0).getData())) {
				getPattern(children.get(0).getData(), t);
			}
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN,
					OptimizationOption.OPTIMIZE_DYNAMIC,
					OptimizationOption.NO_SIDE_EFFECTS
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "reg_match('(\\\\d)(\\\\d)(\\\\d)', 'abc123')"),
				//Java 7 can't be assumed to be working on the system running the doc gen, so we'll hardcode these.
				new ExampleScript("Named captures (Only works if your system is running Java 7)",
				"reg_match('abc(?<foo>\\\\d+)(xyz)', 'abc123xyz')", "{0: abc123xyz, 1: 123, 2: xyz, foo: 123}"),
				new ExampleScript("Named captures with backreferences (Only works if your system is running Java 7)",
				"reg_match('abc(?<foo>\\\\d+)def\\\\k<foo>', 'abc123def123')['foo']", "123")
			};
		}

	}

	@api
	public static class reg_match_all extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "reg_match_all";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "array {pattern, subject} Searches subject for all matches to the regular expression given in pattern, unlike reg_match,"
					+ " which just returns the first match.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_2_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			Pattern pattern = getPattern(args[0], t);
			String subject = args[1].val();
			CArray fret = new CArray(t);
			Matcher m = pattern.matcher(subject);
			Set<String> namedGroups = getNamedGroups(pattern.pattern());
			while(m.find()) {
				CArray ret = CArray.GetAssociativeArray(t);
				ret.set(0, new CString(m.group(0), t), t);

				for(int i = 1; i <= m.groupCount(); i++) {
					ret.set(i, new CString(m.group(i), t), t);
				}
				//Named groups are only supported in Java 7, but we can
				//dynamically enable this feature if they have it.
				try {
					for(String key : namedGroups) {
						ret.set(key, (String) ReflectionUtils.invokeMethod(Matcher.class, m, "group", new Class[]{String.class}, new Object[]{key}), t);
					}
				} catch (ReflectionUtils.ReflectionException e) {
					throw new CREFormatException("Named captures are only supported with Java 7.", t);
				}
				fret.push(ret, t);
			}
			return fret;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(!Construct.IsDynamicHelper(children.get(0).getData())) {
				getPattern(children.get(0).getData(), t);
			}
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN,
					OptimizationOption.OPTIMIZE_DYNAMIC,
					OptimizationOption.NO_SIDE_EFFECTS
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "reg_match_all('(\\\\d{3})', 'abc123456')"),
				//Same thing here, can't guarantee we're running Java 7 when these are generated.
				new ExampleScript("Named captures (Only works if your system is running Java 7)",
				"reg_match_all('abc(?<foo>\\\\d+)(xyz)', 'abc123xyz')[0]['foo']", "123"),
				new ExampleScript("Named captures with backreferences (Only works if your system is running Java 7)",
				"reg_match_all('abc(?<foo>\\\\d+)def\\\\k<foo>', 'abc123def123')[0]['foo']", "123")
			};
		}

	}

	@api
	public static class reg_replace extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "reg_replace";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{3};
		}

		@Override
		public String docs() {
			return "string {pattern, replacement, subject} Replaces any occurances of pattern with the replacement in subject."
					+ " Back references are allowed.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_2_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			Pattern pattern = getPattern(args[0], t);
			String replacement = args[1].val();
			String subject = args[2].val();
			String ret = "";

			try {
				ret = pattern.matcher(subject).replaceAll(replacement);
			} catch (IndexOutOfBoundsException e) {
				throw new CREFormatException("Expecting a regex group at parameter 1 of reg_replace", t);
			} catch (IllegalArgumentException e) {
				throw new CREFormatException(e.getMessage(), t);
			}

			return new CString(ret, t);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			ParseTree data = children.get(0);
			if(!Construct.IsDynamicHelper(data.getData())) {
				String pattern = data.getData().val();
				if(isLiteralRegex(pattern)) {
					//We want to replace this with replace()
					//Note the alternative order of arguments
					ParseTree replace = new ParseTree(new CFunction("replace", t), data.getFileOptions());
					replace.addChildAt(0, children.get(2)); //subject -> main
					replace.addChildAt(1, new ParseTree(new CString(getLiteralRegex(pattern), t), replace.getFileOptions())); //pattern -> what
					replace.addChildAt(2, children.get(1)); //replacement -> that
					return replace;
				} else {
					getPattern(data.getData(), t);
				}
			}
			return null;
//			if(!children.get(0).getData().isDynamic()){
//				getPattern(children.get(0).getData(), t);
//			}
//			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN,
					OptimizationOption.OPTIMIZE_DYNAMIC,
					OptimizationOption.NO_SIDE_EFFECTS
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "reg_replace('\\\\d', 'Z', '123abc')"),
				new ExampleScript("Using backreferences", "reg_replace('abc(\\\\d+)', '$1', 'abc123')"),
				new ExampleScript("Using backreferences with named captures (Only works if your system is running Java 7)",
				"reg_replace('abc(?<foo>\\\\d+)', '${foo}', 'abc123')", "123")
			};
		}

	}

	@api
	@seealso({StringHandling.split.class, ArrayHandling.array_implode.class})
	public static class reg_split extends AbstractFunction implements Optimizable {

		// Variable is more clear when named after the function it represents.
		@SuppressWarnings("checkstyle:constantname")
		private static final String split = new StringHandling.split().getName();

		@Override
		public String getName() {
			return "reg_split";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "array {pattern, subject, [limit]} Splits a string on the given regex, and returns an array of the parts. If"
					+ " nothing matched, an array with one element, namely the original subject, is returned."
					+ " Limit defaults to infinity, but if set, only"
					+ " that number of splits will occur.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_2_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			Pattern pattern = getPattern(args[0], t);
			String subject = args[1].val();
			/**
			 * We use a different indexing notation than Java's regex split. In the case of 0 for the limit, we will
			 * still return an array of length 1, assuming there are actual splits available. In Java, a split of 0 will
			 * return the same as length 1. In our method though, the limit is the number of splits themselves, so 1
			 * means that the array will be length 2, as in, there were 1 splits performed. This matches the behavior of
			 * split().
			 */
			int limit = Integer.MAX_VALUE - 1;
			if(args.length >= 3) {
				limit = Static.getInt32(args[2], t);
			}
			String[] rsplit = pattern.split(subject, limit + 1);
			CArray ret = new CArray(t);
			for(String split : rsplit) {
				ret.push(new CString(split, t), t);
			}
			return ret;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			ParseTree data = children.get(0);
			if(!Construct.IsDynamicHelper(data.getData())) {
				String pattern = data.getData().val();
				if(isLiteralRegex(pattern)) {
					//We want to replace this with split()
					ParseTree splitNode = new ParseTree(new CFunction(split, t), data.getFileOptions());
					splitNode.addChildAt(0, new ParseTree(new CString(getLiteralRegex(pattern), t), splitNode.getFileOptions()));
					splitNode.addChildAt(1, children.get(1));
					return splitNode;
				} else {
					getPattern(data.getData(), t);
				}
			}
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CACHE_RETURN,
					OptimizationOption.OPTIMIZE_DYNAMIC,
					OptimizationOption.NO_SIDE_EFFECTS
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "reg_split('\\\\d', 'a1b2c3')")
			};
		}

	}

	@api
	public static class reg_count extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "reg_count";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "int {pattern, subject} Counts the number of occurances in the subject.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_2_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			Pattern pattern = getPattern(args[0], t);
			String subject = args[1].val();
			long ret = 0;
			Matcher m = pattern.matcher(subject);
			while(m.find()) {
				ret++;
			}
			return new CInt(ret, t);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(!Construct.IsDynamicHelper(children.get(0).getData())) {
				getPattern(children.get(0).getData(), t);
			}
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN,
					OptimizationOption.OPTIMIZE_DYNAMIC,
					OptimizationOption.NO_SIDE_EFFECTS
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "reg_count('\\\\d', '123abc')")
			};
		}
	}

	@api
	public static class reg_escape extends AbstractFunction implements Optimizable {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return new CString(java.util.regex.Pattern.quote(args[0].val()), t);
		}

		@Override
		public String getName() {
			return "reg_escape";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {arg} Escapes arg so that it may be used directly in a regular expression, without fear that"
					+ " it will have special meaning; that is, it escapes all special characters. Use this if you need"
					+ " to use user input or similar as a literal search index.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN,
					OptimizationOption.NO_SIDE_EFFECTS
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "reg_escape('\\\\d+')")
			};
		}

	}

	private static Pattern getPattern(Mixed c, Target t) throws ConfigRuntimeException {
		String regex = "";
		int flags = 0;
		String sflags = "";
		if(c.isInstanceOf(CArray.TYPE)) {
			CArray ca = (CArray) c;
			regex = ca.get(0, t).val();
			sflags = ca.get(1, t).val();
			for(int i = 0; i < sflags.length(); i++) {
				if(sflags.toLowerCase().charAt(i) == 'i') {
					flags |= java.util.regex.Pattern.CASE_INSENSITIVE;
				} else if(sflags.toLowerCase().charAt(i) == 'm') {
					flags |= java.util.regex.Pattern.MULTILINE;
				} else if(sflags.toLowerCase().charAt(i) == 's') {
					flags |= java.util.regex.Pattern.DOTALL;
				} else {
					throw new CREFormatException("Unrecognized flag: " + sflags.toLowerCase().charAt(i), t);
				}
			}
		} else {
			regex = c.val();
		}
		try {
			return Pattern.compile(regex, flags);
		} catch (PatternSyntaxException e) {
			throw new CREFormatException(e.getMessage(), c.getTarget());
		}
	}

	private static boolean isLiteralRegex(String regex) {
		//These are the special characters in a regex. If a regex does not contain any of these
		//characters, we can use a faster method in many cases, though the extra overhead of doing
		//this check only makes sense during optimization, not runtime.

		//We also are going to check for the special case where the whole regex starts with \Q and ends with \E, which
		//indicates that they did something like: reg_split(reg_escape('literal string'), '') which is an easily
		//optimizable case, but we will have to transform the regex to get the actual split index, but that's up
		//to the function to call getLiteralRegex. If the internal of the regex further contains more \Q or \E identifiers,
		//they are doing something more complex, so we're just gonna forgo optimizing that.
		if(regex.startsWith("\\Q") && regex.endsWith("\\E")
				&& !regex.substring(2, regex.length() - 2).contains("\\Q")
				&& !regex.substring(2, regex.length() - 2).contains("\\E")) {
			return true;
		}
		String chars = "[\\^$.|?*+()";
		for(int i = 0; i < chars.length(); i++) {
			if(regex.contains(Character.toString(chars.charAt(i)))) {
				return false;
			}
		}
		return true;
	}

	private static String getLiteralRegex(String regex) {
		if(regex.startsWith("\\Q") && regex.endsWith("\\E")
				&& !regex.substring(2, regex.length() - 2).contains("\\Q")
				&& !regex.substring(2, regex.length() - 2).contains("\\E")) {
			return regex.substring(2, regex.length() - 2);
		} else {
			return regex;
		}
	}

	private static final Pattern NAMED_GROUP = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");

	private static Set<String> getNamedGroups(String regex) {
		Matcher m = NAMED_GROUP.matcher(regex);
		Set<String> ret = new HashSet<String>();
		while(m.find()) {
			ret.add(m.group(1));
		}
		return ret;
	}

}
