

package com.laytonsmith.testing;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.RunnableQueue;
import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorld;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import com.laytonsmith.abstraction.enums.MCTone;
import com.laytonsmith.annotations.convert;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.EventMixinInterface;
import com.laytonsmith.core.exceptions.*;
import com.laytonsmith.core.functions.BasicLogic.equals;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionBase;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.fail;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * 
 * @author Layton
 */
@RunWith(PowerMockRunner.class)

public class StaticTest {
	static com.laytonsmith.core.environments.Environment env;
	
	static{
		try {
			Implementation.setServerType(Implementation.Type.TEST);
			env = Static.GenerateStandaloneEnvironment();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
    /**
     * Tests the boilerplate functions in a Function. While all functions should conform to
     * at least this, it is useful to also use the more strict TestBoilerplate function.
     * @param f 
     */
    public static void TestBoilerplate(FunctionBase ff, String name) throws Exception {
        if(!(ff instanceof Function)){
            return;
        }
        Function f = (Function)ff;
        //For the "quality test code coverage" number, set this to true
        boolean runQualityTestsOnly = false;

        MCServer fakeServer = StaticTest.GetFakeServer();
        MCPlayer fakePlayer = StaticTest.GetOnlinePlayer("wraithguard01", fakeServer);

        //make sure that these functions don't throw an exception. Any other results
        //are fine
        f.isRestricted();
        f.runAsync();
        f.preResolveVariables();
        f.thrown();

        //name should match the given value
        if (!f.getName().equals(name)) {
            fail("Expected name of function to be " + name + ", but was given " + f.getName());
        }

        //docs needs to at least be more than a non-empty string, though in the future this should follow a more strict 
        //requirement set.
        if (f.docs().length() <= 0) {
            fail("docs must return a non-empty string");
        }

        TestDocs(f);

        if (f.numArgs().length == 0) {
            fail("numArgs must return an Integer array with more than zero values");
        }

        //If we want a "quality test coverage" number, we can't run this section, because it bombards the code
        //with random data to see if it fails in expected ways (to simulate how a user could run the scripts)
        //If we are interested in tests that are specific to the functions however, we shouldn't run this.
        if (!runQualityTestsOnly && f.getClass().getAnnotation(noboilerplate.class) == null) {
            TestExec(f, fakePlayer, "fake player");
            TestExec(f, null, "null command sender");
            TestExec(f, StaticTest.GetFakeConsoleCommandSender(), "fake console command sender");
        }
        
        //Let's make sure that if execs is defined in the class, useSpecialExec returns true.
        //Same thing for optimize/canOptimize and optimizeDynamic/canOptimizeDynamic
		if(f instanceof Optimizable){
			Set<Optimizable.OptimizationOption> options = ((Optimizable)f).optimizationOptions();
			if(options.contains(Optimizable.OptimizationOption.CONSTANT_OFFLINE) && options.contains(Optimizable.OptimizationOption.OPTIMIZE_CONSTANT)){
				fail(f.getName() + " declares both CONSTANT_OFFLINE and OPTIMIZE_CONSTANT, which are mutually exclusive.");
			}
		}
        for(Method method : f.getClass().getDeclaredMethods()){
            if(method.getName().equals("execs")){
                if(!f.useSpecialExec()){
                    fail(f.getName() + " declares execs, but returns false for useSpecialExec.");
                }
            }
			
			if(f instanceof Optimizable){
				Set<Optimizable.OptimizationOption> options = ((Optimizable)f).optimizationOptions();
				if(method.getName().equals("optimize")){
					if(!options.contains(Optimizable.OptimizationOption.OPTIMIZE_CONSTANT)){
						fail(f.getName() + " declares optimize, but does not declare that it can OPTIMIZE_CONSTANT");
					}
				}
				if(method.getName().equals("optimizeDynamic")){
					if(!options.contains(Optimizable.OptimizationOption.OPTIMIZE_DYNAMIC)){
						fail(f.getName() + " declares optimizeDynamic, but does not declare that it can OPTIMIZE_DYNAMIC");
					}
				}
			}
        }
        

        //now the only function left to test is exec. This cannot be abstracted, unfortunately.
    }

    /**
     * Checks to see if the documentation follows the specified format
     */
    public static void TestDocs(Function f) {
        //TODO
    }

    private static ArrayList<String> tested = new ArrayList<String>();
    public static void TestExec(Function f, MCCommandSender p, String commandType) throws Exception {
        if(tested.contains(f.getName() + String.valueOf(p))){
            return;
        }
        tested.add(f.getName() + String.valueOf(p));
        env.getEnv(CommandHelperEnvironment.class).SetCommandSender(p);
        //See if the function throws something other than a ConfigRuntimeException or CancelCommandException if we send it bad arguments,
        //keeping in mind of course, that it isn't supposed to be able to accept the wrong number of arguments. Specifically, we want to try
        //strings, numbers, arrays, and nulls
        for (Integer i : f.numArgs()) {
            if (i == Integer.MAX_VALUE) {
                //er.. let's just try with 10...
                i = 10;
            }
            Construct[] con = new Construct[i];
            //Throw the book at it. Most functions will fail, and that is ok, what isn't
            //ok is if it throws an unexpected type of exception. It should only ever
            //throw a ConfigRuntimeException, or a CancelCommandException. Further,
            //if it throws a ConfigRuntimeException, the documentation should state so.
			for (int z = 0; z < 10; z++) {
				for (int a = 0; a < i; a++) {
                    switch (z) {
                        case 0:
                            con[a] = C.onstruct("hi");
                            break;
                        case 1:
                            con[a] = C.onstruct(1);
                            break;
                        case 2:
                            con[a] = C.Array(C.onstruct("hi"), C.onstruct(1));
                            break;
                        case 3:
                            con[a] = C.Null();
                            break;
                        case 4:
                            con[a] = C.onstruct(-1);
                            break;
                        case 5:
                            con[a] = C.onstruct(0);
                            break;
                        case 6:
                            con[a] = C.onstruct(100);
                            break;
                        case 7:
                            con[a] = C.onstruct(a);
                            break;
                        case 8:
                            con[a] = C.onstruct(true);
                            break;
                        case 9:
                            con[a] = C.onstruct(false);
                            break;
                    }
                }
                try {
                    f.exec(Target.UNKNOWN, env, con);
                } catch (CancelCommandException e) {
                } catch (ConfigRuntimeException e) {
                    if (e.getExceptionType() != null) {
                        if (f.thrown() == null || !Arrays.asList(f.thrown()).contains(e.getExceptionType())) {
                            fail("The documentation for " + f.getName() + " doesn't state that it can throw a "
                                    + e.getExceptionType() + ", but it did.");
                        }
                    } //else it's uncatchable, which while it probably shouldn't happen often, it can.
                } catch (Throwable e) {
                    if (e instanceof LoopBreakException && !f.getName().equals("break")) {
                        fail("Only break() can throw LoopBreakExceptions");
                    }
                    if (e instanceof LoopContinueException && !f.getName().equals("continue")) {
                        fail("Only continue() can throw LoopContinueExceptions");
                    }
                    if (e instanceof FunctionReturnException && !f.getName().equals("return")) {
                        fail("Only return() can throw FunctionReturnExceptions");
                    }
                    if(e instanceof NullPointerException){
                        if(!brokenJunk.contains(f.getName() + commandType)){
                            System.err.println(f.getName() + " breaks if you send it the following while using a " + commandType + ": " + Arrays.deepToString(con));
                            System.err.println("Here is the first few stack trace lines:");
                            System.err.println("\t" + e.getStackTrace()[0].toString());
                            System.err.println("\t" + e.getStackTrace()[1].toString());
                            System.err.println("\t" + e.getStackTrace()[2].toString());
                            brokenJunk.add(f.getName() + commandType);
                        }
                    }
                }
            }
        }
    }
    static Set<String> brokenJunk = new TreeSet<String>();

    public static void TestClassDocs(String docs, Class container) {
        if (docs.length() <= 0) {
            fail("The docs for the " + container.getSimpleName() + " class are missing");
        }
    }

    /**
     * Gets the value out of s construct, ignoring information like line numbers.
     * @return 
     */
    public static Object Val(Construct c) {
        return c.val();
    }

    /**
     * Checks to see if two constructs are equal, using the same method that MethodScript equals() uses. In
     * fact, this method depends on equals() working, as it actually uses the function.
     * @param expected
     * @param actual 
     */
    public static void assertCEquals(Construct expected, Construct actual) throws CancelCommandException {
        equals e = new equals();
        CBoolean ret = (CBoolean) e.exec(Target.UNKNOWN, null, expected, actual);
        if (ret.getBoolean() == false) {
            throw new AssertionError("Expected " + expected + " and " + actual + " to be equal to each other");
        }
    }

    /**
     * Does the opposite of assertCEquals
     * @param expected
     * @param actual
     * @throws CancelCommandException 
     */
    public static void assertCNotEquals(Construct expected, Construct actual) throws CancelCommandException {
        equals e = new equals();
        CBoolean ret = (CBoolean) e.exec(Target.UNKNOWN, null, expected, actual);
        if (ret.getBoolean() == true) {
            throw new AssertionError("Did not expect " + expected + " and " + actual + " to be equal to each other");
        }
    }

    /**
     * Verifies that the given construct <em>resolves</em> to true. The resolution uses Static.getBoolean to
     * do the resolution.
     * @param actual 
     */
    public static void assertCTrue(Construct actual) {
        if (!Static.getBoolean(actual)) {
            fail("Expected '" + actual.val() + "' to resolve to true, but it did not");
        }
    }

    /**
     * Verifies that the given construct <em>resolves</em> to false. The resolution uses Static.getBoolean to
     * do the resolution.
     * @param actual 
     */
    public static void assertCFalse(Construct actual) {
        if (Static.getBoolean(actual)) {
            fail("Expected '" + actual.val() + "' to resolve to false, but it did not");
        }
    }

    /**
     * This function is used to assert that the type of a construct is one of the specified types.
     * @param test
     * @param retTypes 
     */
    public static void assertReturn(Construct test, Class... retTypes) {
        if (!Arrays.asList(retTypes).contains(test.getClass())) {
            StringBuilder b = new StringBuilder();
            if (retTypes.length == 1) {
                b.append("Expected return type to be ").append(retTypes[0].getSimpleName()).append(", but found ").append(test.getClass().getSimpleName());
            } else if (retTypes.length == 2) {
                b.append("Expected return type to be either ").append(retTypes[0].getSimpleName()).append(" or ").append(retTypes[1].getSimpleName()).append(", but found ").append(test.getClass().getSimpleName());
            } else {
                b.append("Expected return type to be one of: ");
                for (int i = 0; i < retTypes.length; i++) {
                    if (i < retTypes.length - 1) {
                        b.append(retTypes[i].getSimpleName()).append(", ");
                    } else {
                        b.append("or ").append(retTypes[i].getSimpleName());
                    }
                }
                b.append(", but found ").append(test.getClass().getSimpleName());
            }
            throw new AssertionError(b);
        }
    }

    public static List<Token> tokens(Token... array) {
        List<Token> tokens = new ArrayList<Token>();
        for (Token t : array) {
            tokens.add(t);
        }
        return tokens;
    }
    
    public static MCPlayer GetOnlinePlayer(){
        MCServer s = GetFakeServer();
        return GetOnlinePlayer("wraithguard01", s);
    }
    
    public static MCPlayer GetOnlinePlayer(MCServer s){
        return GetOnlinePlayer("wraithguard01", s);
    }
    
    public static MCPlayer GetOnlinePlayer(String name, MCServer s){
        return GetOnlinePlayer(name, "world", s);
    }
    
    public static MCPlayer GetOnlinePlayer(String name, String worldName, MCServer s){
        MCPlayer p = mock(MCPlayer.class);
        MCWorld w = mock(MCWorld.class);
        MCLocation fakeLocation = StaticTest.GetFakeLocation(w, 0, 0, 0);
        MCItemStack fakeItemStack = mock(MCItemStack.class);
        
        when(w.getName()).thenReturn(worldName);
        when(p.getWorld()).thenReturn(w);
        when(p.isOnline()).thenReturn(true);
        when(p.getName()).thenReturn(name);        
        when(p.getServer()).thenReturn(s); 
        when(p.isOp()).thenReturn(true);
        if(s != null && s.getOnlinePlayers() != null){
            List<MCPlayer> online = new ArrayList<MCPlayer>(Arrays.asList(s.getOnlinePlayers()));
            boolean alreadyOnline = false;
            for(MCPlayer o : online){
                if(o.getName().equals(name)){
                    alreadyOnline = true;
                    break;
                }
            }
            if(!alreadyOnline){
                online.add(p);
                when(s.getOnlinePlayers()).thenReturn(online.toArray(new MCPlayer[]{}));
            }            
        }
        
        
        //Plethora of fake data
        when(p.getCompassTarget()).thenReturn(fakeLocation);
        when(p.getItemAt((Integer)Mockito.any())).thenReturn(fakeItemStack);
        return p;
    }
    
    public static MCPlayer GetOp(String name, MCServer s){
        MCPlayer p = GetOnlinePlayer(name, s);
        when(p.isOp()).thenReturn(true);
        return p;
    }
    
    public static BukkitMCWorld GetWorld(String name){
        BukkitMCWorld w = mock(BukkitMCWorld.class);
        when(w.getName()).thenReturn(name);
        return w;
    }
    
    public static MCConsoleCommandSender GetFakeConsoleCommandSender(){
        MCConsoleCommandSender c = mock(MCConsoleCommandSender.class);
        when(c.getName()).thenReturn("CONSOLE");
        MCServer s = GetFakeServer();
        when(c.getServer()).thenReturn(s);
        return c;
    }
    
    public static MCLocation GetFakeLocation(MCWorld w, double x, double y, double z){
        MCLocation loc = mock(BukkitMCLocation.class);
        when(loc.getWorld()).thenReturn(w);
        when(loc.getX()).thenReturn(x);
        when(loc.getY()).thenReturn(y - 1);
        when(loc.getZ()).thenReturn(z);
        return loc;
    }
    
    public static Object GetVariable(Object instance, String var) throws Exception{
        return GetVariable(instance.getClass(), var, instance);
    }
    public static Object GetVariable(Class c, String var, Object instance) throws Exception{
        Field f = c.getField(var);
        f.setAccessible(true);
        return f.get(instance);
    }
    
    /**
     * Lexes, compiles, and runs a given MethodScript, using the given player.
     * @param script
     * @param player
     * @throws ConfigCompileException 
     */
    public static void Run(String script, MCCommandSender player) throws ConfigCompileException{
        Run(script, player, null, null);
    }
    
    public static void Run(String script, MCCommandSender player, MethodScriptComplete done, Environment env) throws ConfigCompileException{
		InstallFakeServerFrontend();
		if(env == null){
			env = StaticTest.env;
		}
        env.getEnv(CommandHelperEnvironment.class).SetCommandSender(player);
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true)), env, done, null);
    }
    
    public static void RunCommand(String combinedScript, MCCommandSender player, String command) throws ConfigCompileException{
		RunCommand(combinedScript, player, command, env);
	}
    public static void RunCommand(String combinedScript, MCCommandSender player, String command, Environment env) throws ConfigCompileException{
        InstallFakeServerFrontend();
		if(env == null){
			env = StaticTest.env;
		}
        env.getEnv(CommandHelperEnvironment.class).SetCommandSender(player);
        List<Script> scripts = MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(combinedScript, null, false));
        for(Script s : scripts){
            s.compile();
            if(s.match(command)){
                s.run(s.getVariables(command), env, null);
            }
        }
    }
    
	public static String SRun(String script, MCCommandSender player, Environment env) throws ConfigCompileException{
		InstallFakeServerFrontend();
        final StringBuffer b = new StringBuffer();
        Run(script, player, new MethodScriptComplete() {

            public void done(String output) {
                b.append(output);
            }
        }, env);
        return b.toString();		
	}
    public static String SRun(String script, MCCommandSender player) throws ConfigCompileException{
		return SRun(script, player, env);
    }
    //TODO: Fix this
