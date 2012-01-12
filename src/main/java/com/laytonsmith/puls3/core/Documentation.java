/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.puls3.core;

/**
 * Classes that implement this method know how to provide some documentation to the DocGen
 * class.
 * 
 * In general, classes that implement this should also tag themselves with the
 * <code>@docs</code> tag, so the ClassDiscovery method can more easily find them,
 * if the class intends on being parsed by DocGen.
 * @author layton
 */
public interface Documentation {
    /**
     * The name of this code element
     */
    public String getName();
    /**
     * Returns documentation in a format that is specified by the code type
     * @return 
     */
    public String docs();
    /**
     * Returns the version number of when this functionality was added. It should
     * follow the format 0.0.0
     * @return 
     */
    public String since();
}
