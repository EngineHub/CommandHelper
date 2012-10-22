

package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.StringUtils;
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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *
 * @author Layton
 */
public class StringHandling {

    public static String docs() {
        return "These class provides functions that allow strings to be manipulated";
    }
    
    @api
    public static class cc extends AbstractFunction{

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
            for(ParseTree node : nodes){
                if(node.getData().val().equals("sconcat")){
                    for(ParseTree sub : node.getChildren()){
                        list.add(sub);
                    }
                } else {
                    list.add(node);
                }
            }
            
            StringBuilder b = new StringBuilder();
            for(ParseTree node : list){
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
						new ExampleScript("", "cc('These' 'normally' 'have' 'spaces' 'between' 'them')"),
			};
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
						new ExampleScript("Symbolic usage", "'1' . '2' . '3' . '4'"),
			};
		}
		
		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN,
						OptimizationOption.OPTIMIZE_DYNAMIC
			);
		}
		
    }

    @api
	@noprofile
    public static class sconcat extends AbstractFunction implements Optimizable {

        public String getName() {
            return "sconcat";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            StringBuilder b = new StringBuilder();
            for(int i = 0; i < args.length; i++){
                if(i > 0){
                    b.append(" ");
                }
                b.append(args[i].val());
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
			return null;
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Functional usage", "sconcat('1', '2', '3', '4')"),
						new ExampleScript("Implied usage, do to no operators", "'1' '2' '3' '4'"),
			};
		}
		
		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN,
						OptimizationOption.OPTIMIZE_DYNAMIC
			);
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
            return "string {main, what, that} Replaces all instances of 'what' with 'that' in 'main'";
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
						new ExampleScript("No match found", "replace('The same thing', 'not found', '404')"),
			};
		}
		
		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
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
						OptimizationOption.CACHE_RETURN
			);
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
						new ExampleScript("", "'->' . trim('    <- spaces ->    ') . '<-'"),
			};
		}
		
		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
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
						new ExampleScript("", "'->' . trimr('    <- spaces ->    ') . '<-'"),
			};
		}
		
		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
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
						new ExampleScript("", "'->' . triml('    <- spaces ->    ') . '<-'"),
			};
		}
		
		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
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
            if (args[0] instanceof CArray) {
                return new CInt(((CArray) args[0]).size(), t);
            } else {
                return new CInt(args[0].val().length(), t);
            }
        }
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Strings", "length('this is a string')"),
						new ExampleScript("Arrays", "length(array('1', 2, '3', 4))"),
			};
		}
		
		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
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
						new ExampleScript("", "to_upper('Numbers (and SYMBOLS: 25)')"),
			};
		}
		
		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
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
						new ExampleScript("", "to_lower('Numbers (and SYMBOLS: 25)')"),
			};
		}
		
		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
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
                int begin = (int) Static.getInt(args[1]);
                int end;
                if (args.length == 3) {
                    end = (int) Static.getInt(args[2]);
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
						new ExampleScript("If the indexes are too large", "assign(@big, 25)\nsubstr('small', @big)"),
			};
		}
		
		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
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
				new ExampleScript("String not found", "string_position('Where\\'s Waldo?', 'Dunno.')"),
			};
		}
		
		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
    }
	
    @api
    public static class split extends AbstractFunction implements Optimizable {

        public String getName() {
            return "split";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "array {split, string} Splits a string into parts, using the split as the index. Though it can be used in every single case"
					+ " you would use reg_split, this does not use regex,"
					+ " and therefore can take a literal split expression instead of needing an escaped regex, and *may* perform better than the"
					+ " regex versions, as it uses an optimized tokenizer split, instead of Java regex.";
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
			int sp = 0;
			for(int i = 0; i < string.length() - split.length(); i++){				
				if(string.substring(i, i + split.length()).equals(split)){
					//Split point found
					array.push(new CString(string.substring(sp, i), t));
					sp = i + split.length();
					i += split.length();
				}
			}
			if(sp != 0){
				array.push(new CString(string.substring(sp, string.length()), t));
			}
			return array;
        }

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Simple split on one character. Note that unlike reg_split, no escaping is needed on the period.", "split('.', '1.2.3.4.5')"),
				new ExampleScript("Split with multiple characters", "split('ab', 'aaabaaabaaabaa')"),
			};
		}
		
		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CACHE_RETURN
			);
		}
		
    }
	
	
}
