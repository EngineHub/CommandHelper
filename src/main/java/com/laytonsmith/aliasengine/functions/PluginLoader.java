package com.laytonsmith.aliasengine.functions;
import java.net.*;
import java.io.*;

/**
 * This class handles loading the modules classes. Most of this code was copy-pasted,
 * so I can't comment too much on it, because I'm still fairly new to what's happening here,
 * however, I will try to offer some insight. The jar file in which the classes are located
 * must first be added to the classpath with <code>loadJars</code>. The classes are then
 * loaded into the classloader, and then when they need to be instantiated, the URLCLassLoader
 * that is static in this class is used to instatiate them. (Over in frame.java)
 * @author Layton
 */
class PluginLoader {
//    private static URLClassLoader loader;
//    private static String baseClassPath;
//
//    public static ClassLoader getLoader(){
//        return loader;
//    }
//    private static String parseFile(String dir) {
//        String tempJars = "";
//        File file = new File(dir);
//        File[] tempFiles = file.listFiles();
//        String tempString = "";
//        for (int a = 0; a < tempFiles.length; a++) {
//            tempString = tempFiles[a].toString();
//            if (tempString.length() > 4) {
//                if (tempString.substring(tempString.length() - 4, tempString.length()).equals(".jar")) {
//                    tempJars = tempJars + tempString + ";";
//                    try {
//                        if(loader != null){
//                            URL [] ans = new URL[loader.getURLs().length + 1];
//                            System.arraycopy(loader.getURLs(), 0, ans, 0, loader.getURLs().length);
//                            ans[ans.length - 1] = tempFiles[a].toURI().toURL();
//                            loader = new URLClassLoader(ans);
//                        }
//                        else{
//                            loader = new URLClassLoader(new URL[]{tempFiles[a].toURI().toURL()});
//                        }
//                    } catch (MalformedURLException e) {
//                        e.printStackTrace();
//                    } catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//        if(tempJars.length() > 0){
//            if (tempJars.substring(tempJars.length() - 1, tempJars.length()).equals(";")) {
//                tempJars = tempJars.substring(0, tempJars.length() - 1);
//            }
//        }
//        else{
//            return null;
//        }
//        return tempJars;
//    }
//
//    public static void loadJars(String dir) {
//        String jars = parseFile(dir);
//        if(jars == null){
//            return;
//        }
//        String classPath = baseClassPath + System.getProperty("path.separator") + jars;
//
//        System.setProperty("java.class.path", classPath);
//    }
}
