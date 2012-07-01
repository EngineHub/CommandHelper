/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.core.exceptions;

import com.laytonsmith.core.constructs.Target;
import java.io.File;

/**
 *
 * @author Layton
 */
public class ConfigCompileException extends Exception{

    final int col;
    final File file;
    final int line_num;
    final String message;

    /**
     * This turns a ConfigRuntimeException into a compile time exception. Typically only
     * used during optimization.
     * @param e 
     */
    public ConfigCompileException(ConfigRuntimeException e) {
        this(e.getMessage(), e.getTarget(), e);
    }
    public ConfigCompileException(String message, Target t){
        this(message, t, null);
    }

    public ConfigCompileException(String message, Target t, Throwable cause) {
        super(cause);
        this.message = message;
        this.line_num = t.line();
        this.file = t.file();
        this.col = t.col();
    }
    
    public File getFile() {
        return this.file;
    }

    public String getLineNum(){
        return Integer.toString(line_num);
    }


    @Override
    public String getMessage() {
        return message;
    }

    public String getSimpleFile(){
        if(this.file != null){
            return this.file.getName();
        } else {
            return null;
        }
    }

    @Override
    public String toString(){
        if(line_num != 0){
            return "Configuration Compile Exception: " + message + " near line " + line_num + " of configuration file. Please "
                    + "check your config file and try again. " + (file!=null?"(" + file.getAbsolutePath() + ")":"");
        } else{
            return "Configuration Compile Exception: " + message + ". Please check your config file and try again. " 
                    + (file!=null?"(" + file.getAbsolutePath() + ")":"");
        }
    }
}
