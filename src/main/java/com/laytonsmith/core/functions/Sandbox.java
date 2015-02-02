package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.commandhelper.BukkitDirtyRegisteredListener;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.events.BoundEvent;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import org.bukkit.event.Cancellable;

/**
 *
 */
public class Sandbox {

    public static String docs() {
        return "This class is for functions that are experimental. They don't actually get added"
                + " to the documentation, and are subject to removal at any point in time, nor are they"
                + " likely to have good documentation.";
    }

    //This broke as of 1.1
//    @api
//    public static class plugin_cmd extends AbstractFunction {
//
//        public String getName() {
//            return "plugin_cmd";
//        }
//
//        public Integer[] numArgs() {
//            return new Integer[]{2};
//        }
//
//        public String docs() {
//            return "void {plugin, cmd} ";
//        }
//
//        public ExceptionType[] thrown() {
//            return null;
//        }
//
//        public boolean isRestricted() {
//            return true;
//        }
//
//        //
//        public boolean preResolveVariables() {
//            return true;
//        }
//
//        public CHVersion since() {
//            return "0.0.0";
//        }
//
//        public Boolean runAsync() {
//            return false;
//        }
//
//        public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
//            Object o = AliasCore.parent.getServer().getPluginManager();
//            if (o instanceof SimplePluginManager) {
//                SimplePluginManager spm = (SimplePluginManager) o;
//                try {
//                    Method m = spm.getClass().getDeclaredMethod("getEventListeners", Event.Type.class);
//                    m.setAccessible(true);
//                    SortedSet<RegisteredListener> sl = (SortedSet<RegisteredListener>) m.invoke(spm, Event.Type.SERVER_COMMAND);
//                    for(RegisteredListener l : sl){
//                        if (l.getPlugin().getDescription().getName().equalsIgnoreCase(args[0].val())) {
//                            if(env.GetCommandSender() instanceof ConsoleCommandSender){
//                                l.callEvent(new ServerCommandEvent((ConsoleCommandSender)env.GetCommandSender(), args[1].val()));
//                            }
//                        }
//                    }
//                    SortedSet<RegisteredListener> ss = (SortedSet<RegisteredListener>) m.invoke(spm, Event.Type.PLAYER_COMMAND_PREPROCESS);
//
//                    for (RegisteredListener l : ss) {
//                        if (l.getPlugin().getDescription().getName().equalsIgnoreCase(args[0].val())) {
//                            if(env.GetCommandSender() instanceof MCPlayer){
//                                l.callEvent(new PlayerCommandPreprocessEvent(((BukkitMCPlayer)env.GetPlayer())._Player(), args[1].val()));
//                            }
//                            PluginCommand.class.getDeclaredMethods();
//                            Constructor c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
//                            c.setAccessible(true);
//                            List<String> argList = Arrays.asList(args[1].val().split(" "));
//                            Command com = (Command) c.newInstance(argList.get(0).substring(1), l.getPlugin());
//                            l.getPlugin().onCommand(((BukkitMCCommandSender)env.GetCommandSender())._CommandSender(), com, argList.get(0).substring(1), argList.subList(1, argList.size()).toArray(new String[]{}));
//                        }
//                    }
//                } catch (InstantiationException ex) {
//                    Logger.getLogger(Sandbox.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (IllegalAccessException ex) {
//                    Logger.getLogger(Sandbox.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (IllegalArgumentException ex) {
//                    Logger.getLogger(Sandbox.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (InvocationTargetException ex) {
//                    Logger.getLogger(Sandbox.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (NoSuchMethodException ex) {
//                    Logger.getLogger(Sandbox.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (SecurityException ex) {
//                    Logger.getLogger(Sandbox.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//
//            return CVoid.VOID;
//        }
//    }


    @api(environments={CommandHelperEnvironment.class})
    public static class super_cancel extends AbstractFunction {

		@Override
        public String getName() {
            return "super_cancel";
        }

		@Override
        public Integer[] numArgs() {
            return new Integer[]{0};
        }

		@Override
        public String docs() {
            return "void {} \"Super Cancels\" an event. This only will work if play-dirty is set to true. If an event is"
                    + " super cancelled, not only is the cancelled flag set to true, the event stops propagating down, so"
                    + " no other plugins (as in other server plugins, not just CH scripts) will receive the event at all "
                    + " (other than monitor level plugins). This is useful for overridding"
                    + " event handlers for plugins that don't respect the cancelled flag. This function hooks into the play-dirty"
                    + " framework that injects custom event handlers into bukkit.";
        }

