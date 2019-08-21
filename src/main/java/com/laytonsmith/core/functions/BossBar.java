package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCBossBar;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.enums.MCBarColor;
import com.laytonsmith.abstraction.enums.MCBarStyle;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CRELengthException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CREPlayerOfflineException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.HashMap;
import java.util.Map;

public class BossBar {

	public static String docs() {
		return "Functions to create and manage boss bars in Minecraft.";
	}

	private static final Map<String, MCBossBar> BARS = new HashMap<>();

	public abstract static class BossBarFunction extends AbstractFunction {

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}
	}

	@api
	public static class get_bars extends BossBarFunction {

		@Override
		public String getName() {
			return "get_bars";
		}

		@Override
		public String docs() {
			return "array {} Gets an array of boss bar ids currently in use.";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public CArray exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CArray ca = new CArray(t);
			for(String id : BARS.keySet()) {
				ca.push(new CString(id, t), t);
			}
			return ca;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

	}

	@api
	public static class create_bar extends BossBarFunction {

		@Override
		public String getName() {
			return "create_bar";
		}

		@Override
		public String docs() {
			return "void {id, [optionsArray]} Creates a new boss bar with a reference id. An optional array can be"
					+ " given with the keys: title, color, style, visible, percent. Title displays above the boss bar."
					+ " Color can be one of " + StringUtils.Join(MCBarColor.values(), ", ", ", or ", " or ") + "."
					+ " Style can be one of " + StringUtils.Join(MCBarStyle.values(), ", ", ", or ", " or ") + "."
					+ " Visible is a boolean for whether the bar is visible or not."
					+ " Percent is a double from 0.0 to 1.0 representing the how much the bar is filled from left to right."
					+ " Defaults to an empty title with a WHITE, SOLID, visible, full bar.";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			String id = args[0].val();
			if(BARS.containsKey(id)) {
				throw new CREIllegalArgumentException("That boss bar id is already in use.", t);
			}
			String title = "";
			MCBarColor color = MCBarColor.WHITE;
			MCBarStyle style = MCBarStyle.SOLID;
			boolean visible = true;
			double percent = 1.0;
			if(args.length == 2) {
				if(!(args[1].isInstanceOf(CArray.TYPE))) {
					throw new CRECastException("Expected array for parameter 2 of create_bar()", t);
				}
				CArray ca = (CArray) args[1];
				if(ca.containsKey("title")) {
					title = ca.get("title", t).val();
				}
				if(ca.containsKey("color")) {
					try {
						color = MCBarColor.valueOf(ca.get("color", t).val());
					} catch (IllegalArgumentException ex) {
						throw new CREFormatException("Invalid boss bar color.", t);
					}
				}
				if(ca.containsKey("style")) {
					try {
						style = MCBarStyle.valueOf(ca.get("style", t).val());
					} catch (IllegalArgumentException ex) {
						throw new CREFormatException("Invalid boss bar style.", t);
					}
				}
				if(ca.containsKey("visible")) {
					visible = ArgumentValidation.getBoolean(ca.get("visible", t), t);
				}
				if(ca.containsKey("percent")) {
					try {
						percent = Static.getDouble(ca.get("percent", t), t);
					} catch (IllegalArgumentException ex) {
						throw new CRERangeException("Progress percentage must be from 0.0 to 1.0.", t);
					}
				}
			}
			MCBossBar bar = StaticLayer.GetServer().createBossBar(title, color, style);
			if(bar != null) { // if not tests
				bar.setVisible(visible);
				bar.setProgress(percent);
				BARS.put(id, bar);
			}
			return CVoid.VOID;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIllegalArgumentException.class, CRECastException.class, CREFormatException.class,
				CRERangeException.class, CREException.class};
		}

	}

	@api
	public static class update_bar extends BossBarFunction {

		@Override
		public String getName() {
			return "update_bar";
		}

		@Override
		public String docs() {
			return "void {id, optionsArray | id, percent | id, title} Updates the state for the specified boss bar."
					+ " See create_bar() for available option keys and values for the optionsArray."
					+ " If the second argument is a string, it'll use it to update the title. If it's a double, it'll"
					+ " use it to update the percentage filled (0.0 - 1.0).";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			String id = args[0].val();
			MCBossBar bar = BARS.get(id);
			if(bar == null) {
				throw new CRENotFoundException("That boss bar id does not exist.", t);
			}
			if(args[1].isInstanceOf(CString.TYPE)) {
				bar.setTitle(args[1].val());
			} else if(args[1].isInstanceOf(CDouble.TYPE)) {
				try {
					bar.setProgress(Static.getDouble(args[1], t));
				} catch (IllegalArgumentException ex) {
					throw new CRERangeException("Progress percentage must be from 0.0 to 1.0.", t);
				}
			} else if(args[1].isInstanceOf(CArray.TYPE)) {
				CArray ca = (CArray) args[1];
				if(ca.containsKey("title")) {
					bar.setTitle(ca.get("title", t).val());
				}
				if(ca.containsKey("color")) {
					try {
						bar.setColor(MCBarColor.valueOf(ca.get("color", t).val()));
					} catch (IllegalArgumentException ex) {
						throw new CREFormatException("Invalid boss bar color.", t);
					}
				}
				if(ca.containsKey("style")) {
					try {
						bar.setStyle(MCBarStyle.valueOf(ca.get("style", t).val()));
					} catch (IllegalArgumentException ex) {
						throw new CREFormatException("Invalid boss bar style.", t);
					}
				}
				if(ca.containsKey("visible")) {
					bar.setVisible(ArgumentValidation.getBoolean(ca.get("visible", t), t));
				}
				if(ca.containsKey("percent")) {
					try {
						bar.setProgress(Static.getDouble(ca.get("percent", t), t));
					} catch (IllegalArgumentException ex) {
						throw new CRERangeException("Progress percentage must be from 0.0 to 1.0.", t);
					}
				}
			} else {
				throw new CREIllegalArgumentException("Invalid argument for parameter 2 of update_bar()", t);
			}
			return CVoid.VOID;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIllegalArgumentException.class, CRECastException.class, CREFormatException.class,
				CRENotFoundException.class, CRERangeException.class};
		}

	}

	@api
	public static class get_bar extends BossBarFunction {

		@Override
		public String getName() {
			return "get_bar";
		}

		@Override
		public String docs() {
			return "array {id} Gets an array of current options for the specified boss bar.";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			String id = args[0].val();
			MCBossBar bar = BARS.get(id);
			if(bar == null) {
				throw new CRENotFoundException("That boss bar id does not exist.", t);
			}
			CArray ret = CArray.GetAssociativeArray(t);
			ret.set("title", bar.getTitle(), t);
			ret.set("color", bar.getColor().name(), t);
			ret.set("style", bar.getStyle().name(), t);
			ret.set("visible", CBoolean.get(bar.isVisible()), t);
			ret.set("percent", new CDouble(bar.getProgress(), t), t);
			return ret;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class};
		}

	}

	@api
	public static class remove_bar extends BossBarFunction {

		@Override
		public String getName() {
			return "remove_bar";
		}

		@Override
		public String docs() {
			return "void {id} Removes the boss bar for all players.";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			String id = args[0].val();
			MCBossBar bar = BARS.get(id);
			if(bar == null) {
				throw new CRENotFoundException("That boss bar id does not exist.", t);
			}
			bar.removeAllPlayers();
			BARS.remove(id);
			return CVoid.VOID;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class};
		}

	}

	@api
	public static class bar_add_player extends BossBarFunction {

		@Override
		public String getName() {
			return "bar_add_player";
		}

		@Override
		public String docs() {
			return "void {id, player} Adds a player to the specified boss bar.";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCBossBar bar = BARS.get(args[0].val());
			if(bar == null) {
				throw new CRENotFoundException("That boss bar id does not exist.", t);
			}
			bar.addPlayer(Static.GetPlayer(args[1].val(), t));
			return CVoid.VOID;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class, CREPlayerOfflineException.class, CRELengthException.class};
		}
	}

	@api
	public static class bar_remove_player extends BossBarFunction {

		@Override
		public String getName() {
			return "bar_remove_player";
		}

		@Override
		public String docs() {
			return "void {id, player} Removes a player from the specified boss bar.";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCBossBar bar = BARS.get(args[0].val());
			if(bar == null) {
				throw new CRENotFoundException("That boss bar id does not exist.", t);
			}
			bar.removePlayer(Static.GetPlayer(args[1].val(), t));
			return CVoid.VOID;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class, CREPlayerOfflineException.class, CRELengthException.class};
		}
	}

	@api
	public static class get_bar_players extends BossBarFunction {

		@Override
		public String getName() {
			return "get_bar_players";
		}

		@Override
		public String docs() {
			return "array {id} Returns an array of players that can see this boss bar.";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCBossBar bar = BARS.get(args[0].val());
			if(bar == null) {
				throw new CRENotFoundException("That boss bar id does not exist.", t);
			}
			CArray players = new CArray(t);
			for(MCPlayer player : bar.getPlayers()) {
				players.push(new CString(player.getName(), t), t);
			}
			return players;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class};
		}
	}
}
