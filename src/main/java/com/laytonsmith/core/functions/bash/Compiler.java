package com.laytonsmith.core.functions.bash;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.Target;

/**
 *
 * @author layton
 */
public class Compiler {
    
    public static String docs(){
        return "Bash compiler internal functions";
    }
    
    @api(platform=api.Platforms.COMPILER_BASH)
    public static class dyn extends BashFunction{

		@Override
        public String getName() {
            return "dyn";
        }

		@Override
        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

		@Override
        public String docs() {
            return "mixed {p} ";
        }

		@Override
        public String compile(Target t, String... args) {
            if(args.length == 0){
                return "0";
            }
            return args[0];
        }

		@Override
        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
        
    }
}
