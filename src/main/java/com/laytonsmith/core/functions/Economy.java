

package com.laytonsmith.core.functions;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;

/**
 *
 * @author Layton
 */
public class Economy {
    
    private static void CheckInstallation() throws ConfigRuntimeException{
        boolean failure = true;
        try{
            economy.getName();
            failure = false;
        } catch(NoClassDefFoundError e){            
        } catch(NullPointerException e){            
        }
        if(failure){
            throw new ConfigRuntimeException("You are attempting to use"
                    + " an economy function, and your economy setup is not valid."
                    + " Please install Vault and an Economy plugin before attempting"
                    + " to use any of the Economy functions.", ExceptionType.InvalidPluginException, Target.UNKNOWN);
        }
    }
    
    //Small abstraction layer around the economy plugin handler
    private static class Account {
        
        String name;

        private Account(String name) { 
            CheckInstallation();
            this.name = name;
        }

        private boolean SetBalance(double number){
            double current = economy.getBalance(name);
            if(number < current){
                //Withdrawal
                return economy.withdrawPlayer(name, current - number).transactionSuccess();
            } else {
                //Deposit
                return economy.depositPlayer(name, number - current).transactionSuccess();
            }
        }
        
        private boolean divide(double number) {
            return SetBalance(balance() / number);
        }

        private boolean multiply(double number) {
            return SetBalance(balance() * number);
        }

        private boolean subtract(double number) {
            return SetBalance(balance() - number);
        }

        private boolean add(double number) {
            return SetBalance(balance() + number);
        }

        private boolean set(double number) {
            return SetBalance(number);
        }
        
        private double balance(){
            return economy.getBalance(name);      
        }
        
    }
    
    private static class BankAccount {

        String bank_name;
        
        private BankAccount(String bank_name) {
            CheckInstallation();
            this.bank_name = bank_name;
        }
        
        private boolean SetBalance(double number){
            double current = economy.bankBalance(bank_name).balance;
            if(number < current){
                //Withdrawal
                return economy.bankWithdraw(bank_name, current - number).transactionSuccess();
            } else {
                //Deposit
                return economy.bankDeposit(bank_name, number - current).transactionSuccess();
            }
        }

        private boolean remove() {
            return economy.deleteBank(bank_name).transactionSuccess();
        }

        private boolean divide(double number) {
            return SetBalance(balance() / number);
        }

        private boolean multiply(double number) {
            return SetBalance(balance() * number);
        }

        private boolean subtract(double number) {
            return SetBalance(balance() - number);
        }

        private boolean add(double number) {
            return SetBalance(balance() + number);
        }

        private boolean set(double number) {
            return SetBalance(number);
        }
        
        private double balance(){
            return economy.bankBalance(bank_name).balance;           
        }
        
    }
    
    private static net.milkbowl.vault.economy.Economy economy;
    
    public static Boolean setupEconomy(){
        net.milkbowl.vault.economy.Economy economyProvider = Static.getServer().getEconomy();
        if (economyProvider != null) {
            economy = economyProvider;
        }

        return (economy != null);
    }
    
    public static String docs(){
        return "Provides functions to hook into the server's economy plugin. To use any of these functions, you must have one of the"
                + " following economy plugins installed: iConomy 4,5,6, BOSEconomy 6 & 7, EssentialsEcon,"
                + " 3Co, MultiCurrency, MineConomy, eWallet, EconXP, CurrencyCore, CraftConomy."
                + " In addition, you must download the [http://dev.bukkit.org/server-mods/vault/ Vault plugin]. Beyond this,"
                + " there is no special setup to get the economy functions working, assuming they work for you in game using"
                + " the plugin's default controls. Bank controls may not be supported in your particular"
                + " plugin, check the details of that particular plugin.";
    }
    
