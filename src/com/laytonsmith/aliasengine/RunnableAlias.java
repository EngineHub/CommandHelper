

package com.laytonsmith.aliasengine;


import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Constructs.Construct.ConstructType;
import com.laytonsmith.aliasengine.Constructs.Variable;
import com.laytonsmith.aliasengine.functions.Function;
import com.laytonsmith.aliasengine.functions.FunctionList;
import com.sk89q.commandhelper.CommandHelperPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    FunctionList func_list;

    String performAs = null;
    boolean inPerformAs = false;

    public RunnableAlias(String command, ArrayList<GenericTree<Construct>> actions, Player player,
            FunctionList func_list){
        this.command = command;
        this.actions = actions;
        this.player = player;
        this.func_list = func_list;
    }

    public boolean run(){
        boolean ret = true;
        for(GenericTree t : actions){
            try{
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
            } catch(ConfigRuntimeException e){
                CommandHelperPlugin.logger.log(Level.WARNING, "Script runtime exeption: " + e.getMessage());
            }
        }
        return ret;
    }

    private Construct eval(GenericTreeNode<Construct> c) throws CancelCommandException{
        Construct m = c.getData();
        if(m.ctype == ConstructType.FUNCTION){
            try {
                Function f;
                f = func_list.getFunction(m);
                ArrayList<Construct> args = new ArrayList<Construct>();
                for (GenericTreeNode<Construct> c2 : c.getChildren()) {
                    args.add(eval(c2));
                }
                return f.exec(m.line_num, (Construct[]) args.toArray());
                //            if(f.name == FunctionName.DIE){
                //                throw new CancelCommandException(eval(c.getChildren().get(0)));
                //            } else if(f.name == FunctionName.DATA_VALUES){
                //                return Data_Values.val(eval(c.getChildren().get(0)));
                //            } else if(f.name == FunctionName.PLAYER){
                //                if(player != null){
                //                    return player.getName();
                //                } else {
                //                    return "Player";
                //                }
                //            } else if(f.name == FunctionName.MSG){
                //                if(player != null){
                //                    player.sendMessage(eval(c.getChildren().get(0)));
                //                } else {
                //                    System.out.println("Sending message to player: " + eval(c.getChildren().get(0)));
                //                }
                //            } else if(f.name == FunctionName.EQUALS){
                //                if(eval(c.getChildren().get(0)).equals(eval(c.getChildren().get(1)))){
                //                    return "1";
                //                } else {
                //                    return "0";
                //                }
                //            } else if(f.name == FunctionName.IF){
                //                if(eval(c.getChildren().get(0)).equals("0") ||
                //                        eval(c.getChildren().get(0)).equals("false")){
                //                    return eval(c.getChildren().get(2));
                //                } else{
                //                    return eval(c.getChildren().get(1));
                //                }
                //            } else if(f.name == FunctionName.CONCAT){
                //                StringBuilder b = new StringBuilder();
                //                for(int i = 0; i < c.getChildren().size(); i++){
                //                    b.append(eval(c.getChildren().get(i)));
                //                }
                //                return b.toString();
                //            } else if(f.name == FunctionName.PERFORM){
                //                //Construct m = eval(c.getChildren().get(0));
                //            }
            } catch (ConfigCompileException ex) {
                Logger.getLogger(RunnableAlias.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else if(m.ctype == ConstructType.VARIABLE){
            return ((Variable)m);
        } else{
            return m;
        }
        return null;
    }
}
