package com.laytonsmith.core.functions;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.communication.Exceptions.InvalidChannelException;
import com.laytonsmith.communication.Exceptions.InvalidNameException;
import com.laytonsmith.communication.NodePoint;
import com.laytonsmith.communication.Publisher;
import com.laytonsmith.communication.Subscriber;
import com.laytonsmith.communication.Tracking;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

/**
 *
 * @author import
 */
public class SocketCommunication {
	
	public static String docs() {
        return "This class contains functions that are related to socket programming.";
    }
	
    public abstract static class CommFunc extends AbstractFunction {
        public Exceptions.ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.FormatException, 
                ExceptionType.NotFoundException, ExceptionType.IOException};
        }
        
        public boolean isRestricted() {
            return true;
        }

        public Boolean runAsync() {
            return null; // Don't care.
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
    }
    
    @api(environments = {CommandHelperEnvironment.class})
    public static class comm_listen extends CommFunc {
        public Construct exec(Target t, Environment environment, 
                Construct... args) throws ConfigRuntimeException {
            String name = args[0].val();
            String endpoint = args[1].val();
            int type = ZMQ.PUB;
            
            if (args.length == 3) {
                String stype = args[2].val().toUpperCase();

                if (!"PUB".equals(stype) && !"SUB".equals(stype)) {
                    throw new ConfigRuntimeException("You must specify PUB or SUB"
                            + " for comm_listen's third argument!", Exceptions.ExceptionType.NotFoundException, t);
                }

                if ("SUB".equals(stype)) {
                    type = ZMQ.SUB;
                }
            }
            
            NodePoint node;
            
            try {
                node = Tracking.getOrCreate(type, name, environment);
            } catch (InvalidNameException ex) {
                throw new ConfigRuntimeException("Invalid name " + name + 
                        " given to comm_listen!", Exceptions.ExceptionType.FormatException, t);
            }
            
            try {
                node.listen(endpoint);
            } catch (ZMQException e) {
                throw new ConfigRuntimeException("Exception while listening: " + e.getMessage(), 
                        Exceptions.ExceptionType.IOException, t);
            }
            
            return new CVoid(t);
        }

        public String getName() {
            return "comm_listen";
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

        public String docs() {
            return "void {name, endpoint[, type]} Listen. Automatically creates the"
                    + " the socket if it doesn't exist already."
                    + " Type can be PUB or SUB, but defaults to PUB.";
        }
    }
    
    @api(environments = {CommandHelperEnvironment.class})
    public static class comm_connect extends CommFunc {
        public Construct exec(Target t, Environment environment, 
                Construct... args) throws ConfigRuntimeException {
            String name = args[0].val();
            String endpoint = args[1].val();
            int type = ZMQ.SUB;
            
            if (args.length == 3) {
                String stype = args[2].val().toUpperCase();

                if (!"PUB".equals(stype) && !"SUB".equals(stype)) {
                    throw new ConfigRuntimeException("You must specify PUB or SUB"
                            + " for comm_connect's third argument!", Exceptions.ExceptionType.NotFoundException, t);
                }

                if ("PUB".equals(stype)) {
                    type = ZMQ.PUB;
                }
            }
            
            NodePoint node;
            
            try {
                node = Tracking.getOrCreate(type, name, environment);
            } catch (InvalidNameException ex) {
                throw new ConfigRuntimeException("Invalid name " + name + 
                        " given to comm_connect!", Exceptions.ExceptionType.FormatException, t);
            }
            
            try {
                node.connect(endpoint);
            } catch (ZMQException e) {
                throw new ConfigRuntimeException("Exception while connecting: " + e.getMessage(), 
                        Exceptions.ExceptionType.IOException, t);
            }
            
            return new CVoid(t);
        }

        public String getName() {
            return "comm_connect";
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

        public String docs() {
            return "void {name, endpoint[, type]} Connect. Automatically creates the"
                    + " the socket if it doesn't exist already. Type can be PUB "
                    + "or SUB, but defaults to SUB.";
        }
    }
    
    @api(environments = {CommandHelperEnvironment.class})
    public static class comm_disconnect extends CommFunc {
        public Construct exec(Target t, Environment environment, 
                Construct... args) throws ConfigRuntimeException {
            String name = args[0].val();
            String endpoint = args[1].val();
            int type = ZMQ.SUB;
            
            if (args.length == 3) {
                String stype = args[2].val().toUpperCase();

                if (!"PUB".equals(stype) && !"SUB".equals(stype)) {
                    throw new ConfigRuntimeException("You must specify PUB or SUB"
                            + " for comm_disconnect's third argument!", Exceptions.ExceptionType.NotFoundException, t);
                }

                if ("PUB".equals(stype)) {
                    type = ZMQ.PUB;
                }
            }
            
            NodePoint node;
            
            try {
                if (type == ZMQ.PUB) {
                    node = Tracking.getPub(name);
                } else {
                    node = Tracking.getSub(name);
                }
            } catch (InvalidNameException ex) {
                throw new ConfigRuntimeException("Invalid name " + name + 
                        " given to comm_disconnect!", Exceptions.ExceptionType.FormatException, t);
            }
            
            if (node == null) {
                throw new ConfigRuntimeException("Unknown " + name + " "
                        + " given to comm_disconnect!", Exceptions.ExceptionType.NotFoundException, t);
            }
            
            try {
                node.disconnect(endpoint);
            } catch (ZMQException e) {
                throw new ConfigRuntimeException("Exception while disconnecting: " + e.getMessage(), 
                        Exceptions.ExceptionType.IOException, t);
            }
            
            return new CVoid(t);
        }

        public String getName() {
            return "comm_disconnect";
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

        public String docs() {
            return "void {name, endpoint[, type]} Disconnect. Type can be PUB or"
                    + " SUB, but defaults to SUB.";
        }
    }
    
    @api(environments = {CommandHelperEnvironment.class})
    public static class comm_close extends CommFunc {
        public Construct exec(Target t, Environment environment, 
                Construct... args) throws ConfigRuntimeException {
            String name = args[0].val();
            int type = ZMQ.SUB;
            
            if (args.length == 2) {
                String stype = args[1].val().toUpperCase();

                if (!"PUB".equals(stype) && !"SUB".equals(stype)) {
                    throw new ConfigRuntimeException("You must specify PUB or SUB"
                            + " for comm_close's second argument!", Exceptions.ExceptionType.NotFoundException, t);
                }

                if ("PUB".equals(stype)) {
                    type = ZMQ.PUB;
                }
            }
            
            boolean found;
            
            try {
                found = Tracking.close(name, type);
            } catch (InvalidNameException ex) {
                throw new ConfigRuntimeException("Invalid name " + name + 
                        " given to comm_close!", Exceptions.ExceptionType.FormatException, t);
            } catch (ZMQException e) {
                throw new ConfigRuntimeException("Exception while closing: " + e.getMessage(), 
                        Exceptions.ExceptionType.IOException, t);
            }
            
            if (!found) {
                throw new ConfigRuntimeException("Unknown " + name + " "
                        + " given to comm_close!", Exceptions.ExceptionType.NotFoundException, t);
            }
            
            return new CVoid(t);
        }

        public String getName() {
            return "comm_close";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "void {name, type} Close. Type can be PUB or SUB."
                    + " This will disconnect any and all connections and binds"
                    + " related to this name for this type.";
        }
    }
    
    @api(environments = {CommandHelperEnvironment.class})
    public static class comm_subscribe extends CommFunc {
        public Construct exec(Target t, Environment environment, 
                Construct... args) throws ConfigRuntimeException {
            String name = args[0].val();
            String channel = args[1].val();

            NodePoint node;
            
            try {
                node = Tracking.getSub(name);
            } catch (InvalidNameException ex) {
                throw new ConfigRuntimeException("Invalid name " + name + 
                        " given to comm_subscribe!", Exceptions.ExceptionType.FormatException, t);
            }
            
            if (node == null) {
                throw new ConfigRuntimeException("Unknown SUB " + name + 
                        " given to comm_subscribe!", Exceptions.ExceptionType.NotFoundException, t);
            }
            
            try {
                ((Subscriber)node).subscribe(channel);
            } catch (InvalidChannelException ex) {
                throw new ConfigRuntimeException("Invalid channel " + channel + 
                        " given to comm_subscribe!", Exceptions.ExceptionType.FormatException, t);
            } catch (ZMQException e) {
                throw new ConfigRuntimeException("Exception while subscribing: " + e.getMessage(), 
                        Exceptions.ExceptionType.IOException, t);
            }
            
            return new CVoid(t);
        }

        public String getName() {
            return "comm_subscribe";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "void {name, channel} Subscribe SUB <name> to <channel>.";
        }
    }
    
    @api(environments = {CommandHelperEnvironment.class})
    public static class comm_unsubscribe extends CommFunc {
        public Construct exec(Target t, Environment environment, 
                Construct... args) throws ConfigRuntimeException {
            String name = args[0].val();
            String channel = args[1].val();

            NodePoint node;
            
            try {
                node = Tracking.getSub(name);
            } catch (com.laytonsmith.communication.Exceptions.InvalidNameException ex) {
                throw new ConfigRuntimeException("Invalid name " + name + 
                        " given to comm_unsubscribe!", Exceptions.ExceptionType.FormatException, t);
            }
            
            if (node == null) {
                throw new ConfigRuntimeException("Unknown SUB " + name + 
                        " given to comm_unsubscribe!", Exceptions.ExceptionType.NotFoundException, t);
            }
            
            try {
                ((Subscriber)node).unsubscribe(channel);
            } catch (InvalidChannelException ex) {
                throw new ConfigRuntimeException("Invalid channel " + channel + 
                        " given to comm_subscribe!", Exceptions.ExceptionType.FormatException, t);
            } catch (ZMQException e) {
                throw new ConfigRuntimeException("Exception while unsubscribing: " + e.getMessage(), 
                        Exceptions.ExceptionType.IOException, t);
            }
            
            return new CVoid(t);
        }

        public String getName() {
            return "comm_unsubscribe";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "void {name, channel} Unsubscribe SUB <name> from <channel>.";
        }
    }
    
    @api(environments = {CommandHelperEnvironment.class})
    public static class comm_publish extends CommFunc {
        public Construct exec(Target t, Environment environment, 
                Construct... args) throws ConfigRuntimeException {
            String name = args[0].val();
            String channel = args[1].val();
            String message = args[2].val();
            String origpub = name;
            
            if (args.length == 4) {
                origpub = args[3].val();
            }

            NodePoint node;
            
            try {
                node = Tracking.getPub(name);
            
                if (node == null) {
                    throw new ConfigRuntimeException("Unknown PUB " + name + 
                            " given to comm_publish!", Exceptions.ExceptionType.NotFoundException, t);
                }
                
                ((Publisher)node).publish(channel, message, origpub);
            } catch (InvalidChannelException ex) {
                throw new ConfigRuntimeException("Invalid channel " + channel + 
                        " given to comm_publish!", Exceptions.ExceptionType.FormatException, t);
            } catch (InvalidNameException ex) {
                throw new ConfigRuntimeException("Invalid name " + name + 
                        " given to comm_publish!", Exceptions.ExceptionType.FormatException, t);
            } catch (ZMQException e) {
                throw new ConfigRuntimeException("Exception while publishing: " + e.getMessage(), 
                        Exceptions.ExceptionType.IOException, t);
            }
            
            return new CVoid(t);
        }

        public String getName() {
            return "comm_publish";
        }

        public Integer[] numArgs() {
            return new Integer[]{3, 4};
        }

        public String docs() {
            return "void {name, channel, message[, originalid]} Publish <message>"
                    + " to <channel> of PUB with name <name>. if originalid is"
                    + " given, that publisher's name will be used instead of"
                    + " this one.";
        }
    }
}