    @api public static class acc_balance extends AbstractFunction{

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
        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            Account ma = GetAccount(this.getName(), t, args);
            return new CDouble(ma.balance(), t);
        }
        
    }
    @api public static class acc_set extends AbstractFunction{

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

        
        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            if(GetAccount(this.getName(), t, args).set(Static.getNumber(args[1]))){
                return new CVoid(t);
            } else {
                throw new ConfigRuntimeException("An error occured when trying to set the balance on account " + args[0].val(), ExceptionType.PluginInternalException, t);
            }
        }
        
    }
    @api public static class acc_add extends AbstractFunction{

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

        
        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            if(GetAccount(this.getName(), t, args).add(Static.getNumber(args[1]))){
                return new CVoid(t);
            } else {
                throw new ConfigRuntimeException("An error occured when trying to add to the balance on account " + args[0].val(), ExceptionType.PluginInternalException, t);
            }
        }
        
    }
    @api public static class acc_subtract extends AbstractFunction{

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

        
        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            if(GetAccount(this.getName(), t, args).subtract(Static.getNumber(args[1]))){
                return new CVoid(t);
            } else {
                throw new ConfigRuntimeException("An error occured when trying to subtract from the balance on account " + args[0].val(), ExceptionType.PluginInternalException, t);
            }
        }
        
    }
    @api public static class acc_multiply extends AbstractFunction{

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

        
        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            if(GetAccount(this.getName(), t, args).multiply(Static.getNumber(args[1]))){
                return new CVoid(t);
            } else {
                throw new ConfigRuntimeException("An error occured when trying to multiply the balance on account " + args[0].val(), ExceptionType.PluginInternalException, t);
            }
        }
        
    }
    @api public static class acc_divide extends AbstractFunction{

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

        
        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            if(GetAccount(this.getName(), t, args).divide(Static.getNumber(args[1]))){
                return new CVoid(t);
            } else {
                throw new ConfigRuntimeException("An error occured when trying to divide the balance on account " + args[0].val(), ExceptionType.PluginInternalException, t);
            }
        }
        
    }
    @api public static class acc_remove extends AbstractFunction{

        public String getName() {
            return "acc_remove";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "void {account_name} Removes the specified account from the game - Currently unimplemented, due to lack of support in Vault. Calling"
                    + " this function will currently always throw an exception.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException, ExceptionType.InvalidPluginException};
        }

        public boolean isRestricted() {
            return true;
        }

        
        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            throw new ConfigRuntimeException("An error occured while trying to remove the player's account, due to"
                    + " this operation being unsupported in Vault. If you want to see this feature supported, "
                    + " contact the authors of Vault!", ExceptionType.PluginInternalException, t);
//            if(GetAccount(this.getName(), t, args).remove()){
//                return new CVoid(t);
//            } else {
//                throw new ConfigRuntimeException("An error occured when trying to set the balance on account " + args[0].val(), ExceptionType.PluginInternalException, t);
//            }
        }
        
    }
    @api public static class bacc_balance extends AbstractFunction{

        public String getName() {
            return "bacc_balance";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "void {bank_name} Gets the specified bank account's balance";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException, ExceptionType.InvalidPluginException};
        }

        public boolean isRestricted() {
            return true;
        }

        
        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            return new CDouble(GetBankAccount(this.getName(), t, args).balance(), t);
        }
        
    }
    @api public static class bacc_set extends AbstractFunction{

        public String getName() {
            return "bacc_set";
        }

        public Integer[] numArgs() {
            return new Integer[]{3};
        }

        public String docs() {
            return "void {bank_name, value} Sets the bank account's balance to the given amount";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException, ExceptionType.InvalidPluginException, ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }

        
        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            if(GetBankAccount(this.getName(), t, args).set(Static.getNumber(args[2]))){
                return new CVoid(t);
            } else {
                throw new ConfigRuntimeException("An error occured when trying to set the balance on bank account " + args[0].val() + ":" + args[1].val(), ExceptionType.PluginInternalException, t);
            }
        }
        
    }
    @api public static class bacc_add extends AbstractFunction{

        public String getName() {
            return "bacc_add";
        }

        public Integer[] numArgs() {
            return new Integer[]{3};
        }

        public String docs() {
            return "void {bank_name, value} Adds the specified amount to the bank account's balance";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException, ExceptionType.InvalidPluginException, ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }

        
        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            if(GetBankAccount(this.getName(), t, args).add(Static.getNumber(args[2]))){
                return new CVoid(t);
            } else {
                throw new ConfigRuntimeException("An error occured when trying to add to the balance on bank account " + args[0].val() + ":" + args[1].val(), ExceptionType.PluginInternalException, t);
            }
        }
        
    }
    @api public static class bacc_subtract extends AbstractFunction{

        public String getName() {
            return "bacc_subtract";
        }

        public Integer[] numArgs() {
            return new Integer[]{3};
        }

        public String docs() {
            return "void {bank_name, value} Subtracts the specified amount from the bank account's balance";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException, ExceptionType.InvalidPluginException, ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }

        
        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            if(GetBankAccount(this.getName(), t, args).subtract(Static.getNumber(args[2]))){
                return new CVoid(t);
            } else {
                throw new ConfigRuntimeException("An error occured when trying to subtract from the balance on bank account " + args[0].val() + ":" + args[1].val(), ExceptionType.PluginInternalException, t);
            }
        }
        
    }
    @api public static class bacc_multiply extends AbstractFunction{

        public String getName() {
            return "bacc_multiply";
        }

        public Integer[] numArgs() {
            return new Integer[]{3};
        }

        public String docs() {
            return "void {bank_name, value} Multiplies the given bank account's balance by the given value";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException, ExceptionType.InvalidPluginException, ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }

        
        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            if(GetBankAccount(this.getName(), t, args).multiply(Static.getNumber(args[2]))){
                return new CVoid(t);
            } else {
                throw new ConfigRuntimeException("An error occured when trying to multiply the balance on bank account " + args[0].val() + ":" + args[1].val(), ExceptionType.PluginInternalException, t);
            }
        }
        
    }
    @api public static class bacc_divide extends AbstractFunction{

        public String getName() {
            return "bacc_divide";
        }

        public Integer[] numArgs() {
            return new Integer[]{3};
        }

        public String docs() {
            return "void {bank_name, value} Divides the bank account's balance by the given value";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException, ExceptionType.InvalidPluginException, ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }

        
        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            if(GetBankAccount(this.getName(), t, args).divide(Static.getNumber(args[2]))){
                return new CVoid(t);
            } else {
                throw new ConfigRuntimeException("An error occured when trying to divide the balance on bank account " + args[0].val() + ":" + args[1].val(), ExceptionType.PluginInternalException, t);
            }
        }
        
    }
    @api public static class bacc_remove extends AbstractFunction{

        public String getName() {
            return "bacc_remove";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "void {bank_name} Removes the given bank account from the game";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException, ExceptionType.InvalidPluginException};
        }

        public boolean isRestricted() {
            return true;
        }

        
        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            if(GetBankAccount(this.getName(), t, args).remove()){
                return new CVoid(t);
            } else {
                throw new ConfigRuntimeException("An error occured when trying to remove the bank account " + args[0].val() + ":" + args[1].val(), ExceptionType.PluginInternalException, t);
            }
        }
        
    }
    
    
    private static Account GetAccount(String fname, Target tile, Construct ... args){
        String name = args[0].val();
        Account m = new Account(name);
        if(m == null){
            throw new ConfigRuntimeException("Could not access an account by that name (" + args[0].val() + ")", ExceptionType.PluginInternalException, tile);
        } else {
            return m;
        }
    }
    
    private static BankAccount GetBankAccount(String fname, Target tile, Construct ... args){
        String bank_name = args[0].val();
        BankAccount m = new BankAccount(bank_name);
        if(m == null){
            throw new ConfigRuntimeException("Could not access a bank account by that name (" + args[0].val() + ")", ExceptionType.PluginInternalException, tile);
        } else {
            return m;
        }
    }

}
