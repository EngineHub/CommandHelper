/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.commandhelper;

import com.laytonsmith.abstraction.MCChatColor;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.io.File;
import java.util.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author Layton
 */
public class CommandHelperInterpreterListener implements Listener {

    Set<String> interpreterMode = new HashSet<String>();
    Map<String, String> multilineMode = new HashMap<String, String>();
    
    public boolean isInInterpreterMode(MCPlayer p){
        return (interpreterMode.contains(p.getName()));
    }

    @EventHandler(priority= EventPriority.LOWEST)
    public void onPlayerChat(PlayerChatEvent event) {
        if (interpreterMode.contains(event.getPlayer().getName())) {
            MCPlayer p = new BukkitMCPlayer(event.getPlayer());
            textLine(p, event.getMessage());
            event.setCancelled(true);
        }

    }

    @EventHandler(priority= EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        interpreterMode.remove(event.getPlayer().getName());
        multilineMode.remove(event.getPlayer().getName());
    }

    @EventHandler(priority= EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (interpreterMode.contains(event.getPlayer().getName())) {
            MCPlayer p = new BukkitMCPlayer(event.getPlayer());
            textLine(p, event.getMessage());
            event.setCancelled(true);
        }
    }

    public void textLine(MCPlayer p, String line) {
        if (line.equals("-")) {
            //Exit interpreter mode
            interpreterMode.remove(p.getName());
            Static.SendMessage(p, MCChatColor.YELLOW + "Now exiting interpreter mode");
        } else if (line.equals(">>>")) {
            //Start multiline mode
            if (multilineMode.containsKey(p.getName())) {
                Static.SendMessage(p, MCChatColor.RED + "You are already in multiline mode!");
            } else {
                multilineMode.put(p.getName(), "");
                Static.SendMessage(p, MCChatColor.YELLOW + "You are now in multiline mode. Type <<< on a line by itself to execute.");
                Static.SendMessage(p, ":" + MCChatColor.GRAY + ">>>");
            }
        } else if (line.equals("<<<")) {
            //Execute multiline
            Static.SendMessage(p, ":" + MCChatColor.GRAY + "<<<");
            String script = multilineMode.get(p.getName());
            multilineMode.remove(p.getName());
            try {
                execute(script, p);
            } catch (ConfigCompileException e) {
                Static.SendMessage(p, MCChatColor.RED + e.getMessage() + ":" + e.getLineNum());
            }
        } else {
            if (multilineMode.containsKey(p.getName())) {
                //Queue multiline
                multilineMode.put(p.getName(), multilineMode.get(p.getName()) + line + "\n");
                Static.SendMessage(p, ":" + MCChatColor.GRAY + line);
            } else {
                try {
                    //Execute single line
                    execute(line, p);
                } catch (ConfigCompileException ex) {
                    Static.SendMessage(p, MCChatColor.RED + ex.getMessage());
                }
            }
        }
    }

    public void reload() {
    }

    public void execute(String script, final MCPlayer p) throws ConfigCompileException {
        List<Token> stream = MScriptCompiler.lex("include('plugins/CommandHelper/auto_include.ms')\n" + script, new File("Interpreter"));
        GenericTreeNode tree = MScriptCompiler.compile(stream);
        interpreterMode.remove(p.getName());
        Env env = new Env();
        env.SetPlayer(p);
        try {
            MScriptCompiler.execute(tree, env, new MScriptComplete() {

                public void done(String output) {
                    output = output.trim();
                    if (output.equals("")) {
                        Static.SendMessage(p, ":");
                    } else {
                        if (output.startsWith("/")) {
                            //Run the command
                            Static.SendMessage(p, ":" + MCChatColor.YELLOW + output);
                            p.chat(output);
                        } else {
                            //output the results
                            Static.SendMessage(p, ":" + MCChatColor.GREEN + output);
                        }
                    }
                    interpreterMode.add(p.getName());
                }
            }, null);
        } catch (CancelCommandException e) {
            interpreterMode.add(p.getName());
        } catch(Exception e){
            Static.SendMessage(p, MCChatColor.RED + e.toString());
            e.printStackTrace();
            interpreterMode.add(p.getName());
        }
    }

    public void startInterpret(String playername) {
        interpreterMode.add(playername);
    }
}
