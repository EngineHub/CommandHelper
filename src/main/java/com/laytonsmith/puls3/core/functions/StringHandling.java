/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.puls3.core.functions;

import com.laytonsmith.puls3.core.api;
import com.laytonsmith.puls3.core.exceptions.CancelCommandException;
import com.laytonsmith.puls3.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.puls3.core.constructs.CArray;
import com.laytonsmith.puls3.core.constructs.CEntry;
import com.laytonsmith.puls3.core.constructs.CInt;
import com.laytonsmith.puls3.core.constructs.CLabel;
import com.laytonsmith.puls3.core.constructs.CString;
import com.laytonsmith.puls3.core.constructs.Construct;
import com.laytonsmith.puls3.core.Env;
import com.laytonsmith.puls3.core.GenericTreeNode;
import com.laytonsmith.puls3.core.Static;
import com.laytonsmith.puls3.core.functions.Exceptions.ExceptionType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author Layton
 */
public class StringHandling {

    public static String docs() {
        return "These class provides functions that allow strings to be manipulated";
    }
    
    //@api
    public static class cc implements Function{

        public String getName() {
            return "cc";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "string {args...} The cousin to <strong>c</strong>on<strong>c</strong>at, this function does some magic under the covers"
                    + " to remove the auto-concatenation effect in bare strings. Take the following examples.";
        }

        public ExceptionType[] thrown() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isRestricted() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean preResolveVariables() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String since() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Boolean runAsync() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
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

        public Boolean runAsync() {
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return null;
        }

        public Construct execs(int line_num, File f, Env env, List<GenericTreeNode<Construct>> nodes) throws CancelCommandException, ConfigRuntimeException {
            StringBuilder b = new StringBuilder();
            boolean centry = false;
            Construct key = null;
            for (int i = 0; i < nodes.size(); i++) {
                Construct c = env.GetScript().preResolveVariable(env.GetScript().eval(nodes.get(i), env));
                if (i == 0) {
                    if (c instanceof CLabel) {
                        key = c;
                        centry = true;
                        break;
                    }
                }
                if (!centry) {
                    if (i > 1 || i > 0 && !centry) {
                        b.append(" ");
                    }
                    b.append(c.val());
                }
            }
            if (centry) {
                Construct value;
                if (nodes.subList(1, nodes.size()).size() > 1) {
                    //it's a string
                    StringBuilder c = new StringBuilder();
                    for (int i = 1; i < nodes.size(); i++) {
                        Construct d = env.GetScript().preResolveVariable(env.GetScript().eval(nodes.get(i), env));
                        if (i > 1) {
                            c.append(" ");
                        }
                        c.append(d.val());                        
                    }
                    value = new CString(c.toString(), line_num, f);
                } else {
                    value = env.GetScript().eval(nodes.subList(1, nodes.size()).get(0), env);
                }
                value = env.GetScript().preResolveVariable(value);
                return new CEntry(key, value, line_num, f);
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

        public Boolean runAsync() {
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String location = args[0].val();
            //Verify this file is not above the craftbukkit directory (or whatever directory the user specified
            if (!Static.CheckSecurity(location)) {
                throw new ConfigRuntimeException("You do not have permission to access the file '" + location + "'",
                        ExceptionType.SecurityException, line_num, f);
            }
            try {
                String s = file_get_contents(location);
                s = s.replaceAll("\n|\r\n", "\n");
                return new CString(s, line_num, f);
            } catch (Exception ex) {
                Static.getLogger().log(Level.SEVERE, "Could not read in file while attempting to find " + new File(location).getAbsolutePath()
                        + "\nFile " + (new File(location).exists() ? "exists" : "does not exist"));
                ex.printStackTrace();
                throw new ConfigRuntimeException("File could not be read in.",
                        ExceptionType.IOException, line_num, f);
            }
        }

        public String docs() {
            return "string {file} Reads in a file from the file system at location var1 and returns it as a string. The path is relative to"
                    + " the server, not Puls3. If the file is not found, or otherwise can't be read in, an IOException is thrown."
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

        public Boolean runAsync() {
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
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

        public Boolean runAsync() {
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
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

        public Boolean runAsync() {
            return null;
        }
    }

    @api
    public static class trim implements Function {

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

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.0.1";
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return new CString(args[0].val().trim(), args[0].getLineNum(), args[0].getFile());
        }

        public Boolean runAsync() {
            return null;
        }
    }

    @api
    public static class length implements Function {

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

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.1.2";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if (args[0] instanceof CArray) {
                return new CInt(((CArray) args[0]).size(), line_num, f);
            } else {
                return new CInt(args[0].val().length(), line_num, f);
            }
        }
    }

    @api
    public static class to_upper implements Function {

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

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.1.2";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return new CString(args[0].val().toUpperCase(), line_num, f);
        }
    }

    @api
    public static class to_lower implements Function {

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

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.1.2";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return new CString(args[0].val().toLowerCase(), line_num, f);
        }
    }

    @api
    public static class substr implements Function {

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

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.1.2";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            try {
                String s = args[0].val();
                int begin = (int) Static.getInt(args[1]);
                int end;
                if (args.length == 3) {
                    end = (int) Static.getInt(args[2]);
                } else {
                    end = s.length();
                }
                return new CString(s.substring(begin, end), line_num, f);
            } catch (IndexOutOfBoundsException e) {
                throw new ConfigRuntimeException("The indices given are not valid for string '" + args[0].val() + "'",
                        ExceptionType.RangeException, line_num, f);
            }
        }
    }
}