//    public static void RunVars(List<Variable> vars, String script, MCCommandSender player) throws ConfigCompileException{
//        Env env = new Env();
//        env.SetCommandSender(player);
//        MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null));
//        injectAliasCore();
//        Script s = MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(script, null), env).get(0);        
//        s.compile();
//        s.run(vars, env, null);
//        
//    }
    
    //Blarg. Dumb thing.
//    private static void injectAliasCore() throws ConfigCompileException{
//        PermissionsResolverManager prm = mock(PermissionsResolverManager.class);
//        CommandHelperPlugin chp = mock(CommandHelperPlugin.class);
//        AliasCore ac = new AliasCore(new File("plugins/CommandHelper/config.txt"), 
//                new File("plugins/CommandHelper/LocalPackages"), 
//                new File("plugins/CommandHelper/preferences.ini"), 
//                new File("plugins/CommandHelper/main.ms"), prm, chp);
//        try{
//            Field aliasCore = CommandHelperPlugin.class.getDeclaredField("ac");
//            aliasCore.setAccessible(true);
//            aliasCore.set(null, ac);
//        } catch(Exception e){
//            throw new RuntimeException("Core could not be injected", e);
//        }
//    }
    
    /**
     * Creates an entire fake server environment, adding players and everything.
     */
    public static MCServer GetFakeServer(){
        MCServer fakeServer = mock(MCServer.class);
        String [] pnames = new String[]{"wraithguard01", "wraithguard02", "wraithguard03"};
        ArrayList<MCPlayer> pps = new ArrayList<MCPlayer>();
        for(String p : pnames){
            MCPlayer pp = GetOnlinePlayer(p, fakeServer);
            pps.add(pp);
        }
        when(fakeServer.getOnlinePlayers()).thenReturn(pps.toArray(new MCPlayer[]{}));  
        CommandHelperPlugin.myServer = fakeServer;  
        return fakeServer;
    }
    
    private static boolean frontendInstalled = false;
    /**
     * This installs a fake server frontend. You must have already included @PrepareForTest(Static.class)
     * in the calling test code, which will allow the proper static methods to be mocked.
     */
    public static void InstallFakeServerFrontend(){
        if(frontendInstalled){
            return;
        }
		ClassDiscovery.getDefaultInstance().addDiscoveryLocation(ClassDiscovery.GetClassContainer(Static.class));
		ClassDiscovery.getDefaultInstance().addDiscoveryLocation(ClassDiscovery.GetClassContainer(StaticTest.class));
		Implementation.setServerType(Implementation.Type.TEST);
        AliasCore fakeCore = mock(AliasCore.class);
        fakeCore.autoIncludes = new ArrayList<File>();
		SetPrivate(CommandHelperPlugin.class, "ac", fakeCore, AliasCore.class);       
       frontendInstalled = true;
		try {
			Prefs.init(new File("preferences.ini"));
		} catch (IOException ex) {
			Logger.getLogger(StaticTest.class.getName()).log(Level.SEVERE, null, ex);
		}
		CHLog.initialize(new File("."));
    }
    
    /**
     * Installs the fake convertor into the server, so event based calls will
     * work. Additionally, adds the fakePlayer to the server, if player based
     * events are to be called, this is the player returned.
     * @param fakePlayer 
     */
    public static void InstallFakeConvertor(MCPlayer fakePlayer) throws Exception{
        InstallFakeServerFrontend();               
        try {
            //We need to add the test directory to the ClassDiscovery path
            //This should probably not be hard coded at some point.
            ClassDiscovery.getDefaultInstance().addDiscoveryLocation(new File("./target/test-classes").toURI().toURL());
        }
        catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
        
        Implementation.setServerType(Implementation.Type.TEST);
        MCServer fakeServer = GetFakeServer();
        TestConvertor.fakeServer = fakeServer;
        FakeServerMixin.fakePlayer = fakePlayer;       
        
    }
    
    @convert(type=Implementation.Type.TEST)
    public static class TestConvertor extends AbstractConvertor{
        
        private static MCServer fakeServer;
		private RunnableQueue queue = new RunnableQueue("TestConvertorRunnableQueue");

        public MCLocation GetLocation(MCWorld w, double x, double y, double z, float yaw, float pitch) {
             return StaticTest.GetFakeLocation(w, x, y + 1, z);
        }

        public Class GetServerEventMixin() {
            return FakeServerMixin.class;
        }

        public MCEnchantment[] GetEnchantmentValues() {
            Convertor c = new BukkitConvertor();
            return c.GetEnchantmentValues();
        }

        public MCEnchantment GetEnchantmentByName(String name) {
            Convertor c = new BukkitConvertor();
            return c.GetEnchantmentByName(name);
        }

        public MCServer GetServer() {
            return fakeServer;
        }

        public MCItemStack GetItemStack(int type, int qty) {
            Convertor c = new BukkitConvertor();
            return c.GetItemStack(type, qty);
        }

        public void Startup(CommandHelperPlugin chp) {
            //Nothing.
        }

        public int LookupItemId(String materialName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String LookupMaterialName(int id) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public MCItemStack GetItemStack(int type, int data, int qty) {
            Convertor c = new BukkitConvertor();
            return c.GetItemStack(type, data, qty);
        }

        public int SetFutureRunnable(DaemonManager dm, long ms, Runnable r) {
			//This needs fixing later
			queue.invokeLater(dm, r);
			return 0;
        }

        public void ClearAllRunnables() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void ClearFutureRunnable(int id) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int SetFutureRepeater(DaemonManager dm, long ms, long initialDelay, Runnable r) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public MCEntity GetCorrectEntity(MCEntity e) {
            return e;
        }

		public MCInventory GetEntityInventory(int entityID) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public MCInventory GetLocationInventory(MCLocation location) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public MCNote GetNote(int octave, MCTone tone, boolean sharp) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public int getMaxBlockID() {
			return 0;
		}

		public int getMaxItemID() {
			return 0;
		}

		public int getMaxRecordID() {
			return 0;
		}

		public MCColor GetColor(final int red, final int green, final int blue) {
			return new MCColor() {

				public int getRed() {
					return red;
				}

				public int getGreen() {
					return green;
				}

				public int getBlue() {
					return blue;
				}

				public MCColor build(int red, int green, int blue) {
					return GetColor(red, green, blue);
				}
			};
		}

		public MCFireworkBuilder GetFireworkBuilder() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public MCPluginMeta GetPluginMeta() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public MCMaterial getMaterial(int id) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public MCItemMeta GetCorrectMeta(MCItemMeta im) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public List<MCEntity> GetEntitiesAt(MCLocation loc, double radius) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public MCRecipe GetNewRecipe(MCRecipeType type, MCItemStack result) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public MCRecipe GetRecipe(MCRecipe unspecific) {
			throw new UnsupportedOperationException("Not supported yet.");
		}
    }
    
    public static class FakeServerMixin implements EventMixinInterface{
        
        public static MCPlayer fakePlayer;
        public boolean cancelled = false;
        
        public FakeServerMixin(AbstractEvent e){
            
        }

        public void cancel(BindableEvent e, boolean state) {
            cancelled = state;
        }

        public boolean isCancellable(BindableEvent o) {
            return true;
        }

        public Map<String, Construct> evaluate_helper(BindableEvent e) throws EventException {
            Map<String, Construct> map = new HashMap<String, Construct>();
            if(fakePlayer != null){
                map.put("player", new CString(fakePlayer.getName(), Target.UNKNOWN));
            }
            return map;
        }

        public void manualTrigger(BindableEvent e) {
            throw new RuntimeException("Manual triggering is not supported in tests yet");
        }

        public boolean isCancelled(BindableEvent o) {
            return cancelled;
        }
        
    }
    
    /**
     * Returns the value of a private (or any other variable for that matter) data member contained in the object provided.
     * If the value isn't there, the test fails automatically.
     * @param in The object to look in, or the Class object for static varibles.
     * @param name The name of the variable to get.
     * @return 
     */
    public static <T> T GetPrivate(Object in, String name, Class<T> expected){
        return GetSetPrivate(in, name, null, false, expected);
    }
    
	/**
	 * Sets the value of a private (or any other variable for that matter) data member contained in the object provided.
	 * @param in Either the class of the object (for static variables) or an instance of the object.
	 * @param name
	 * @param value
	 * @param expected S
	 */
    public static void SetPrivate(Object in, String name, Object value, Class expected){
        GetSetPrivate(in, name, value, true, expected);
    }
    
    private static <T> T GetSetPrivate(Object in, String name, Object value, boolean isSet, Class<T> expected){
        Object ret = null;
        try {
            Field f = null;
            Class search = in.getClass();
			if(in instanceof Class){
				search = (Class)in;
			}
            while(search != null){
                try{
                    f = search.getDeclaredField(name);
                    break;
                } catch(NoSuchFieldException e){
                    search = search.getSuperclass();
                    continue;
                }
            }
            if(f == null){
                throw new NoSuchFieldException();
            }
            f.setAccessible(true);
            if(expected != null && !expected.isAssignableFrom(f.getType())){
                fail("Expected the value to be a " + expected.getName() + ", but it was actually a " + f.getType().getName());
            }
            if(isSet){
                f.set(in, value);
            } else {
                ret = f.get(in);               
            }
        }
        catch (IllegalArgumentException ex) {
            //This shouldn't happen ever, since we are using the class provided by in, and sending
            //get/set in as well.
            fail(ex.getMessage());
        }
        catch (IllegalAccessException ex) {
            //This shouldn't happen ever, since we set it to accessible
            fail(ex.getMessage());
        }
        catch (NoSuchFieldException ex) {
            fail("No such field \"" + name + "\" exists in the class " + in.getClass().getName());
        } catch (SecurityException ex) {
            fail("A security policy is preventing the test from getting \"" + name + "\" in the object provided.");
        }
        return (T)ret;
    }
    
}
