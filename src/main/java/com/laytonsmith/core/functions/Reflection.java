package com.laytonsmith.core.functions;

import com.laytonsmith.core.Env;
import com.laytonsmith.core.api;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.io.File;

/**
 *
 * @author layton
 */
public class Reflection {
    public static String docs() {
        return "This class of functions allows scripts to hook deep into the interpreter itself,"
                + " and get meta information about the operations of a script. This is useful for"
                + " debugging, testing, and ultra dynamic scripting. See the"
                + " [[CommandHelper/Reflection|guide to reflection]] on the wiki for more"
                + " details. In order to make the most of these functions, you should familiarize"
                + " yourself with the general workings of the language. These functions explore"
                + " extremely advanced concepts, and should normally not be used; especially"
                + " if you are not familiar with the language.";
    }
    
    @api public static class reflect_pull implements Function{

        public String getName() {
            return "reflect_pull";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "mixed {param, [args, ...]} Returns information about the runtime in a usable"
                    + " format. Depending on the information returned, it may be useable directly,"
                    + " or it may be more of a referential format. The following items can be retrieved:"
                    + "<table><thead><tr><th>param</th><th>args</th><th>returns/description</th></tr></thead><tbody>"
                    + "<tr><td>label</td><td></td><td>Return the label that the script is currently running under</td></tr>"
                    + "<tr><td>command</td><td></td><td>Returns the command that was used to fire off this script (if applicable)</td></tr>"
                    + "<tr><td>varlist</td><td>[name]</td><td>Returns a list of currently in scope variables. If name"
                    + " is provided, the currently set value is instead returned.</td></tr>"
                    + "<tr><td>line_num</td><td></td><td>The current line number</td></tr>"
                    + "<tr><td>file</td><td></td><td>The absolute path to the current file</td></tr>"
                    
                    + "</tbody></table>";
                    //+ "<tr><td></td><td></td><td></td></tr>"
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.FormatException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            if(args.length < 1){
                throw new ConfigRuntimeException("Not enough parameters was sent to " + getName(), ExceptionType.InsufficientArgumentsException, line_num, f);
            }
            
            String param = args[0].val();
            if("label".equalsIgnoreCase(param)){
                return new CString(env.GetLabel(), line_num, f);
            } else if("command".equalsIgnoreCase(param)){
                return new CString(env.GetCommand(), line_num, f);
            } else if("varlist".equalsIgnoreCase(param)){
                if(args.length == 1){
                    //No name provided
                    CArray ca = new CArray(line_num, f);
                    for(String name : env.GetVarList().keySet()){
                        ca.push(new CString(name, line_num, f));
                    }
                    return ca;
                } else if(args.length == 2){
                    //The name was provided
                    String name = args[1].val();
                    return env.GetVarList().get(name, line_num, f).ival();
                }
            } else if("line_num".equalsIgnoreCase(param)){
                return new CInt(line_num, line_num, f);
            } else if("file".equalsIgnoreCase(param)){
                if(f == null){
                    return new CString("Unknown (maybe the interpreter?)", line_num, f);
                } else {
                    return new CString(f.getAbsolutePath(), line_num, f);
                }
            }
            
            throw new ConfigRuntimeException("The arguments passed to " + getName() + " are incorrect. Please check them and try again.", 
                    ExceptionType.FormatException, line_num, f);
        }

        public String since() {
            return "3.3.1";
        }
        
    }
}
