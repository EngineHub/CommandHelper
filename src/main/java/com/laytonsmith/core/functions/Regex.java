

package com.laytonsmith.core.functions;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 * @author Layton
 */
public class Regex {
    
    public static String docs(){
        return "This class provides regular expression functions. For more details, please see the page on "
                + "[[CommandHelper/Regex|regular expressions]]. Note that all the functions are just passthroughs"
                + " to the Java regex mechanism. If you need to set a flag on the regex, where the api calls"
                + " for a pattern, instead send array('pattern', 'flags') where flags is any of i, m, or s."
                + " Alternatively, using the embedded flag system that Java provides is also valid.";
    }
    
    @api public static class reg_match extends AbstractFunction{

        public String getName() {
            return "reg_match";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "array {pattern, subject} Searches for the given pattern, and returns an array with the results. Captures are supported."
                    + " If the pattern is not found anywhere in the subject, an empty array is returned. The indexes of the array"
                    + " follow typical regex fashion; the 0th element is the whole match, and 1-n are the captures specified in"
                    + " the regex.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.FormatException};
        }

        public boolean isRestricted() {
            return false;
        }

        
        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            Pattern pattern = getPattern(args[0], t);
            String subject = args[1].val();
            CArray ret = new CArray(t);
            Matcher m = pattern.matcher(subject);
            if(m.find()){
                ret.push(new CString(m.group(0), t));

                for(int i = 1; i <= m.groupCount(); i++){
                    if(m.group(i) == null){
                        ret.push(new CNull(t));
                    } else {
                        ret.push(Static.resolveConstruct(m.group(i), t));
                    }
                }
            }
            return ret;
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }
        
        @Override
        public boolean canOptimizeDynamic() {
            return true;
        }                

