package com.laytonsmith.core.functions.bash;

import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.api;
import com.laytonsmith.core.constructs.Target;

/**
 *
 * @author layton
 */
public class Compiler {
    
    @api(platform=api.Platforms.COMPILER_BASH)
    public static class dyn extends BashFunction{

        public String compile(Target t, String... args) {
            if(args.length == 0){
                return "0";
            }
            return args[0];
        }

        public String docs() {
            return "mixed {p} ";
        }

        public String getName() {
            return "dyn";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
        
    }
    
    public static String docs(){
        return "Bash compiler internal functions";
    }
}
