/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core;

import java.io.File;

/**
 * This class is for testing concepts
 * @author Layton
 */
public class MainSandbox {
    public static void main(String[] argv) {
        File f = new File("target/test/file.zip/blah/file.txt");
        System.out.println(f.getAbsolutePath());
        
    }
    
    
    
    public void print(){
        for(int i = 0; i < 500; i++){
            System.out.println(i);
        }
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
