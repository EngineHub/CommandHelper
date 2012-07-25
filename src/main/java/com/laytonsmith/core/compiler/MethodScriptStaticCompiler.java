package com.laytonsmith.core.compiler;

import com.laytonsmith.core.GenericTreeNode;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.api;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.CompiledFunction;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The static compiler uses the dynamic compiler, but using a platform specific framework,
 * ends up with output that can be written to out to disk, and run natively, or compiles
 * to another language entirely.
 * @author layton
 */
public final class MethodScriptStaticCompiler {
    
    private MethodScriptStaticCompiler(){}
    
    /**
     * Compiles the script, converting it into mid level object code, or in
     * the case of a language compiler, the other language's source code.
     * @param script
     * @param platform
     * @return 
     */
    public static String compile(String script, api.Platforms platform, File file) throws ConfigCompileException{
        //First, we optimize. The "core" functions are always run through
        //the native interpreter's compiler for optimization.
        GenericTreeNode<Construct> tree = MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, file));
        StringBuilder b = new StringBuilder();
        for(GenericTreeNode<Construct> node : tree.getChildren()){
            go(node, b, platform);
        }
        return b.toString();
    }
    
    private static void go(GenericTreeNode<Construct> node, StringBuilder b, api.Platforms platform) throws ConfigCompileException{
        if(node.hasChildren()){
            FunctionBase f = FunctionList.getFunction(node.data, platform);            
            if(!(f instanceof CompiledFunction)){
                throw new ConfigCompileException("The function " + f.getName() + " is unknown in this platform.", node.data.getTarget());
            }
            CompiledFunction cf = (CompiledFunction)f;
            List<String> children = new ArrayList<String>();
            for(GenericTreeNode<Construct> baby : node.getChildren()){
                StringBuilder bb = new StringBuilder();
                go(baby, bb, platform);
                children.add(bb.toString());
            }
            b.append(cf.compile(node.data.getTarget(), children.toArray(new String[children.size()])));
        } else {
            if(platform.getResolver() == null){
                b.append(node.data.val());
            } else {
                b.append(platform.getResolver().outputConstant(node.data));
            }
        }
    }
}
