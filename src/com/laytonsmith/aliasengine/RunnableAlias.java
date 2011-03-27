

package com.laytonsmith.aliasengine;


import com.laytonsmith.aliasengine.AliasConfig.Function;
import com.laytonsmith.aliasengine.AliasConfig.Variable;
import com.laytonsmith.aliasengine.functions.Construct;
import com.laytonsmith.aliasengine.functions.Construct.ConstructType;
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

    String performAs = null;
    boolean inPerformAs = false;

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
            if(f.name == FunctionName.DIE){
                throw new CancelCommandException(eval(c.getChildren().get(0)));
            } else if(f.name == FunctionName.DATA_VALUES){
                return Data_Values.val(eval(c.getChildren().get(0)));
            } else if(f.name == FunctionName.PLAYER){
                if(player != null){
                    return player.getName();
                } else {
                    return "Player";
                }
            } else if(f.name == FunctionName.MSG){
                if(player != null){
                    player.sendMessage(eval(c.getChildren().get(0)));
                } else {
                    System.out.println("Sending message to player: " + eval(c.getChildren().get(0)));
                }
            } else if(f.name == FunctionName.EQUALS){
                if(eval(c.getChildren().get(0)).equals(eval(c.getChildren().get(1)))){
                    return "1";
                } else {
                    return "0";
                }
            } else if(f.name == FunctionName.IF){
                if(eval(c.getChildren().get(0)).equals("0") ||
                        eval(c.getChildren().get(0)).equals("false")){
                    return eval(c.getChildren().get(2));
                } else{
                    return eval(c.getChildren().get(1));
                }
            } else if(f.name == FunctionName.CONCAT){
                StringBuilder b = new StringBuilder();
                for(int i = 0; i < c.getChildren().size(); i++){
                    b.append(eval(c.getChildren().get(i)));
                }
                return b.toString();
            } else if(f.name == FunctionName.PERFORM){
                //Construct m = eval(c.getChildren().get(0));
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
