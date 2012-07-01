/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.ZipReader;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author Layton
 */
public class StringHandling {

    @api
    public static class cc extends AbstractFunction{

        public String docs() {
            return "string {args...} The cousin to <strong>c</strong>on<strong>c</strong>at, this function does some magic under the covers"
                    + " to remove the auto-concatenation effect in bare strings. Take the following example: cc(bare string) -> barestring";
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            return new CVoid(t);
        }

        @Override
        public Construct execs(Target t, Env env, Script parent, GenericTreeNode<Construct>... nodes) {
            //if any of the nodes are sconcat, move their children up a level
            List<GenericTreeNode<Construct>> list = new ArrayList<GenericTreeNode<Construct>>();
            for(GenericTreeNode<Construct> node : nodes){
                if(node.getData().val().equals("sconcat")){
                    for(GenericTreeNode<Construct> sub : node.getChildren()){
                        list.add(sub);
                    }
                } else {
                    list.add(node);
                }
            }
            
            StringBuilder b = new StringBuilder();
            for(GenericTreeNode<Construct> node : list){
                Construct c = parent.seval(node, env);
                b.append(c.val());
            }
            return new CString(b.toString(), t);
        }

        public String getName() {
            return "cc";
        }

        public boolean isRestricted() {
            return false;
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public GenericTreeNode<Construct> optimizeSpecial(Target target, List<GenericTreeNode<Construct>> children) {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        public boolean preResolveVariables() {
            return false;
        }

        public Boolean runAsync() {
            return null;
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }

        public ExceptionType[] thrown() {
            return null;
        }

        @Override
        public boolean useSpecialExec() {
            return true;
        }
                
        
    }
    
    @api
    public static class concat extends AbstractFunction {

        @Override
        public boolean canOptimize() {
            return true;
        }

        public String docs() {
            return "string {var1, [var2...]} Concatenates any number of arguments together, and returns a string";
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                b.append(args[i].val());
            }
            return new CString(b.toString(), t);
        }

        public String getName() {
            return "concat";
        }

        public boolean isRestricted() {
            return false;
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            return exec(t, null, args);
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return null;
        }

        public CHVersion since() {
            return CHVersion.V3_0_1;
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public void varList(IVariableList varList) {
        }                
    }

    @api
    public static class length extends AbstractFunction {

        @Override
        public boolean canOptimize() {
            return true;
        }

        public String docs() {
            return "int {str | array} Returns the character length of str, if the value is castable to a string, or the length of the array, if an array is given";
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if (args[0] instanceof CArray) {
                return new CInt(((CArray) args[0]).size(), t);
            } else {
                return new CInt(args[0].val().length(), t);
            }
        }

        public String getName() {
            return "length";
        }

        public boolean isRestricted() {
            return false;
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return null;
        }

        public CHVersion since() {
            return CHVersion.V3_1_2;
        }
        
        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public void varList(IVariableList varList) {
        }
    }

    @api
    public static class parse_args extends AbstractFunction {

        @Override
        public boolean canOptimize() {
            return true;
        }

        public String docs() {
            return "array {string} Parses string into an array, where string is a space seperated list of arguments. Handy for turning"
                    + " $ into a usable array of items with which to script against. Extra spaces are ignored, so you would never get an empty"
                    + " string as an input.";
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String[] sa = args[0].val().split(" ");
            ArrayList<Construct> a = new ArrayList<Construct>();
            for (String s : sa) {
                if (!s.trim().equals("")) {
                    a.add(new CString(s.trim(), t));
                }
            }
            Construct[] csa = new Construct[a.size()];
            for (int i = 0; i < a.size(); i++) {
                csa[i] = a.get(i);
            }
            return new CArray(t, csa);
        }

        public String getName() {
            return "parse_args";
        }

        public boolean isRestricted() {
            return false;
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return null;
        }

        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        
        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public void varList(IVariableList varList) {
        }
    }

    @api
    public static class read extends AbstractFunction {

        public static String file_get_contents(String file_location) throws Exception {
            return new ZipReader(new File(file_location)).getFileContents();
        }

        public String docs() {
            return "string {file} Reads in a file from the file system at location var1 and returns it as a string. The path is relative to"
                    + " the server, not CommandHelper. If the file is not found, or otherwise can't be read in, an IOException is thrown."
                    + " If the file specified is not within base-dir (as specified in the preferences file), a SecurityException is thrown."
                    + " The line endings for the string returned will always be \\n, even if they originally were \\r\\n.";
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String location = args[0].val();
            location = new File(t.file().getParentFile(), location).getAbsolutePath();
            //Verify this file is not above the craftbukkit directory (or whatever directory the user specified
            if (!Security.CheckSecurity(location)) {
                throw new ConfigRuntimeException("You do not have permission to access the file '" + location + "'",
                        ExceptionType.SecurityException, t);
            }
            try {
                String s = file_get_contents(location);
                s = s.replaceAll("\n|\r\n", "\n");
                return new CString(s, t);
            } catch (Exception ex) {
                Static.getLogger().log(Level.SEVERE, "Could not read in file while attempting to find " + new File(location).getAbsolutePath()
                        + "\nFile " + (new File(location).exists() ? "exists" : "does not exist"));
                throw new ConfigRuntimeException("File could not be read in.",
                        ExceptionType.IOException, t);
            }
        }

        public String getName() {
            return "read";
        }

        public boolean isRestricted() {
            return true;
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            //Because we do disk IO
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_0_1;
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.IOException, ExceptionType.SecurityException};
        }

        public void varList(IVariableList varList) {
        }
    }

