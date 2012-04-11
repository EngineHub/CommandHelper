/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.ZipReader;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.GenericTreeNode;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.Security;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author Layton
 */
public class IncludeCache {
    private static final CHLog.Tags TAG = CHLog.Tags.INCLUDES;
    private static HashMap<File, GenericTreeNode<Construct>> cache = new HashMap<File, GenericTreeNode<Construct>>();
    
    private static void add(File file, GenericTreeNode<Construct> tree){
        cache.put(file, tree);
    }
    
    public static GenericTreeNode<Construct> get(File file, Target t){
        CHLog.Log(TAG, CHLog.Level.DEBUG, "Loading " + file.getAbsolutePath(), t);
        if(!cache.containsKey(file)){
            CHLog.Log(TAG, CHLog.Level.VERBOSE, "Cache does not already contain include file, compiling, then caching.", t);
            //We have to pull the file from the FS, and compile it.
            if(Security.CheckSecurity(file.getAbsolutePath())){
                CHLog.Log(TAG, CHLog.Level.VERBOSE, "Security check passed", t);
                try {
                    String s = new ZipReader(file).getFileContents();
                    GenericTreeNode<Construct> tree = MethodScriptCompiler.compile(MethodScriptCompiler.lex("g(\n" + s + "\n)", file));
                    CHLog.Log(TAG, CHLog.Level.VERBOSE, "Compilation succeeded, adding to cache.", t);
                    IncludeCache.add(file, tree);
                } catch (ConfigCompileException ex) {
                    throw new ConfigRuntimeException("There was a compile error when trying to include the script at " + file
                            + "\n" + ex.getMessage() + " :: " + file.getName() + ":" + ex.getLineNum(), 
                            Exceptions.ExceptionType.IncludeException, t);
                } catch (IOException ex) {
                    throw new ConfigRuntimeException("The script at " + file + " could not be found or read in.", 
                            Exceptions.ExceptionType.IOException, t);
                }
            } else {
                throw new ConfigRuntimeException("The script cannot access " + file + " due to restrictions imposed by the base-dir setting.", 
                        Exceptions.ExceptionType.SecurityException, t);
            }
        }
        CHLog.Log(TAG, CHLog.Level.INFO, "Returning " + file.getAbsolutePath() + " from cache", t);
        return cache.get(file);
    }
    
    public static void clearCache(){
        CHLog.Log(TAG, CHLog.Level.INFO, "Clearing include cache", Target.UNKNOWN);
        cache.clear();
    }
}
