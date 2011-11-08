/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.api;
import com.laytonsmith.aliasengine.Constructs.CDouble;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Env;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import com.laytonsmith.aliasengine.exceptions.ConfigRuntimeException;
import com.nijikokun.register.payment.Method;
import com.nijikokun.register.payment.Method.MethodAccount;
import com.nijikokun.register.payment.Method.MethodBankAccount;
import com.nijikokun.register.payment.Methods;
import java.io.File;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Layton
 */
public class Economy {
    
    public static String docs(){
        return "Provides functions to hook into the server's economy plugin. To use any of these functions, you must have one of the"
                + " following economy plugins installed: iConomy 4, 5, & 6+, BOSEconomy 6 & 7, Essentials Economy 2.2.17+, MultiCurrency."
                + " No special installation is required beyond simply getting the economy plugin working by itself. Using any of these functions"
                + " without one of the economy plugins will cause it to throw a InvalidPluginException at runtime.";
    }
    
    @api public static class acc_balance implements Function{

        public String getName() {
            return "acc_balance";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "double {account_name} Returns the balance of the given account name.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException, ExceptionType.InvalidPluginException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.2.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            MethodAccount ma = GetAccount(this.getName(), line_num, f, args);
            return new CDouble(ma.balance(), line_num, f);
        }
        
    }
    @api public static class acc_set implements Function{

        public String getName() {
            return "acc_set";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "void {account_name, value} Sets the account's balance to the given amount";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException, ExceptionType.InvalidPluginException, ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.2.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            if(GetAccount(this.getName(), line_num, f, args).set(Static.getNumber(args[1]))){
                return new CVoid(line_num, f);
            } else {
                throw new ConfigRuntimeException("An error occured when trying to set the balance on account " + args[0].val(), ExceptionType.PluginInternalException, line_num, f);
            }
        }
        
    }
    @api public static class acc_add implements Function{

        public String getName() {
            return "acc_add";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "void {account_name, to_add} Adds an amount to the specified account";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException, ExceptionType.InvalidPluginException, ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.2.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            if(GetAccount(this.getName(), line_num, f, args).add(Static.getNumber(args[1]))){
                return new CVoid(line_num, f);
            } else {
                throw new ConfigRuntimeException("An error occured when trying to add to the balance on account " + args[0].val(), ExceptionType.PluginInternalException, line_num, f);
            }
        }
        
    }
    @api public static class acc_subtract implements Function{

        public String getName() {
            return "acc_subtract";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "void {account_name, to_subtract} Subtracts the given amount from the specified account";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException, ExceptionType.InvalidPluginException, ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.2.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            if(GetAccount(this.getName(), line_num, f, args).subtract(Static.getNumber(args[1]))){
                return new CVoid(line_num, f);
            } else {
                throw new ConfigRuntimeException("An error occured when trying to subtract from the balance on account " + args[0].val(), ExceptionType.PluginInternalException, line_num, f);
            }
        }
        
    }
    @api public static class acc_multiply implements Function{

        public String getName() {
            return "acc_multiply";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "void {account_name, to_multiply} Multiplies the account balance by the given amount";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException, ExceptionType.InvalidPluginException, ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.2.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            if(GetAccount(this.getName(), line_num, f, args).multiply(Static.getNumber(args[1]))){
                return new CVoid(line_num, f);
            } else {
                throw new ConfigRuntimeException("An error occured when trying to multiply the balance on account " + args[0].val(), ExceptionType.PluginInternalException, line_num, f);
            }
        }
        
    }
    @api public static class acc_divide implements Function{

        public String getName() {
            return "acc_divide";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "void {account_name, to_divide} Divides the account by the given amount";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException, ExceptionType.InvalidPluginException, ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.2.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            if(GetAccount(this.getName(), line_num, f, args).divide(Static.getNumber(args[1]))){
                return new CVoid(line_num, f);
            } else {
                throw new ConfigRuntimeException("An error occured when trying to divide the balance on account " + args[0].val(), ExceptionType.PluginInternalException, line_num, f);
            }
        }
        
    }
    @api public static class acc_remove implements Function{

        public String getName() {
            return "acc_remove";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "void {account_name} Removes the specified account from the game";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException, ExceptionType.InvalidPluginException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.2.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            if(GetAccount(this.getName(), line_num, f, args).remove()){
                return new CVoid(line_num, f);
            } else {
                throw new ConfigRuntimeException("An error occured when trying to set the balance on account " + args[0].val(), ExceptionType.PluginInternalException, line_num, f);
            }
        }
        
    }
    @api public static class bacc_balance implements Function{

        public String getName() {
            return "bacc_balance";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "void {bank_name, account_name} Gets the specified bank account's balance";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException, ExceptionType.InvalidPluginException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.2.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            return new CDouble(GetBankAccount(this.getName(), line_num, f, args).balance(), line_num, f);
        }
        
    }
    @api public static class bacc_set implements Function{

        public String getName() {
            return "bacc_set";
        }

        public Integer[] numArgs() {
            return new Integer[]{3};
        }