        @Override
        public GenericTreeNode<Construct> optimizeDynamic(Target t, List<GenericTreeNode<Construct>> children) throws ConfigCompileException, ConfigRuntimeException {
            if(!children.get(0).getData().isDynamic()){
                getPattern(children.get(0).getData(), t);
            }
            return null;
        } 
        
    }
    
    @api public static class reg_match_all extends AbstractFunction{

        public String getName() {
            return "reg_match_all";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "array {pattern, subject} Searches subject for all matches to the regular expression given in pattern, unlike reg_match,"
                    + " which just returns the first match.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.FormatException};
        }

        public boolean isRestricted() {
            return false;
        }

        
        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            Pattern pattern = getPattern(args[0], t);
            String subject = args[1].val();
            CArray fret = new CArray(t);
            Matcher m = pattern.matcher(subject);
            while(m.find()){
                CArray ret = new CArray(t);
                ret.push(new CString(m.group(0), t));

                for(int i = 1; i <= m.groupCount(); i++){
                    ret.push(new CString(m.group(i), t));
                }
                fret.push(ret);
            }
            return fret;
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }
        
        @Override
        public boolean canOptimizeDynamic() {
            return true;
        }                

        @Override
        public GenericTreeNode<Construct> optimizeDynamic(Target t, List<GenericTreeNode<Construct>> children) throws ConfigCompileException, ConfigRuntimeException {
            if(!children.get(0).getData().isDynamic()){
                getPattern(children.get(0).getData(), t);
            }
            return null;
        } 
        
    }
    
    @api public static class reg_replace extends AbstractFunction{

        public String getName() {
            return "reg_replace";
        }

        public Integer[] numArgs() {
            return new Integer[]{3};
        }

        public String docs() {
            return "string {pattern, replacement, subject} Replaces any occurances of pattern with the replacement in subject."
                    + " Back references are allowed.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.FormatException};
        }

        public boolean isRestricted() {
            return false;
        }

        
        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            Pattern pattern = getPattern(args[0], t);
            String replacement = args[1].val();
            String subject = args[2].val();
            String ret = "";
            
            try {
            	ret = pattern.matcher(subject).replaceAll(replacement);
            } catch (IndexOutOfBoundsException e) {
            	throw new ConfigRuntimeException("Expecting a regex group at parameter 1 of reg_replace",
            			ExceptionType.FormatException, t);
            }
            
            return new CString(ret, t);
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }
        
        @Override
        public boolean canOptimizeDynamic() {
            return true;
        }                

        @Override
        public GenericTreeNode<Construct> optimizeDynamic(Target t, List<GenericTreeNode<Construct>> children) throws ConfigCompileException, ConfigRuntimeException {
            if(!children.get(0).getData().isDynamic()){
                getPattern(children.get(0).getData(), t);
            }
            return null;
        } 
        
    }
    
    @api public static class reg_split extends AbstractFunction{

        public String getName() {
            return "reg_split";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "array {pattern, subject} Splits a string on the given regex, and returns an array of the parts. If"
                    + " nothing matched, an array with one element, namely the original subject, is returned.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.FormatException};
        }

        public boolean isRestricted() {
            return false;
        }

        
        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            Pattern pattern = getPattern(args[0], t);
            String subject = args[1].val();
            String [] rsplit = pattern.split(subject);
            CArray ret = new CArray(t);
            for(String split : rsplit){
                ret.push(new CString(split, t));
            }
            return ret;
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }
        
        @Override
        public boolean canOptimizeDynamic() {
            return true;
        }                

        @Override
        public GenericTreeNode<Construct> optimizeDynamic(Target t, List<GenericTreeNode<Construct>> children) throws ConfigCompileException, ConfigRuntimeException {
            if(!children.get(0).getData().isDynamic()){
                getPattern(children.get(0).getData(), t);
            }
            return null;
        } 
        
    }  
    
    @api public static class reg_count extends AbstractFunction{

        public String getName() {
            return "reg_count";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "int {pattern, subject} Counts the number of occurances in the subject.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.FormatException};
        }

        public boolean isRestricted() {
            return false;
        }

        
        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            Pattern pattern = getPattern(args[0], t);
            String subject = args[1].val();
            long ret = 0;
            Matcher m = pattern.matcher(subject);
            while(m.find()){
                ret++;
            }
            return new CInt(ret, t);
        }
        
        @Override
        public boolean canOptimize() {
            return true;
        }                

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }
        
        @Override
        public boolean canOptimizeDynamic() {
            return true;
        }                

        @Override
        public GenericTreeNode<Construct> optimizeDynamic(Target t, List<GenericTreeNode<Construct>> children) throws ConfigCompileException, ConfigRuntimeException {
            if(!children.get(0).getData().isDynamic()){
                getPattern(children.get(0).getData(), t);
            }
            return null;
        }                
        
    }
    
    @api
    public static class reg_escape extends AbstractFunction{

        public ExceptionType[] thrown() {
            return null;
        }

        public boolean isRestricted() {
            return false;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            return new CString(Pattern.quote(args[0].val()), t);
        }

        public String getName() {
            return "reg_escape";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "string {arg} Escapes arg so that it may be used directly in a regular expression, without fear that"
                    + " it will have special meaning; that is, it escapes all special characters. Use this if you need"
                    + " to use user input or similar as a literal search index.";
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }

        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }                
        
    }
    
    private static Pattern getPattern(Construct c, Target t) throws ConfigRuntimeException{
        String regex = "";
        int flags = 0;
        String sflags = "";
        if(c instanceof CArray){
            CArray ca = (CArray)c;
            regex = ca.get(0, t).val();
            sflags = ca.get(1, t).val();
            for(int i = 0; i < sflags.length(); i++){
                if(sflags.toLowerCase().charAt(i) == 'i'){
                    flags |= Pattern.CASE_INSENSITIVE;
                } else if(sflags.toLowerCase().charAt(i) == 'm'){
                    flags |= Pattern.MULTILINE;
                } else if(sflags.toLowerCase().charAt(i) == 's'){
                    flags |= Pattern.DOTALL;
                } else {
                    throw new ConfigRuntimeException("Unrecognized flag: " + sflags.toLowerCase().charAt(i), ExceptionType.FormatException, t);
                }
            }
        } else {
            regex = c.val();
        }
        try{
            return Pattern.compile(regex, flags);
        } catch(PatternSyntaxException e){
            throw new ConfigRuntimeException(e.getMessage(), ExceptionType.FormatException, t);
        }
    }
}