		@Override
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.BindException};
        }

		@Override
        public boolean isRestricted() {
            return true;
        }
		@Override
        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

		@Override
        public Boolean runAsync() {
            return null;
        }

		@Override
        public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
            BoundEvent.ActiveEvent original = environment.getEnv(GlobalEnv.class).GetEvent();
            if (original == null) {
                throw new ConfigRuntimeException("is_cancelled cannot be called outside an event handler", ExceptionType.BindException, t);
            }
            if (original.getUnderlyingEvent() != null && original.getUnderlyingEvent() instanceof Cancellable
                    && original.getUnderlyingEvent() instanceof org.bukkit.event.Event) {
                ( (Cancellable) original.getUnderlyingEvent() ).setCancelled(true);
                BukkitDirtyRegisteredListener.setCancelled((org.bukkit.event.Event) original.getUnderlyingEvent());
            }
            environment.getEnv(GlobalEnv.class).GetEvent().setCancelled(true);
            return CVoid.VOID;
        }
    }

    @api(environments={CommandHelperEnvironment.class})
    public static class enchant_inv_unsafe extends AbstractFunction {

		@Override
        public String getName() {
            return "enchant_inv_unsafe";
        }

		@Override
        public Integer[] numArgs() {
            return new Integer[]{3, 4};
        }

		@Override
        public String docs() {
            return "void {[player], slot, type, level} Works the same as enchant_inv, except anything goes. "
                    + " You can enchant a fish with a level 5000 enchantment if you wish. Side effects"
                    + " may include nausia, dry mouth, insomnia, or server crashes. (Seriously, this might"
                    + " crash your server, be careful with it.)";
        }

		@Override
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.EnchantmentException, ExceptionType.PlayerOfflineException};
        }

		@Override
        public boolean isRestricted() {
            return true;
        }
		@Override
        public CHVersion since() {
            return CHVersion.V0_0_0;
        }

		@Override
        public Boolean runAsync() {
            return false;
        }

		@Override
        public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer m = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
            int offset = 1;
            if (args.length == 4) {
                m = Static.GetPlayer(args[0].val(), t);
                offset = 0;
            }
            MCItemStack is = null;
            if (args[1 - offset] instanceof CNull) {
                is = m.getItemInHand();
            } else {
                int slot = Static.getInt32(args[1 - offset], t);
                is = m.getInventory().getItem(slot);
            }
            CArray enchantArray = new CArray(t);
            if (!( args[2 - offset] instanceof CArray )) {
                enchantArray.push(args[2 - offset]);
            } else {
                enchantArray = (CArray) args[2 - offset];
            }

            CArray levelArray = new CArray(t);
            if (!( args[3 - offset] instanceof CArray )) {
                levelArray.push(args[3 - offset]);
            } else {
                levelArray = (CArray) args[3 - offset];
            }
            for (String key : enchantArray.stringKeySet()) {
                MCEnchantment e = StaticLayer.GetEnchantmentByName(Enchantments.ConvertName(enchantArray.get(key, t).val()));
                if (e == null) {
                    throw new ConfigRuntimeException(enchantArray.get(key, t).val().toUpperCase() + " is not a valid enchantment type", ExceptionType.EnchantmentException, t);
                }
                int level = Static.getInt32(new CString(Enchantments.ConvertLevel(levelArray.get(key, t).val()), t), t);

                is.addUnsafeEnchantment(e, level);
            }
            return CVoid.VOID;
        }
    }

    @api(environments={CommandHelperEnvironment.class})
    public static class raw_set_pvanish extends AbstractFunction {

		@Override
        public String getName() {
            return "raw_set_pvanish";
        }

		@Override
        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

		@Override
        public String docs() {
            return "void {[player], isVanished, otherPlayer} Sets the visibility"
                    + " of the current player (or the one specified) to visible or invisible"
                    + " (based on the value of isVanished) from the view of the otherPlayer."
                    + " This is the raw access function, you probably shouldn't use this, as"
                    + " the CommandHelper vanish api functions will probably be easier to use.";
        }

		@Override
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
        }

		@Override
        public boolean isRestricted() {
            return true; //lol, very
        }
		@Override
        public Boolean runAsync() {
            return false;
        }

		@Override
        public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer me;
            boolean isVanished;
            MCPlayer other;
            if (args.length == 2) {
                me = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
                isVanished = Static.getBoolean(args[0]);
                other = Static.GetPlayer(args[1], t);
            } else {
                me = Static.GetPlayer(args[0], t);
                isVanished = Static.getBoolean(args[1]);
                other = Static.GetPlayer(args[2], t);
            }

            other.setVanished(isVanished, me);

            return CVoid.VOID;
        }

		@Override
        public CHVersion since() {
            return CHVersion.V3_3_0;
        }
    }

    @api(environments={CommandHelperEnvironment.class})
    public static class raw_pcan_see extends AbstractFunction {

		@Override
        public String getName() {
            return "raw_pcan_see";
        }

		@Override
        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

		@Override
        public String docs() {
            return "boolean {[player], other} Returns a boolean stating if the other player can"
                    + " see this player or not. This is the raw access function, you probably shouldn't use this, as"
                    + " the CommandHelper vanish api functions will probably be easier to use.";
        }

		@Override
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
        }

		@Override
        public boolean isRestricted() {
            return true;
        }
		@Override
        public Boolean runAsync() {
            return false;
        }

		@Override
        public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer me;
            MCPlayer other;
            if (args.length == 1) {
                me = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
                other = Static.GetPlayer(args[0], t);
            } else {
                me = Static.GetPlayer(args[0], t);
                other = Static.GetPlayer(args[1], t);
            }
            return CBoolean.get(me.canSee(other));
        }

		@Override
        public CHVersion since() {
            return CHVersion.V3_3_0;
        }
    }

	@api
	@hide("This is an easter egg.")
	public static class moo extends DummyFunction {

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}


		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			String saying = args[0].val();
			String divider = "";
			for(int i = 0; i < saying.length() + 4; i++){
				divider += "-";
			}
			return new CString(divider + "\n"
				+ "| " + saying + " |\n"
				+ divider + "\n"
				+ " \\   ^__^\n"
				+ "  \\  (oo)\\_______\n"
				+ "     (__)\\       )\\/\\\n"
				+ "         ||----w |\n"
				+ "         ||     ||\n", t);
		}

	}

	@api
	@hide("This is an easter egg.")
	public static class moo2 extends DummyFunction {

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			String saying = args[0].val();
			String divider = "";
			for(int i = 0; i < saying.length() + 4; i++){
				divider += "-";
			}
			return new CString(
				  "                     " + divider + "\n"
				+ "                     | " + saying + " |\n"
				+ "                     " + divider + "\n"
				+ "              ^__^   /\n"
				+ "      _______/(oo)  /\n"
				+ " /\\/(        /(__)\n"
				+ "      | w----||\n"
				+ "      ||     ||\n", t);
		}

	}
}
