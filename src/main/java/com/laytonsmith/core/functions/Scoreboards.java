package com.laytonsmith.core.functions;

import java.util.Set;

import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.abstraction.MCObjective;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCScoreboard;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.MCTeam;
import com.laytonsmith.abstraction.enums.MCCriteria;
import com.laytonsmith.abstraction.enums.MCDisplaySlot;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;

/**
 * 
 * @author jb_aero
 */
public class Scoreboards {
	public static String docs() {
		return "A class of functions for manipulating the server scoreboard.";
	}
	
	public static abstract class SBFunction extends AbstractFunction {

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
	
	@api
	public static class get_objectives extends SBFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCScoreboard s = Static.getServer().getMainScoreboard();
			Set<MCObjective> os;
			if (args.length == 1) {
				MCCriteria crit;
				try {
					crit = MCCriteria.valueOf(args[0].val());
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
				obj.set("displayName", new CString(o.getDisplayName(), t), t);
				Construct slot = new CNull(t);
				if (o.getDisplaySlot() != null) {
					slot = new CString(o.getDisplaySlot().name(), t);
				}
				obj.set("slot", slot, t);
				obj.set("modifiable", new CBoolean(o.isModifiable(), t), t);
				obj.set("criteria", new CString(o.getCriteria(), t), t);
				ret.push(obj);
			}
			return ret;
		}

		public String getName() {
			return "get_objectives";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "array {[criteria]} Returns an array of arrays about the objectives in the current scoreboard."
					+ " If criteria is given, only objectives with that criteria will be returned."
					+ " The arrays contain the keys name, displayname, slot, modifiable, and criteria.";
		}
	}
	
