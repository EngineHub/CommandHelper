package com.laytonsmith.communication;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;

public class Subscriber extends NodePoint implements Runnable {
    public static interface MessageCallback {
        public void process(String subscriber, String channel, String publisher, String message);
    }
    
    private Set<MessageCallback> callbacks;
    private String name;

    public Subscriber(String name) {
        callbacks = Collections.synchronizedSet(new HashSet<MessageCallback>());
        owningThread = new Thread(this, "subscriber-" + name);
        this.name = name;
    }
    
    public void init(Context context) {
        super.init(context, ZMQ.SUB);
    }
    
    public void addCallback(MessageCallback toRun) {
        callbacks.add(toRun);
    }
    
    public void remCallback(MessageCallback toRun) {
        callbacks.remove(toRun);
    }
    
    private String sanitizeChannel(String channel) {
        String chan = channel.trim();
        
        if (!channel.equals("*")) {
            chan += '\0';
        } else {
            chan = "";
        }
        
        return chan;
    }
    
    public void subscribe(String channel) throws Exceptions.InvalidChannelException {
        if (!Util.isValidChannel(channel)) {
            throw new Exceptions.InvalidChannelException(channel);
        }
        
        String chan = sanitizeChannel(channel);
        socket.subscribe(chan.getBytes());
    }
    
    public void unsubscribe(String channel) throws Exceptions.InvalidChannelException {
        if (!Util.isValidChannel(channel)) {
            throw new Exceptions.InvalidChannelException(channel);
        }
        
        String chan = sanitizeChannel(channel);
        socket.unsubscribe(chan.getBytes());
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted() && alive) {
            String recv;
            
            try {
                // UGLY HACK WARNING!
                // Unrecoverable exception from ZMQ, if we let recvStr
                // block and the thread gets terminated.
                String raw = socket.recvStr(ZMQ.DONTWAIT); 
                
                if (raw == null) {
                    Thread.sleep(5);
                    continue;
                } else {
                    recv = raw.trim();
                }
            } catch (Exception e) {
                break;
            }
            
            if (recv.isEmpty()) {
                continue;
            }
            
            String[] split = recv.split("\0", 3);
            
            if (split.length != 3) {
                Logger.getLogger(Subscriber.class.getName()).log(Level.WARNING, 
                            "Malformed packet received. Skipping.");
                continue;
            }
            
            String channel = split[0];
            String identifier = split[1];
            String message = split[2];
            
            for (MessageCallback toRun : callbacks) {
                try {
                    // Let the callback figure out threading issues.
                    toRun.process(this.name, channel, identifier, message);
                } catch (Exception ex) {
                    Logger.getLogger(Subscriber.class.getName()).log(Level.SEVERE, 
                            "Error processing callback", ex);
                }
            }
        }
        
        cleanup();
    }
        
    public static void main (String[] args) throws InterruptedException, Exceptions.InvalidChannelException {
        Context context = ZMQ.context(1);
        
        Subscriber sub = new Subscriber("testing");
        sub.init(context);
        sub.subscribe("*");
        sub.connect("tcp://localhost:5556");
        
        sub.addCallback(new MessageCallback() {
            public void process(String subscriber, String channel, String publisher, String message) {
                String msg = "Received %s from %s on channel %s";
                System.out.println(String.format(msg, message, publisher, channel));
            }
        });
        
        sub.start();
        
        Thread.sleep(5000);
        
        System.out.println("Stopping");
        sub.stop();
        
        System.out.println("Terminating");
        context.term();
    }
}