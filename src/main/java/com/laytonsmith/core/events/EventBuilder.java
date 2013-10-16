package com.laytonsmith.core.events;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.annotations.abstraction;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author layton
 */
public final class EventBuilder {  
    
    private EventBuilder(){}
    
    private static final Map<Class<BindableEvent>, Method> methods = new HashMap<Class<BindableEvent>, Method>();
    private static final Map<Class<BindableEvent>, Constructor<? extends BindableEvent>> constructors = new HashMap<Class<BindableEvent>, Constructor<? extends BindableEvent>>();
    private static final Map<Class<BindableEvent>, Class<BindableEvent>> eventImplementations = new HashMap<Class<BindableEvent>, Class<BindableEvent>>();
    static{
        //First, we need to pull all the event implementors
        for(Class c : ClassDiscovery.getDefaultInstance().loadClassesWithAnnotation(abstraction.class)){
            if(BindableEvent.class.isAssignableFrom(c)){
                abstraction abs = (abstraction)c.getAnnotation(abstraction.class);
                if(abs.type().equals(Implementation.GetServerType())){
                    Class cinterface = null;
                    for(Class implementor : c.getInterfaces()){
                        if(BindableEvent.class.isAssignableFrom(implementor)){
                            cinterface = implementor;
                            break;
                        }
                    }
                    eventImplementations.put(cinterface, c);
                    //Also, warm it up
                    warmup(cinterface);
                }
            }
        }              
    }
    
    /**
     * Finds the _instantiate method in an event, and caches it for later use.
     * @param clazz 
     */
    private static void warmup(Class<? extends BindableEvent> clazz){
        if(!methods.containsKey((Class<BindableEvent>)clazz)){
            Class implementor = eventImplementations.get((Class<BindableEvent>)clazz);
            Method method = null;
            for(Method m : implementor.getMethods()){
                if(m.getName().equals("_instantiate") && (m.getModifiers() & Modifier.STATIC) != 0){
                    method = m;
                    break;
                }
            }
            if(method == null){
                System.err.println("UNABLE TO CACHE A CONSTRUCTOR FOR " + clazz.getSimpleName()
                         + ". Manual triggering will be impossible, and errors will occur"
                        + " if an attempt is made. Did you forget to add"
                        + " public static <Event> _instantiate(...) to " + clazz.getSimpleName() + "?");
            }
            methods.put((Class<BindableEvent>)clazz, method);
        }
    }
    
    public static <T extends BindableEvent> T instantiate(Class<? extends BindableEvent> clazz, Object... params){        
        try{
            if(!methods.containsKey((Class<BindableEvent>)clazz)){
                warmup(clazz);
            }
            Object o = methods.get((Class<BindableEvent>)clazz).invoke(null, params);
            //Now, we have an instance of the underlying object, which the instance
            //of the event BindableEvent should know how to handle in a constructor.
            if(!constructors.containsKey((Class<BindableEvent>)clazz)){
                Class bindableEvent = eventImplementations.get((Class<BindableEvent>)clazz);
                Constructor constructor = null;
                for(Constructor c : bindableEvent.getConstructors()){
                    if(c.getParameterTypes().length == 1){
                        //looks promising
                        if(c.getParameterTypes()[0].equals(o.getClass())){
                            //This is it
                            constructor = c;
                            break;
                        }
                    }
                }
                if(constructor == null){
                    throw new ConfigRuntimeException("Cannot find an acceptable constructor that follows the format:"
                            + " public " + bindableEvent.getClass().getSimpleName() + "(" + o.getClass().getSimpleName() + " event)."
                            + " Please notify the plugin author of this error.", null, Target.UNKNOWN);
                }
                constructors.put((Class<BindableEvent>)clazz, constructor);
            }
            //Construct a new instance, then return it.
            Constructor constructor = constructors.get((Class<BindableEvent>)clazz);
            BindableEvent be = (BindableEvent) constructor.newInstance(o);            
            return (T)be;
        
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    
//    public static MCPlayerJoinEvent MCPlayerJoinEvent(MCPlayer player, String message){
//        return instantiate(MCPlayerJoinEvent.class, player, message);
//    }    

}
