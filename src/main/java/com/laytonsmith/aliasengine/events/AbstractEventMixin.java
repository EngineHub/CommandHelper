/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.events;

import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.exceptions.EventException;
import java.util.Map;

/**
 *
 * @author layton
 */
public interface AbstractEventMixin {
    public void cancel(Object e);
    public Map<String, Construct> evaluate_helper(Object e) throws EventException;
    public void manualTrigger(Object e);
}
