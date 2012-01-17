/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core;

import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.PureUtilities.ClassDiscovery;
import com.laytonsmith.core.functions.Regex;
import com.sun.java.swing.plaf.windows.WindowsTreeUI.CollapsedIcon;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
//import net.minecraft.server.Packet;

/**
 * This class is for testing concepts
 * @author Layton
 */
public class MainSandbox {
    public static void main(String[] args) throws Exception{
        
    }
    
    

    
    public static class hashClass{
        int x;
        int y;
        int z;
        String world;

        private static long top = (long)(Math.pow(2, 63) - Math.pow(2, 56));
        private static long middle = (long)(Math.pow(2, 55) - Math.pow(2, 28));
        private static long bottom = (long)Math.pow(2, 27) - 1;
        public static long GetHashCode(int x, int y, int z) {
            System.out.println(top);
            System.out.println(middle);
            System.out.println(bottom);
            long hash = 0;
            hash |= ((long)y << 55) & top;
            hash |= ((long)x << 27) & middle;
            hash |= (long)z & bottom;            
            return hash;
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
