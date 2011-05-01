

package com.laytonsmith.aliasengine;


import com.laytonsmith.aliasengine.Constructs.*;
import com.laytonsmith.aliasengine.Constructs.Construct.ConstructType;
import com.laytonsmith.aliasengine.functions.BasicLogic._if;
import com.laytonsmith.aliasengine.functions.DataHandling._for;
import com.laytonsmith.aliasengine.functions.Function;
import com.laytonsmith.aliasengine.functions.FunctionList;
import com.laytonsmith.aliasengine.functions.IVariableList;
import com.sk89q.bukkit.migration.PermissionsResolverManager;
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
    ArrayList<GenericTree<Construct>> actions;
    FunctionList func_list;
    String label;
    IVariableList varList = new IVariableList();
    String originalCommand;

    String performAs = null;
    boolean inPerformAs = false;

    public RunnableAlias(String label, ArrayList<GenericTree<Construct>> actions, Player player,
            FunctionList func_list, String originalCommand){
        this.label = label;
        this.actions = actions;
        this.player = player;
        this.func_list = func_list;
        this.originalCommand = originalCommand;
    }

    public boolean run(){
        boolean ret = true;
        for(GenericTree t : actions){
            try{
            StringBuilder b = new StringBuilder();
            List<GenericTreeNode<Construct>> l = t.build(GenericTreeTraversalOrderEnum.PRE_ORDER);
            try{
                for(GenericTreeNode<Construct> g : l){
                    if(((Construct)g.data).val() == null){
                        b.append("");
                    }
                    else if(((Construct)g.data).val().equals("root")){
                        for(GenericTreeNode<Construct> gg : g.getChildren()){
                            b.append(eval(gg).val()).append(" ");
                        }
                    }
                }
                String cmd = b.toString().trim();
                System.out.println("CH: Running original command ----> " + originalCommand);
                System.out.println("on " + player.getName());
                if(player == null){
                    System.out.println("Player is null, assuming test harness is running");
                } else {
                    if(cmd.length() > 0 && cmd.charAt(0) == '/'){
                        Static.getServer().dispatchCommand(player, cmd.substring(1));
                    }
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

    public Construct eval(GenericTreeNode<Construct> c) throws CancelCommandException{
        Construct m = c.getData();
        if(m.ctype == ConstructType.FUNCTION){
            try {
                Function f;
                f = func_list.getFunction(m);
                //We have special handling for loop and other control flow functions
                if(f instanceof _for){
                    _for fr = (_for)f;
                    List<GenericTreeNode<Construct>> ch = c.getChildren();
                    try{
                        return fr.execs(m.line_num, player, this, ch.get(0), ch.get(1), ch.get(2), ch.get(3));
                    } catch(IndexOutOfBoundsException e){
                        throw new ConfigRuntimeException("Invalid number of parameters passed to for");
                    }
                } else if(f instanceof _if){
                    _if fr = (_if)f;
                    List<GenericTreeNode<Construct>> ch = c.getChildren();
                    try{
                        return fr.execs(m.line_num, player, this, ch.get(0), ch.get(1), ch.size() > 2?ch.get(2):null);
                    } catch(IndexOutOfBoundsException e){
                        throw new ConfigRuntimeException("Invalid number of parameters passed to if");
                    }
                }
                ArrayList<Construct> args = new ArrayList<Construct>();
                for (GenericTreeNode<Construct> c2 : c.getChildren()) {
                    args.add(eval(c2));
                }
                if(f.isRestricted()){
                    boolean perm;
                    PermissionsResolverManager perms = Static.getPermissionsResolverManager();
                    if(perms != null){
                        perm = perms.hasPermission(player.getName(), "ch.func.use." + f.getName())
                                || perms.hasPermission(player.getName(), "commandhelper.func.use." + f.getName());
                        if(label != null && (perms.hasPermission(player.getName(), "ch.alias." + label)) ||
                                perms.hasPermission(player.getName(), "commandhelper.alias." + label)){
                            perm = true;
                        }
                    } else {
                        perm = true;
                    }
                    if(player.isOp()){
                        perm = true;
                    }
                    if(!perm){
                        throw new CancelCommandException("You do not have permission to use the " + f.getName() + " function.");
                    }
                }
                Object [] a = args.toArray();
                Construct[] ca = new Construct[a.length];
                for(int i = 0; i < a.length; i++){
                    ca[i] = (Construct) a[i];
                    //if it's a variable, go ahead and cast it to the correct data type
                    if(ca[i].ctype == ConstructType.VARIABLE){
                        ca[i] = Static.resolveConstruct(ca[i].val(), ca[i].line_num);
                    }
                    //CArray, CBoolean, CDouble, CInt, CMap, CNull, CString, CVoid.
                    if(!(ca[i] instanceof CArray || ca[i] instanceof CBoolean || ca[i] instanceof CDouble 
                            || ca[i] instanceof CInt || ca[i] instanceof CMap || ca[i] instanceof CNull
                            || ca[i] instanceof CString || ca[i] instanceof CVoid || ca[i] instanceof IVariable)){
                        throw new ConfigRuntimeException("Invalid Construct being passed as an argument to a function");
                    }
                }
                if(f.preResolveVariables()){
                    for(int i = 0; i < ca.length; i++){
                        if(ca[i] instanceof IVariable){
                            IVariable v = (IVariable) ca[i];
                            ca[i] = varList.get(v.getName()).ival();
                        }
                    }
                }
                f.varList(varList);
                return f.exec(m.line_num, player, ca);
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
