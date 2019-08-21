package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
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

	public static String docs() {
		return "This class provides methods for making a text \"marquee\", like a stock ticker. Because this is a threading operation, and could be potentially"
				+ " resource intensive, the heavy lifting has been implemented natively.";
	}

	// TODO: This should be removed in favor of a common runtime environment stash.
	private static final Map<String, com.laytonsmith.PureUtilities.Marquee> MARQUEE_MAP =
			new HashMap<String, com.laytonsmith.PureUtilities.Marquee>();

	@api
	public static class marquee extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(final Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			final String marqueeName;
			final String text;
			final int stringWidth;
			final int delayTime;
			final CClosure callback;
			int offset = -1;
			if(args.length == 5) {
				offset = 0;
				marqueeName = args[0].val();
			} else {
				marqueeName = null;
			}
			text = args[1 + offset].val();
			stringWidth = Static.getInt32(args[2 + offset], t);
			delayTime = Static.getInt32(args[3 + offset], t);
			if(args[4 + offset].isInstanceOf(CClosure.TYPE)) {
				callback = ((CClosure) args[4 + offset]);
			} else {
				throw new CRECastException("Expected argument " + (4 + offset + 1) + " to be a closure, but was not.", t);
			}
			final com.laytonsmith.PureUtilities.Marquee m = new com.laytonsmith.PureUtilities.Marquee(text, stringWidth, delayTime, new com.laytonsmith.PureUtilities.Marquee.MarqueeCallback() {

				@Override
				public void stringPortion(final String portion, com.laytonsmith.PureUtilities.Marquee m) {
					try {
						StaticLayer.GetConvertor().runOnMainThreadAndWait(new Callable<Object>() {

							@Override
							public Object call() throws Exception {
								callback.executeCallable(new Mixed[]{new CString(portion, t)});
								return null;
							}
						});
					} catch (Exception e) {
						//We don't want this to affect our code, so just log it,
						//but we also want to stop this marquee
						String message = "An error occured while running " + (marqueeName == null ? "an unnamed marquee" : "the " + marqueeName + " marquee")
								+ ". To prevent further errors, it has been temporarily stopped.";
						Logger.getLogger(Marquee.class.getName()).log(Level.SEVERE, message, e);
						m.stop();
					}
				}
			});
			m.start();
			StaticLayer.GetConvertor().addShutdownHook(new Runnable() {

				@Override
				public void run() {
					m.stop();
				}
			});
			if(marqueeName != null) {
				MARQUEE_MAP.put(marqueeName, m);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "marquee";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{4, 5};
		}

		@Override
		public String docs() {
			return "void {[marqueeName], text, stringWidth, delayTime, callback} Sets up a marquee, which will automatically"
					+ " split up a given string for you, and call the callback. The split string will automatically wrap, handle"
					+ " buffering spaces, and scroll through the text. ---- marqueeName is optional, but required if you wish"
					+ " to stop the marquee at any point. text is the text that the marquee should scroll, stringWidth is the"
					+ " width of the string you wish to recieve, delayTime is the"
					+ " time between character scrolls, and callback is a closure that should recieve a string which will be exactly"
					+ " stringWidth long. (The string will have been wrapped as needed if it is less than that size.)"
					+ " This is usually used in combination with signs, but in theory could be used with anything that uses text.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	public static class marquee_stop extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String marqueeName = args[0].val();
			if(MARQUEE_MAP.containsKey(marqueeName)) {
				MARQUEE_MAP.get(marqueeName).stop();
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "marquee_stop";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {marqueeName} Stops a named marquee.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}
}
