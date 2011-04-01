/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.CancelCommandException;

/**
 *
 * @author layton
 */
public class die implements Function {

    public Integer []numArgs() {
        return new Integer[] {0,1};
    }

    public Construct exec(int line_num, Construct... args) throws CancelCommandException{
        if(args.length == 0){
            throw new CancelCommandException("");
        } else if(args.length == 1){
            throw new CancelCommandException(args[0].val());
        } else{
            return null;
        }
    }

    public String getName(){ return "die"; }

}
