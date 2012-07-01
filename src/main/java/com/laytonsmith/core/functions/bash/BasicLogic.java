package com.laytonsmith.core.functions.bash;

import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.api;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.functions.FunctionBase;

/**
 *
 * @author layton
 */
public class BasicLogic {
    @api(platform=api.Platforms.COMPILER_BASH)
    public static class _if extends BashFunction{

        public boolean appearInDocumentation() {
            return true;
        }

        public String compile(Target t, String ... args) {
            String s = "if [ " + args[0] + " ]; then\n"
                 + args[1] + "\n";            
            
            if(args.length == 3){
                s += "else\n";
                s += args[2] + "\n";
            }
            
            s += "fi\n";
            
            return s;
        }

        public String docs() {
            return "void {condition, ifcode, elsecode} Runs the ifcode if condition is true, otherwise, "
                    + "runs the false code. Note that nothing is ever returned.";
        }

        public String getName() {
            return "if";
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
        
    }
    @api(platform=api.Platforms.COMPILER_BASH)
    public static class equals extends BashFunction{

        public String compile(Target t, String... args) {
            return args[0] + " == " + args[1];
        }

        public String docs() {
            return "boolean {arg1, arg2} Compares two arguments for equality";
        }

        public String getName() {
            return "equals";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
        
    }
    
    public static String docs(){
        return "Contains basic logic functions for bash";
    }
}
