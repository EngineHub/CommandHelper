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
import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.arguments.ArgumentBuilder;
import com.laytonsmith.core.arguments.Generic;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.interfaces.MObject;
import java.util.ArrayList;
import java.util.List;

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
	
	public static class Objective extends MObject {
		public String name;
		public String displayName;
		public MCDisplaySlot slot;
		public boolean modifiable;
		public String criteria;
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
				Objective obj = new Objective();
				obj.name = o.getName();
				obj.displayName = o.getDisplayName();
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
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Returns an array of arrays about the objectives in the current scoreboard."
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
		public String displayName;
		public String prefix;
		public String suffix;
		public int size;
		public List<String> players;
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
				Team to = new Team();
				to.name = team.getName();
				to.displayName = team.getDisplayName();
				to.prefix = team.getPrefix();
				to.suffix = team.getSuffix();
				to.size = team.getSize();
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
			return new Integer[]{0};
		}

		public String docs() {
			return "Returns an array of Teams, which describes the teams on the current scoreboard.";
		}
		
		public Argument returnType() {
			return new Argument("", CArray.class).setGenerics(new Generic(Team.class));
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.NONE;
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
					//Ignored
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
			return "Adds a new objective to the scoreboard,"
					+ " throwing an exception if the name is already in use."
					+ " You can put anything for the criteria, but only the Criteria enum values are server"
					+ " supported.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "name"),
						new Argument("", MCCriteria.class, CString.class, "criteria").setOptionalDefault(MCCriteria.DUMMY)
					);
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
			return "Adds a new team to the scoreboard,"
					+ " throws an exception if a team already exists with the given name.";
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
	
	@typename("ObjectiveDisplay")
	public static class ObjectiveDisplay extends MObject {
		public String name;
		public MCDisplaySlot slot;
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
			ObjectiveDisplay dis;
			if (args[1] instanceof CArray) {
				dis = MObject.Construct(ObjectiveDisplay.class, (CArray) args[1], t);
			} else {
				dis = new ObjectiveDisplay();
				dis.name = args[1].val();
			}
			if(dis.name != null){
				if (dis.name.length() > 32) {
					throw new ConfigRuntimeException("Display name can only be 32 characters but was " + dis.name.length(),
							ExceptionType.LengthException, t);
				}
				o.setDisplayName(dis.name);
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
		public String name;
		public String prefix;
		public String suffix;
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
			TeamDisplay dis;
			if (args[1] instanceof CArray) {
				dis = MObject.Construct(TeamDisplay.class, (CArray)args[1], t);
			} else {
				dis = new TeamDisplay();
				dis.name = args[1].val();
			}
			if (dis.name != null) {
				if (dis.name.length() > 32) {
					throw new ConfigRuntimeException("Display name can only be 32 characters but was " + dis.name.length(),
							ExceptionType.LengthException, t);
				}
				o.setDisplayName(dis.name);
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
			return new Integer[]{2};
		}

		public String docs() {
			return "void {teamName, array | teamName, displayName} Sets the display"
					+ " name, prefix, and/or suffix of the given team. If arg 2 is not an array,"
					+ " it is assumed to be the displayname, otherwise arg 2 should be an array"
					+ " with keys 'name', 'prefix', and/or 'suffix', affecting their respective properties."
					+ " Null name resets it to the actual name, and null prefix or suffix removes it from"
					+ " all displays. name can be a max of 32 characters, prefix and suffix can only be 16.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "teamName"),
						new Argument("", CString.class, TeamDisplay.class, "info")
					);
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
			return "Adds a player to a team, given the team exists."
					+ " Offline players can be added, so the name must be exact."
					+ " The player will be removed from any other team on the same scoreboard.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "teamName"),
						new Argument("", CString.class, "player")
					);
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
						new Argument("", CString.class, "player")
					);
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
			return "Unregisters an objective from the scoreboard.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "objectiveName")
					);
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
			return "Unregisters a team from the scoreboard.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "teamName")
					);
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
			return "Returns the player's score for the given objective."
					+ " Works for offline players, so the name must be exact.";
		}
		
		public Argument returnType() {
			return new Argument("", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "objectiveName"),
						new Argument("", CString.class, "player")
					);
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
			o.getScore(ofp).setScore(args[2].primitive(t).castToInt32(t));
			return new CVoid(t);
		}

		public String getName() {
			return "set_score";
		}

		public Integer[] numArgs() {
			return new Integer[]{3};
		}

		public String docs() {
			return "Sets the player's score for the given objective."
					+ " Works for offline players, so the name must be exact.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "objectiveName"),
						new Argument("", CString.class, "player"),
						new Argument("", CInt.class, "score")
					);
		}
	}
}
