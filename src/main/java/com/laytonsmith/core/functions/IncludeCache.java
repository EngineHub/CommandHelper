/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.fileutility.FileUtility;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.GenericTreeNode;
import com.laytonsmith.core.MScriptCompiler;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

/**
 *
 * @author Layton
 */
public class IncludeCache {
    private static HashMap<File, GenericTreeNode<Construct>> cache = new HashMap<File, GenericTreeNode<Construct>>();
    
    public static void add(File file, GenericTreeNode<Construct> tree){
        cache.put(file, tree);
    }
    
    public static GenericTreeNode<Construct> get(File file, int line_num, File myFile){
        if(!cache.containsKey(file)){
            //We have to pull the file from the FS, and compile it.
            if(Static.CheckSecurity(file.getAbsolutePath())){
                try {
                    String s = FileUtility.read(file);
                    GenericTreeNode<Construct> tree = MScriptCompiler.compile(MScriptCompiler.lex("g(\n" + s + "\n)", file));
                    IncludeCache.add(file, tree);
                } catch (ConfigCompileException ex) {
                    throw new ConfigRuntimeException("There was a compile error when trying to include the script at " + file
                            + "\n" + ex.getMessage() + " :: " + file.getName() + ":" + ex.getLineNum(), 
                            Exceptions.ExceptionType.IncludeException, line_num, myFile);
                } catch (FileNotFoundException ex) {
                    throw new ConfigRuntimeException("The script at " + file + " could not be found.", 
                            Exceptions.ExceptionType.IOException, line_num, myFile);
                }
            } else {
                throw new ConfigRuntimeException("The script cannot access " + file + " due to restrictions imposed by the base-dir setting.", 
                        Exceptions.ExceptionType.SecurityException, line_num, myFile);
            }
        }
        return cache.get(file);
    }
    
    public static void clearCache(){
        cache.clear();
    }
}
