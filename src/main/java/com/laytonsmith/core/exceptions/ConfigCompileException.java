/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.core.exceptions;

import java.io.File;

/**
 *
 * @author Layton
 */
public class ConfigCompileException extends Exception{

    int line_num;
    String message;
    File file;

    public ConfigCompileException(String message, int line_num, File f) {
        this.message = message;
        this.line_num = line_num;
        this.file = f;
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
                    + "check your config file and try again. " + (file!=null?"(" + file.getAbsolutePath() + ")":"");
        } else{
            return "Configuration Compile Exception: " + message + ". Please check your config file and try again. " 
                    + (file!=null?"(" + file.getAbsolutePath() + ")":"");
        }
    }

    public File getFile() {
        return this.file;
    }

    public String getSimpleFile(){
        if(this.file != null){
            return this.file.getName();
        } else {
            return null;
        }
    }
}
