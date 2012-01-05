/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.events;

import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.exceptions.EventException;
import com.laytonsmith.aliasengine.exceptions.PrefilterNonMatchException;
import java.util.Map;

/**
 *
 * @author layton
 */
public interface AbstractEventHandler {
    public boolean matches(Map<String, Construct> prefilter, Object e) throws PrefilterNonMatchException;
    public Map<String, Construct> evaluate(Object e, AbstractEventMixin mixin) throws EventException;
    public Object convert(CArray manual);
    public void modifyEvent(String key, Construct value, Object event);
}
