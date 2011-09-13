/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine;

import com.laytonsmith.aliasengine.Constructs.CNull;
import com.laytonsmith.aliasengine.Constructs.Construct;

/**
 * This class is for testing concepts
 * @author Layton
 */
public class MainSandbox {
    public static void main(String[] args) throws Exception{
        CNull c = new CNull(0, null);
        CNull d = c.clone();
        System.out.println(d.getLineNum());
    }
}
