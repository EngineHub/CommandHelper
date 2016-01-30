package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.abstraction.MCObjective;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCScoreboard;
import com.laytonsmith.abstraction.MCTeam;
import com.laytonsmith.abstraction.enums.MCCriteria;
import com.laytonsmith.abstraction.enums.MCDisplaySlot;
import com.laytonsmith.abstraction.enums.MCNameTagVisibility;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.NotInitializedYetException;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CRELengthException;
import com.laytonsmith.core.exceptions.CRE.CRENullPointerException;
import com.laytonsmith.core.exceptions.CRE.CREPlayerOfflineException;
import com.laytonsmith.core.exceptions.CRE.CREScoreboardException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jb_aero
 */
public class Scoreboards {

	public static String docs() {
		return "A class of functions for manipulating the server scoreboard.";
	}

	/**
	 * The name storing the server's main scoreboard
	 */
	public static final String MAIN = "main";
	private static final String DEF_MSG = "Scoreboard defaults to '" + MAIN + "' if not given.";
	private static Map<String, MCScoreboard> boards = new HashMap<String, MCScoreboard>();

	static {
		if (!isBoard(MAIN)) {
			try{
				addBoard(MAIN, Static.getServer().getMainScoreboard(), Target.UNKNOWN);
			} catch(NotInitializedYetException e){
				//This should only happen during testing or from the shell (or some other cases) so just log
				//it and continue on.
				e.printStackTrace(StreamUtils.GetSystemErr());
			}
		}
	}

	/**
	 * Checks if a scoreboard is being tracked
	 * @param id Name to check for
	 * @return if there is a scoreboard with the given id
	 */
	public static boolean isBoard(String id) {
		return boards.containsKey(id);
	}

