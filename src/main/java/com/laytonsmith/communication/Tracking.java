package com.laytonsmith.communication;

import com.laytonsmith.annotations.shutdown;
import com.laytonsmith.annotations.startup;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.constructs.Target;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.laytonsmith.communication.Exceptions.InvalidNameException;
import com.laytonsmith.communication.Subscriber.MessageCallback;
import com.laytonsmith.abstraction.events.standalone.Communication;
import com.laytonsmith.core.environments.Environment;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;

/**
 *
 * @author import
 */
public class Tracking {
    
    @startup
    public static void startup() {
        context = ZMQ.context(1);
    }
    
    @shutdown
    public static void shutdown() {
        Set<String> keys = publishers.keySet();
        for(String key : keys) { 
            Publisher pub = publishers.get(key);
            pub.stop();
        }
        
        publishers.clear();
        
        keys = subscribers.keySet();
        for (String key : keys) {
            Subscriber sub = subscribers.get(key);
            sub.stop();
        }
        
        subscribers.clear();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Tracking.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        context.term();
        context = null;
    }
    
    private static Map<String, Publisher> publishers = new HashMap<String, Publisher>();
    private static Map<String, Subscriber> subscribers = new HashMap<String, Subscriber>();
    public static Context context;
    
    public static boolean hasPublisher(String name) throws InvalidNameException {
        if (!Util.isValidName(name)) {
            throw new InvalidNameException(name);
        }
        
        return publishers.containsKey(name);
    }
    
    public static boolean hasSubscriber(String name) throws InvalidNameException {
        if (!Util.isValidName(name)) {
            throw new InvalidNameException(name);
        }
        
        return subscribers.containsKey(name);
    }
    
    public static Publisher getPub(String name) throws InvalidNameException {
        if (!Util.isValidName(name)) {
            throw new InvalidNameException(name);
        }
        
        return publishers.get(name);
    }
    
    public static Subscriber getSub(String name) throws InvalidNameException {
        if (!Util.isValidName(name)) {
            throw new InvalidNameException(name);
        }
        
        return subscribers.get(name);
    }
    
    public static NodePoint getOrCreate(int type, String name, final Environment env) 
			throws InvalidNameException {
        NodePoint retn = null;
        
        if (!Util.isValidName(name)) {
            throw new InvalidNameException(name);
        }
        
        if (type == ZMQ.PUB) {
            retn = getPub(name);
            
            if (retn == null) {
                Publisher pub = new Publisher(name);
                pub.init(context);
                pub.start();
                
                publishers.put(name, pub);
                return pub;
            }
        } else if (type == ZMQ.SUB) {
            retn = getSub(name);
            
            if (retn == null) {
                Subscriber sub = new Subscriber(name);
                sub.init(context);
                sub.start();
                
                subscribers.put(name, sub);

                sub.addCallback(new MessageCallback() {
                    public void process(String subscriber, String channel, 
							String publisher, String message) {
                        Communication.fireReceived(subscriber, channel, 
								publisher, message, env);
                    }
                });
                
                return sub;
            }
        }
        
        CHLog.GetLogger().i(CHLog.Tags.RUNTIME, name + " was not created!", Target.UNKNOWN);
        
        return retn;
    }

    public static boolean close(String name, int type) throws InvalidNameException {
        NodePoint node = null;
        
        if (!Util.isValidName(name)) {
            throw new InvalidNameException(name);
        }
        
        if (type == ZMQ.PUB) {
            node = publishers.remove(name);
        } else if (type == ZMQ.SUB) {
            node = subscribers.remove(name);
        }
        
        if (node != null) {
            node.stop();
            
            return true;
        }
        
        return false;
    }
}
