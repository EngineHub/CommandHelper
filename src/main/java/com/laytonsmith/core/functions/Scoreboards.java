package com.laytonsmith.core.functions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.abstraction.MCObjective;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCScoreboard;
import com.laytonsmith.abstraction.MCTeam;
import com.laytonsmith.abstraction.enums.MCCriteria;
import com.laytonsmith.abstraction.enums.MCDisplaySlot;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.NotInitializedYetException;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.arguments.ArgumentBuilder;
import com.laytonsmith.core.arguments.Generic;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.annotations.FormatString;
import com.laytonsmith.core.natives.interfaces.MObject;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
				e.printStackTrace(System.err);
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
	 * @param board Scoreboard, either from {@link MCServer#getNewScoreboard} or {@link MCPlayer#getScoreboard()}
	 * @param t
	 * @throws ConfigRuntimeException if the cache already contains the board or the id
	 */
	public static void addBoard(String id, MCScoreboard board, Target t) {
		if (isBoard(id)) {
			throw new ScoreboardException("That id is already in use.", t);
		}
		if (isBoard(board)) {
			throw new ScoreboardException("That Scoreboard is already added.", t);
		}
		boards.put(id, board);
	}
	
	/**
	 * Gets a scoreboard from the cache
	 * @param id Name of the scoreboard to look for
	 * @param t
	 * @return the scoreboard with the given id
	 * @throws ConfigRuntimeException if the cache does not contain id
	 */
	public static MCScoreboard getBoard(String id, Target t) {
		if (!isBoard(id)) {
			throw new ScoreboardException("The specified scoreboard does not exist.", t);
		}
		return boards.get(id);
	}
	
	/**
	 * Searches the cache for the given scoreboard, and returns the ID matching it.
	 * @param board The scoreboard to find the ID for
	 * @param t
	 * @return the ID of the scoreboard
	 * @throws ConfigRuntimeException if the cache does not contain the given scoreboard
	 */
	public static String getBoardID(MCScoreboard board, Target t) {
		for (Map.Entry<String, MCScoreboard> e : boards.entrySet()) {
			if (board.equals(e.getValue())) {
				return e.getKey();
			}
		}
		throw new ScoreboardException("The given scoreboard has not been registered yet.", t);
	}
	
	/**
	 * Removes a scoreboard from the cache, without clearing any of its data
	 * @param id The scoreboard to remove
	 * @param t
	 * @throws ConfigRuntimeException if used on MAIN or if the cache does not contain id
	 */
	public static void removeBoard(String id, Target t) {
		if (id.equalsIgnoreCase(MAIN)) {
			throw new ScoreboardException("Cannot remove the main server scoreboard.", t);
		}
		if (!isBoard(id)) {
			throw new ScoreboardException("The specified scoreboard does not exist.", t);
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
	 */
	public static MCScoreboard assignBoard(int numArgsToReadName, int indexOfName, Target t, Mixed... args) {
		if (args.length == numArgsToReadName) {
			return getBoard(args[indexOfName].val(), t);
		}
		return getBoard(MAIN, t);
	}
	
	public static class ScoreboardException extends ConfigRuntimeException {
		public ScoreboardException(String msg, Target t) {
			super(msg, ExceptionType.ScoreboardException, t);
		}
	}
	
	/**
	 * Contains methods that should be the same for most scoreboard functions
	 */
	public static abstract class SBFunction extends AbstractFunction {
		
		/**
		 * @return true
		 */
		public boolean isRestricted() {
			return true;
		}
		/**
		 * @return false
		 */
		public Boolean runAsync() {
			return false;
		}
		/**
		 * @return {@link CHVersion#V3_3_1}
		 */
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		/**
		 * @return Array containing only {@link ExceptionType#ScoreboardException}
		 */
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.ScoreboardException};
		}
	}
	public static class Objective extends MObject {
		public String name;
		public String displayname;
		public MCDisplaySlot slot;
		public boolean modifiable;
		public String criteria;
	}
	
	@api
	public static class get_pscoreboard extends SBFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.ScoreboardException};
		}

		public Construct exec(Target t, Environment environment,
				Mixed... args) throws ConfigRuntimeException {
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

		public String getName() {
			return "get_pscoreboard";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Returns the id of the scoreboard assigned to a player."
					+ " If it is not already cached, it will be added using the player's name."
					+ " Using this method, it should be possible to import scoreboards created by other plugins.";
		}
		
		public Argument returnType() {
			return new Argument("", CString.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "player")
					);
		}
	}
	
	@api
	public static class set_pscoreboard extends SBFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.ScoreboardException};
		}

		public Construct exec(Target t, Environment environment,
				Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = Static.GetPlayer(args[0], t);
			p.setScoreboard(getBoard(args[1].val(), t));
			return new CVoid(t);
		}

		public String getName() {
			return "set_pscoreboard";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "Sets the scoreboard to be used by a player."
					+ " The scoreboard argument is the id of a registered scoreboard.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "player"),
						new Argument("", CString.class, "scoreboard")
					);
		}
	}
	
	@api
	public static class get_scoreboards extends SBFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public Construct exec(Target t, Environment environment,
				Mixed... args) throws ConfigRuntimeException {
			CArray ret = new CArray(t);
			for (String id : boards.keySet()) {
				ret.push(new CString(id, t));
			}
			return ret;
		}

		public String getName() {
			return "get_scoreboards";
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public String docs() {
			return "Returns an array of the registered scoreboard ID's."
					+ " The special scoreboard '"+ MAIN + "' represents the server's main"
					+ " scoreboard which can be managed by the vanilla /scoreboard command.";
		}
		
		public Argument returnType() {
			return new Argument("", CArray.class).setGenerics(new Generic(CString.class));
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.NONE;
		}
	}
	
	@api
	public static class get_objectives extends SBFunction {

		public Construct exec(Target t, Environment environment,
				Mixed... args) throws ConfigRuntimeException {
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
				Objective obj = new Objective();
				obj.name = o.getName();
				obj.displayname = o.getDisplayName();
				obj.slot = o.getDisplaySlot();
				obj.modifiable = o.isModifiable();
				obj.criteria = o.getCriteria();
				ret.push(obj.deconstruct(t));
			}
			return ret;
		}

		public String getName() {
			return "get_objectives";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1, 2};
		}

		public String docs() {
			return "Returns an array of arrays about the objectives"
					+ " on the given scoreboard, which defaults to '" + MAIN + "' if not given."
					+ " If criteria is given, only objectives with that criteria will be returned."
					+ " The arrays contain the keys name, displayname, slot, modifiable, and criteria.";
		}

		public Argument returnType() {
			return new Argument("", CArray.class).setGenerics(new Generic(Objective.class));
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", MCCriteria.class, "criteria").setOptionalDefault(MCCriteria.DUMMY)
					);
		}
	}
	
	public static class Team extends MObject {
		public String name;
		public String displayname;
		public String prefix;
		public String suffix;
		public int size;
		public List<String> players;
		public TeamOptions options;
		
		@typename("TeamOptions")
		public static class TeamOptions extends MObject {
			public boolean friendlyfire;
			public boolean friendlyinvisibles;
		}
	}
	@api
	public static class get_teams extends SBFunction {

		public Construct exec(Target t, Environment environment,
				Mixed... args) throws ConfigRuntimeException {
			MCScoreboard s;
			if (args.length == 0) {
				s = getBoard(MAIN, t);
			} else {
				s = getBoard(args[0].val(), t);
			}
			CArray ret = new CArray(t);
			for (MCTeam team : s.getTeams()) {
				Team to = new Team();
				to.name = team.getName();
				to.displayname = team.getDisplayName();
				to.prefix = team.getPrefix();
				to.suffix = team.getSuffix();
				to.size = team.getSize();
				to.options = new Team.TeamOptions();
				to.options.friendlyfire = team.allowFriendlyFire();
				to.options.friendlyinvisibles = team.canSeeFriendlyInvisibles();
				List<String> pl = new ArrayList<String>();
				for (MCOfflinePlayer ofp : team.getPlayers()) {
					pl.add(ofp.getName());
				}
				to.players = pl;
				ret.push(to.deconstruct(t));
			}
			return ret;
		}

		public String getName() {
			return "get_teams";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Returns an array of arrays about the teams on the given scoreboard,"
					+ " which defaults to '" + MAIN + "' if not given.";
		}
		
		public Argument returnType() {
			return new Argument("", CArray.class).setGenerics(new Generic(Team.class));
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "scoreboard").setOptionalDefault(MAIN)
					);
		}
	}
	
	@api
	public static class create_scoreboard extends SBFunction {

		public Construct exec(Target t, Environment environment,
				Mixed... args) throws ConfigRuntimeException {
			addBoard(args[0].val(), Static.getServer().getNewScoreboard(), t);
			return new CVoid(t);
		}

		public String getName() {
			return "create_scoreboard";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Creates a new scoreboard identified by the given name,"
					+ " and stores it internally for later use. Throws an exception if the name is already in use.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "name")
					);
		}
	}
	
	@api
	public static class create_objective extends SBFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.LengthException, ExceptionType.ScoreboardException};
		}
		
		public Construct exec(Target t, Environment environment,
				Mixed... args) throws ConfigRuntimeException {
			MCScoreboard s = assignBoard(3, 2, t, args);
			String name = args[0].val();
			if (name.length() > 16) {
				throw new Exceptions.LengthException("Objective names should be no more than 16 characters", t);
			}
			MCCriteria criteria = MCCriteria.DUMMY;
			if (args.length == 2) {
				try {
					criteria = MCCriteria.valueOf(args[1].val().toUpperCase());
				} catch (IllegalArgumentException iae) {
					// Leave as dummy
				}
			}
			try {
				s.registerNewObjective(name, criteria.getCriteria());
			} catch (IllegalArgumentException iae) {
				throw new ScoreboardException("An objective by that name already exists.", t);
			}
			return new CVoid(t);
		}

		public String getName() {
			return "create_objective";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		public String docs() {
			return "Adds a new objective to the scoreboard,"
					+ " throwing a ScoreboardException if the name is already in use. You can put anything,"
					+ " but if none of the other values match, 'dummy' will be used."
					+ " Those values which are not 'dummy' are server-managed."
					+ " Throws a LengthException if the name is more than 16 characters. " + DEF_MSG;
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "name"),
						new Argument("", MCCriteria.class, CString.class, "criteria").setOptionalDefault(MCCriteria.DUMMY),
						new Argument("", CString.class, "scoreboard").setOptionalDefault(MAIN)
					);
		}
	}
	
	@api
	public static class create_team extends SBFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.LengthException, ExceptionType.ScoreboardException};
		}
		
		public Construct exec(Target t, Environment environment,
				Mixed... args) throws ConfigRuntimeException {
			MCScoreboard s = assignBoard(2, 1, t, args);
			String name = args[0].val();
			if (name.length() > 16) {
				throw new Exceptions.LengthException("Team names should be no more than 16 characters.", t);
			}
			try {
				s.registerNewTeam(name);
			} catch (IllegalArgumentException iae) {
				throw new ScoreboardException("A team by that name already exists.", t);
			}
			return new CVoid(t);
		}

		public String getName() {
			return "create_team";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Adds a new team to the scoreboard,"
					+ " throws a ScoreboardException if a team already exists with the given name."
					+ " Throws a LengthException if the team name is more than 16 characters. " + DEF_MSG;
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "name"),
						new Argument("", CString.class, "scoreboard").setOptionalDefault(MAIN)
					);
		}
	}
	
	@typename("ObjectiveDisplay")
	public static class ObjectiveDisplay extends MObject {
		public String displayname;
		public MCDisplaySlot slot;
	}
	
	@api
	public static class set_objective_display extends SBFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException, ExceptionType.LengthException,
					ExceptionType.ScoreboardException};
		}

		public Construct exec(Target t, Environment environment,
				Mixed... args) throws ConfigRuntimeException {
			MCScoreboard s = assignBoard(3, 2, t, args);
			MCObjective o = s.getObjective(args[0].val());
			if (o == null) {
				throw new ScoreboardException("No objective by that name exists.", t);
			}
			ObjectiveDisplay dis;
			if (args[1] instanceof CArray) {
				dis = MObject.Construct(ObjectiveDisplay.class, (CArray) args[1], t);
			} else {
				dis = new ObjectiveDisplay();
				dis.displayname = args[1].val();
			}
			if(dis.displayname != null){
				if (dis.displayname.length() > 32) {
					throw new ConfigRuntimeException("Display name can only be 32 characters but was " + dis.displayname.length(),
							ExceptionType.LengthException, t);
				}
				o.setDisplayName(dis.displayname);
			} else {
				o.setDisplayName(o.getName());
			}
			o.setDisplaySlot(dis.slot);
			return new CVoid(t);
		}

		public String getName() {
			return "set_objective_display";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public String docs() {
			return "void {objectiveName, array, [scoreboard] | objectiveName, displayname, [scoreboard]}"
					+ " Sets the display name and/or slot of the given objective. If arg 2 is not an array,"
					+ " it is assumed to be the displayname, otherwise arg 2 should be an array"
					+ " with keys 'displayname' and/or 'slot', affecting their respective properties."
					+ " Null name resets it to the actual name, and null slot removes it from"
					+ " all displays. Slot can be one of: " + StringUtils.Join(MCDisplaySlot.values(), ", ", ", or ")
					+ ". Displayname can be a max of 32 characters, otherwise it throws a LengthException. " + DEF_MSG;
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "objectiveName"),
						new Argument("", CString.class, ObjectiveDisplay.class, "objective")
					);
		}
	}
	
	@typename("TeamDisplay")
	public static class TeamDisplay extends MObject {
		public String displayname;
		public String prefix;
		public String suffix;
	}
	
	@api
	public static class set_team_display extends SBFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.LengthException, ExceptionType.ScoreboardException};
		}

		public Construct exec(Target t, Environment environment,
				Mixed... args) throws ConfigRuntimeException {
			MCScoreboard s = assignBoard(3, 2, t, args);
			MCTeam o = s.getTeam(args[0].val());
			if (o == null) {
				throw new ScoreboardException("No team by that name exists.", t);
			}
			TeamDisplay dis;
			if (args[1] instanceof CArray) {
				dis = MObject.Construct(TeamDisplay.class, (CArray)args[1], t);
			} else {
				dis = new TeamDisplay();
				dis.displayname = args[1].val();
			}
			if (dis.displayname != null) {
				if (dis.displayname.length() > 32) {
					throw new ConfigRuntimeException("Display name can only be 32 characters but was " + dis.displayname.length(),
							ExceptionType.LengthException, t);
				}
				o.setDisplayName(dis.displayname);
			} else {
				o.setDisplayName(o.getName());
			}
			if (dis.prefix != null) {
				if (dis.prefix.length() > 16) {
					throw new ConfigRuntimeException("Prefix can only be 16 characters but was " + dis.prefix.length(),
							ExceptionType.LengthException, t);
				}
				o.setPrefix(dis.prefix);
			} else {
				o.setPrefix("");
			}
			if (dis.suffix != null) {
				if (dis.suffix.length() > 16) {
					throw new ConfigRuntimeException("Suffix can only be 16 characters but was " + dis.suffix.length(),
							ExceptionType.LengthException, t);
				}
				o.setSuffix(dis.suffix);
			} else {
				o.setSuffix("");
			}
			return new CVoid(t);
		}

		public String getName() {
			return "set_team_display";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

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
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "teamName"),
						new Argument("", CString.class, TeamDisplay.class, "info"),
						new Argument("", CString.class, "scoreboard").setOptionalDefault(MAIN)
					);
		}
	}
	
	@api
	public static class team_add_player extends SBFunction {
		
		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.LengthException, ExceptionType.ScoreboardException};
		}

		public Construct exec(Target t, Environment environment,
				Mixed... args) throws ConfigRuntimeException {
			MCScoreboard s = assignBoard(3, 2, t, args);
			MCTeam team = s.getTeam(args[0].val());
			if (team == null) {
				throw new ScoreboardException("No team by that name exists.", t);
			}
			if (args[1].val().length() > 16) {
				throw new Exceptions.LengthException("Player names can only be 16 characters.", t);
			}
			team.addPlayer(Static.getServer().getOfflinePlayer(args[1].val()));
			return new CVoid(t);
		}

		public String getName() {
			return "team_add_player";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public String docs() {
			return "Adds a player to a team, given the team exists."
					+ " Offline players can be added, so the name must be exact. Alternatively,"
					+ " this allows you to add fake players, but names can still only be 16 characters."
					+ " The player will be removed from any other team on the same scoreboard. " + DEF_MSG;
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "teamName"),
						new Argument("", CString.class, "player"),
						new Argument("", CString.class, "scoreboard").setOptionalDefault(MAIN)
					);
		}
	}
	
	@api
	public static class team_remove_player extends SBFunction {

		public Construct exec(Target t, Environment environment,
				Mixed... args) throws ConfigRuntimeException {
			MCScoreboard s = assignBoard(3, 2, t, args);
			MCTeam team = s.getTeam(args[0].val());
			if (team == null) {
				throw new ScoreboardException("No team by that name exists.", t);
			}
			return new CBoolean(team.removePlayer(
					Static.getServer().getOfflinePlayer(args[1].val())), t);
		}

		public String getName() {
			return "team_remove_player";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public String docs() {
			return "Attempts to remove a player from a team,"
					+ " and returns true if successful. If the player was not part of"
					+ " the team, returns false.";
		}
		
		public Argument returnType() {
			return new Argument("", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "teamName"),
						new Argument("", CString.class, "player"),
						new Argument("", CString.class, "scoreboard").setOptionalDefault(MAIN)
					);
		}
	}
	
	@api
	public static class remove_scoreboard extends SBFunction {

		public Construct exec(Target t, Environment environment,
				Mixed... args) throws ConfigRuntimeException {
			String id = args[0].val();
			boolean nullify = true;
			if (args.length == 2) {
				nullify = args[1].primitive(t).castToBoolean();
			}
			if (nullify) {
				MCScoreboard s = getBoard(id, t);
				for (MCOfflinePlayer p : s.getPlayers()) {
					s.resetScores(p);
					if (p.isOnline()) {
						p.getPlayer().setScoreboard(getBoard(MAIN, t));
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
			return new CVoid(t);
		}

		public String getName() {
			return "remove_scoreboard";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Stops tracking the given scoreboard. If nullify is true,"
					+ " all scores, teams, and objectives will be cleared,"
					+ " and all tracked players currently online will be switched to the main scoreboard,"
					+ " essentially removing all references to the board so it can be garbage-collected."
					+ " The scoreboard cannot be \"" + MAIN + "\" because that board never goes away.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "scoreboard").addAnnotation(new FormatString("(?i)(?!" + Pattern.quote(MAIN) + ")")),
						new Argument("", CBoolean.class, "nullify").setOptionalDefault(true)
					);
		}
	}
	
	@api
	public static class remove_objective extends SBFunction {

		public Construct exec(Target t, Environment environment,
				Mixed... args) throws ConfigRuntimeException {
			MCScoreboard s = assignBoard(2, 1, t, args);
			MCObjective o = s.getObjective(args[0].val());
			try {
				o.unregister();
			} catch (NullPointerException npe) {
				throw new ScoreboardException("The objective does not exist.", t);
			} catch (IllegalStateException ise) {
				throw new ScoreboardException("The objective has already been unregistered.", t);
			}
			return new CVoid(t);
		}

		public String getName() {
			return "remove_objective";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Unregisters an objective from the scoreboard. " + DEF_MSG;
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "objectiveName"),
						new Argument("", CString.class, "scoreboard").setOptionalDefault(MAIN)
					);
		}
	}
	
	@api
	public static class remove_team extends SBFunction {

		public Construct exec(Target t, Environment environment,
				Mixed... args) throws ConfigRuntimeException {
			MCScoreboard s = assignBoard(2, 1, t, args);
			MCTeam team = s.getTeam(args[0].val());
			try {
				team.unregister();
			} catch (NullPointerException npe) {
				throw new ScoreboardException("The team does not exist.", t);
			} catch (IllegalStateException ise) {
				throw new ScoreboardException("The team has already been unregistered.", t);
			}
			return new CVoid(t);
		}

		public String getName() {
			return "remove_team";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Unregisters a team from the scoreboard. " + DEF_MSG;
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "teamName"),
						new Argument("", CString.class, "scoreboard").setOptionalDefault(MAIN)
					);
		}
	}
	
	@api
	public static class get_pscore extends SBFunction {

		public Construct exec(Target t, Environment environment,
				Mixed... args) throws ConfigRuntimeException {
			MCScoreboard s = assignBoard(3, 2, t, args);
			MCObjective o = s.getObjective(args[0].val());
			if (o == null) {
				throw new ScoreboardException("The given objective does not exist.", t);
			}
			MCOfflinePlayer ofp = Static.getServer().getOfflinePlayer(args[1].val());
			return new CInt(o.getScore(ofp).getScore(), t);
		}

		public String getName() {
			return "get_pscore";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public String docs() {
			return "Returns the player's score for the given objective."
					+ " Works for offline players, so the name must be exact. " + DEF_MSG;
		}
		
		public Argument returnType() {
			return new Argument("", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "objectiveName"),
						new Argument("", CString.class, "player"),
						new Argument("", CString.class, "scoreboard").setOptionalDefault(MAIN)
					);
		}
	}
	
	@api
	public static class set_pscore extends SBFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.LengthException, ExceptionType.ScoreboardException};
		}
		
		public Construct exec(Target t, Environment environment,
				Mixed... args) throws ConfigRuntimeException {
			MCScoreboard s = assignBoard(4, 3, t, args);
			MCObjective o = s.getObjective(args[0].val());
			if (o == null) {
				throw new ScoreboardException("The given objective does not exist.", t);
			}
			if (args[1].val().length() > 16) {
				throw new Exceptions.LengthException("Player names can only be 16 characters.", t);
			}
			MCOfflinePlayer ofp = Static.getServer().getOfflinePlayer(args[1].val());
			o.getScore(ofp).setScore(args[2].primitive(t).castToInt32(t));
			return new CVoid(t);
		}

		public String getName() {
			return "set_pscore";
		}

		public Integer[] numArgs() {
			return new Integer[]{3, 4};
		}

		public String docs() {
			return "Sets the player's score for the given objective."
					+ " Works for offline players, so the name must be exact. Alternatively,"
					+ " you can set scores for fake players to create custom displays,"
					+ " but the 16 character name limit still applies. " + DEF_MSG;
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "objectiveName"),
						new Argument("", CString.class, "player"),
						new Argument("", CInt.class, "score"),
						new Argument("", CString.class, "scoreboard").setOptionalDefault(MAIN)
					);
		}
	}
	
	@api
	public static class reset_all_pscores extends SBFunction {

		public Construct exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			assignBoard(2, 1, t, args).resetScores(Static.getServer().getOfflinePlayer(args[0].val()));
			return new CVoid(t);
		}

		public String getName() {
			return "reset_all_pscores";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Resets all scores for a player tracked by the given scoreboard."
					+ " This means they will not be show up on any displays. " + DEF_MSG;
		}

		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					PlayerManagement.PLAYER_ARG,
					new Argument("", CString.class, "scoreboard").setOptionalDefaultNull()
				);
		}

	}
	
	@api
	public static class set_team_options extends SBFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.ScoreboardException, ExceptionType.FormatException};
		}
		
		public Construct exec(Target t, Environment environment,
				Mixed... args) throws ConfigRuntimeException {
			MCScoreboard s = assignBoard(3, 2, t, args);
			MCTeam team = s.getTeam(args[0].val());
			if (team == null) {
				throw new ScoreboardException("No team by that name exists.", t);
			}
			if (args[1] instanceof CArray) {
				Team.TeamOptions options = MObject.Construct(Team.TeamOptions.class, (CArray)args[1], t);				
				team.setAllowFriendlyFire(options.friendlyfire);
				team.setCanSeeFriendlyInvisibles(options.friendlyinvisibles);
			} else {
				throw new Exceptions.FormatException("Expected arg 2 to be an array.", t);
			}
			return new CVoid(t);
		}

		public String getName() {
			return "set_team_options";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public String docs() {
			return "Sets various options about the team. " + DEF_MSG;
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "teamName"),
						new Argument("", Team.TeamOptions.class, "options"),
						new Argument("", CString.class, "scoreboard").setOptionalDefault(MAIN)
					);
		}
		
	}
}
