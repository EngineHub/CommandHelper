/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.tools;

import com.laytonsmith.PureUtilities.SerializedPersistance;
import com.laytonsmith.PureUtilities.TermColors;
import static com.laytonsmith.PureUtilities.TermColors.*;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.GenericTreeNode;
import com.laytonsmith.core.Main;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.MethodScriptComplete;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.sk89q.wepif.PermissionsResolverManager;
import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a command line implementation of the in game interpreter mode.
 * This should only be run while the server is stopped, as it has full
 * access to filesystem resources. Many things won't work as intended, but
 * pure abstract functions should still work fine.
 * @author layton
 */
public class Interpreter {
    static boolean multilineMode = false;
    static String script;

    public static void start(){
        try {
            MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex("player()", null)), new Env(), null, null);
        } catch (ConfigCompileException ex) {}        
        Static.persist = new SerializedPersistance(new File("CommandHelper/persistance.ser"));
        if(TermColors.SYSTEM == TermColors.SYS.WINDOWS){
            TermColors.DisableColors();
        }
        Scanner scanner = new Scanner(System.in);
        if(System.console() != null){
            pl(YELLOW + "You are now in cmdline interpreter mode. Type a dash (-) on a line by itself to exit, and >>> to enter"
                    + " multiline mode.\nMost Minecraft features will not work, and your working directory is the"
                    + " CommandHelper.jar directory, not the Server directory. Have fun!");
            p(BLUE + ":" + WHITE);
        }
        if(System.console() == null){
            //We need to read in everything, it's basically in multiline mode
            StringBuilder script = new StringBuilder();
            String line = null;
            try{
                while((line = scanner.nextLine()) != null){
                    script.append(line).append("\n");
                }
            } catch(NoSuchElementException e){
                //Done
            }
            try {
                execute(script.toString());
                System.out.print(TermColors.reset());
                System.exit(0);
            } catch (ConfigCompileException ex) {
                ConfigRuntimeException.DoReport(ex, null, null);
                System.out.print(TermColors.reset());
                System.exit(1);
            }
            
        }
        try{
            while(textLine(scanner.nextLine())){
                if(System.console() != null)
                    p(BLUE + ":" + WHITE);
            }
        } catch(NoSuchElementException e){
            //End of file
        }
    }

    public static boolean textLine(String line) {
        if (line.equals("-")) {
            //Exit interpreter mode
            pl(YELLOW + "Now exiting interpreter mode" + reset());
            return false;
        } else if (line.equals(">>>")) {
            //Start multiline mode
            if (multilineMode) {
                pl(RED + "You are already in multiline mode!");
            } else {
                multilineMode = true;
                pl(YELLOW + "You are now in multiline mode. Type <<< on a line by itself to execute.");
                pl(":" + WHITE + ">>>");
            }
        } else if (line.equals("<<<")) {
            //Execute multiline
            pl(":" + WHITE + "<<<");
            multilineMode = false;
            try {
                execute(script);
                script = "";
            } catch (ConfigCompileException e) {
                ConfigRuntimeException.DoReport(e, null, null);
            }
        } else {
            if (multilineMode) {
                //Queue multiline
                script = script + line + "\n";
            } else {
                try {
                    //Execute single line
                    execute(line);
                } catch (ConfigCompileException ex) {
                    ConfigRuntimeException.DoReport(ex, null, null);
                }
            }
        }
        return true;
    }

    public static void execute(String script) throws ConfigCompileException {
        List<Token> stream = MethodScriptCompiler.lex(script, new File("Interpreter"));
        GenericTreeNode tree = MethodScriptCompiler.compile(stream);
        Env env = new Env();
        env.SetPlayer(null);
        env.SetLabel("*");
        env.SetCustom("cmdline", true);
        try {
            MethodScriptCompiler.execute(tree, env, new MethodScriptComplete() {

                public void done(String output) {
                    output = output.trim();
                    if (output.equals("")) {
                        if(System.console() != null)
                            pl(":");
                    } else {
                        if (output.startsWith("/")) {
                            //Run the command
                            pl((System.console()!=null?":" + YELLOW:"") + output);
                        } else {
                            //output the results
                            pl((System.console()!=null?":" + GREEN:"") + output);
                        }
                    }
                }
            }, null);
        } catch (CancelCommandException e) {
            if(System.console() != null)
                pl(":");
        } catch (ConfigRuntimeException e){
            ConfigRuntimeException.DoReport(e);
            //No need for the full stack trace        
        } catch(Exception e){
            pl(RED + e.toString());
            e.printStackTrace();
        } catch(NoClassDefFoundError e){
            System.err.println(RED + Main.getNoClassDefFoundErrorMessage(e) + reset());
            System.err.println("Since you're running from standalone interpreter mode, this is not a fatal error, but one of the functions you just used required"
                    + " an actual backing engine that isn't currently loaded. (It still might fail even if you load the engine though.) You simply won't be"
                    + " able to use that function here.");
        }
    }
}
