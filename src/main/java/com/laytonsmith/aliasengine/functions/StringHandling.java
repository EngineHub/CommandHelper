/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.api;
import com.laytonsmith.aliasengine.exceptions.CancelCommandException;
import com.laytonsmith.aliasengine.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CEntry;
import com.laytonsmith.aliasengine.Constructs.CInt;
import com.laytonsmith.aliasengine.Constructs.CLabel;
import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.logging.Level;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Layton
 */
public class StringHandling {

    public static String docs() {
        return "These class provides functions that allow strings to be manipulated";
    }

    @api
    public static class concat implements Function {

        public String getName() {
            return "concat";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }
        
        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                b.append(args[i].val());
            }
            return new CString(b.toString(), line_num, f);
        }

        public String docs() {
            return "string {var1, [var2...]} Concatenates any number of arguments together, and returns a string";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }
        public String since() {
            return "3.0.1";
        }
        public Boolean runAsync(){
            return null;
        }
    }

    @api
    public static class sconcat implements Function {

        public String getName() {
            return "sconcat";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            StringBuilder b = new StringBuilder();
            boolean centry = false;
            for (int i = 0; i < args.length; i++) {
                if(i == 0 && args[0] instanceof CLabel){
                    centry = true;
                    continue;
                }
                if (i > 1 || i > 0 && !(args[0] instanceof CLabel)) {
                    b.append(" ");
                }
                b.append(args[i].val());
            }
            if(centry){
                return new CEntry(((CLabel)args[0]).cVal(), Static.resolveConstruct(b.toString(), line_num, f), line_num, f);
            } else {
                return new CString(b.toString(), line_num, f);
            }
        }

        public String docs() {
            return "string {var1, [var2...]} Concatenates any number of arguments together, but puts a space between elements";
        }
        
        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }
        public String since() {
            return "3.0.1";
        }
        public Boolean runAsync(){
            return null;
        }
    }

    @api
    public static class read implements Function {

        public static String file_get_contents(String file_location) throws Exception {
            BufferedReader in = new BufferedReader(new FileReader(file_location));
            StringBuilder ret = new StringBuilder();
            String str;
            while ((str = in.readLine()) != null) {
                ret.append(str).append("\n");
            }
            in.close();
            return ret.toString();
        }

        public String getName() {
            return "read";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String location = args[0].val();
            //Verify this file is not above the craftbukkit directory (or whatever directory the user specified
            if(!Static.CheckSecurity(location)){
                throw new ConfigRuntimeException("You do not have permission to access the file '" + location + "'", 
                        ExceptionType.SecurityException, line_num, f);
            }
            try {
                String s = file_get_contents(location);
                s = s.replaceAll("\n|\r\n", "\n");
                return new CString(s, line_num, f);
            } catch (Exception ex) {
                Static.getLogger().log(Level.SEVERE, "Could not read in file while attempting to find " + new File(location).getAbsolutePath()
                        + "\nFile " + (new File(location).exists()?"exists":"does not exist"));
                ex.printStackTrace();
                throw new ConfigRuntimeException("File could not be read in.", 
                        ExceptionType.IOException, line_num, f);
            }
        }

        public String docs() {
            return "string {file} Reads in a file from the file system at location var1 and returns it as a string. The path is relative to"
                    + " CraftBukkit, not CommandHelper. If the file is not found, or otherwise can't be read in, an IOException is thrown."
                    + " If the file specified is not within base-dir (as specified in the preferences file), a SecurityException is thrown."
                    + " The line endings for the string returned will always be \\n, even if they originally were \\r\\n.";
        }
        
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.IOException, ExceptionType.SecurityException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }
        public String since() {
            return "3.0.1";
        }
        public Boolean runAsync(){
            //Because we do disk IO
            return true;
        }
    }

    @api
    public static class replace implements Function {

        public String getName() {
            return "replace";
        }

        public Integer[] numArgs() {
            return new Integer[]{3};
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String thing = args[0].val();
            String what = args[1].val();
            String that = args[2].val();
            return new CString(thing.replace(what, that), line_num, f);
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

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }
        public String since() {
            return "3.0.1";
        }
        public Boolean runAsync(){
            return null;
        }
    }

    @api
    public static class parse_args implements Function {

        public String getName() {
            return "parse_args";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String[] sa = args[0].val().split(" ");
            ArrayList<Construct> a = new ArrayList<Construct>();
            for (String s : sa) {
                if (!s.trim().equals("")) {
                    a.add(new CString(s.trim(), line_num, f));
                }
            }
            Construct[] csa = new Construct[a.size()];
            for (int i = 0; i < a.size(); i++) {
                csa[i] = a.get(i);
            }
            return new CArray(line_num, f, csa);
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

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }
        public String since() {
            return "3.0.1";
        }
        public Boolean runAsync(){
            return null;
        }
    }
    
    @api public static class trim implements Function{

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

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.0.1";
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return new CString(args[0].val().trim(), args[0].getLineNum(), args[0].getFile());
        }
        public Boolean runAsync(){
            return null;
        }
        
    }
    
    @api public static class length implements Function{

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

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.1.2";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(args[0] instanceof CArray){
                return new CInt(((CArray)args[0]).size(), line_num, f);
            } else {
                return new CInt(args[0].val().length(), line_num, f);
            }
        }
        
    }
    
    @api public static class to_upper implements Function{

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

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.1.2";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return new CString(args[0].val().toUpperCase(), line_num, f);
        }
        
    }
    
    @api public static class to_lower implements Function{

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

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.1.2";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return new CString(args[0].val().toLowerCase(), line_num, f);
        }        
    }
    
    @api public static class substr implements Function{

        public String getName() {
            return "substr";
        }

        public Integer[] numArgs() {
            return new Integer[]{2,3};
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

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.1.2";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            try{
                String s = args[0].val();
                int begin = (int)Static.getInt(args[1]);
                int end;
                if(args.length == 3){
                    end = (int)Static.getInt(args[2]);
                } else {
                    end = s.length();
                }
                return new CString(s.substring(begin, end), line_num, f);
            } catch(IndexOutOfBoundsException e){
                throw new ConfigRuntimeException("The indices given are not valid for string '" + args[0].val() + "'",
                        ExceptionType.RangeException, line_num, f);
            }
        }
        
    }     
}
