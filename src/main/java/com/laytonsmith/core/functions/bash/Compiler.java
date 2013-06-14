package com.laytonsmith.core.functions.bash;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.Target;

/**
 *
 */
public class Compiler {
    
    public static String docs(){
        return "Bash compiler internal functions";
    }
    
    @api(platform=api.Platforms.COMPILER_BASH)
    public static class dyn extends BashFunction{

        public String getName() {
            return "dyn";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "mixed {p} ";
        }

        public String compile(Target t, String... args) {
            if(args.length == 0){
                return "0";
            }
            return args[0];
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
        
    }
}
