

package com.laytonsmith.aliasengine;

import com.laytonsmith.aliasengine.AliasConfig.Construct;
import com.laytonsmith.aliasengine.AliasConfig.ConstructType;
import com.laytonsmith.aliasengine.AliasConfig.Function;
import com.laytonsmith.aliasengine.AliasConfig.Variable;
import com.laytonsmith.aliasengine.AliasConfig.Name;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;

/**
 * This class is the bridge between the Bukkit API and the generic Alias Engine.
 * It essentially accepts an array of GenericTrees, which it then parses. It
 * expects that the variables are already filled in with their values from
 * the user command.
 * @author Layton
 */
public class RunnableAlias {
    Player player;
    String command;
    ArrayList<GenericTree<Construct>> actions;

    public RunnableAlias(String command, ArrayList<GenericTree<Construct>> actions, Player player){
        this.command = command;
        this.actions = actions;
        this.player = player;
    }

    public boolean run(){
        boolean ret = true;
        for(GenericTree t : actions){
            StringBuilder b = new StringBuilder();
            List<GenericTreeNode<Construct>> l = t.build(GenericTreeTraversalOrderEnum.PRE_ORDER);
            try{
                for(GenericTreeNode<Construct> g : l){
                    if(g.data.type.equals("root")){
                        for(GenericTreeNode<Construct> gg : g.getChildren()){
                            b.append(eval(gg)).append(" ");
                        }
                    }
                }
                String cmd = b.toString().trim();
                System.out.println("Running command ----> " + cmd);
                System.out.println("on player " + player);
                if(player != null){
                    player.chat(cmd);
                } else{
                    System.out.println("Player is null, assuming test harness is running");
                }
            } catch(CancelCommandException e){
                if(player == null){
                    System.out.println("Command Cancelled with message: " + e.message);
                } else{
                    player.sendMessage(e.message);
                }
            } finally {
                ret = false;
            }
        }
        return ret;
    }

    private String eval(GenericTreeNode<Construct> c) throws CancelCommandException{
        Construct m = c.getData();
        if(m.ctype == ConstructType.FUNCTION){
            Function f = (Function)m;
            ArrayList<String> args = new ArrayList<String>();
//            for(GenericTreeNode<Construct> c2 : c.getChildren()){
//                args.add(eval(c2));
//            }
            if(f.name == Name.DIE){
                throw new CancelCommandException(eval(c.getChildren().get(0)));
            } else if(f.name == Name.DATA_VALUES){
                return Data_Values.val(eval(c.getChildren().get(0)));
            } else if(f.name == Name.PLAYER){
                if(player != null){
                    return player.getName();
                } else {
                    return "Player";
                }
            } else if(f.name == Name.MSG){
                if(player != null){
                    player.sendMessage(eval(c.getChildren().get(0)));
                } else {
                    System.out.println("Sending message to player: " + eval(c.getChildren().get(0)));
                }
            } else if(f.name == Name.EQUALS){
                if(eval(c.getChildren().get(0)).equals(eval(c.getChildren().get(1)))){
                    return "1";
                } else {
                    return "0";
                }
            } else if(f.name == Name.IF){
                if(eval(c.getChildren().get(0)).equals("0")){
                    return eval(c.getChildren().get(2));
                } else{
                    return eval(c.getChildren().get(1));
                }
            }
            //It's feasible we don't return anything, so return a blank screen
            return "";
        } else if(m.ctype == ConstructType.VARIABLE){
            return ((Variable)m).def;
        } else{
            return m.value;
        }
    }
}
