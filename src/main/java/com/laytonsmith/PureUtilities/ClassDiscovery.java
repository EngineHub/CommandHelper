/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.PureUtilities;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ClassDiscovery {

    public static Class[] GetClassesWithinPackageHierarchy(){
        return GetClassesWithinPackageHierarchy(null);
    }
    /**
     * Gets all the classes in the specified location. The url can point to a jar, or a
     * file system location. If null, the current binary is used.
     * @param url
     * @return 
     */
    public static Class[] GetClassesWithinPackageHierarchy(String url){
        if(url == null){
            url = ClassDiscovery.class.getResource(ClassDiscovery.class.getSimpleName() + ".class").toString();
        }
        List<String> classNameList = new ArrayList<String>();
        if(url.startsWith("file:")){
            //We are running from the file system
            //First, get the "root" of the class structure. We assume it's
            //the root of this class
            String fileName = Pattern.quote(ClassDiscovery.class.getCanonicalName().replace(".", File.separator));
            fileName = fileName.replaceAll("\\\\Q", "").replaceAll("\\\\E", "") + ".class";
            String root = url.replaceAll("file:", "").replaceAll(fileName, "");
            System.out.println(root);
            //Ok, now we have the "root" of the known class structure. Let's recursively
            //go through everything and pull out the files
            List<File> fileList = new ArrayList<File>();
            descend(new File(root), fileList);
            //Now, we have all the class files in the package. But, it's the absolute path
            //to all of them. We have to first remove the "front" part
            for(File f : fileList){
                classNameList.add(f.getAbsolutePath().replaceFirst(Pattern.quote(root), ""));
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
        return files.toArray(new Class[files.size()]);
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
                return;
            }
        } else {
            for(File child : start.listFiles()){
                descend(child, fileList);
            }
        }
    }
//    static private String GetClassName_afterPackageAsPath(
//            final String pFileName,
//            final String pPkgAsPath) {
//        final String CName = pFileName.substring(0, pFileName.length() - 6).replace('/', '.').replace('\\', '.');
//        final String CName_AfterPackageAsPath = CName.substring(pPkgAsPath.length());
//        return CName_AfterPackageAsPath;
//    }
//
//    static private String GetClassName_ofPackageAsPath(
//            final String pFileName,
//            final String pPkgAsPath) {
//
//        final boolean aIsClass = pFileName.endsWith(".class");
//        if (!aIsClass) {
//            return null;
//        }
//
//        final boolean aIsBelongToPackage = pFileName.startsWith(pPkgAsPath);
//        if (!aIsBelongToPackage) {
//            return null;
//        }
//
//        final String aClassName = ClassDiscovery.GetClassName_afterPackageAsPath(pFileName, pPkgAsPath);
//        return aClassName;
//    }
//
//    static private File GetPackageFile(
//            final String pPkgName,
//            final File pPkgPath) {
//
//        final String aPkgFileName = pPkgPath.getAbsoluteFile().toString() + '/' + pPkgName.replace('.', '/');
//        final File aPkgFile = new File(aPkgFileName);
//
//        final boolean aIsExist = aPkgFile.exists();
//        final boolean aIsDirectory = aPkgFile.isDirectory();
//        final boolean aIsExist_asDirectory = aIsExist && aIsDirectory;
//        if (!aIsExist_asDirectory) {
//            return null;
//        }
//
//        return aPkgFile;
//    }
//
//    static private boolean Check_isJarFile(final File pFile) {
//        final boolean aIsJarFile = pFile.toString().endsWith(".jar");
//        return aIsJarFile;
//    }
//
//    static private ArrayList<String> DiscoverClassNames_fromJarFile(final PkgInfo pPkgInfo) {
//
//        final ArrayList<String> aClassNames = new ArrayList<String>();
//        try {
//            final JarFile JF = new JarFile(pPkgInfo.PkgPath);
//            final Enumeration<JarEntry> JEs = JF.entries();
//
//            while (JEs.hasMoreElements()) {
//                final JarEntry aJE = JEs.nextElement();
//                final String aJEName = aJE.getName();
//
//                final String aSimpleName = GetClassName_ofPackageAsPath(aJEName, pPkgInfo.PkgAsPath);
//                if (aSimpleName == null) {
//                    continue;
//                }
//
//                final String aClassName = pPkgInfo.PkgName + '.' + aSimpleName;
//                aClassNames.add(aClassName);
//            }
//
//            JF.close();
//        } catch (IOException e) {
//        }
//
//        return aClassNames;
//    }
//
//    static private void DiscoverClassNames_fromDirectory(
//            final String pAbsolutePackagePath,
//            final String pPackageName,
//            final File pPackageFolder,
//            final ArrayList<String> pClassNames) {
//        final File[] aFiles = pPackageFolder.listFiles();
//        for (File aFile : aFiles) {
//            if (aFile.isDirectory()) {
//                DiscoverClassNames_fromDirectory(pAbsolutePackagePath, pPackageName, aFile, pClassNames);
//                continue;
//            }
//
//            final String aFileName = aFile.getAbsolutePath().substring(pAbsolutePackagePath.length() + 1);
//            final boolean aIsClassFile = aFileName.endsWith(".class");
//            if (!aIsClassFile) {
//                continue;
//            }
//
//            final String aSimpleName = aFileName.substring(0, aFileName.length() - 6).replace('/', '.').replace('\\', '.');
//            final String aClassName = pPackageName + '.' + aSimpleName;
//            pClassNames.add(aClassName);
//        }
//    }
//
//    static private ArrayList<String> DiscoverClassNames_fromDirectory(PkgInfo pPkgInfo) {
//
//        final ArrayList<String> aClassNames = new ArrayList<String>();
//        final File aPkgFile = ClassDiscovery.GetPackageFile(pPkgInfo.PkgName, pPkgInfo.PkgPath);
//        if (aPkgFile == null) {
//            return aClassNames;
//        }
//
//        DiscoverClassNames_fromDirectory(aPkgFile.getAbsolutePath(), pPkgInfo.PkgName, aPkgFile, aClassNames);
//        return aClassNames;
//    }
//
//    static public class PkgInfo {
//
//        PkgInfo(
//                final File pPkgPath,
//                final String pPkgName,
//                final String pPkgAsPath) {
//
//            this.PkgPath = pPkgPath;
//            this.PkgName = pPkgName;
//            this.PkgAsPath = pPkgAsPath;
//        }
//        final File PkgPath;
//        final String PkgName;
//        final String PkgAsPath;
//    }
//
//    static public PkgInfo GetPackageInfoOf(Class<?> pClass) {
//        File aPkgPath = null;
//        String aPkgName = null;
//        String aPkgAsPath = null;
//
//        try {
//            aPkgPath = new File(pClass.getProtectionDomain().getCodeSource().getLocation().toURI());
//            aPkgName = pClass.getPackage().getName();
//            aPkgAsPath = aPkgName.replace('.', '/') + '/';
//        } catch (Throwable e) {
//        }
//
//        if (aPkgPath == null) {
//            return null;
//        }
//
//        final PkgInfo aPkgInfo = new PkgInfo(aPkgPath, aPkgName, aPkgAsPath);
//        return aPkgInfo;
//    }
//
//    static public ArrayList<String> DiscoverClassNames_inPackage(final PkgInfo pPkgInfo) {
//
//        if (pPkgInfo == null) {
//            return null;
//        }
//
//        ArrayList<String> aClassNames = new ArrayList<String>();
//        if (pPkgInfo.PkgPath.isDirectory()) {
//
//            aClassNames = ClassDiscovery.DiscoverClassNames_fromDirectory(pPkgInfo);
//
//        } else if (pPkgInfo.PkgPath.isFile()) {
//            boolean aIsJarFile = ClassDiscovery.Check_isJarFile(pPkgInfo.PkgPath);
//            if (!aIsJarFile) {
//                return null;
//            }
//
//            aClassNames = ClassDiscovery.DiscoverClassNames_fromJarFile(pPkgInfo);
//        }
//
//        return aClassNames;
//    }
//
//    /**
//     * Returns an array of class in the same package as the the SeedClass
//     * 
//     * @param pFilterName     - Regular expression to match the desired classes' name (nullif no filtering needed)
//     * @param pFilterClass   - The super class of the desired classes (null if no filtering needed)
//     * 
//     * @return                - The array of matched classes, null if there is a problem.
//     * 
//     * @author The rest       - Nawaman  http://nawaman.net
//     * @author Package as Dir - Jon Peck http://jonpeck.com
//     *                              (adapted from http://www.javaworld.com/javaworld/javatips/jw-javatip113.html)
//     */
//    @SuppressWarnings("unchecked")
//    public static <T> Class<? extends T>[] DiscoverClasses(
//            final Class<?> pSeedClass,
//            final String pFilterName,
//            final Class<T> pFilterClass) {
//
//        final Pattern aClsNamePattern = (pFilterName == null) ? null : Pattern.compile(pFilterName);
//
//        PkgInfo aPkgInfo = null;
//
//        try {
//            aPkgInfo = GetPackageInfoOf(pSeedClass);
//        } catch (Throwable e) {
//        }
//
//        if (aPkgInfo == null) {
//            return null;
//        }
//
//        final ArrayList<String> aClassNames = DiscoverClassNames_inPackage(aPkgInfo);
//
//        if (aClassNames == null) {
//            return null;
//        }
//
//        if (aClassNames.size() == 0) {
//            return null;
//        }
//
//        final ArrayList<Class<?>> aClasses = new ArrayList<Class<?>>();
//        for (final String aClassName : aClassNames) {
//
//            if ((aClsNamePattern != null) && !aClsNamePattern.matcher(aClassName).matches()) {
//                continue;
//            }
//
//            // Get the class and filter it
//            Class<?> aClass = null;
//            try {
//                aClass = Class.forName(aClassName);
//            } catch (ClassNotFoundException e) {
//                continue;
//            } catch (NoClassDefFoundError e) {
//                continue;
//            }
//
//            if ((pFilterClass != null) && !pFilterClass.isAssignableFrom(aClass)) {
//                continue;
//            }
//
//            if (pFilterClass != null) {
//                if (!pFilterClass.isAssignableFrom(aClass)) {
//                    continue;
//                }
//
//                aClasses.add(aClass.asSubclass(pFilterClass));
//            } else {
//                aClasses.add(aClass);
//            }
//        }
//
//        Class<? extends T>[] aClassesArray = aClasses.toArray((Class<? extends T>[]) (new Class[aClasses.size()]));
//
//        return aClassesArray;
//    }
//
//    static public void main(String... pArgs) {
//        Class<?> aSeedClass = ClassDiscovery.class;
//        try {
//            aSeedClass = Class.forName(pArgs[0]);
//        } catch (Exception E) {
//        }
//
//        if (aSeedClass == null) {
//            aSeedClass = ClassDiscovery.class;
//        }
//
//        final Class<?>[] aClasses = DiscoverClasses(aSeedClass, null, null);
//
//        System.out.println("[");
//        if (aClasses != null) {
//            for (Class aClass : aClasses) {
//                System.out.println("\t" + aClass);
//            }
//        }
//        System.out.println("]");
//    }
}
