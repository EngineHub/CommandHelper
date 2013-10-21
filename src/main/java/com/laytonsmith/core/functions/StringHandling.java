package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils.ReflectionException;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.noprofile;
import com.laytonsmith.core.*;
import com.laytonsmith.core.compiler.OptimizationUtilities;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.interfaces.Sizable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Formatter;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Layton
 */
public class StringHandling {

	public static String docs() {
		return "These class provides functions that allow strings to be manipulated";
	}

	@api
	public static class cc extends AbstractFunction {

		public String getName() {
			return "cc";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "string {args...} The cousin to <strong>c</strong>on<strong>c</strong>at, this function does some magic under the covers"
					+ " to remove the auto-concatenation effect in bare strings. Take the following example: cc(bare string) -> barestring";
		}

		public ExceptionType[] thrown() {
			return null;
		}

		public boolean isRestricted() {
			return false;
		}

		@Override
		public boolean preResolveVariables() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CVoid(t);
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			//if any of the nodes are sconcat, move their children up a level
			List<ParseTree> list = new ArrayList<ParseTree>();
			for (ParseTree node : nodes) {
				if (node.getData().val().equals("sconcat")) {
					for (ParseTree sub : node.getChildren()) {
						list.add(sub);
					}
				} else {
					list.add(node);
				}
			}

