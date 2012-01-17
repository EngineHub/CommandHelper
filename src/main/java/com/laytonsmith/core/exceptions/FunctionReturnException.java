/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.exceptions;

import com.laytonsmith.core.constructs.Construct;

/**
 *
 * @author Layton
 */
public class FunctionReturnException extends RuntimeException{
    Construct ret;
    public FunctionReturnException(Construct ret){
        this.ret = ret;
    }
    public Construct getReturn(){
        return ret;
    }
}
