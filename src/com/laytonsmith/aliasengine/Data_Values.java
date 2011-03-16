/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine;

import java.util.List;
import org.bukkit.Material;

/**
 *
 * @author layton
 */
public class Data_Values {
    public static String val(String val) throws CancelCommandException{
        try{
            return Integer.toString(Integer.parseInt(val));
        } catch(NumberFormatException e){
            List<Material> l = MaterialUtils.getList(val);
            if(l.size() > 1){
              StringBuilder b = new StringBuilder();
              for(Material m : l){
                    b.append(m.name()).append(" ");
              }
              throw new CancelCommandException("Multiple matches. Did you mean: " + b.toString());
            } else if(l.size() == 1){
                return Integer.toString(MaterialUtils.getMaterial(val).getId());
            }
            else{
                //Couldn't find the data value
                throw new CancelCommandException("Could not find data value for \"" + val + "\"");
            }
        }
    }
}
