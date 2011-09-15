
package com.laytonsmith.aliasengine;

//import com.laytonsmith.aliasengine.Constructs.*;
//import com.laytonsmith.aliasengine.Constructs.Construct.*;
//import com.laytonsmith.aliasengine.Constructs.Token.TType;
//import com.laytonsmith.aliasengine.functions.Function;
//import com.laytonsmith.aliasengine.functions.FunctionList;
//import com.sk89q.bukkit.migration.PermissionsResolverManager;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Stack;
//import org.bukkit.entity.Player;

/**
 *
 * @author Layton
 */
public class AliasConfig {
//
//    /**
//     * Once the config file is compiled, it gets added to this variable
//     */
//    Alias aliasFile = null;
//    FunctionList func_list;
//    PermissionsResolverManager perms;
//    List<Script> scripts = new ArrayList<Script>();
//
//    /**
//     * Constructor. Creates a compiled version of the given script.
//     * @param config The config script to compile
//     * @throws ConfigCompileException If there is a compiler error in the script
//     */
//    public AliasConfig(String config, User u, PermissionsResolverManager perms) throws ConfigCompileException{
//        func_list = new FunctionList(u);
//        this.perms = perms;
//        //Convert all newlines into \n newlines
//        config = config.replaceAll("\r\n", "\n");
//        config = config + "\n"; //add a newline at the end so that parser will work. If it's extra,
//        //nothing bad will happen, it'll just be ignored.
//        ArrayList<Token> token_list = new ArrayList<Token>();
//        //Set our state variables
//        boolean state_in_quote = false;
//        boolean in_comment = false;
//        boolean in_opt_var = false;
//        StringBuffer buf = new StringBuffer();
//        int line_num = 1;
//        //first we lex
//        for(int i = 0; i < config.length(); i++){
//            Character c = config.charAt(i);
//            Character c2 = null;
//            if(i < config.length() - 1){
//                c2 = config.charAt(i + 1);
//            }
//            if(c == '\n'){
//                line_num++;
//            }
//            if((token_list.isEmpty() || token_list.get(token_list.size() - 1).type.equals(TType.NEWLINE)) &&
//                    c == '#'){
//                in_comment = true;
//            }
//            if(in_comment && c != '\n'){
//                continue;
//            }
//            if(c == '[' && !state_in_quote){
//                if(buf.length() > 0){
//                    token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num));
//                    buf = new StringBuffer();
//                }
//                token_list.add(new Token(TType.OPT_VAR_START, "[", line_num));
//                in_opt_var = true;
//                continue;
//            }
//            if(c == '=' && !state_in_quote){
//                if(buf.length() > 0){
//                    token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num));
//                    buf = new StringBuffer();
//                }
//                if(in_opt_var){
//                    token_list.add(new Token(TType.OPT_VAR_ASSIGN, "=", line_num));
//                } else{
//                    token_list.add(new Token(TType.ALIAS_END, "=", line_num));
//                }
//                continue;
//            }
//            if(c == ']' && !state_in_quote){
//                if(buf.length() > 0){
//                    token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num));
//                    buf = new StringBuffer();
//                }
//                token_list.add(new Token(TType.OPT_VAR_END, "]", line_num));
//                in_opt_var = false;
//                continue;
//            }
//            if(c == ':' && !state_in_quote){
//                if(buf.length() > 0){
//                    token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num));
//                    buf = new StringBuffer();
//                }
//                token_list.add(new Token(TType.IDENT, ":", line_num));
//                continue;
//            }
//            if(c == ',' && !state_in_quote){
//                if(buf.length() > 0){
//                    token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num));
//                    buf = new StringBuffer();
//                }
//                token_list.add(new Token(TType.COMMA, ",", line_num));
//                continue;
//            }
//            if(c == '(' && !state_in_quote){
//                if(buf.length() > 0){
//                    token_list.add(new Token(TType.FUNC_NAME, buf.toString(), line_num));
//                    buf = new StringBuffer();
//                }
//                token_list.add(new Token(TType.FUNC_START, "(", line_num));
//                continue;
//            }
//            if(c == ')' && !state_in_quote){
//                if(buf.length() > 0){
//                    token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num));
//                    buf = new StringBuffer();
//                }
//                token_list.add(new Token(TType.FUNC_END, ")", line_num));
//                continue;
//            }
//            if(Character.isWhitespace(c) && !state_in_quote && c != '\n'){
//                //ignore the whitespace, but end the previous token
//                if(buf.length() > 0){
//                    token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num));
//                    buf = new StringBuffer();
//                }
//            }
//            else if(c == '\''){
//                if(state_in_quote){
//                    token_list.add(new Token(TType.STRING, buf.toString(), line_num));
//                    buf = new StringBuffer();
//                    state_in_quote = false;
//                    continue;
//                } else{
//                    state_in_quote = true;
//                    if(buf.length() > 0){
//                        token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num));
//                        buf = new StringBuffer();
//                    }
//                    continue;
//                }
//            } else if(c == '\\'){
//                //escaped characters
//                if(state_in_quote){
//                    if(c2 == '\\'){
//                        buf.append("\\");
//                    } else if(c2 == '\''){
//                        buf.append("'");
//                    } else{
//                        //Since we might expand this list later, don't let them
//                        //use unescaped backslashes
//                        throw new ConfigCompileException("The escape sequence \\" + c2 + " is not a recognized escape sequence", line_num);
//                    }
//                    
//                    i++;
//                    continue;
//                } else{
//                    //Control character backslash
//                    token_list.add(new Token(TType.SEPERATOR, "\\", line_num));
//                }
//            } else if(state_in_quote){
//                buf.append(c);
//                continue;
//            } else if(c == '\n'){
//                if(buf.length() > 0){
//                    token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num));
//                    buf = new StringBuffer();
//                }
//                token_list.add(new Token(TType.NEWLINE, "\n", line_num));
//                in_comment = false;
//                continue;
//            } else{ //in a literal
//                buf.append(c);
//                continue;
//            }
//        } //end lexing
//
//        //So, there has to be a more efficient way to do this, but I don't
//        //really care right now. It's only read in occasionally, so it's no biggie.
//
//        //take out extra newlines
//        ArrayList<Token> tokens1 = new ArrayList<Token>();
//        for(int i = 0; i < token_list.size(); i++){
//            try{
//                if(token_list.get(i).type.equals(TType.NEWLINE)){
//                    tokens1.add(new Token(TType.NEWLINE, "\n", line_num));
//                    while(token_list.get(++i).type.equals(TType.NEWLINE)){
//                    }
//                }
//                tokens1.add(token_list.get(i));
//            } catch(IndexOutOfBoundsException e){}
//        }
//
//        //Handle multiline constructs
//        ArrayList<Token> tokens1_1 = new ArrayList<Token>();
//        boolean inside_multiline = false;
//        for(int i = 0; i < tokens1.size(); i++){
//            Token prevToken = i - 1 >= tokens1.size()?tokens1.get(i - 1):new Token(TType.UNKNOWN, "", 0);
//            Token thisToken = tokens1.get(i);
//            Token nextToken = i + 1 < tokens1.size()?tokens1.get(i + 1):new Token(TType.UNKNOWN, "", 0);
//            //take out newlines between the = >>> and <<< tokens (also the tokens)
//            if(thisToken.type.equals(TType.ALIAS_END) && nextToken.val().equals(">>>")){
//                inside_multiline = true;
//                tokens1_1.add(thisToken);
//                i++;
//                continue;
//            }
//            if(thisToken.val().equals("<<<")){
//                if(!inside_multiline){
//                    throw new ConfigCompileException("Found multiline end symbol, and no multiline start found",
//                            thisToken.line_num);
//                }
//                inside_multiline = false;
//                continue;
//            }
//            if(thisToken.val().equals(">>>") && !prevToken.type.equals(TType.ALIAS_END)){
//                throw new ConfigCompileException("Multiline symbol must follow the alias_end token", thisToken.line_num);
//            }
//
//            //If we're not in a multiline construct, or we are in it and it's not a newline, add
//            //it
//            if(!inside_multiline || (inside_multiline && !thisToken.type.equals(TType.NEWLINE))){
//                tokens1_1.add(thisToken);
//            }
//        }
//
//        //take out newlines that are behind a \
//        ArrayList<Token> tokens2 = new ArrayList<Token>();
//        for(int i = 0; i < tokens1_1.size(); i++){
//            if(!tokens1_1.get(i).type.equals(TType.STRING) && tokens1_1.get(i).val().equals("\\") && tokens1_1.size() > i
//                    && tokens1_1.get(i + 1).type.equals(TType.NEWLINE)){
//                tokens2.add(tokens1_1.get(i));
//                i++;
//                continue;
//            }
//            tokens2.add(tokens1_1.get(i));
//        }
//
//
//
//        //look at the tokens, and get meaning from them
//        for(Token t : tokens2){
//            if(t.type.equals(TType.UNKNOWN)){
//                if(t.val().matches("/.*")){
//                    t.type = TType.COMMAND;
//                } else if(t.val().matches("\\\\")){
//                    t.type = TType.SEPERATOR;
//                } else if(t.val().matches("\\$[a-zA-Z0-9_]+")){
//                    t.type = TType.VARIABLE;
//                } else if(t.val().matches("\\@[a-zA-Z0-9_]+")){
//                    t.type = TType.IVARIABLE;
//                } else if(t.val().equals("$")){
//                    t.type = TType.FINAL_VAR;
//                }
//                else {
//                    t.type = TType.LIT;
//                }
//            }
//        }
//
//        //split the tokens into seperate commands now
//        ArrayList<Token> aliasBuffer = new ArrayList<Token>();
//        ArrayList<ArrayList<Token>> commandBuffer = new ArrayList<ArrayList<Token>>();
//        ArrayList<Token> tinyCmdBuffer = new ArrayList<Token>();
//        boolean in_alias = true;
//        Alias a = new Alias();
//        for(Token t : tokens2){
//            if(t.type.equals(TType.NEWLINE)){
//                if(aliasBuffer.size() > 0){
//                    if(in_alias){
//                        throw new ConfigCompileException("Unexpected newline", t.line_num);
//                    }
//                    if(commandBuffer.size() > 0 || tinyCmdBuffer.size() > 0){
//                        commandBuffer.add((ArrayList<Token>)tinyCmdBuffer.clone());
//                        a.alias.add(new AliasString(aliasBuffer));
//                        ArrayList<AliasString> tempBuf = new ArrayList<AliasString>();
//                        for(int i = 0; i < commandBuffer.size(); i++){
//                            ArrayList<Token> tlist = commandBuffer.get(i);
//                            tempBuf.add(new AliasString(tlist));
//                        }
//                        a.real.add(tempBuf);
//                        aliasBuffer.clear();
//                        commandBuffer.clear();
//                        tinyCmdBuffer.clear();
//                        in_alias = true;
//                        continue;
//                    }
//                }
//            }
//            else if(t.type.equals(TType.ALIAS_END)){
//                in_alias = false;
//            } else {
//                if(in_alias){
//                    aliasBuffer.add(t);
//                } else{
//                    if(t.type.equals(TType.SEPERATOR)){
//                        commandBuffer.add((ArrayList<Token>)tinyCmdBuffer.clone());
//                        tinyCmdBuffer.clear();
//                    } else{
//                        tinyCmdBuffer.add(t);
//                    }
//                }
//            }
//
//        }
//        //Check the syntax. Look for mismatched variables, mismatched parenthesis and square brackets,
//        //missing commands, bad function names, strings on the left side (except directly after a
//        //opt_var_assign), optional args on the right
//        /**
//         * Valid tokens on left:
//         * command, opt_var_start, variable, final_var, opt_var_assign, opt_var_end, lit,
//         * string (only directly after opt_var_assign)
//         */
//        for(int i = 0; i < a.alias.size(); i++){
//            AliasString c = a.alias.get(i);
//            ArrayList<AliasString> real = a.real.get(i);
//            ArrayList<Variable> left_vars = new ArrayList<Variable>();
//            ArrayList<Variable> right_vars = new ArrayList<Variable>();
//            boolean inside_opt_var = false;
//            boolean after_no_def_opt_var = false;
//            boolean has_label = false;
//            for(int j = 0; j < c.tokens.size(); j++){
//               Token t = c.tokens.get(j);
//               //Token prev_token = j - 2 >= 0?c.tokens.get(j - 2):new Token(TType.UNKNOWN, "", t.line_num);
//               Token last_token = j - 1 >= 0?c.tokens.get(j - 1):new Token(TType.UNKNOWN, "", t.line_num);
//               Token next_token = j + 1 < c.tokens.size()?c.tokens.get(j + 1):new Token(TType.UNKNOWN, "", t.line_num);
//               Token after_token = j + 2 < c.tokens.size()?c.tokens.get(j + 2):new Token(TType.UNKNOWN, "", t.line_num);
//               
//               if(j == 0){
//                   if(next_token.type == TType.IDENT){
//                       a.labels.add(t.val());
//                       has_label = true;
//                   } else {
//                       a.labels.add(null);
//                   }
//               }
//               
//               if(t.type == TType.IDENT){
//                   continue;
//               }
//
//               if(t.type.equals(TType.FINAL_VAR) && c.tokens.size() - j >= 5){
//                   throw new ConfigCompileException("FINAL_VAR must be the last argument in the alias", t.line_num);
//               }
//               if(t.type.equals(TType.VARIABLE) || t.type.equals(TType.FINAL_VAR)){
//                   left_vars.add(new Variable(t.val(), null, t.line_num));
//               }
//               if(j == 0 && !t.type.equals(TType.COMMAND)){
//                   if(!(next_token.type == TType.IDENT && after_token.type == TType.COMMAND)){
//                       throw new ConfigCompileException("Expected command (/command) at start of alias." +
//                               " Instead, found " + t.type + " (" + t.val() + ")", t.line_num);
//                   }
//               }
//               if(after_no_def_opt_var && !inside_opt_var){
//                   if(t.type.equals(TType.LIT) || t.type.equals(TType.STRING) || t.type.equals(TType.OPT_VAR_ASSIGN)){
//                       throw new ConfigCompileException("You cannot have anything other than optional arguments after your" +
//                               " first optional argument, other that other optional arguments with no default", t.line_num);
//                   }
//               }
//               if(!t.type.equals(TType.OPT_VAR_START) &&
//                       !t.type.equals(TType.OPT_VAR_ASSIGN) &&
//                       !t.type.equals(TType.OPT_VAR_END) &&
//                       !t.type.equals(TType.VARIABLE) &&
//                       !t.type.equals(TType.LIT) &&
//                       !t.type.equals(TType.COMMAND) &&
//                       !t.type.equals(TType.FINAL_VAR)){
//                   if(!(t.type.equals(TType.STRING) && j - 1 > 0 && c.tokens.get(j - 1).type.equals(TType.OPT_VAR_ASSIGN)))
//                       throw new ConfigCompileException("Unexpected " + t.type + " (" + t.val() + ")", t.line_num);
//               }
//               if(last_token.type.equals(TType.COMMAND)){
//                   if(!(t.type.equals(TType.VARIABLE) || t.type.equals(TType.OPT_VAR_START) || t.type.equals(TType.FINAL_VAR) ||
//                            t.type.equals(TType.LIT))){
//                       throw new ConfigCompileException("Unexpected " + t.type + " (" + t.val() + ") after command", t.line_num);
//                   }
//               }
//               if(last_token.type.equals(TType.OPT_VAR_START)){
//                   inside_opt_var = true;
//                   if(!(t.type.equals(TType.FINAL_VAR) || t.type.equals(TType.VARIABLE))){
//                       throw new ConfigCompileException("Unexpected " + t.type.toString() + " (" + t.val() + ")", t.line_num);
//                   }
//               }
//               if(inside_opt_var && t.type.equals(TType.OPT_VAR_ASSIGN)){
//                   if(!((next_token.type.equals(TType.STRING) || next_token.type.equals(TType.LIT)) && after_token.type.equals(TType.OPT_VAR_END) ||
//                           (next_token.type.equals(TType.OPT_VAR_END)))){
//                       throw new ConfigCompileException("Unexpected token in optional variable", t.line_num);
//                   } else if(next_token.type.equals(TType.STRING) || next_token.type.equals(TType.LIT)){
//                       left_vars.get(left_vars.size() - 1).def = next_token.val();
//                   } else {
//                       left_vars.get(left_vars.size() - 1).def = "";
//                   }
//               }
//               if(t.type.equals(TType.OPT_VAR_END)){
//                   if(!inside_opt_var){
//                       throw new ConfigCompileException("Unexpected " + t.type.toString(), t.line_num);
//                   }
//                   inside_opt_var = false;
//                   if(last_token.type.equals(TType.VARIABLE)
//                           || last_token.type.equals(TType.FINAL_VAR)){
//                       after_no_def_opt_var = true;
//                   }
//               }
//            }
//            if(has_label){
//                //remove the label from the alias
//                a.alias.get(i).tokens.remove(0);
//                a.alias.get(i).tokens.remove(0);
//            }
//            ArrayList<GenericTree<Construct>> rightTreeOne = new ArrayList<GenericTree<Construct>>();
//
//            //Compile the right side into a program
//            for(int j = 0; j < real.size(); j++){
//                AliasString r = real.get(j);
//                int paren_stack = 0;
//                GenericTree<Construct> root = new GenericTree<Construct>();
//                //root.setRoot(new GenericTreeNode<Construct>(new Token(TType.ROOT, "root", 0)));
//                GenericTreeNode<Construct> node = root.getRoot();
//                Stack<GenericTreeNode> parents = new Stack<GenericTreeNode>();
//                parents.push(root.getRoot());
//                for(int k = 0; k < r.tokens.size(); k++){
//                    Token t = r.tokens.get(k);
//                    //Token before = k - 2 >= 0?r.tokens.get(k - 2):new Token(TType.UNKNOWN, "", t.line_num);
//                    Token prev = k - 1 >= 0?r.tokens.get(k - 1):new Token(TType.UNKNOWN, "", t.line_num);
//                    //Token next = k + 1 < r.tokens.size()?r.tokens.get(k + 1):new Token(TType.UNKNOWN, "", t.line_num);
//                    //Token after = k + 2 < r.tokens.size()?r.tokens.get(k + 2):new Token(TType.UNKNOWN, "", t.line_num);
//                    if(k == 0 && !t.type.equals(TType.COMMAND) && !t.type.equals(TType.FUNC_NAME)){
//                        throw new ConfigCompileException("Expected command (/command) or function at start of" +
//                                " command", t.line_num);
//                    }
//                    if(t.type.equals(TType.OPT_VAR_ASSIGN) || t.type.equals(TType.OPT_VAR_START)
//                            || t.type.equals(TType.OPT_VAR_END)){
//                        throw new ConfigCompileException("Unexpected " + t.type.toString(), t.line_num);
//                    }
//                    if(t.type.equals(TType.LIT)){
//                        //See what the type of each literal is, and see if it's a double, int, boolean, null, or other keyword.
//                        node.addChild(new GenericTreeNode<Construct>(Static.resolveConstruct(t.val(), t.line_num)));
//                    } else if(t.type.equals(TType.STRING)){
//                        node.addChild(new GenericTreeNode<Construct>(new CString(t.val(), t.line_num)));
//                    } else if(t.type.equals(TType.IVARIABLE)){
//                        node.addChild(new GenericTreeNode<Construct>(new IVariable(t.val(), line_num)));
//                    } else if(t.type.equals(TType.VARIABLE) || t.type.equals(TType.FINAL_VAR)){
//                        node.addChild(new GenericTreeNode<Construct>(new Variable(t.val(), null, t.line_num)));
//                        right_vars.add(new Variable(t.val(), null, t.line_num));
//                    } else if(t.type.equals(TType.FUNC_NAME)){
//                        try {
//                            GenericTreeNode<Construct> f = new GenericTreeNode<Construct>(new CFunction(t.val(), t.line_num));
//                            node.addChild(f);
//                            node = f;
//                            parents.push(f);
//                        } catch (Exception ex) {
//                            throw new ConfigCompileException("\"" + t.val() + "\" is not a built in function", t.line_num);
//                        }
//                    } else if(t.type.equals(TType.FUNC_START)){
//                        if(!prev.type.equals(TType.FUNC_NAME)){
//                            throw new ConfigCompileException("Unexpected parenthesis", t.line_num);
//                        }
//                        paren_stack++;
//                        continue;
//                    } else if(t.type.equals(TType.FUNC_END)){
//                        paren_stack--;
//                        if(paren_stack < 0){
//                            throw new ConfigCompileException("Unexpected parenthesis", t.line_num);
//                        }
//                        parents.pop();
//                        node = parents.peek();
//                    } else if(t.type.equals(TType.COMMAND)){
//                        //For the sake of standardization, add this as a string
//                        node.addChild(new GenericTreeNode<Construct>(new CString(t.val(), t.line_num)));
//                    } else if(t.type.equals(TType.COMMA)){
//                        continue;
//                    } else{
//                        throw new ConfigCompileException("Unexpected " + t.type + " (" + t.val() + ")", t.line_num);
//                    }
//                }
//                rightTreeOne.add(root);
//                List<GenericTreeNode<Construct>> l = root.build(GenericTreeTraversalOrderEnum.PRE_ORDER);
//                for(GenericTreeNode<Construct> n : l){
//                    if(n.getData() instanceof CFunction){
//                        int args = n.getChildren().size();
//                        Function f = func_list.getFunction((CFunction)n.getData());
//                        List<Integer> numArgs = Arrays.asList(f.numArgs());
//                        if(f.isRestricted()){
//                            boolean perm;
//                            if(u != null && perms != null){
//                                perm = perms.hasPermission(u.player.getName(), "ch.func.compile." + f.getName())
//                                        || perms.hasPermission(u.player.getName(), "commandhelper.func.compile." + f.getName());
//                            } else {
//                                perm = true;
//                            }
//                            if(!perm){
//                                throw new ConfigCompileException("You do not have permission to compile the " + f.getName() + " function" +
//                                        " into your script.");
//                            }
//                        }
//                        if(!numArgs.contains(args) &&
//                                !numArgs.contains(Integer.MAX_VALUE)){
//                            throw new ConfigCompileException("Incorrect number of args for " +
//                                    ((CFunction)n.getData()).toString(), n.data.line_num);
//                        }
//                    }
//                }
//                //Look to see if all the variables defined on the left are used on the right
//                for(Variable left_var : left_vars){
//                    boolean found = false;
//                    for(Variable right_var : right_vars){
//                        if(left_var.name.equals(right_var.name)){
//                            found = true;
//                            break;
//                        }
//                    }
//                    if(!found){
//                        //Just a warning in this case
//                        if((Boolean)Static.getPreferences().getPreference("show-warnings")){
//                            System.err.println("Notice: Defined variable " + left_var.name + " is unused near line " + left_var.line_num);
//                        }
//                    }
//                }
//                //Look to see if all the variables on the right are defined on the left
//                for(Variable right_var : right_vars){
//                    boolean found = false;
//                    for(Variable left_var : left_vars){
//                        if(right_var.name.equals(left_var.name)){
//                            found = true;
//                            right_var.def = left_var.def;
//                            break;
//                        }
//                    }
//                    if(!found){
//                        //This is a fatal error
//                        throw new ConfigCompileException("Undefined variable " + right_var.name, right_var.line_num);
//                    }
//                }
//
//
//            }
//            a.rightTrees.add(rightTreeOne);
//        }
//        //Compile the (now syntactically correct) left side into a ArrayList<Construct>
//        for(int i = 0; i < a.alias.size(); i++){
//            AliasString c = a.alias.get(i);
//            ArrayList<Construct> ac = new ArrayList<Construct>();
//            for(int j = 0; j < c.tokens.size(); j++){
//                Token t = c.tokens.get(j);
//                if(t.type.equals(TType.VARIABLE)){
//                    ac.add(new Variable(t.val(), null, t.line_num));
//                } else if(t.type.equals(TType.FINAL_VAR)){
//                    Variable v = new Variable(t.val(), null, t.line_num);
//                    v.final_var = true;
//                    ac.add(v);
//                }else if(t.type.equals(TType.OPT_VAR_START)){
//                    if(j + 2 < c.tokens.size() && c.tokens.get(j + 2).type.equals(TType.OPT_VAR_ASSIGN)){
//                        Variable v = new Variable(c.tokens.get(j + 1).val(),
//                                c.tokens.get(j + 3).val(), t.line_num);
//                        v.optional = true;
//                        if(c.tokens.get(j + 1).type.equals(TType.FINAL_VAR))
//                            v.final_var = true;
//                        ac.add(v);
//                        j += 4;
//                    } else{
//                        t = c.tokens.get(j + 1);
//                        Variable v = new Variable(t.val(), null, t.line_num);
//                        v.optional = true;
//                        if(t.val().equals("$"))
//                            v.final_var = true;
//                        ac.add(v);
//                        j += 2;
//                    }
//                } else if(t.type.equals(TType.COMMAND)){
//                    ac.add(new Command(t.val(), t.line_num));
//                } else{
//                    //Don't care what it is now. Just add it.
//                    //ac.add(t);
//                }
//
//            }
//            a.aliasConstructs.add(ac);
//        }
//
//        //Check for commands with ambiguous signatures.
//
//        for(int i = 0; i < a.aliasConstructs.size(); i++){
//            ArrayList<Construct> thisCommand = a.aliasConstructs.get(i);
//            for(int j = 0; j < a.aliasConstructs.size(); j++){
//                if(i == j){
//                    //Of course this command is going to match it's own signature
//                    continue;
//                }
//                ArrayList<Construct> thatCommand = a.aliasConstructs.get(j);
//                boolean soFarAMatch = true;
//                for(int k = 0; k < thisCommand.size(); k++){
//                    try{
//                        Construct c1 = thisCommand.get(k);
//                        Construct c2 = thatCommand.get(k);
//                        if(c1.ctype != c2.ctype || ((c1 instanceof Variable) && !((Variable)c1).optional)){
//                            soFarAMatch = false;
//                        } else {
//                            //It's a literal, check to see if it's the same literal
//                            if(c1.val() == null || !c1.val().equals(c2.val())){
//                                soFarAMatch = false;
//                            }
//                        }
//                    } catch(IndexOutOfBoundsException e){
//                        /**
//                         * The two commands:
//                         * /cmd $var1 [$var2]
//                         * /cmd $var1
//                         * would cause this exception to be thrown, but the signatures
//                         * are the same, so the fact that they've matched this far means
//                         * they are ambiguous. However,
//                         * /cmd $var1 $var2
//                         * /cmd $var1
//                         * is not ambiguous
//                         */
//                        //thatCommand is the short one
//                        if(!(thisCommand.get(k) instanceof Variable) ||
//                                (thisCommand.get(k) instanceof Variable &&
//                                !((Variable)thisCommand.get(k)).optional)){
//                            soFarAMatch = false;
//                        }
//                    }
//                }
//                if(thatCommand.size() > thisCommand.size()){
//                    int k = thisCommand.size();
//                    //thisCommand is the short one
//                    if(!(thatCommand.get(k) instanceof Variable) ||
//                            (thatCommand.get(k) instanceof Variable &&
//                            !((Variable)thatCommand.get(k)).optional)){
//                        soFarAMatch = false;
//                    }
//                }
//
//                if(soFarAMatch) {
//                    String commandThis = "";
//                    for (Construct c : thisCommand) {
//                        commandThis += c.val() + " ";
//                    }
//                    String commandThat = "";
//                    for (Construct c : thatCommand) {
//                        commandThat += c.val() + " ";
//                    }
//                    throw new ConfigCompileException("The command " + commandThis + "is ambiguous because it "
//                            + "matches the signature of " + commandThat, thisCommand.get(0).line_num);
//                }
//            }
//        }
//
//        if(a.real.isEmpty()){
//            System.out.println("No Aliases were defined.");
//        }
//        else{
//            System.out.println("Config file compiled sucessfully, with " + a.alias.size() + " alias(es) defined.");
//            aliasFile = a;
//        }
//    }
//
//    /**
//     * This is the command that runs the magic once the config file is compiled and tucked away.
//     * It takes a real command from the user and sees if it fits with any of the aliases. If it
//     * does, it actually returns the commands to run. Since it's a bit more complicated than just
//     * returning a string, it returns a "RunnableAlias" that has implementation specific functions
//     * in it. Mostly what this function does is, selects the proper command from the command
//     * list, fills in the variables, then returns the ArrayList.
//     * @param command
//     * @return
//     */
//    public RunnableAlias getRunnableAliases(String command, Player player){
////        if(this.aliasFile == null){
////            return null;
////        }
////        String[] cmds = command.split(" ");
////        ArrayList<String> args = new ArrayList(Arrays.asList(cmds));
////        for(int i = 0; i < this.aliasFile.alias.size(); i++){
////            ArrayList<Construct> tokens = this.aliasFile.aliasConstructs.get(i);
////            boolean isAMatch = true;
////            StringBuilder lastVar = new StringBuilder();
////            int lastJ = 0;
////            try{
////                for(int j = 0; j < tokens.size(); j++){
////                    if(!isAMatch){                        
////                        break;
////                    }
////                    lastJ = j;
////                    Construct c = tokens.get(j);
////                    String arg = args.get(j);
////                    if(c.ctype == ConstructType.COMMAND || 
////                            c.ctype == ConstructType.TOKEN ||
////                            c.ctype == ConstructType.LITERAL){
////                        if(!c.val().equals(arg)){
////                            isAMatch = false;
////                        }
////                    }
////                    if(j == tokens.size() - 1){
////                        if(tokens.get(j).ctype == ConstructType.VARIABLE){
////                            Variable lv = (Variable)tokens.get(j);
////                            if(lv.final_var){
////                                for(int a = j; a < args.size(); a++){
////                                    if(lastVar.length() == 0){
////                                        lastVar.append(args.get(a));
////                                    }
////                                    else {
////                                        lastVar.append(" ").append(args.get(a));
////                                    }
////                                }
////                            }
////                        }
////                    }
////                }
////            } catch(IndexOutOfBoundsException e){
////                if(tokens.get(lastJ).ctype == ConstructType.VARIABLE &&
////                        !((Variable)tokens.get(lastJ)).optional){
////                    isAMatch = false;
////                }
////            }
////            if(isAMatch){
////                //Now, pull out the variables. That's all we care about in the left
////                //side now
////                ArrayList<Variable> vars = new ArrayList<Variable>();
////                Variable v = null;
////                for(int j = 0; j < tokens.size(); j++){
////                    try{
////                        if(tokens.get(j).ctype == ConstructType.VARIABLE){
////                            if(((Variable)tokens.get(j)).name.equals("$")){
////                                v = new Variable(((Variable)tokens.get(j)).name,
////                                        lastVar.toString(), 0);
////                            } else {
////                                v = new Variable(((Variable)tokens.get(j)).name,
////                                        args.get(j), 0);
////                            }
////                        }
////                    } catch(IndexOutOfBoundsException e){
////                        v = new Variable(((Variable)tokens.get(j)).name,
////                                ((Variable)tokens.get(j)).def, 0);
////                    }
////                    if(v != null)
////                        vars.add(v);
////                }
////
////                ArrayList<GenericTree<Construct>> tree = aliasFile.rightTrees.get(i);
////                for(GenericTree<Construct> t : tree){
////                    for(GenericTreeNode g : t.build(GenericTreeTraversalOrderEnum.PRE_ORDER)){
////                        Construct c = (Construct)g.data;
////                        if(c instanceof Variable){
////                            Variable var = (Variable)c;
////                            String def = "";
////                            for(Variable vv : vars){
////                                if(vv.name.equals(var.name)){
////                                    def = vv.def;
////                                    break;
////                                }
////                            }
////                            var.def = def;
////                        }
////                    }
////                }
////                return new RunnableAlias(this.aliasFile.labels.get(i), tree, player, func_list, command);
////            }
////        }
//
//        return null;
//    }
//
//    public int totalAliases(){
//        return aliasFile.alias.size();
//    }
//
//
//    public class Alias{
//        ArrayList<AliasString> alias = new ArrayList<AliasString>();
//        ArrayList<ArrayList<Construct>> aliasConstructs = new ArrayList<ArrayList<Construct>>();
//        ArrayList<ArrayList<AliasString>> real = new ArrayList<ArrayList<AliasString>>();
//        ArrayList<String> labels = new ArrayList<String>();
//        ArrayList<ArrayList<GenericTree<Construct>>> rightTrees = new ArrayList<ArrayList<GenericTree<Construct>>>();
//        HashMap<AliasString, ArrayList<AliasString>> map = new HashMap<AliasString, ArrayList<AliasString>>();
//        @Override
//        public String toString(){
//            StringBuilder b = new StringBuilder();
//            for(int i = 0; i < alias.size(); i++){
//                b.append("\nAlias: ");
//                b.append(alias.get(i));
//                for(int j = 0; j < real.get(i).size(); j++){
//                    b.append("\n\t").append(real.get(i).get(j));
//                }
//            }
//            return b.toString();
//        }
//    }
//
//    public class AliasString{
//        ArrayList<Token> tokens = new ArrayList<Token>();
//        public AliasString(ArrayList<Token> a){
//            tokens = (ArrayList<Token>) a.clone();
//        }
//        @Override
//        public String toString(){
//            StringBuilder b = new StringBuilder();
//            for(Token t : tokens){
//                b.append(t.toSimpleString()).append(" ");
//            }
//            return b.toString();
//        }
//    }

}
