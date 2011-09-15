/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine.functions.exceptions;

/**
 *
 * @author Layton
 */
public class ConfigCompileException extends Exception{

    int line_num;
    String message;

    public ConfigCompileException(String message, int line_num) {
        this.message = message;
        this.line_num = line_num;
    }

    public ConfigCompileException(String string) {
        message = string;
        line_num = 0;
    }
    
    @Override
    public String getMessage() {
        return message;
    }

    public String getLineNum(){
        return Integer.toString(line_num);
    }


    @Override
    public String toString(){
        if(line_num != 0){
            return "Configuration Compile Exception: " + message + " near line " + line_num + " of configuration file. Please "
                    + "check your config file and try again.";
        } else{
            return "Configuration Compile Exception: " + message + ". Please check your config file and try again.";
        }
    }
}
