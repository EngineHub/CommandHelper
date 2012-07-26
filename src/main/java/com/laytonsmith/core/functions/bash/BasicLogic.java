package com.laytonsmith.core.functions.bash;

import com.laytonsmith.core.CHVersion;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.constructs.Target;

/**
 *
 * @author layton
 */
public class BasicLogic {
    public static String docs(){
        return "Contains basic logic functions for bash";
    }
    @api(platform=api.Platforms.COMPILER_BASH)
    public static class _if extends BashFunction{

        public String getName() {
            return "if";
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

        public String docs() {
            return "void {condition, ifcode, elsecode} Runs the ifcode if condition is true, otherwise, "
                    + "runs the false code. Note that nothing is ever returned.";
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

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
        
    }
    
    @api(platform=api.Platforms.COMPILER_BASH)
    public static class equals extends BashFunction{

        public String getName() {
            return "equals";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "boolean {arg1, arg2} Compares two arguments for equality";
        }

        public String compile(Target t, String... args) {
            return args[0] + " == " + args[1];
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
        
    }
}
