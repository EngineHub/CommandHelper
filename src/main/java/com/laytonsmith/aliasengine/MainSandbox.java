/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine;

import com.laytonsmith.aliasengine.Constructs.CNull;
import com.laytonsmith.aliasengine.functions.ClassDiscovery;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
//import net.minecraft.server.Packet;

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
    
    public static void printPackets(){
//        Class [] classes = ClassDiscovery.DiscoverClasses(net.minecraft.server.Packet.class, null, null);
//        for(Class c : classes){
//            //The superclass can be null if it's an interface
//            if(c != null && c.getSuperclass() != null && c.getSuperclass().equals(net.minecraft.server.Packet.class)){
//                try {
//                    System.out.println("Packet subclass: " + c.getSimpleName());
//                    Constructor [] constructors = c.getConstructors();
//                    for(Constructor constructor : constructors){
//                        if(constructor.getParameterTypes().length != 0){
//                            //Candidate for use
//                            System.out.println("\tConstructor: " + Arrays.toString(constructor.getParameterTypes()));
//                        } //Else it's a no-arg constructor, which happens from time to time
//                    }
//                } catch (Exception ex) {
//                    Logger.getLogger(MainSandbox.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }
    }
}