			StringBuilder b = new StringBuilder();
			for (ParseTree node : list) {
				Construct c = parent.seval(node, env);
				b.append(c.val());
			}
			return new CString(b.toString(), t);
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		public ParseTree optimizeSpecial(Target target, List<ParseTree> children) {
			throw new UnsupportedOperationException("Not yet implemented");
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("", "cc('These' 'normally' 'have' 'spaces' 'between' 'them')"),};
		}
	}

	@api
	public static class concat extends AbstractFunction implements Optimizable {

		public String getName() {
			return "concat";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			StringBuilder b = new StringBuilder();
			for (int i = 0; i < args.length; i++) {
				b.append(args[i].val());
			}
			return new CString(b.toString(), t);
		}

		public String docs() {
			return "string {var1, [var2...]} Concatenates any number of arguments together, and returns a string";
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		public Boolean runAsync() {
			return null;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			OptimizationUtilities.pullUpLikeFunctions(children, this.getName());
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Functional usage", "concat('1', '2', '3', '4')"),
						new ExampleScript("Symbolic usage", "'1' . '2' . '3' . '4'"),};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN,
					OptimizationOption.OPTIMIZE_DYNAMIC);
		}
	}

	@api
	@noprofile
	public static class sconcat extends AbstractFunction implements Optimizable {

		private static final String g = new Meta.g().getName();
		private static final String p = new Compiler.p().getName();
		
		public String getName() {
			return "sconcat";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			StringBuilder b = new StringBuilder();
			for (int i = 0; i < args.length; i++) {
				if (i > 0) {
					b.append(" ");
				}
				b.append(args[i] == null?"":args[i].val());
			}
			return new CString(b.toString(), t);
		}

//        @Override
//        public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
//            StringBuilder b = new StringBuilder();
//            boolean centry = false;
//            Construct key = null;
//            for (int i = 0; i < nodes.length; i++) {
//                Construct c = parent.seval(nodes[i], env);
//                if (i == 0) {
//                    if (c instanceof CLabel) {
//                        key = c;
//                        centry = true;
//                        break;
//                    }
//                }
//                if (!centry) {
//                    if (i > 1 || i > 0 && !centry) {
//                        b.append(" ");
//                    }
//                    b.append(c.val());
//                }
//            }
//            if (centry) {
//                Construct value;
//                if (nodes.length > 2) {
//                    //it's a string
//                    StringBuilder c = new StringBuilder();
//                    for (int i = 1; i < nodes.length; i++) {
//                        Construct d = parent.seval(nodes[i], env);
//                        if (i > 1) {
//                            c.append(" ");
//                        }
//                        c.append(d.val());                        
//                    }
//                    value = new CString(c.toString(), t);
//                } else {
//                    value = parent.seval(nodes[1], env);
//                }
//                return new CEntry(key, value, t);
//            } else {
//                return new CString(b.toString(), t);
//            }
//        }
		public String docs() {
			return "string {var1, [var2...]} Concatenates any number of arguments together, but puts a space between elements";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		public Boolean runAsync() {
			return null;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			OptimizationUtilities.pullUpLikeFunctions(children, this.getName());
			//Remove empty g or p children
			Iterator<ParseTree> it = children.iterator();
			while(it.hasNext()){
				ParseTree n = it.next();
				if(n.getData() instanceof CFunction && 
						(g.equals(n.getData().val())
							|| p.equals(n.getData().val())) 
						&& !n.hasChildren()){
					it.remove();
				}
			}
			//If we don't have any children, remove us as well
			if(children.size() == 1){
				return children.get(0);
			}
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Functional usage", "sconcat('1', '2', '3', '4')"),
						new ExampleScript("Implied usage, due to no operators", "'1' '2' '3' '4'"),};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN,
					OptimizationOption.OPTIMIZE_DYNAMIC);
		}
	}

	@api
	public static class replace extends AbstractFunction implements Optimizable {

		public String getName() {
			return "replace";
		}

		public Integer[] numArgs() {
			return new Integer[]{3};
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			String thing = args[0].val();
			String what = args[1].val();
			String that = args[2].val();
			return new CString(thing.replace(what, that), t);
		}

		public String docs() {
			return "string {subject, search, replacement} Replaces all instances of 'search' with 'replacement' in 'subject'";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		public Boolean runAsync() {
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("", "replace('Where in the world is Carmen Sandiego?', 'Carmen Sandiego', 'Waldo')"),
						new ExampleScript("No match found", "replace('The same thing', 'not found', '404')"),};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}
	}

	@api
	public static class parse_args extends AbstractFunction implements Optimizable {

		public String getName() {
			return "parse_args";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			String[] sa = args[0].val().split(" ");
			ArrayList<Construct> a = new ArrayList<Construct>();
			for (String s : sa) {
				if (!s.trim().isEmpty()) {
					a.add(new CString(s.trim(), t));
				}
			}
			Construct[] csa = new Construct[a.size()];
			for (int i = 0; i < a.size(); i++) {
				csa[i] = a.get(i);
			}
			return new CArray(t, csa);
		}

		public String docs() {
			return "array {string} Parses string into an array, where string is a space seperated list of arguments. Handy for turning"
					+ " $ into a usable array of items with which to script against. Extra spaces are ignored, so you would never get an empty"
					+ " string as an input.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		public Boolean runAsync() {
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Demonstrates basic usage", "parse_args('This turns into 5 arguments')"),
						new ExampleScript("Demonstrates usage with extra spaces", "parse_args('This   trims   extra   spaces')")
					};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}
	}

	@api
	public static class trim extends AbstractFunction implements Optimizable {

		public String getName() {
			return "trim";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "string {s} Returns the string s with leading and trailing whitespace cut off";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			return new CString(args[0].val().trim(), args[0].getTarget());
		}

		public Boolean runAsync() {
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("", "'->' . trim('    <- spaces ->    ') . '<-'"),};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}
	}

	@api
	public static class trimr extends AbstractFunction implements Optimizable {

		public String getName() {
			return "trimr";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "string {s} Returns the string s with trailing whitespace cut off";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			return new CString(StringUtils.trimRight(args[0].val()), args[0].getTarget());
		}

		public Boolean runAsync() {
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("", "'->' . trimr('    <- spaces ->    ') . '<-'"),};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}
	}

	@api
	public static class triml extends AbstractFunction implements Optimizable {

		public String getName() {
			return "triml";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "string {s} Returns the string s with leading whitespace cut off";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			return new CString(StringUtils.trimLeft(args[0].val()), t);
		}

		public Boolean runAsync() {
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("", "'->' . triml('    <- spaces ->    ') . '<-'"),};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}
	}

	@api
	public static class length extends AbstractFunction implements Optimizable {

		public String getName() {
			return "length";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "int {str | array} Returns the character length of str, if the value is castable to a string, or the length of the array, if an array is given";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_1_2;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			if (args[0] instanceof Sizable) {
				return new CInt(((Sizable) args[0]).size(), t);
			} else {
				return new CInt(args[0].val().length(), t);
			}
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Strings", "length('this is a string')"),
						new ExampleScript("Arrays", "length(array('1', 2, '3', 4))"),};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}
	}

	@api
	public static class to_upper extends AbstractFunction implements Optimizable {

		public String getName() {
			return "to_upper";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "string {str} Returns an all caps version of str";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_1_2;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			return new CString(args[0].val().toUpperCase(), t);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("", "to_upper('uppercase')"),
						new ExampleScript("", "to_upper('MiXeD cAsE')"),
						new ExampleScript("", "to_upper('Numbers (and SYMBOLS: 25)')"),};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}
	}

	@api
	public static class to_lower extends AbstractFunction implements Optimizable {

		public String getName() {
			return "to_lower";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "string {str} Returns an all lower case version of str";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_1_2;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			return new CString(args[0].val().toLowerCase(), t);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("", "to_lower('LOWERCASE')"),
						new ExampleScript("", "to_lower('MiXeD cAsE')"),
						new ExampleScript("", "to_lower('Numbers (and SYMBOLS: 25)')"),};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}
	}

	@api
	public static class substr extends AbstractFunction implements Optimizable {

		public String getName() {
			return "substr";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public String docs() {
			return "string {str, begin, [end]} Returns a substring of the given string str, starting from index begin, to index end, or the"
					+ " end of the string, if no index is given. If either begin or end are out of bounds of the string, an exception is thrown."
					+ " substr('hamburger', 4, 8) returns \"urge\", substr('smiles', 1, 5) returns \"mile\", and substr('lightning', 5) returns \"ning\"."
					+ " See also length().";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.RangeException, ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_1_2;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			try {
				String s = args[0].val();
				int begin = Static.getInt32(args[1], t);
				int end;
				if (args.length == 3) {
					end = Static.getInt32(args[2], t);
				} else {
					end = s.length();
				}
				return new CString(s.substring(begin, end), t);
			} catch (IndexOutOfBoundsException e) {
				throw new ConfigRuntimeException("The indices given are not valid for string '" + args[0].val() + "'",
						ExceptionType.RangeException, t);
			}
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("", "substr('hamburger', 4, 8)"),
						new ExampleScript("", "substr('smiles', 1, 5)"),
						new ExampleScript("", "substr('lightning', 5)"),
						new ExampleScript("If the indexes are too large", "assign(@big, 25)\nsubstr('small', @big)"),};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}
	}

	@api
	public static class string_position extends AbstractFunction implements Optimizable {

		public String getName() {
			return "string_position";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "int {haystack, needle} Finds the numeric position of the first occurence of needle in haystack. haystack is the string"
					+ " to search in, and needle is the string to search with. Returns the position of the needle (starting with 0) or -1 if"
					+ " the string is not found at all.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.NullPointerException};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			String haystack = args[0].nval();
			String needle = args[1].nval();
			Static.AssertNonCNull(t, args);
			return new CInt(haystack.indexOf(needle), t);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Basic usage", "string_position('this is the string', 'string')"),
						new ExampleScript("String not found", "string_position('Where\\'s Waldo?', 'Dunno.')"),};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}
	}

	@api
	public static class split extends AbstractFunction implements Optimizable {

		public String getName() {
			return "split";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public String docs() {
			return "array {split, string, [limit]} Splits a string into parts, using the split as the index. Though it can be used in every single case"
					+ " you would use reg_split, this does not use regex,"
					+ " and therefore can take a literal split expression instead of needing an escaped regex, and *may* perform better than the"
					+ " regex versions, as it uses an optimized tokenizer split, instead of Java regex. Limit defaults to infinity, but if set, only"
					+ " that number of splits will occur.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			//http://stackoverflow.com/questions/2667015/is-regex-too-slow-real-life-examples-where-simple-non-regex-alternative-is-bett
			//According to this, regex isn't necessarily slower, but we do want to escape the pattern either way, since the main advantage
			//of this function is convenience (not speed) however, if we can eek out a little extra speed too, excellent.
			CArray array = new CArray(t);
			String split = args[0].val();
			String string = args[1].val();
			int limit = Integer.MAX_VALUE;
			if(args.length >= 3){
				limit = Static.getInt32(args[2], t);
			}
			int sp = 0;
			if(split.length() == 0){
				//Empty string, so special case.
				for(int i = 0; i < string.length(); i++){
					array.push(new CString(string.charAt(i), t));
				}
				return array;
			}
			int splitsFound = 0;
			for (int i = 0; i < string.length() - split.length() && splitsFound < limit; i++) {
				if (string.substring(i, i + split.length()).equals(split)) {
					//Split point found
					splitsFound++;
					array.push(new CString(string.substring(sp, i), t));
					sp = i + split.length();
					i += split.length() - 1;
				}
			}
			if (sp != 0) {
				array.push(new CString(string.substring(sp, string.length()), t));
			} else {
				//It was not found anywhere, so put the whole string in
				array.push(args[1]);
			}
			return array;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Simple split on one character. Note that unlike reg_split, no escaping is needed on the period.", "split('.', '1.2.3.4.5')"),
						new ExampleScript("Split with multiple characters", "split('ab', 'aaabaaabaaabaa')"),
						new ExampleScript("Split all characters", "split('', 'abcdefg')"),
						new ExampleScript("Split with limit", "split('|', 'this|is|a|limit', 1)")
			};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CACHE_RETURN);
		}
	}

	@api
	public static class sprintf extends AbstractFunction implements Optimizable {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException,
						ExceptionType.InsufficientArgumentsException,
						ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if (args.length == 0) {
				throw new ConfigRuntimeException(getName() + " expects 1 or more argument", ExceptionType.InsufficientArgumentsException, t);
			}
			String formatString = args[0].val();
			Object[] params = new Object[args.length - 1];
			List<FormatString> parsed;
			try{
				parsed = parse(formatString, t);
			} catch(IllegalFormatException e){
				throw new ConfigRuntimeException(e.getMessage(), ExceptionType.FormatException, t);
			}
			if (requiredArgs(parsed) != args.length - 1) {
				throw new ConfigRuntimeException("The specified format string: \"" + formatString + "\""
						+ " expects " + requiredArgs(parsed) + " argument, but " + (args.length - 1) + " were provided.", ExceptionType.InsufficientArgumentsException, t);
			}

			List<Construct> flattenedArgs = new ArrayList<Construct>();
			if (args.length == 2 && args[1] instanceof CArray) {
				if (((CArray) args[1]).inAssociativeMode()) {
					throw new ConfigRuntimeException("If the second argument to " + getName() + " is an array, it may not be associative.", ExceptionType.CastException, t);
				} else {
					for (int i = 0; i < ((CArray) args[1]).size(); i++) {
						flattenedArgs.add(((CArray) args[1]).get(i));
					}
				}
			} else {
				for (int i = 1; i < args.length; i++) {
					flattenedArgs.add(args[i]);
				}
			}
			//Now figure out how to cast things, now that we know our argument numbers will match up
			for (int i = 0; i < requiredArgs(parsed); i++) {
				Construct arg = flattenedArgs.get(i);
				FormatString fs = parsed.get(i);
				Character c = fs.getExpectedType();
				params[i] = convertArgument(arg, c, i, t);
			}
			//Ok, done.
			return new CString(String.format(formatString, params), t);
		}

		private Object convertArgument(Construct arg, Character c, int i, Target t) {
			Object o;
			if (Conversion.isValid(c)) {
				if (c == 't' || c == 'T') {
					//Datetime, parse as long
					o = Static.getInt(arg, t);
				} else if (Conversion.isCharacter(c)) {
					//Character, parse as string, and verify it's of length 1
					String s = arg.val();
					if (s.length() > 1) {
						throw new Exceptions.FormatException("Expecting a string of length one in argument " + (i + 1) + " in " + getName()
								+ "but \"" + s + "\" was found instead.", t);
					}
					o = s.charAt(0);
				} else if (Conversion.isFloat(c)) {
					//Float, parse as double
					o = Static.getDouble(arg, t);
				} else if (Conversion.isInteger(c)) {
					//Integer, parse as long
					o = Static.getInt(arg, t);
				} else {
					//Further processing is needed
					if (c == Conversion.BOOLEAN || c == Conversion.BOOLEAN_UPPER) {
						//Boolean, parse as such
						o = Static.getBoolean(arg);
					} else {
						//Else it's either a string or a hash code, in which case
						//we will treat it as a string anyways
						o = arg.val();
					}
				}
			} else {
				//Hmm, shouldn't have been able to get here, but whatever.
				throw ConfigRuntimeException.CreateUncatchableException("Conversion is invalid: " + c, t);
			}
			return o;
		}

		private static class Conversion {
			// Byte, Short, Integer, Long, BigInteger
			// (and associated primitives due to autoboxing)

			static final char DECIMAL_INTEGER = 'd';
			static final char OCTAL_INTEGER = 'o';
			static final char HEXADECIMAL_INTEGER = 'x';
			static final char HEXADECIMAL_INTEGER_UPPER = 'X';
			// Float, Double, BigDecimal
			// (and associated primitives due to autoboxing)
			static final char SCIENTIFIC = 'e';
			static final char SCIENTIFIC_UPPER = 'E';
			static final char GENERAL = 'g';
			static final char GENERAL_UPPER = 'G';
			static final char DECIMAL_FLOAT = 'f';
			static final char HEXADECIMAL_FLOAT = 'a';
			static final char HEXADECIMAL_FLOAT_UPPER = 'A';
			// Character, Byte, Short, Integer
			// (and associated primitives due to autoboxing)
			static final char CHARACTER = 'c';
			static final char CHARACTER_UPPER = 'C';
			// java.util.Date, java.util.Calendar, long
			static final char DATE_TIME = 't';
			static final char DATE_TIME_UPPER = 'T';
			// if (arg.TYPE != boolean) return boolean
			// if (arg != null) return true; else return false;
			static final char BOOLEAN = 'b';
			static final char BOOLEAN_UPPER = 'B';
			// if (arg instanceof Formattable) arg.formatTo()
			// else arg.toString();
			static final char STRING = 's';
			static final char STRING_UPPER = 'S';
			// arg.hashCode()
			static final char HASHCODE = 'h';
			static final char HASHCODE_UPPER = 'H';
			static final char LINE_SEPARATOR = 'n';
			static final char PERCENT_SIGN = '%';

			static boolean isValid(char c) {
				return (isGeneral(c) || isInteger(c) || isFloat(c) || isText(c)
						|| c == 't' || isCharacter(c));
			}

			// Returns true iff the Conversion is applicable to all objects.
			static boolean isGeneral(char c) {
				switch (c) {
					case BOOLEAN:
					case BOOLEAN_UPPER:
					case STRING:
					case STRING_UPPER:
					case HASHCODE:
					case HASHCODE_UPPER:
						return true;
					default:
						return false;
				}
			}

			// Returns true iff the Conversion is applicable to character.
			static boolean isCharacter(char c) {
				switch (c) {
					case CHARACTER:
					case CHARACTER_UPPER:
						return true;
					default:
						return false;
				}
			}

			// Returns true iff the Conversion is an integer type.
			static boolean isInteger(char c) {
				switch (c) {
					case DECIMAL_INTEGER:
					case OCTAL_INTEGER:
					case HEXADECIMAL_INTEGER:
					case HEXADECIMAL_INTEGER_UPPER:
						return true;
					default:
						return false;
				}
			}

			// Returns true iff the Conversion is a floating-point type.
			static boolean isFloat(char c) {
				switch (c) {
					case SCIENTIFIC:
					case SCIENTIFIC_UPPER:
					case GENERAL:
					case GENERAL_UPPER:
					case DECIMAL_FLOAT:
					case HEXADECIMAL_FLOAT:
					case HEXADECIMAL_FLOAT_UPPER:
						return true;
					default:
						return false;
				}
			}

			// Returns true iff the Conversion does not require an argument
			static boolean isText(char c) {
				switch (c) {
					case LINE_SEPARATOR:
					case PERCENT_SIGN:
						return true;
					default:
						return false;
				}
			}
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			if (children.isEmpty()) {
				throw new ConfigCompileException(getName() + " expects 1 or more argument", t);
			} else if (children.get(0).isConst()) {
				ParseTree me = new ParseTree(new CFunction(getName(), t), children.get(0).getFileOptions());
				me.setChildren(children);
				me.setOptimized(true); //After this run, we will be, anyways.
				if(children.size() == 2 && children.get(1).getData() instanceof CFunction && ((CFunction)children.get(1).getData()).getFunction().getName().equals(new DataHandling.array().getName())){
					//Normally we can't do anything with a hardcoded array, it's considered dynamic. But in this case, we can at least pull up the arguments,
					//because the array's size is constant, even if the arguments in it aren't.
					ParseTree array = children.get(1);
					children.remove(1);
					boolean allIndexesStatic = true;
					for(int i = 0; i < array.numberOfChildren(); i++){
						ParseTree child = array.getChildAt(i);
						if(child.isDynamic()){
							allIndexesStatic = false;
						}
						children.add(child);
					}
					if(allIndexesStatic){
						me.hasBeenMadeStatic(true);
					}
				}
				//We can check the format string and make sure it doesn't throw an IllegalFormatException.
				try {
					List<FormatString> parsed = parse(children.get(0).getData().val(), t);
					if (requiredArgs(parsed) != children.size() - 1) {
						throw new ConfigRuntimeException("The specified format string: \"" + children.get(0).getData().val() + "\""
								+ " expects " + requiredArgs(parsed) + " argument, but " + (children.size() - 1) + " were provided.", ExceptionType.InsufficientArgumentsException, t);
					}
					//If the arguments are constant, we can actually check them too
					for(int i = 1; i < children.size(); i++){
						//We skip the dynamic ones, but the constant ones we can know for sure
						//if they are convertable or not.
						if(children.get(i).isConst()){
							convertArgument(children.get(i).getData(), parsed.get(i - 1).getExpectedType(), i + 1, t);
						}
					}
				} catch (IllegalFormatException e) {
					throw new ConfigRuntimeException(e.getMessage(), ExceptionType.FormatException, t);
				}
				return me;
			} else {
				return null;
			}
		}

		private static class FormatString {

			private Object ref;
			private static final Class FormatString;
			private static final Class FixedString;
			private static final Class FormatSpecifier;

			static {
				Class tFormatString = null;
				Class tFixedString = null;
				Class tFormatSpecifier = null;
				for (Class c : Formatter.class.getDeclaredClasses()) {
					if (c.getSimpleName().equals("FormatString")) {
						tFormatString = c;
						continue;
					}
					if (c.getSimpleName().equals("FixedString")) {
						tFixedString = c;
						continue;
					}
					if (c.getSimpleName().equals("FormatSpecifier")) {
						tFormatSpecifier = c;
						continue;
					}
				}
				FormatString = tFormatString;
				FixedString = tFixedString;
				FormatSpecifier = tFormatSpecifier;
			}

			public FormatString(Object ref) {
				if (ref == null) {
					throw new NullPointerException();
				}
				if (!FormatString.isAssignableFrom(ref.getClass())) {
					throw new RuntimeException("Unexpected class type. Was expecting ref to be an instance of " + FormatString.getName() + " but was " + ref.getClass().getName());
				}
				this.ref = ref;
			}

			public Character getExpectedType() {
				if (ref.getClass() == FixedString) {
					return null;
				} else if (ref.getClass() == FormatSpecifier) {
					if(((Boolean)ReflectionUtils.get(FormatSpecifier, ref, "dt"))){
						return 't';
					}
					return ((Character) ReflectionUtils.get(FormatSpecifier, ref, "c"));
				} else {
					throw new RuntimeException("Unknown type: " + ref.getClass());
				}
			}
			
			public int getArgIndex(){
				return ((Integer)ReflectionUtils.get(FormatSpecifier, ref, "index"));
			}
			
			public boolean isFixed(){
				return getExpectedType() == '%' || getExpectedType() == 'n';
			}
		}

		private List<FormatString> parse(String format, Target t) {
			List<FormatString> list = new ArrayList<FormatString>();
			Object parse;
			try{
				parse = ReflectionUtils.invokeMethod(Formatter.class, new Formatter(), "parse", new Class[]{String.class}, new Object[]{format});
			} catch(ReflectionException e){
				if(e.getCause() instanceof InvocationTargetException){
					Throwable th = e.getCause().getCause();
					throw new ConfigRuntimeException("A format exception was thrown for the argument \"" + format + "\": " + th.getClass().getSimpleName() + ": " + th.getMessage(), ExceptionType.FormatException, t);
				} else {
					//This is unexpected
					throw ConfigRuntimeException.CreateUncatchableException(e.getMessage(), t);
				}
			}
			int length = Array.getLength(parse);
			for (int i = 0; i < length; i++) {
				FormatString s = new FormatString(Array.get(parse, i));
				if (s.getExpectedType() != null) {
					list.add(s);
				}
			}
			return list;
		}
		
		private int requiredArgs(List<FormatString> list){
			Set<Integer> knownIndexes = new HashSet<Integer>();
			int count = 0;
			for(FormatString s : list){
				if(s.isFixed()){
					continue;
				}
				int index = s.getArgIndex();
				if(index == 0){
					count++;
				} else {
					knownIndexes.add(index);
				}
			}
			count += knownIndexes.size();
			return count;
		}

		public String getName() {
			return "sprintf";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "string {formatString, parameters... | formatString, array(parameters...)} Returns a string formatted to the"
					+ " given formatString specification, using the parameters passed in. The formatString should be formatted"
					+ " according to [http://docs.oracle.com/javase/6/docs/api/java/util/Formatter.html#syntax this standard],"
					+ " with the caveat that the parameter types are automatically cast to the appropriate type, if possible."
					+ " Calendar/time specifiers, (t and T) expect an integer which represents unix time, but are otherwise"
					+ " valid. All format specifiers in the documentation are valid.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.CONSTANT_OFFLINE, OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "sprintf('%d', 1)"),
				new ExampleScript("Multiple arguments", "sprintf('%d%d', 1, '2')"),
				new ExampleScript("Multiple arguments in an array", "sprintf('%d%d', array(1, 2))"),
				new ExampleScript("Compile error, missing parameters", "sprintf('%d')", true),
				new ExampleScript("Other formatting: float with precision (using integer)", "sprintf('%07.3f', 4)"),
				new ExampleScript("Other formatting: float with precision (with rounding)", "sprintf('%07.3f', 3.4567)"),
				new ExampleScript("Other formatting: time", "sprintf('%1$tm %1$te,%1$tY', time())", ":06 13,2013"),
				new ExampleScript("Literal percent sign", "sprintf('%%')"),
				new ExampleScript("Hexidecimal formatting", "sprintf('%x', 15)"),
				new ExampleScript("Other formatting: character", "sprintf('%c', 's')"),
				new ExampleScript("Other formatting: character (with capitalization)", "sprintf('%C', 's')"),
				new ExampleScript("Other formatting: scientific notation", "sprintf('%e', '2345')"),
				new ExampleScript("Other formatting: plain string", "sprintf('%s', 'plain string')"),
				new ExampleScript("Other formatting: boolean", "sprintf('%b', 1)"),
				new ExampleScript("Other formatting: boolean (with capitalization)", "sprintf('%B', 0)"),
				new ExampleScript("Other formatting: hash code", "sprintf('%h', 'will be hashed')"),
			};
		}
		
	}
	
	@api
	public static class string_get_bytes extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			String val = args[0].val();
			String encoding = "UTF-8";
			if(args.length == 2){
				encoding = args[1].val();
			}
			try {
				return CByteArray.wrap(val.getBytes(encoding), t);
			} catch (UnsupportedEncodingException ex) {
				throw new Exceptions.FormatException("Unknown encoding type \"" + encoding + "\"", t);
			}
		}

		public String getName() {
			return "string_get_bytes";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "byte_array {string, [encoding]} Returns this string as a byte_array, encoded using the specified encoding,"
					+ " or UTF-8 if no encoding is specified. Valid encodings are the encoding types that java supports. If the"
					+ " encoding is invalid, a FormatException is thrown.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api
	public static class string_from_bytes extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CByteArray ba = Static.getByteArray(args[0], t);
			String encoding = "UTF-8";
			if(args.length == 2){
				encoding = args[1].val();
			}
			try {
				return new CString(new String(ba.asByteArrayCopy(), encoding), t);
			} catch (UnsupportedEncodingException ex) {
				throw new Exceptions.FormatException("Unknown encoding type \"" + encoding + "\"", t);
			}
		}

		public String getName() {
			return "string_from_bytes";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "string {byte_array, [encoding]} Returns a new string, given the byte array encoding provided. The encoding defaults"
					+ " to UTF-8, but may be specified. A FormatException is thrown if the encoding type is invalid.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api
	public static class string_append extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CResource m = (CResource) args[0];
			StringBuffer buf = ResourceManager.GetResource(m, StringBuffer.class, t);
			for(int i = 1; i < args.length; i++){
				buf.append(args[i].val());
			}
			return new CVoid(t);
		}

		public String getName() {
			return "string_append";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "void {resource, toAppend...} Appends any number of values to the underlying"
					+ " string builder. This is much more efficient than doing normal concatenation"
					+ " with a string when building a string in a loop. The underlying resource may"
					+ " be converted to a string via a cast, string(@res).";
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "@res = res_create_resource('STRING_BUILDER')\n"
					+ "foreach(1..100, @i,\n"
					+ "\tstring_append(@res, @i, '.')\n"
					+ ")\n"
					+ "@string = string(@res)\n"
					+ "res_free_resource(@res) #This line is super important!\n"
					+ "msg(@string)"
					+ ""),
				new ExampleScript("Basic usage, showing performance benefits", 
					"@to = 100000\n"
					+ "@t1 = time()\n"
					+ "@res = res_create_resource('STRING_BUILDER')\n"
					+ "foreach(range(0, @to), @i,\n"
					+ "\tstring_append(@res, @i, '.')\n"
					+ ")\n"
					+ "res_free_resource(@res)\n"
					+ "@t2 = time()\n"
					+ "@t3 = time()\n"
					+ "@str = ''\n"
					+ "foreach(range(0, @to), @i,\n"
					+ "\t@str .= @i . '.'\n"
					+ ")\n"
					+ "@t4 = time()\n"
					+ "msg('Task 1 took '.(@t2 - @t1).'ms under '.@to.' iterations')\n"
					+ "msg('Task 2 took '.(@t4 - @t3).'ms under '.@to.' iterations')")
			};
		}
		
	}
	
	@api public static class char_from_unicode extends AbstractFunction implements Optimizable {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			try{
				return new CString(new String(Character.toChars(Static.getInt32(args[0], t))), t);
			} catch(IllegalArgumentException ex){
				throw new Exceptions.RangeException("Code point out of range: " + args[0].val(), t);
			}
		}

		public String getName() {
			return "char_from_unicode";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "string {unicode} Returns the unicode character for a given unicode value. This is meant"
					+ " for dynamic input that needs converting to a unicode character, if you're hardcoding"
					+ " it, you should just use '\\u1234' syntax instead, however, this is the dynamic equivalent"
					+ " of the \\u string escape, so '\\u1234' == char_from_unicode(parse_int('1234', 16)). Despite the name,"
					+ " certain unicode escapes may return multiple characters, so there is no guarantee that"
					+ " length(char_from_unicode(@val)) will equal 1.";
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "char_from_unicode(parse_int('2665', 16))")
			};
		}

		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.CONSTANT_OFFLINE);
		}
		
	}
	
	@api public static class unicode_from_char extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if(args[0].val().toCharArray().length == 0){
				throw new Exceptions.RangeException("Empty string cannot be converted to unicode.", t);
			}
			int i = Character.codePointAt(args[0].val().toCharArray(), 0);
			return new CInt(i, t);
		}

		public String getName() {
			return "unicode_from_char";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "int {character} Returns the unicode code point for a given character. The character is a string, but it should"
					+ " only be 1 code point character (which may be length(@character) == 2).";
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "to_radix(unicode_from_char('\\u2665'), 16)")
			};
		}
		
	}
	
	@api public static class levenshtein extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CInt(StringUtils.LevenshteinDistance(args[0].val(), args[1].val()), t);
		}

		public String getName() {
			return "levenshtein";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "int {string1, string2} Returns the levenshtein distance of two character sequences. For"
				+ " instance, \"123\" and \"133\" would have a string distance of 1, while \"123\""
				+ " and \"123\" would be 0, since they are the same string.";
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("", "levenshtein('123', '123')"),
				new ExampleScript("", "levenshtein('test', 'testing')"),
				new ExampleScript("", "levenshtein('133', '123')")
			};
		}
		
	}
}
