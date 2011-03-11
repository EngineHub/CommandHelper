

package com.laytonsmith.aliasengine;

import com.laytonsmith.Alias.Tree.GenericTree;
import com.laytonsmith.Alias.Tree.GenericTreeNode;
import com.laytonsmith.Alias.Tree.GenericTreeTraversalOrderEnum;
import com.laytonsmith.aliasengine.AliasConfig.Construct;
import com.laytonsmith.aliasengine.AliasConfig.ConstructType;
import com.laytonsmith.aliasengine.AliasConfig.Variable;
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

    public RunnableAlias(String command, ArrayList<GenericTree<Construct>> actions){
        this.command = command;
        this.actions = actions;
    }

    public void run(){
        for(GenericTree t : actions){
            StringBuilder b = new StringBuilder();
            List<GenericTreeNode<Construct>> l = t.build(GenericTreeTraversalOrderEnum.PRE_ORDER);
            for(GenericTreeNode<Construct> g : l){
                if(g.data.type.equals("root")){
                    continue;
                }
                Construct c = g.getData();
                if(c.ctype == ConstructType.FUNCTION){
                    b.append(eval(c));
                } else if(c.ctype == ConstructType.VARIABLE){
                    b.append(((Variable)c).def);
                } else {
                    b.append(c.value);
                }
                b.append(" ");
            }
            System.out.println("Running command: " + b.toString().trim());
        }
    }

    private String eval(Construct c){
        return "";
    }
}