	@api
	public static class get_teams extends SBFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			Set<MCTeam> ts = Static.getServer().getMainScoreboard().getTeams();
			CArray ret = new CArray(t);
			for (MCTeam team : ts) {
				CArray to = CArray.GetAssociativeArray(t);
				to.set("name", new CString(team.getName(), t), t);
				to.set("displayname", new CString(team.getDisplayName(), t), t);
				to.set("prefix", new CString(team.getPrefix(), t), t);
				to.set("suffix", new CString(team.getSuffix(), t), t);
				to.set("size", new CInt(team.getSize(), t), t);
				CArray pl = new CArray(t);
				for (MCOfflinePlayer ofp : team.getPlayers()) {
					pl.push(new CString(ofp.getName(), t));
				}
				to.set("players", pl, t);
				ret.push(to);
			}
			return ret;
		}

		public String getName() {
			return "get_teams";
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public String docs() {
			return "array {} Returns an array of arrays about the teams on the current scoreboard."
					+ " The arrays contain the keys name, displayname, prefix, suffix, size, and players.";
		}
	}
	
	@api
	public static class create_objective extends SBFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.ScoreboardException};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCScoreboard s = Static.getServer().getMainScoreboard();
			String name = args[0].val();
			MCCriteria criteria = MCCriteria.DUMMY;
			if (args.length == 2) {
				try {
					criteria = MCCriteria.valueOf(args[1].val().toUpperCase());
				} catch (IllegalArgumentException iae) {

				}
			}
			try {
				s.registerNewObjective(name, criteria.getCriteria());
			} catch (IllegalArgumentException iae) {
				throw new ConfigRuntimeException("An objective by that name already exists.",
						ExceptionType.ScoreboardException, t);
			}
			return new CVoid(t);
		}

		public String getName() {
			return "create_objective";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "void {name[, criteria]} Adds a new objective to the scoreboard,"
					+ " throwing an exception if the name is already in use."
					+ " The vanilla criteria names are " + StringUtils.Join(MCCriteria.values(), ", ", ", and ")
					+ ". You can put anything, but if none of the other values match,"
					+ " 'dummy' will be used. Those values which are not 'dummy' are server-managed.";
		}
	}
	
	@api
	public static class create_team extends SBFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.ScoreboardException};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCScoreboard s = Static.getServer().getMainScoreboard();
			String name = args[0].val();
			try {
				s.registerNewTeam(name);
			} catch (IllegalArgumentException iae) {
				throw new ConfigRuntimeException("A team by that name already exists.",
						ExceptionType.ScoreboardException, t);
			}
			return new CVoid(t);
		}

		public String getName() {
			return "create_team";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "void {name} Adds a new team to the scoreboard,"
					+ " throws an exception if a team already exists with the given name.";
		}
	}
	
	@api
	public static class set_objective_display extends SBFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException, ExceptionType.LengthException,
					ExceptionType.ScoreboardException};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCScoreboard s = Static.getServer().getMainScoreboard();
			MCObjective o = s.getObjective(args[0].val());
			if (o == null) {
				throw new ConfigRuntimeException("No objective by that name exists.",
						ExceptionType.ScoreboardException, t);
			}
			CArray dis = new CArray(t);
			if (args[1] instanceof CArray) {
				dis = (CArray) args[1];
			} else {
				dis.set("name", args[1], t);
			}
			if (dis.containsKey("slot")) {
				MCDisplaySlot slot;
				if (dis.get("slot", t) instanceof CNull) {
					slot = null;
				} else {
					try {
						slot = MCDisplaySlot.valueOf(dis.get("slot", t).val().toUpperCase());
					} catch (IllegalArgumentException iae) {
						throw new Exceptions.FormatException("Unknown DisplaySlot", t);
					}
				}
				o.setDisplaySlot(slot);
			}
			if (dis.containsKey("name")) {
				String dname;
				if (dis.get("name", t) instanceof CNull) {
					dname = o.getName();
				} else {
					dname = dis.get("name", t).val();
				}
				if (dname.length() > 32) {
					throw new ConfigRuntimeException("Display name can only be 32 characters but was " + dname.length(),
							ExceptionType.LengthException, t);
				}
				o.setDisplayName(dname);
			}
			return new CVoid(t);
		}

		public String getName() {
			return "set_objective_display";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "void {objectiveName, array | objectiveName, displayName} Sets the display"
					+ " name and/or slot of the given objective. If arg 2 is not an array,"
					+ " it is assumed to be the displayname, otherwise arg 2 should be an array"
					+ " with keys 'name' and/or 'slot', affecting their respective properties."
					+ " Null name resets it to the actual name, and null slot removes it from"
					+ " all displays. Slot can be one of: "
					+ StringUtils.Join(MCDisplaySlot.values(), ", ", ", or ")
					+ ". Displayname can be a max of 32 characters.";
		}
	}
	
	@api
	public static class set_team_display extends SBFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException, ExceptionType.LengthException,
					ExceptionType.ScoreboardException};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCScoreboard s = Static.getServer().getMainScoreboard();
			MCTeam o = s.getTeam(args[0].val());
			if (o == null) {
				throw new ConfigRuntimeException("No team by that name exists.",
						ExceptionType.ScoreboardException, t);
			}
			CArray dis = new CArray(t);
			if (args[1] instanceof CArray) {
				dis = (CArray) args[1];
			} else {
				dis.set("name", args[1], t);
			}
			if (dis.containsKey("name")) {
				String dname;
				if (dis.get("name", t) instanceof CNull) {
					dname = o.getName();
				} else {
					dname = dis.get("name", t).val();
				}
				if (dname.length() > 32) {
					throw new ConfigRuntimeException("Display name can only be 32 characters but was " + dname.length(),
							ExceptionType.LengthException, t);
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
					throw new ConfigRuntimeException("Prefix can only be 16 characters but was " + prefix.length(),
							ExceptionType.LengthException, t);
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
					throw new ConfigRuntimeException("Suffix can only be 16 characters but was " + suffix.length(),
							ExceptionType.LengthException, t);
				}
				o.setSuffix(suffix);
			}
			return new CVoid(t);
		}

		public String getName() {
			return "set_team_display";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "void {teamName, array | teamName, displayName} Sets the display"
					+ " name, prefix, and/or suffix of the given team. If arg 2 is not an array,"
					+ " it is assumed to be the displayname, otherwise arg 2 should be an array"
					+ " with keys 'name', 'prefix', and/or 'suffix', affecting their respective properties."
					+ " Null name resets it to the actual name, and null prefix or suffix removes it from"
					+ " all displays. Slot can be one of: "
					+ StringUtils.Join(MCDisplaySlot.values(), ", ", ", or ")
					+ ". Displayname can be a max of 32 characters, prefix and suffix can only be 16.";
		}
	}
	
	@api
	public static class team_add_player extends SBFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.ScoreboardException};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCServer s = Static.getServer();
			MCTeam team = s.getMainScoreboard().getTeam(args[0].val());
			if (team == null) {
				throw new ConfigRuntimeException("No team by that name exists.",
						ExceptionType.ScoreboardException, t);
			}
			team.addPlayer(s.getOfflinePlayer(args[1].val()));
			return new CVoid(t);
		}

		public String getName() {
			return "team_add_player";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "void {teamName, player} Adds a player to a team, given the team exists."
					+ " Offline players can be added, so the name must be exact."
					+ " The player will be removed from any other team on the same scoreboard.";
		}
	}
	
	@api
	public static class team_remove_player extends SBFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.ScoreboardException};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCServer s = Static.getServer();
			MCTeam team = s.getMainScoreboard().getTeam(args[0].val());
			if (team == null) {
				throw new ConfigRuntimeException("No team by that name exists.",
						ExceptionType.ScoreboardException, t);
			}
			return new CBoolean(team.removePlayer(s.getOfflinePlayer(args[1].val())), t);
		}

		public String getName() {
			return "team_remove_player";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "boolean {teamname, player} Attempts to remove a player from a team,"
					+ " and returns true if successful. If the player was not part of"
					+ " the team, returns false.";
		}
		
	}
	
	@api
	public static class remove_objective extends SBFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.ScoreboardException};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCObjective o = Static.getServer().getMainScoreboard().getObjective(args[0].val());
			try {
				o.unregister();
			} catch (NullPointerException npe) {
				throw new ConfigRuntimeException("The objective does not exist.",
						ExceptionType.ScoreboardException, t);
			} catch (IllegalStateException ise) {
				throw new ConfigRuntimeException("The objective has already been unregistered.",
						ExceptionType.ScoreboardException, t);
			}
			return new CVoid(t);
		}

		public String getName() {
			return "remove_objective";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "void {objectivename} Unregisters an objective from the scoreboard.";
		}
	}
	
	@api
	public static class remove_team extends SBFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.ScoreboardException};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCTeam team = Static.getServer().getMainScoreboard().getTeam(args[0].val());
			try {
				team.unregister();
			} catch (NullPointerException npe) {
				throw new ConfigRuntimeException("The team does not exist.",
						ExceptionType.ScoreboardException, t);
			} catch (IllegalStateException ise) {
				throw new ConfigRuntimeException("The team has already been unregistered.",
						ExceptionType.ScoreboardException, t);
			}
			return new CVoid(t);
		}

		public String getName() {
			return "remove_team";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "void {teamname} Unregisters a team from the scoreboard.";
		}
	}
	
	@api
	public static class get_score extends SBFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.ScoreboardException};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCObjective o = Static.getServer().getMainScoreboard().getObjective(args[0].val());
			if (o == null) {
				throw new ConfigRuntimeException("The given objective does not exist.",
						ExceptionType.ScoreboardException, t);
			}
			MCOfflinePlayer ofp = Static.getServer().getOfflinePlayer(args[1].val());
			return new CInt(o.getScore(ofp).getScore(), t);
		}

		public String getName() {
			return "get_score";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "int {objectiveName, player} Returns the player's score for the given objective."
					+ " Works for offline players, so the name must be exact.";
		}
	}
	
	@api
	public static class set_score extends SBFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.ScoreboardException};
		}

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCObjective o = Static.getServer().getMainScoreboard().getObjective(args[0].val());
			if (o == null) {
				throw new ConfigRuntimeException("The given objective does not exist.",
						ExceptionType.ScoreboardException, t);
			}
			MCOfflinePlayer ofp = Static.getServer().getOfflinePlayer(args[1].val());
			o.getScore(ofp).setScore(Static.getInt32(args[2], t));
			return new CVoid(t);
		}

		public String getName() {
			return "set_score";
		}

		public Integer[] numArgs() {
			return new Integer[]{3};
		}

		public String docs() {
			return "void {objectiveName, player, int} Sets the player's score for the given objective."
					+ " Works for offline players, so the name must be exact.";
		}
	}
}
