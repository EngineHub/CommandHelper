

package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.ZipReader;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Security;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CREIOException;
import com.laytonsmith.core.exceptions.CRE.CREIncludeException;
import com.laytonsmith.core.exceptions.CRE.CRESecurityException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 */
public class IncludeCache {
    private static final CHLog.Tags TAG = CHLog.Tags.INCLUDES;
    private static HashMap<File, ParseTree> cache = new HashMap<File, ParseTree>();

    private static void add(File file, ParseTree tree){
        cache.put(file, tree);
    }

    public static ParseTree get(File file, Target t){
        CHLog.GetLogger().Log(TAG, LogLevel.DEBUG, "Loading " + file.getAbsolutePath(), t);
        if(!cache.containsKey(file)){
            CHLog.GetLogger().Log(TAG, LogLevel.VERBOSE, "Cache does not already contain include file, compiling, then caching.", t);
            //We have to pull the file from the FS, and compile it.
            if(Security.CheckSecurity(file.getAbsolutePath())){
                CHLog.GetLogger().Log(TAG, LogLevel.VERBOSE, "Security check passed", t);
                try {
                    String s = new ZipReader(file).getFileContents();
                    ParseTree tree = MethodScriptCompiler.compile(MethodScriptCompiler.lex(s, file, true));
                    CHLog.GetLogger().Log(TAG, LogLevel.VERBOSE, "Compilation succeeded, adding to cache.", t);
                    IncludeCache.add(file, tree);
                } catch (ConfigCompileException ex) {
                    throw ConfigRuntimeException.BuildException("There was a compile error when trying to include the script at " + file
                            + "\n" + ex.getMessage() + " :: " + file.getName() + ":" + ex.getLineNum(),
                            CREIncludeException.class, t);
				} catch(ConfigCompileGroupException ex){
					StringBuilder b = new StringBuilder();
					b.append("There were compile errors when trying to include the script at ").append(file).append("\n");
					for(ConfigCompileException e : ex.getList()){
						b.append(e.getMessage()).append(" :: ").append(e.getFile().getName()).append(":").append(e.getLineNum());
					}
					throw ConfigRuntimeException.BuildException(b.toString(), CREIncludeException.class, t);
                } catch (IOException ex) {
                    throw ConfigRuntimeException.BuildException("The script at " + file + " could not be found or read in.",
                            CREIOException.class, t);
                }
            } else {
                throw ConfigRuntimeException.BuildException("The script cannot access " + file + " due to restrictions imposed by the base-dir setting.",
                        CRESecurityException.class, t);
            }
        }
        CHLog.GetLogger().Log(TAG, LogLevel.INFO, "Returning " + file.getAbsolutePath() + " from cache", t);
        return cache.get(file);
    }

    public static void clearCache(){
        CHLog.GetLogger().Log(TAG, LogLevel.INFO, "Clearing include cache", Target.UNKNOWN);
        cache.clear();
    }
}
