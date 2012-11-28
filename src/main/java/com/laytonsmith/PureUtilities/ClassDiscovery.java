/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.PureUtilities;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ClassDiscovery {
    
    private ClassDiscovery(){}
    
    /**
     * Adds a jar or file path location to be scanned by the default call to GetClassesWithinPackageHierarchy.
     * This is useful if an external library wishes to be considered by the scanner.
     * @param url 
     */
    public static void InstallDiscoveryLocation(String url){
        additionalURLs.add(url);
    }
    
    /**
     * Clears the class cache.
     * Upon the first call to the rather expensive GetClassesWithinPackageHierarchy(String) method,
     * the returned classes are cached instead of being regenerated. This method is automatically
     * called if a new discovery location is installed, but if new classes are being generated
     * dynamically, this cache will become stale, and you should clear the cache for a particular url.
     */
    public static void InvalidateCache(String url){
        classCache.remove(url);
    }
    
    /**
     * Equivalent to InvalidateCache(null);
     */
    public static void InvalidateCache(){        
        InvalidateCache(null);
    }
    
    /**
     * There's no need to rescan the project every time GetClassesWithinPackageHierarchy is called,
     * unless we add a new discovery location, or code is being generated on the fly or something crazy
     * like that, so let's cache unless told otherwise.
     */
    private static Map<String, Class[]> classCache = new HashMap<String, Class[]>();
    
    private static Set<String> additionalURLs = new HashSet<String>();

    public static Class[] GetClassesWithinPackageHierarchy(){
        List<Class> classes = new ArrayList<Class>();
        classes.addAll(Arrays.asList(GetClassesWithinPackageHierarchy(null)));
        for(String url : additionalURLs){
            classes.addAll(Arrays.asList(GetClassesWithinPackageHierarchy(url)));
        }
        return classes.toArray(new Class[classes.size()]);
    }
    /**
     * Gets all the classes in the specified location. The url can point to a jar, or a
     * file system location. If null, the current binary is used.
     * @param url
     * @return 
     */
    public static Class[] GetClassesWithinPackageHierarchy(String url){
        if(classCache.containsKey(url)){
            return classCache.get(url);
        }
        String originalURL = url;
        if(url == null){
            url = ClassDiscovery.class.getResource(ClassDiscovery.class.getSimpleName() + ".class").toString();
        }
        List<String> classNameList = new ArrayList<String>();
        if(url.startsWith("file:")){
            //We are running from the file system
            //First, get the "root" of the class structure. We assume it's
            //the root of this class
            String fileName = Pattern.quote(ClassDiscovery.class.getCanonicalName().replace(".", "/"));
            fileName = fileName/*.replaceAll("\\\\Q", "").replaceAll("\\\\E", "")*/ + ".class";
            String root = url.replaceAll("file:" + (TermColors.SYSTEM==TermColors.SYS.WINDOWS?"/":""), "").replaceAll(fileName, "");
            //System.out.println(root);
            //Ok, now we have the "root" of the known class structure. Let's recursively
            //go through everything and pull out the files
            List<File> fileList = new ArrayList<File>();
            descend(new File(root), fileList);
            //Now, we have all the class files in the package. But, it's the absolute path
            //to all of them. We have to first remove the "front" part
            for(File f : fileList){
                classNameList.add(f.getAbsolutePath().replaceFirst(Pattern.quote(new File(root).getAbsolutePath() + File.separator), ""));
            }
            
        } else if(url.startsWith("jar:")) {
            //We are running from a jar
            CodeSource src = ClassDiscovery.class.getProtectionDomain().getCodeSource();
            if (src != null) {
              ZipInputStream zip = null;
                try {
                    URL jar = src.getLocation();
                    zip = new ZipInputStream(jar.openStream());
                    ZipEntry ze;
                    while((ze = zip.getNextEntry()) != null){
                        classNameList.add(ze.getName());
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ClassDiscovery.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        zip.close();
                    } catch (IOException ex) {
                        Logger.getLogger(ClassDiscovery.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

              
            } 
        }
        //Ok, now we need to go through the list, and throw out entries
        //that are anonymously named (they end in $\d.class) because they
        //are inaccessible anyways
        List<Class> files = new ArrayList<Class>();
        for(String s : classNameList){
            //Don't consider anonymous inner classes
            if(!s.matches(".*\\$(?:\\d)*\\.class") && s.endsWith(".class")){
                //Now, replace any \ with / and replace / with ., and remove the .class,
                //and forName it.
                String className = s.replaceAll("\\.class", "").replaceAll("\\\\", "/").replaceAll("[/]", ".");
                try {
                    //Don't initialize it, so we don't have to deal with ExceptionInInitializer errors
                    Class c = Class.forName(className, false, ClassDiscovery.class.getClassLoader());
                    files.add(c);
                } catch (ClassNotFoundException ex) {
                    //It can't be loaded? O.o Oh well.
                } catch (NoClassDefFoundError ex){
                    //Must have been an external library
                }
            }
        }
        //Put the results in the cache
        Class[] ret = files.toArray(new Class[files.size()]);
        classCache.put(originalURL, ret);
        return ret;
    }
    
    public static Class[] GetClassesWithAnnotation(Class annotation){
        List<Class> classes = new ArrayList<Class>();
        for(Class c : GetClassesWithinPackageHierarchy()){
            if(c.getAnnotation(annotation) != null){
                classes.add(c);
            }
        }
        return classes.toArray(new Class[classes.size()]);
    }
    
    private static void descend(File start, List<File> fileList){
        if(start.isFile()){
            if(start.getName().endsWith(".class")){       
                fileList.add(start);
            }
        } else {
            for(File child : start.listFiles()){
                descend(child, fileList);
            }
        }
    }
}
