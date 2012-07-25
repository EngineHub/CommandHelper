package com.laytonsmith.PureUtilities;

import java.util.List;

/**
 *
 * @author lsmith
 */
public class StringUtils {
    
    public static String Join(Object [] list, String glue){
        StringBuilder b = new StringBuilder();
        boolean first = true;
        for(Object o : list){
            if(!first){
                b.append(glue);
            }
            first = false;
            b.append(o);
        }
        return b.toString();
    }
    
    public static String Join(List list, String glue){
        StringBuilder b = new StringBuilder();
        boolean first = true;
        for(Object o : list){
            if(!first){
                b.append(glue);
            }
            first = false;
            b.append(o);
        }
        return b.toString();
    }
}