	/**
	 * Checks if a scoreboard is being tracked
	 * @param board Scoreboard to check for
	 * @return if the given scoreboard is already being tracked
	 */
	public static boolean isBoard(MCScoreboard board) {
		for (MCScoreboard s : boards.values()) {
			if (s.equals(board)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds a scoreboard to the cache
	 * @param id The name to save the new scoreboard as
	 * @param board Scoreboard, either from {@link com.laytonsmith.abstraction.MCServer#getNewScoreboard()} or {@link MCPlayer#getScoreboard()}
	 * @param t
	 * @throws CREScoreboardException if the cache already contains the board or the id
	 */
	public static void addBoard(String id, MCScoreboard board, Target t) throws CREScoreboardException {
		if (isBoard(id)) {
			throw new CREScoreboardException("That id is already in use.", t);
		}
		if (isBoard(board)) {
			throw new CREScoreboardException("That Scoreboard is already added.", t);
		}
		boards.put(id, board);
	}

	/**
	 * Gets a scoreboard from the cache
	 * @param id Name of the scoreboard to look for
	 * @param t
	 * @return the scoreboard with the given id
	 * @throws CREScoreboardException if the cache does not contain id or if the MCScoreboard object is null
	 */
	public static MCScoreboard getBoard(String id, Target t) throws CREScoreboardException {
		if (!isBoard(id)) {
			throw new CREScoreboardException("The specified scoreboard does not exist.", t);
		}
		MCScoreboard ret = boards.get(id);
		if (ret == null) {
			throw new CREScoreboardException("The specified scoreboard is null. Are you running from cmdline mode?", t);
		}
		return ret;
	}

	/**
	 * Searches the cache for the given scoreboard, and returns the ID matching it.
	 * @param board The scoreboard to find the ID for
	 * @param t
	 * @return the ID of the scoreboard
	 * @throws CREScoreboardException if the cache does not contain the given scoreboard
	 */
	public static String getBoardID(MCScoreboard board, Target t) throws CREScoreboardException {
		for (Map.Entry<String, MCScoreboard> e : boards.entrySet()) {
			if (board.equals(e.getValue())) {
				return e.getKey();
			}
		}
		throw new CREScoreboardException("The given scoreboard has not been registered yet.", t);
	}

	/**
	 * Removes a scoreboard from the cache, without clearing any of its data
	 * @param id The scoreboard to remove
	 * @param t
	 * @throws CREScoreboardException if used on MAIN or if the cache does not contain id
	 */
	public static void removeBoard(String id, Target t) throws CREScoreboardException {
		if (id.equalsIgnoreCase(MAIN)) {
			throw new CREScoreboardException("Cannot remove the main server scoreboard.", t);
		}
		if (!isBoard(id)) {
			throw new CREScoreboardException("The specified scoreboard does not exist.", t);
		}
		boards.remove(id);
	}

	/**
	 * A shortcut for making a scoreboard argument optional
	 * @param numArgsToReadName the number of arguments that will cause the function to check user input
	 * @param indexOfName the index that will contain the name of the scoreboard
	 * @param t
	 * @param args the array of arguments passed to the function
	 * @return the scoreboard chosen, defaulting to main if numArgsToReadName was not matched
	 * @throws CREScoreboardException if the specified scoreboard does not exist
	 */
	public static MCScoreboard assignBoard(int numArgsToReadName, int indexOfName, Target t, Construct... args) throws CREScoreboardException {
		if (args.length == numArgsToReadName) {
			return getBoard(args[indexOfName].val(), t);
		}
		return getBoard(MAIN, t);
	}

	/**
	 * Contains methods that should be the same for most scoreboard functions
	 */
	public static abstract class SBFunction extends AbstractFunction {

		/**
		 * @return true
		 */
		@Override
		public boolean isRestricted() {
			return true;
		}
		/**
		 * @return false
		 */
		@Override
		public Boolean runAsync() {
			return false;
		}
		
		/**
		 * @return Array containing only {@link ExceptionType#CREScoreboardException}
		 */
		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREScoreboardException.class};
		}
	}

	@api
	public static class get_pscoreboard extends SBFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CREScoreboardException.class};
		}

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCPlayer p = Static.GetPlayer(args[0], t);
			String ret;
			try {
				ret = getBoardID(p.getScoreboard(), t);
			} catch (ConfigRuntimeException cre) {
				ret = p.getName();
				addBoard(ret, p.getScoreboard(), t);
			}
			return new CString(ret, t);
		}

		@Override
		public String getName() {
			return "get_pscoreboard";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "scoreboard {player} Returns the id of the scoreboard assigned to a player."
					+ " If it is not already cached, it will be added using the player's name."
					+ " Using this method, it should be possible to import scoreboards created by other plugins.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class set_pscoreboard extends SBFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CREScoreboardException.class};
		}

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCPlayer p = Static.GetPlayer(args[0], t);
			p.setScoreboard(assignBoard(2, 1, t, args));
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_pscoreboard";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {player, [scoreboard]} Sets the scoreboard to be used by a player."
					+ " The scoreboard argument is the id of a registered scoreboard. " + DEF_MSG;
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class get_scoreboards extends SBFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			CArray ret = new CArray(t);
			for (String id : boards.keySet()) {
				ret.push(new CString(id, t), t);
			}
			return ret;
		}

		@Override
		public String getName() {
			return "get_scoreboards";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Returns an array of the registered scoreboard ID's."
					+ " The special scoreboard '"+ MAIN + "' represents the server's main"
					+ " scoreboard which can be managed by the vanilla /scoreboard command.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class get_objectives extends SBFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCScoreboard s;
			if (args.length == 0) {
				s = getBoard(MAIN, t);
			} else {
				s = getBoard(args[0].val(), t);
			}
			Set<MCObjective> os;
			if (args.length == 2) {
				MCCriteria crit;
				try {
					crit = MCCriteria.valueOf(args[1].val());
				} catch (IllegalArgumentException iae) {
					crit = MCCriteria.DUMMY;
				}
				os = s.getObjectivesByCriteria(crit.getCriteria());
			} else {
				os = s.getObjectives();
			}
			CArray ret = new CArray(t);
			for (MCObjective o : os) {
				CArray obj = CArray.GetAssociativeArray(t);
				obj.set("name", new CString(o.getName(), t), t);
				obj.set("displayname", new CString(o.getDisplayName(), t), t);
				Construct slot = CNull.NULL;
				if (o.getDisplaySlot() != null) {
					slot = new CString(o.getDisplaySlot().name(), t);
				}
				obj.set("slot", slot, t);
				obj.set("modifiable", CBoolean.get(o.isModifiable()), t);
				obj.set("criteria", new CString(o.getCriteria(), t), t);
				ret.push(obj, t);
			}
			return ret;
		}

		@Override
		public String getName() {
			return "get_objectives";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1, 2};
		}

		@Override
		public String docs() {
			return "array {[scoreboard], [criteria]} Returns an array of arrays about the objectives"
					+ " on the given scoreboard, which defaults to '" + MAIN + "' if not given."
					+ " If criteria is given, only objectives with that criteria will be returned."
					+ " The arrays contain the keys name, displayname, slot, modifiable, and criteria.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class get_teams extends SBFunction {

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCScoreboard s;
			if (args.length == 0) {
				s = getBoard(MAIN, t);
			} else {
				s = getBoard(args[0].val(), t);
			}
			CArray ret = new CArray(t);
			for (MCTeam team : s.getTeams()) {
				CArray to = CArray.GetAssociativeArray(t);
				to.set("name", new CString(team.getName(), t), t);
				to.set("displayname", new CString(team.getDisplayName(), t), t);
				to.set("prefix", new CString(team.getPrefix(), t), t);
				to.set("suffix", new CString(team.getSuffix(), t), t);
				to.set("size", new CInt(team.getSize(), t), t);
				CArray ops = CArray.GetAssociativeArray(t);
				ops.set("friendlyfire", CBoolean.get(team.allowFriendlyFire()), t);
				ops.set("friendlyinvisibles", CBoolean.get(team.canSeeFriendlyInvisibles()), t);
				if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_8)) {
					ops.set("nametagvisibility", new CString(team.getNameTagVisibility().name(), t), t);
				}
				to.set("options", ops, t);
				CArray pl = new CArray(t);
				for (String entry : team.getEntries()) {
					pl.push(new CString(entry, t), t);
				}
				to.set("players", pl, t);
				ret.push(to, t);
			}
			return ret;
		}

		@Override
		public String getName() {
			return "get_teams";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "array {[scoreboard]} Returns an array of arrays about the teams on the given scoreboard,"
					+ " which defaults to '" + MAIN + "' if not given. The arrays contain the keys name,"
					+ " displayname, prefix, suffix, size, options, and players.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class create_scoreboard extends SBFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENullPointerException.class};
		}
		
		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCScoreboard newBoard = Static.getServer().getNewScoreboard();
			if (newBoard == null) {
				throw ConfigRuntimeException.BuildException(
						"Could not create scoreboard, the server returned a null scoreboard"
						+ " (Are you running in cmdline mode?)", CRENullPointerException.class, t);
			}
			addBoard(args[0].val(), newBoard, t);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "create_scoreboard";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {name} Creates a new scoreboard identified by the given name,"
					+ " and stores it internally for later use. Throws an exception if the name is already in use.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class create_objective extends SBFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRELengthException.class, CREScoreboardException.class};
		}

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCScoreboard s = assignBoard(3, 2, t, args);
			String name = args[0].val();
			if (name.length() > 16) {
				throw new CRELengthException("Objective names should be no more than 16 characters", t);
			}
			MCCriteria criteria = MCCriteria.DUMMY;
			if (args.length > 1) {
				try {
					criteria = MCCriteria.valueOf(args[1].val().toUpperCase());
				} catch (IllegalArgumentException iae) {
					// Leave as dummy
				}
			}
			try {
				s.registerNewObjective(name, criteria.getCriteria());
			} catch (IllegalArgumentException iae) {
				throw new CREScoreboardException("An objective by that name already exists.", t);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "create_objective";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		@Override
		public String docs() {
			return "void {name, [criteria, [scoreboard]]} Adds a new objective to the scoreboard,"
					+ " throwing a CREScoreboardException if the name is already in use. The vanilla criteria names are "
					+ StringUtils.Join(MCCriteria.values(), ", ", ", and ") + ". You can put anything,"
					+ " but if none of the other values match, 'dummy' will be used."
					+ " Those values which are not 'dummy' are server-managed."
					+ " Throws a LengthException if the name is more than 16 characters. " + DEF_MSG;
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class create_team extends SBFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRELengthException.class, CREScoreboardException.class};
		}

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCScoreboard s = assignBoard(2, 1, t, args);
			String name = args[0].val();
			if (name.length() > 16) {
				throw new CRELengthException("Team names should be no more than 16 characters.", t);
			}
			try {
				s.registerNewTeam(name);
			} catch (IllegalArgumentException iae) {
				throw new CREScoreboardException("A team by that name already exists.", t);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "create_team";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {name, [scoreboard]} Adds a new team to the scoreboard,"
					+ " throws a CREScoreboardException if a team already exists with the given name."
					+ " Throws a LengthException if the team name is more than 16 characters. " + DEF_MSG;
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class set_objective_display extends SBFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRELengthException.class,
					CREScoreboardException.class};
		}

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCScoreboard s = assignBoard(3, 2, t, args);
			MCObjective o = s.getObjective(args[0].val());
			if (o == null) {
				throw new CREScoreboardException("No objective by that name exists.", t);
			}
			CArray dis = CArray.GetAssociativeArray(t);
			if (args[1] instanceof CArray) {
				dis = (CArray) args[1];
			} else {
				dis.set("displayname", args[1], t);
			}
			if (dis.containsKey("slot")) {
				MCDisplaySlot slot;
				if (dis.get("slot", t) instanceof CNull) {
					slot = null;
				} else {
					try {
						slot = MCDisplaySlot.valueOf(dis.get("slot", t).val().toUpperCase());
					} catch (IllegalArgumentException iae) {
						throw new CREFormatException("Unknown displayslot: " + dis.get("slot", t).val(), t);
					}
				}
				o.setDisplaySlot(slot);
			}
			if (dis.containsKey("displayname")) {
				String dname;
				if (dis.get("displayname", t) instanceof CNull) {
					dname = o.getName();
				} else {
					dname = dis.get("displayname", t).val();
				}
				if (dname.length() > 32) {
					throw new CRELengthException("Displayname can only be 32 characters but was "
							+ dname.length(), t);
				}
				o.setDisplayName(dname);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_objective_display";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {objectiveName, array, [scoreboard] | objectiveName, displayname, [scoreboard]}"
					+ " Sets the display name and/or slot of the given objective. If arg 2 is not an array,"
					+ " it is assumed to be the displayname, otherwise arg 2 should be an array"
					+ " with keys 'displayname' and/or 'slot', affecting their respective properties."
					+ " Null name resets it to the actual name, and null slot removes it from"
					+ " all displays. Slot can be one of: " + StringUtils.Join(MCDisplaySlot.values(), ", ", ", or ")
					+ ". Displayname can be a max of 32 characters, otherwise it throws a LengthException. " + DEF_MSG;
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class set_team_display extends SBFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRELengthException.class, CREScoreboardException.class};
		}

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCScoreboard s = assignBoard(3, 2, t, args);
			MCTeam o = s.getTeam(args[0].val());
			if (o == null) {
				throw new CREScoreboardException("No team by that name exists.", t);
			}
			CArray dis = CArray.GetAssociativeArray(t);
			if (args[1] instanceof CArray) {
				dis = (CArray) args[1];
			} else {
				dis.set("displayname", args[1], t);
			}
			if (dis.containsKey("displayname")) {
				String dname;
				if (dis.get("displayname", t) instanceof CNull) {
					dname = o.getName();
				} else {
					dname = dis.get("displayname", t).val();
				}
				if (dname.length() > 32) {
					throw new CRELengthException("Displayname can only be 32 characters but was "
							+ dname.length(), t);
				}
				o.setDisplayName(dname);
			}
			if (dis.containsKey("prefix")) {
				String prefix;
				if (dis.get("prefix", t) instanceof CNull) {
					prefix = "";
				} else {
					prefix = dis.get("prefix", t).val();
				}
				if (prefix.length() > 16) {
					throw new CRELengthException("Prefix can only be 16 characters but was "
							+ prefix.length(), t);
				}
				o.setPrefix(prefix);
			}
			if (dis.containsKey("suffix")) {
				String suffix;
				if (dis.get("suffix", t) instanceof CNull) {
					suffix = "";
				} else {
					suffix = dis.get("suffix", t).val();
				}
				if (suffix.length() > 16) {
					throw new CRELengthException("Suffix can only be 16 characters but was "
							+ suffix.length(), t);
				}
				o.setSuffix(suffix);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_team_display";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {teamName, array, [scoreboard] | teamName, displayname, [scoreboard]}"
					+ " Sets the display name, prefix, and/or suffix of the given team."
					+ " If arg 2 is not an array, it is assumed to be the displayname,"
					+ " otherwise arg 2 should be an array with keys 'displayname', 'prefix',"
					+ " and/or 'suffix', affecting their respective properties."
					+ " Null name resets it to the actual name, and null prefix or suffix removes it from"
					+ " all displays. Displayname can be a max of 32 characters,"
					+ " prefix and suffix can only be 16, otherwise a LengthException is thrown. " + DEF_MSG;
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class team_add_player extends SBFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRELengthException.class, CREScoreboardException.class};
		}

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCScoreboard s = assignBoard(3, 2, t, args);
			MCTeam team = s.getTeam(args[0].val());
			if (team == null) {
				throw new CREScoreboardException("No team by that name exists.", t);
			}
			if (args[1].val().length() > 40
					|| (args[1].val().length() > 16 && Static.getServer().getMinecraftVersion().lt(MCVersion.MC1_8_7))){
				throw new CRELengthException("Player name is too long.", t);
			}
			team.addEntry(args[1].val());
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "team_add_player";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {teamName, player, [scoreboard]} Adds a player to a team, given the team exists. This allows"
					+ " you to add fake players with up to 40 characters. (16 characters prior to 1.8.7) The player"
					+ " will be removed from any other team on the same scoreboard. " + DEF_MSG;
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class team_remove_player extends SBFunction {

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCScoreboard s = assignBoard(3, 2, t, args);
			MCTeam team = s.getTeam(args[0].val());
			if (team == null) {
				throw new CREScoreboardException("No team by that name exists.", t);
			}
			return CBoolean.get(team.removeEntry(args[1].val()));
		}

		@Override
		public String getName() {
			return "team_remove_player";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "boolean {teamname, player, [scoreboard]} Attempts to remove a player from a team,"
					+ " and returns true if successful, for false if the player was not part of the team." + DEF_MSG;
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class remove_scoreboard extends SBFunction {

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			String id = args[0].val();
			boolean nullify = true;
			if (args.length == 2) {
				nullify = Static.getBoolean(args[1]);
			}
			if (nullify) {
				MCScoreboard s = getBoard(id, t);
				for (String e : s.getEntries()) {
					s.resetScores(e);
				}
				for (MCPlayer p : Static.getServer().getOnlinePlayers()) {
					if (s.equals(p.getScoreboard())) {
						p.setScoreboard(getBoard(MAIN, t));
					}
				}
				for (MCTeam g : s.getTeams()) {
					g.unregister();
				}
				for (MCObjective o : s.getObjectives()) {
					o.unregister();
				}
			}
			removeBoard(id, t);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "remove_scoreboard";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {scoreboard, [nullify]} Stops tracking the given scoreboard, unless it is '"
					+ MAIN + "', because that never goes away. If nullify is true (defaults to true),"
					+ " all scores, teams, and objectives will be cleared,"
					+ " and all tracked players currently online will be switched to the main scoreboard,"
					+ " essentially removing all references to the board so it can be garbage-collected.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class remove_objective extends SBFunction {

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCScoreboard s = assignBoard(2, 1, t, args);
			MCObjective o = s.getObjective(args[0].val());
			try {
				o.unregister();
			} catch (NullPointerException npe) {
				throw new CREScoreboardException("The objective does not exist.", t);
			} catch (IllegalStateException ise) {
				throw new CREScoreboardException("The objective has already been unregistered.", t);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "remove_objective";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {objectivename, [scoreboard]} Unregisters an objective from the scoreboard. " + DEF_MSG;
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class remove_team extends SBFunction {

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCScoreboard s = assignBoard(2, 1, t, args);
			MCTeam team = s.getTeam(args[0].val());
			try {
				team.unregister();
			} catch (NullPointerException npe) {
				throw new CREScoreboardException("The team does not exist.", t);
			} catch (IllegalStateException ise) {
				throw new CREScoreboardException("The team has already been unregistered.", t);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "remove_team";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {teamname, [scoreboard]} Unregisters a team from the scoreboard. " + DEF_MSG;
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class get_pscore extends SBFunction {

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCScoreboard s = assignBoard(3, 2, t, args);
			MCObjective o = s.getObjective(args[0].val());
			if (o == null) {
				throw new CREScoreboardException("The given objective does not exist.", t);
			}
			return new CInt(o.getScore(args[1].val()).getScore(), t);
		}

		@Override
		public String getName() {
			return "get_pscore";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "int {objectiveName, player, [scoreboard]} Returns the player's score for the given objective."
					+ " Works for offline players, so the name must be exact. " + DEF_MSG;
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class set_pscore extends SBFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRELengthException.class, CREScoreboardException.class};
		}

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCScoreboard s = assignBoard(4, 3, t, args);
			MCObjective o = s.getObjective(args[0].val());
			if (o == null) {
				throw new CREScoreboardException("The given objective does not exist.", t);
			}
			if (args[1].val().length() > 40){
				throw new CRELengthException("Player names can only be 40 characters.", t);
			}
			o.getScore(args[1].val()).setScore(Static.getInt32(args[2], t));
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_pscore";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{3, 4};
		}

		@Override
		public String docs() {
			return "void {objectiveName, name, int, [scoreboard]} Sets the player's score for the given objective."
					+ " You can set scores for fake players with up to 40 characters. " + DEF_MSG;
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class reset_all_pscores extends SBFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			assignBoard(2, 1, t, args).resetScores(args[0].val());
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "reset_all_pscores";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {player, [scoreboard]} Resets all scores for a player tracked by the given scoreboard."
					+ " This means they will not be show up on any displays. " + DEF_MSG;
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class set_team_options extends SBFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREScoreboardException.class, CREFormatException.class};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCScoreboard s = assignBoard(3, 2, t, args);
			MCTeam team = s.getTeam(args[0].val());
			if (team == null) {
				throw new CREScoreboardException("No team by that name exists.", t);
			}
			if (args[1] instanceof CArray) {
				CArray options = (CArray) args[1];
				if (options.containsKey("friendlyfire")) {
					team.setAllowFriendlyFire(Static.getBoolean(options.get("friendlyfire", t)));
				}
				if (options.containsKey("friendlyinvisibles")) {
					team.setCanSeeFriendlyInvisibles(Static.getBoolean(options.get("friendlyinvisibles", t)));
				}
				if (options.containsKey("nametagvisibility") && Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_8)) {
					MCNameTagVisibility visibility;
					try {
						visibility = MCNameTagVisibility.valueOf(options.get("nametagvisibility", t).val().toUpperCase());
					} catch (IllegalArgumentException iae) {
						throw new CREFormatException("Unknown nametagvisibility: "
								+ options.get("nametagvisibility", t).val(), t);
					}
					team.setNameTagVisibility(visibility);
				}
			} else {
				throw new CREFormatException("Expected arg 2 to be an array.", t);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_team_options";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {teamName, array, [scoreboard]} Sets various options about the team from an array,"
					+ " checking for keys 'friendlyfire', 'friendlyinvisibles' and 'nametagvisibility'. " + DEF_MSG;
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
}
