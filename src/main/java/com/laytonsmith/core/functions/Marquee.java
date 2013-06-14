package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.arguments.ArgumentBuilder;
import com.laytonsmith.core.arguments.Generic;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class Marquee {
	public static String docs(){
		return "This class provides methods for making a text \"marquee\", like a stock ticker. Because this is a threading operation, and could be potentially"
				+ " resource intensive, the heavy lifting has been implemented natively.";
	}
		
	//TODO: This should be removed in favor of a common runtime environment stash
	private static Map<String, com.laytonsmith.PureUtilities.Marquee> marqeeMap = new HashMap<String, com.laytonsmith.PureUtilities.Marquee>();
	@api public static class marquee extends AbstractFunction{

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(final Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			final String marqueeName;
			final String text;
			final int stringWidth;
			final int delayTime;
			final CClosure callback;
			int offset = -1;
			if(args.length == 5){
				offset = 0;
				marqueeName = args[0].val();
			} else {
				marqueeName = null;
			}
			text = args[1 + offset].val();
			stringWidth = args[2 + offset].primitive(t).castToInt32(t);
			delayTime = args[3 + offset].primitive(t).castToInt32(t);
			if(args[4 + offset] instanceof CClosure){
				callback = ((CClosure)args[4 + offset]);
			} else {
				throw new Exceptions.CastException("Expected argument " + (4 + offset + 1) + " to be a closure, but was not.", t);
			}
			final com.laytonsmith.PureUtilities.Marquee m = new com.laytonsmith.PureUtilities.Marquee(text, stringWidth, delayTime, new com.laytonsmith.PureUtilities.Marquee.MarqueeCallback() {

				public void stringPortion(final String portion, com.laytonsmith.PureUtilities.Marquee m) {
					try{
						StaticLayer.GetConvertor().runOnMainThreadAndWait(new Callable<Object>(){

							public Object call() throws Exception {
									callback.execute(new Construct[]{new CString(portion, t)});
									return null;
							}
						});
					} catch(Exception e){
						//We don't want this to affect our code, so just log it,
						//but we also want to stop this marquee
						String message = "An error occured while running " + (marqueeName==null?"an unnamed marquee":"the " + marqueeName + " marquee")
								+ ". To prevent further errors, it has been temporarily stopped.";
						Logger.getLogger(Marquee.class.getName()).log(Level.SEVERE, message, e);
						m.stop();
					}
				}
			});
			m.start();
			StaticLayer.GetConvertor().addShutdownHook(new Runnable() {

				public void run() {
					m.stop();
				}
			});
			if(marqueeName != null){
				marqeeMap.put(marqueeName, m);
			}
			return new CVoid(t);
		}

		public String getName() {
			return "marquee";
		}

		public Integer[] numArgs() {
			return new Integer[]{4, 5};
		}

		public String docs() {
			return "Sets up a marquee, which will automatically"
					+ " split up a given string for you, and call the callback. The split string will automatically wrap, handle"
					+ " buffering spaces, and scroll through the text. ---- marqueeName is optional, but required if you wish"
					+ " to stop the marquee at any point. text is the text that the marquee should scroll, stringWidth is the"
					+ " width of the string you wish to recieve, delayTime is the"
					+ " time between character scrolls, and callback is a closure that should recieve a string which will be exactly"
					+ " stringWidth long. (The string will have been wrapped as needed if it is less than that size.)"
					+ " This is usually used in combination with signs, but in theory could be used with anything that uses text.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "marqueeName").setOptionalDefaultNull(),
						new Argument("", CString.class, "text"),
						new Argument("", CInt.class, "stringWidth"),
						new Argument("", CInt.class, "delayTime"),
						new Argument("", CClosure.class, "callback").setGenerics(new Generic(CString.class))
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api public static class marquee_stop extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String marqueeName = args[0].val();
			if(marqeeMap.containsKey(marqueeName)){
				marqeeMap.get(marqueeName).stop();
			}
			return new CVoid(t);
		}

		public String getName() {
			return "marquee_stop";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Stops a named marquee.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "marqueeName")
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
}
