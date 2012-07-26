

package com.laytonsmith.core;

import com.laytonsmith.persistance.DataSource;
import com.laytonsmith.persistance.DataSourceFactory;
import java.net.URI;
import java.util.Arrays;

/**
 * This class is for testing concepts
 * @author Layton
 */
public class MainSandbox {
    public static void main(String[] argv) throws Exception {
//        String[] uris = new String[]{"ser://test.ser", "ser:///test.ser", "ser:///this/is/a/path.txt", "ser://this/is/a/path.txt"};
//        for(String s : uris){
//            URI uri = new URI(s);
//            System.out.println("For the URI " + uri.toString() + ", the following are set:");
//            System.out.println("Scheme: " + uri.getScheme());
//            System.out.println("Scheme specific part: " + uri.getSchemeSpecificPart());
//            System.out.println("Authority: " + uri.getAuthority());
//            System.out.println("User info: " + uri.getUserInfo());
//            System.out.println("Host: " + uri.getHost());
//            System.out.println("Port: " + uri.getPort());
//            System.out.println("Path: " + uri.getPath());
//            System.out.println("Query: " + uri.getQuery());
//            System.out.println("Fragment: " + uri.getFragment());
//            System.out.println("\n\n***********************************\n\n");
//        }
//        System.out.println();
        String [] srcs = new String[]{"yml://test.yml", "ini://test.ini", "ser://test.ser"};
        for(String src : srcs){
            System.out.println("For source: " + src);
            DataSource ds = DataSourceFactory.GetDataSource(new URI(src));
            ds.set(new String[]{"a", "b"}, "value1");
            ds.set(new String[]{"a", "b", "c1"}, "value2");
            ds.set(new String[]{"a", "b", "c2"}, "value3");
            for(String[] array : ds.keySet()){
                System.out.println(Arrays.toString(array));
            }
        }
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
