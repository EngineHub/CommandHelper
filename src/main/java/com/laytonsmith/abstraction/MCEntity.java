/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

import org.bukkit.event.entity.EntityDamageEvent;

/**
 *
 * @author layton
 */
public interface MCEntity extends AbstractionObject{
    public int getEntityId();

    public MCDamageCause getLastDamageCause();
    
    public void fireEntityDamageEvent(MCDamageCause dc);

}
