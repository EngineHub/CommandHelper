/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.GenericTreeNode;
import java.io.File;
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
    
    public static GenericTreeNode<Construct> get(File file){
        return cache.get(file);
    }
    
    public static void clearCache(){
        cache.clear();
    }
}
