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
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.taskmanager.TaskManagerImpl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.util.Collections;
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
	private final CommandHelperPlugin plugin;
	private Map<String, String> multilineMode = new HashMap<>();

	public boolean isInInterpreterMode(String player) {
		return (interpreterMode.contains(player));
	}

	public CommandHelperInterpreterListener(CommandHelperPlugin plugin) {
		this.plugin = plugin;
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
		if(line.equals("-")) {
			//Exit interpreter mode
			interpreterMode.remove(p.getName());
			Static.SendMessage(p, MCChatColor.YELLOW + "Now exiting interpreter mode");
		} else if(line.equals(">>>")) {
			//Start multiline mode
			if(multilineMode.containsKey(p.getName())) {
				Static.SendMessage(p, MCChatColor.RED + "You are already in multiline mode!");
			} else {
				multilineMode.put(p.getName(), "");
				Static.SendMessage(p, MCChatColor.YELLOW + "You are now in multiline mode. Type <<< on a line by itself to execute.");
				Static.SendMessage(p, ":" + MCChatColor.GRAY + ">>>");
			}
		} else if(line.equals("<<<")) {
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
		} else {
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
			}
		}
	}

	public void execute(String script, final MCPlayer p) throws ConfigCompileException, ConfigCompileGroupException {
		TokenStream stream = MethodScriptCompiler.lex(script, null, new File("Interpreter"), true);
		interpreterMode.remove(p.getName());
		GlobalEnv gEnv = new GlobalEnv(plugin.executionQueue, plugin.profiler, plugin.persistenceNetwork,
				CommandHelperFileLocations.getDefault().getConfigDirectory(),
				plugin.profiles, new TaskManagerImpl());
		gEnv.SetDynamicScriptingMode(true);
		CommandHelperEnvironment cEnv = new CommandHelperEnvironment();
		cEnv.SetPlayer(p);
		CompilerEnvironment compilerEnv = new CompilerEnvironment();
		compilerEnv.setLogCompilerWarnings(false);
		Environment env = Environment.createEnvironment(gEnv, cEnv, compilerEnv);
		ParseTree tree = MethodScriptCompiler.compile(stream, env, env.getEnvClasses());
		try {
			MethodScriptCompiler.registerAutoIncludes(env, null);
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
				interpreterMode.add(p.getName());
			}, null);
		} catch (CancelCommandException e) {
			interpreterMode.add(p.getName());
		} catch (ConfigRuntimeException e) {
			ConfigRuntimeException.HandleUncaughtException(e, env);
			Static.SendMessage(p, MCChatColor.RED + e.toString());
			interpreterMode.add(p.getName());
		} catch (Exception e) {
			Static.SendMessage(p, MCChatColor.RED + e.toString());
			Static.getLogger().log(Level.SEVERE, null, e);
			interpreterMode.add(p.getName());
		}
	}

	public void startInterpret(String playername) {
		interpreterMode.add(playername);
	}
}
