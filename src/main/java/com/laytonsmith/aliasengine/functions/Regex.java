/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.api;
import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CInt;
import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Env;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import com.laytonsmith.aliasengine.exceptions.ConfigRuntimeException;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Layton
 */
public class Regex {
    
    public static String docs(){
        return "This class provides regular expression functions. For more details, please see the page on "
                + "[[CommandHelper/Regex|regular expressions]]";
    }
    
    @api public static class reg_match implements Function{

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

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.2.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            Pattern pattern = getPattern(args[0], line_num, f);
            String subject = args[1].val();
            CArray ret = new CArray(line_num, f);
            Matcher m = pattern.matcher(subject);
            if(m.find()){
                ret.push(new CString(m.group(0), line_num, f));

                for(int i = 1; i <= m.groupCount(); i++){
                    ret.push(new CString(m.group(i), line_num, f));
                }
            }
            return ret;
        }
        
    }
    
    @api public static class reg_match_all implements Function{

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

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.2.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            Pattern pattern = getPattern(args[0], line_num, f);
            String subject = args[1].val();
            CArray fret = new CArray(line_num, f);
            Matcher m = pattern.matcher(subject);
            while(m.find()){
                CArray ret = new CArray(line_num, f);
                ret.push(new CString(m.group(0), line_num, f));

                for(int i = 1; i <= m.groupCount(); i++){
                    ret.push(new CString(m.group(i), line_num, f));
                }
                fret.push(ret);
            }
            return fret;
        }
        
    }
    
    @api public static class reg_replace implements Function{

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

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.2.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            Pattern pattern = getPattern(args[0], line_num, f);
            String replacement = args[1].val();
            String subject = args[2].val();
            String ret = "";
            
            ret = pattern.matcher(subject).replaceAll(replacement);
            
            return new CString(ret, line_num, f);
        }
        
    }
    
    @api public static class reg_split implements Function{

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

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.2.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            Pattern pattern = getPattern(args[0], line_num, f);
            String subject = args[1].val();
            String [] rsplit = pattern.split(subject);
            CArray ret = new CArray(line_num, f);
            for(String split : rsplit){
                ret.push(new CString(split, line_num, f));
            }
            return ret;
        }
        
        
    }  
    
    @api public static class reg_count implements Function{

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

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.2.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            Pattern pattern = getPattern(args[0], line_num, f);
            String subject = args[1].val();
            long ret = 0;
            Matcher m = pattern.matcher(subject);
            while(m.find()){
                ret++;
            }
            return new CInt(ret, line_num, f);
        }
        
    }
    
    private static Pattern getPattern(Construct c, int line_num, File f){
        String regex = "";
        int flags = 0;
        String sflags = "";
        if(c instanceof CArray){
            CArray ca = (CArray)c;
            regex = ca.get(0, line_num).val();
            sflags = ca.get(1, line_num).val();
            for(int i = 0; i < sflags.length(); i++){
                if(sflags.toLowerCase().charAt(i) == 'i'){
                    flags |= Pattern.CASE_INSENSITIVE;
                } else if(sflags.toLowerCase().charAt(i) == 'm'){
                    flags |= Pattern.MULTILINE;
                } else if(sflags.toLowerCase().charAt(i) == 's'){
                    flags |= Pattern.DOTALL;
                } else {
                    throw new ConfigRuntimeException("Unrecognized flag: " + sflags.toLowerCase().charAt(i), ExceptionType.FormatException, line_num, f);
                }
            }
        } else {
            regex = c.val();
        }
        return Pattern.compile(regex, flags);
    }
}
