/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.puls3.abstraction;

import com.laytonsmith.puls3.Puls3Plugin;

/**
 *
 * @author layton
 */
public interface Convertor {

    public MCLocation GetLocation(MCWorld w, double x, double y, double z, float yaw, float pitch);
    public Class GetServerEventMixin();

    public MCEnchantment[] GetEnchantmentValues();

    public MCEnchantment GetEnchantmentByName(String name);

    public MCServer GetServer();

    public MCItemStack GetItemStack(int type, int qty);

    public void Startup(Puls3Plugin chp);
    
    public int LookupItemId(String materialName);

    public String LookupMaterialName(int id);
    
}
