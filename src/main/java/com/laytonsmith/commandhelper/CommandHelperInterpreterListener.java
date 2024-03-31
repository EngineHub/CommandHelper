package com.laytonsmith.commandhelper;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import com.laytonsmith.abstraction.enums.MCChatColor;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.TokenStream;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.environments.RuntimeMode;
import com.laytonsmith.core.environments.StaticRuntimeEnv;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 *
 *
 */
public class CommandHelperInterpreterListener implements Listener {

	private final Set<String> interpreterMode = Collections.synchronizedSet(new HashSet<>());
	private final Map<String, String> multilineMode = new HashMap<>();
	private final Map<String, Environment> interpreterEnvs = new HashMap<>();
	private final Map<String, StaticAnalysis> interpreterSAs = new HashMap<>();

	public boolean isInInterpreterMode(String player) {
		return (interpreterMode.contains(player));
	}

	public CommandHelperInterpreterListener() {
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChat(final AsyncPlayerChatEvent event) {
		if(interpreterMode.contains(event.getPlayer().getName())) {
			final MCPlayer p = new BukkitMCPlayer(event.getPlayer());
			event.setCancelled(true);
			StaticLayer.SetFutureRunnable(null, 0, () -> textLine(p, event.getMessage()));
		}

	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent event) {
		interpreterMode.remove(event.getPlayer().getName());
		multilineMode.remove(event.getPlayer().getName());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if(event.isCancelled()) {
			return;
		}
		if(interpreterMode.contains(event.getPlayer().getName())) {
			MCPlayer p = new BukkitMCPlayer(event.getPlayer());
			textLine(p, event.getMessage());
			event.setCancelled(true);
		}
	}

	public void textLine(MCPlayer p, String line) {
		switch(line) {
			case "-":
				//Exit interpreter mode
				interpreterMode.remove(p.getName());
				interpreterEnvs.remove(p.getName());
				interpreterSAs.remove(p.getName());
				Static.SendMessage(p, MCChatColor.YELLOW + "Now exiting interpreter mode");
				break;
			case ">>>":
				//Start multiline mode
				if(multilineMode.containsKey(p.getName())) {
					Static.SendMessage(p, MCChatColor.RED + "You are already in multiline mode!");
				} else {
					multilineMode.put(p.getName(), "");
					Static.SendMessage(p, MCChatColor.YELLOW + "You are now in multiline mode. Type <<< on a line by itself to execute.");
					Static.SendMessage(p, ":" + MCChatColor.GRAY + ">>>");
				}
				break;
			case "<<<":
				//Execute multiline
				Static.SendMessage(p, ":" + MCChatColor.GRAY + "<<<");
				String script = multilineMode.get(p.getName());
				multilineMode.remove(p.getName());
				try {
					execute(script, p);
				} catch (ConfigCompileException e) {
					Static.SendMessage(p, MCChatColor.RED + e.getMessage() + ":" + e.getLineNum());
				} catch (ConfigCompileGroupException ex) {
					for(ConfigCompileException e : ex.getList()) {
						Static.SendMessage(p, MCChatColor.RED + e.getMessage() + ":" + e.getLineNum());
					}
				}
				break;
			case "~":
				if(interpreterEnvs.containsKey(p.getName())) {
					Environment env = interpreterEnvs.get(p.getName());
					Static.SendMessage(p, MCChatColor.GRAY + "Environment cleared.");
					env.getEnv(GlobalEnv.class).GetProcs().clear();
					env.getEnv(GlobalEnv.class).GetVarList().clear();
					for(Thread t : env.getEnv(StaticRuntimeEnv.class).GetDaemonManager().getActiveThreads()) {
						t.interrupt();
					}
					env.getEnv(StaticRuntimeEnv.class).getExecutionQueue().stopAll();
					env.getEnv(StaticRuntimeEnv.class).getIncludeCache().clear();
				}
				if(interpreterSAs.containsKey(p.getName())) {
					interpreterSAs.remove(p.getName());
				}
				break;
			default:
				if(multilineMode.containsKey(p.getName())) {
					//Queue multiline
					multilineMode.put(p.getName(), multilineMode.get(p.getName()) + line + "\n");
					Static.SendMessage(p, ":" + MCChatColor.GRAY + line);
				} else {
					try {
						//Execute single line
						execute(line, p);
					} catch (ConfigCompileException ex) {
						Static.SendMessage(p, MCChatColor.RED + ex.getMessage());
					} catch (ConfigCompileGroupException e) {
						for(ConfigCompileException ex : e.getList()) {
							Static.SendMessage(p, MCChatColor.RED + ex.getMessage());
						}
					}
				}	break;
		}
	}

	/**
	 * Executes the given script as the given player in a new environment. Exceptions in script runtime are printed
	 * to the player.
	 * If the player is in interpreter mode, then this mode is removed until the script execution terminates.
	 * @param script - The script to execute.
	 * @param p - The player to execute the script as.
	 * @throws ConfigCompileException If compilation fails.
	 * @throws ConfigCompileGroupException Container for multiple {@link ConfigCompileException}s.
	 */
	public void execute(String script, final MCPlayer p) throws ConfigCompileException, ConfigCompileGroupException {
		TokenStream stream = MethodScriptCompiler.lex(script, null, new File("Interpreter"), true);
		Environment env = interpreterEnvs.computeIfAbsent(p.getName(), (player) -> {
			StaticRuntimeEnv staticRuntimeEnv = Static.getAliasCore().getStaticRuntimeEnv();
			GlobalEnv gEnv = new GlobalEnv(CommandHelperFileLocations.getDefault().getConfigDirectory(),
					EnumSet.of(RuntimeMode.EMBEDDED, RuntimeMode.INTERPRETER));
			gEnv.SetDynamicScriptingMode(true);
			CommandHelperEnvironment cEnv = new CommandHelperEnvironment();
			cEnv.SetPlayer(p);
			CompilerEnvironment compilerEnv = new CompilerEnvironment();
			compilerEnv.setLogCompilerWarnings(false);
			Environment e = Environment.createEnvironment(gEnv, staticRuntimeEnv, cEnv, compilerEnv);
			return e;
		});
		ParseTree tree = MethodScriptCompiler.compile(stream, env, env.getEnvClasses(), interpreterSAs.computeIfAbsent(p.getName(), (player) -> {
			StaticAnalysis sa = new StaticAnalysis(true);
			sa.setLocalDisabled(true);
			return sa;
		}));
		if(tree.getChildren().size() == 1 && tree.getChildAt(0).getData() instanceof IVariable ivar) {
			Mixed i = env.getEnv(GlobalEnv.class).GetVarList()
					.get(ivar.getVariableName(), ivar.getTarget(), env).ival();
			Static.SendMessage(p, ":" + MCChatColor.GREEN + i.val());
			return;
		}
		final boolean isInterpeterMode = interpreterMode.remove(p.getName());
		try {
			env.getEnv(StaticRuntimeEnv.class).getIncludeCache().executeAutoIncludes(env, null);
			MethodScriptCompiler.execute(tree, env, output -> {
				output = output.trim();
				if(output.isEmpty()) {
					Static.SendMessage(p, ":");
				} else {
					if(output.startsWith("/")) {
						//Run the command
						Static.SendMessage(p, ":" + MCChatColor.YELLOW + output);
						p.chat(output);
					} else {
						//output the results
						Static.SendMessage(p, ":" + MCChatColor.GREEN + output);
					}
				}
				if(isInterpeterMode) {
					interpreterMode.add(p.getName());
				} else {
					Static.SendMessage(p, MCChatColor.YELLOW + "No longer in interpreter mode.");
				}
			}, null);
			return;
		} catch (CancelCommandException e) {
		} catch (ConfigRuntimeException e) {
			ConfigRuntimeException.HandleUncaughtException(e, env);
			Static.SendMessage(p, MCChatColor.RED + e.toString());
		} catch (Exception e) {
			Static.SendMessage(p, MCChatColor.RED + e.toString());
			Static.getLogger().log(Level.SEVERE, null, e);
		}
		if(isInterpeterMode) {
			interpreterMode.add(p.getName());
		}
	}

	public void startInterpret(String playername) {
		interpreterMode.add(playername);
	}
}
