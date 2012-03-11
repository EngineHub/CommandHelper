/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

import com.laytonsmith.commandhelper.CommandHelperPlugin;

/**
 * This should be implemented once for each server type. It mostly wraps
 * static methods, but also provides methods for getting other server specific
 * things. You can get an instance of the current Convertor by looking for the
 * <code>@convert</code> tag. StaticLayer wraps all the functionality for you
 * however.
 * @author layton
 */
public interface Convertor {

    public MCLocation GetLocation(MCWorld w, double x, double y, double z, float yaw, float pitch);
    public Class GetServerEventMixin();

    public MCEnchantment[] GetEnchantmentValues();

    public MCEnchantment GetEnchantmentByName(String name);

    public MCServer GetServer();

    public MCItemStack GetItemStack(int type, int qty);

    public void Startup(CommandHelperPlugin chp);
    
    public int LookupItemId(String materialName);

    public String LookupMaterialName(int id);

    public MCItemStack GetItemStack(int type, byte data, int qty);
    
    /**
     * A future runnable is run on a server accessible thread at roughly the time specified in the future.
     * This is no guarantee however, as the particular server implementation may make this hard to do. The
     * value returned is 
     * @param r
     * @return 
     */
    public int SetFutureRunnable(long ms, Runnable r);

    public void ClearAllRunnables();

    public void ClearFutureRunnable(int id);

    public int SetFutureRepeater(long ms, Runnable r);
    
}