        public String docs() {
            return "void {bank_name, account_name, value} Sets the bank account's balance to the given amount";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException, ExceptionType.InvalidPluginException, ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.2.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            if(GetBankAccount(this.getName(), line_num, f, args).set(Static.getNumber(args[2]))){
                return new CVoid(line_num, f);
            } else {
                throw new ConfigRuntimeException("An error occured when trying to set the balance on bank account " + args[0].val() + ":" + args[1].val(), ExceptionType.PluginInternalException, line_num, f);
            }
        }
        
    }
    @api public static class bacc_add implements Function{

        public String getName() {
            return "bacc_add";
        }

        public Integer[] numArgs() {
            return new Integer[]{3};
        }

        public String docs() {
            return "void {bank_name, account_name, value} Adds the specified amount to the bank account's balance";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException, ExceptionType.InvalidPluginException, ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.2.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            if(GetBankAccount(this.getName(), line_num, f, args).add(Static.getNumber(args[2]))){
                return new CVoid(line_num, f);
            } else {
                throw new ConfigRuntimeException("An error occured when trying to add to the balance on bank account " + args[0].val() + ":" + args[1].val(), ExceptionType.PluginInternalException, line_num, f);
            }
        }
        
    }
    @api public static class bacc_subtract implements Function{

        public String getName() {
            return "bacc_subtract";
        }

        public Integer[] numArgs() {
            return new Integer[]{3};
        }

        public String docs() {
            return "void {bank_name, account_name, value} Subtracts the specified amount from the bank account's balance";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException, ExceptionType.InvalidPluginException, ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.2.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            if(GetBankAccount(this.getName(), line_num, f, args).subtract(Static.getNumber(args[2]))){
                return new CVoid(line_num, f);
            } else {
                throw new ConfigRuntimeException("An error occured when trying to subtract from the balance on bank account " + args[0].val() + ":" + args[1].val(), ExceptionType.PluginInternalException, line_num, f);
            }
        }
        
    }
    @api public static class bacc_multiply implements Function{

        public String getName() {
            return "bacc_multiply";
        }

        public Integer[] numArgs() {
            return new Integer[]{3};
        }

        public String docs() {
            return "void {bank_name, account_name, value} Multiplies the given bank account's balance by the given value";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException, ExceptionType.InvalidPluginException, ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.2.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            if(GetBankAccount(this.getName(), line_num, f, args).multiply(Static.getNumber(args[2]))){
                return new CVoid(line_num, f);
            } else {
                throw new ConfigRuntimeException("An error occured when trying to multiply the balance on bank account " + args[0].val() + ":" + args[1].val(), ExceptionType.PluginInternalException, line_num, f);
            }
        }
        
    }
    @api public static class bacc_divide implements Function{

        public String getName() {
            return "bacc_divide";
        }

        public Integer[] numArgs() {
            return new Integer[]{3};
        }

        public String docs() {
            return "void {bank_name, account_name, value} Divides the bank account's balance by the given value";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException, ExceptionType.InvalidPluginException, ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.2.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            if(GetBankAccount(this.getName(), line_num, f, args).divide(Static.getNumber(args[2]))){
                return new CVoid(line_num, f);
            } else {
                throw new ConfigRuntimeException("An error occured when trying to divide the balance on bank account " + args[0].val() + ":" + args[1].val(), ExceptionType.PluginInternalException, line_num, f);
            }
        }
        
    }
    @api public static class bacc_remove implements Function{

        public String getName() {
            return "bacc_remove";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "void {bank_name, account_name} Removes the given bank account from the game";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException, ExceptionType.InvalidPluginException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.2.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            if(GetBankAccount(this.getName(), line_num, f, args).remove()){
                return new CVoid(line_num, f);
            } else {
                throw new ConfigRuntimeException("An error occured when trying to remove the bank account " + args[0].val() + ":" + args[1].val(), ExceptionType.PluginInternalException, line_num, f);
            }
        }
        
    }
    
    public static Method GetMethod(int line_num, File file){
        com.nijikokun.register.payment.Methods m = new Methods();
        if(!Methods.hasMethod()){
            Methods.setMethod(Static.getServer().getPluginManager());
            //initialize our plugin if it isn't already
//            Plugin [] plugins = Static.getServer().getPluginManager().getPlugins();
//            for(Plugin plugin : plugins){
//                if(m.setMethod(plugin)){
//                    break;
//                }
//            }
        }
        if(Methods.getMethod() == null){
            throw new ConfigRuntimeException("No Economy plugins appear to be loaded", ExceptionType.InvalidPluginException, line_num, file);
        } else {
            return Methods.getMethod();
        }
    }
    
    public static MethodAccount GetAccount(String fname, int line_num, File file, Construct ... args){
        String name = args[0].val();
        MethodAccount m = GetMethod(line_num, file).getAccount(name);
        if(m == null){
            throw new ConfigRuntimeException("Could not access an account by that name (" + args[0].val() + ")", ExceptionType.PluginInternalException, line_num, file);
        } else {
            return m;
        }
    }
    
    public static MethodBankAccount GetBankAccount(String fname, int line_num, File file, Construct ... args){
        String bank_name = args[0].val();
        String account_name = args[1].val();
        MethodBankAccount m = GetMethod(line_num, file).getBankAccount(bank_name, account_name);
        if(m == null){
            throw new ConfigRuntimeException("Could not access a bank account by that name (" + args[0].val() + ":" + args[1].val() + ")", ExceptionType.PluginInternalException, line_num, file);
        } else {
            return m;
        }
    }

}