    @api
    public static class replace extends AbstractFunction {

        @Override
        public boolean canOptimize() {
            return true;
        }

        public String docs() {
            return "string {main, what, that} Replaces all instances of 'what' with 'that' in 'main'";
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String thing = args[0].val();
            String what = args[1].val();
            String that = args[2].val();
            return new CString(thing.replace(what, that), t);
        }

        public String getName() {
            return "replace";
        }

        public boolean isRestricted() {
            return false;
        }

        public Integer[] numArgs() {
            return new Integer[]{3};
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return null;
        }

        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        
        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public void varList(IVariableList varList) {
        }
    }

    @api
    public static class sconcat extends AbstractFunction {

        @Override
        public boolean canOptimize() {
            return true;
        }

        public String docs() {
            return "string {var1, [var2...]} Concatenates any number of arguments together, but puts a space between elements";
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
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
//        public Construct execs(Target t, Env env, Script parent, GenericTreeNode<Construct>... nodes) {
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

        public String getName() {
            return "sconcat";
        }

        public boolean isRestricted() {
            return false;
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        @Override
        public Construct optimize(Target t, Construct... args) {
            return exec(t, null, args);
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return null;
        }

        public CHVersion since() {
            return CHVersion.V3_0_1;
        }      
        
        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public void varList(IVariableList varList) {
        }  
    }

    @api
    public static class substr extends AbstractFunction {

        @Override
        public boolean canOptimize() {
            return true;
        }

        public String docs() {
            return "string {str, begin, [end]} Returns a substring of the given string str, starting from index begin, to index end, or the"
                    + " end of the string, if no index is given. If either begin or end are out of bounds of the string, an exception is thrown."
                    + " substr('hamburger', 4, 8) returns \"urge\", substr('smiles', 1, 5) returns \"mile\", and substr('lightning', 5) returns \"ning\"."
                    + " See also length().";
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
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

        public String getName() {
            return "substr";
        }

        public boolean isRestricted() {
            return false;
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return null;
        }

        public CHVersion since() {
            return CHVersion.V3_1_2;
        }
        
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.RangeException, ExceptionType.CastException};
        }

        public void varList(IVariableList varList) {
        }
    }

    @api
    public static class to_lower extends AbstractFunction {

        @Override
        public boolean canOptimize() {
            return true;
        }

        public String docs() {
            return "string {str} Returns an all lower case version of str";
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return new CString(args[0].val().toLowerCase(), t);
        }

        public String getName() {
            return "to_lower";
        }

        public boolean isRestricted() {
            return false;
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return null;
        }

        public CHVersion since() {
            return CHVersion.V3_1_2;
        }
        
        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public void varList(IVariableList varList) {
        }
    }

    @api
    public static class to_upper extends AbstractFunction {

        @Override
        public boolean canOptimize() {
            return true;
        }

        public String docs() {
            return "string {str} Returns an all caps version of str";
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return new CString(args[0].val().toUpperCase(), t);
        }

        public String getName() {
            return "to_upper";
        }

        public boolean isRestricted() {
            return false;
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return null;
        }

        public CHVersion since() {
            return CHVersion.V3_1_2;
        }
        
        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public void varList(IVariableList varList) {
        }
    }

    @api
    public static class trim extends AbstractFunction {

        @Override
        public boolean canOptimize() {
            return true;
        }

        public String docs() {
            return "string {s} Returns the string s with leading and trailing whitespace cut off";
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return new CString(args[0].val().trim(), args[0].getTarget());
        }

        public String getName() {
            return "trim";
        }

        public boolean isRestricted() {
            return false;
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return null;
        }

        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        
        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public void varList(IVariableList varList) {
        }
    }

    public static String docs() {
        return "These class provides functions that allow strings to be manipulated";
    }
}
