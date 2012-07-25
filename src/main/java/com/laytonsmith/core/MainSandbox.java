/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.WebUtility;
import com.laytonsmith.PureUtilities.WebUtility.HTTPResponse;
import com.laytonsmith.persistance.DataSource;
import com.laytonsmith.persistance.DataSourceFactory;
import com.laytonsmith.persistance.YMLDataSource;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * This class is for testing concepts
 * @author Layton
 */
public class MainSandbox {
    public static void main(String[] argv) throws Exception {
        Main.main(new String[]{"--version"});
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
