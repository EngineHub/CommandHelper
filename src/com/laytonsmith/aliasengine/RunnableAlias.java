/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine;

import java.util.ArrayList;
import org.bukkit.entity.Player;

/**
 * This class is the bridge between the Bukkit API and the generic Alias Engine
 * @author Layton
 */
public class RunnableAlias {
    static Player player;
    public class Action{
        String type;
        String action;
    }
    String command;
    ArrayList<Action> actions;

    public RunnableAlias(String command, ArrayList<Action> actions){
        this.command = command;
        this.actions = actions;
    }

    public void run(){
        if(player == null){
            System.out.println("Running Alias outside of Bukkit implementation, simulating call:");
            for(Action a : actions){
                if(a.type.equals("die")){
                    System.out.println("Command will not be run, dying with message:");
                    System.out.println(a.action);
                    return;
                }
                if(a.type.equals("msg")){
                    System.out.println("Message to player: " + a.action);
                }
            }
            System.out.println("Player runs command: " + command);
        }
        for(Action a :actions){
            if(a.type.equals("die")){
                player.sendMessage(a.action);
                return;
            }
            if(a.type.equals("msg")){
                player.sendMessage(a.action);
            }
        }
        player.performCommand(command);
    }
}
