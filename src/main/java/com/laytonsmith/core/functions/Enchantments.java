package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREEnchantmentException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CREPlayerOfflineException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Enchantments {

	public static String docs() {
		return "Provides methods for dealing with enchanted items";
	}

	/**
	 * Takes a minecraft enchantment name and returns an MCEnchantment
	 *
	 * @throws CREEnchantmentException
	 * @param name
	 * @param t
	 * @return
	 */
	public static MCEnchantment GetEnchantment(String name, Target t) {
		MCEnchantment e = StaticLayer.GetEnchantmentByName(name);
		if(e == null) {
			throw new CREEnchantmentException(name + " is not a valid enchantment type", t);
		}
		return e;
	}

	/**
	 * Converts the roman numeral into an integer (as a string). If the value passed in is already an integer, it is
	 * returned as is.
	 *
	 * @param value
	 * @return
	 */
	public static int ConvertLevel(Environment env, Mixed value) {
		if(value.isInstanceOf(CInt.TYPE, null, env)) {
			return (int) ((CInt) value).getInt();
		}
		String lc = value.val().toLowerCase().trim();
		try {
			return Integer.parseInt(lc);
		} catch (NumberFormatException e) {
			//Maybe roman numeral?
		}
		return romanToWestern(lc);
	}

	public static int romanToWestern(String roman) {

		int western = 0; //the numerical version
		char currentChar;
		char nextChar;

		int i = 0;

		while(i < roman.length()) {
			currentChar = roman.charAt(i);
			if(i < roman.length() - 1) {
				nextChar = roman.charAt(i + 1);
				if(getValue(currentChar) < getValue(nextChar)) {
					western += (getValue(nextChar) - getValue(currentChar));
					i += 2;
				} else {
					western += getValue(currentChar);
					i++;
				}
			} else {
				western += getValue(currentChar);
				i++;
			}
		}
		return western;

	}

	private static int getValue(char l) { //Converts the numeral to a number
		String letter = String.valueOf(l);
		if(letter.equalsIgnoreCase("I")) {
			return 1;
		}
		if(letter.equalsIgnoreCase("V")) {
			return 5;
		}
		if(letter.equalsIgnoreCase("X")) {
			return 10;
		}
		if(letter.equalsIgnoreCase("L")) {
			return 50;
		}
		if(letter.equalsIgnoreCase("C")) {
			return 100;
		}
		if(letter.equalsIgnoreCase("D")) {
			return 500;
		}
		if(letter.equalsIgnoreCase("M")) {
			return 1000;
		}
		return 0;
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class enchant_item extends AbstractFunction {

		@Override
		public String getName() {
			return "enchant_item";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{3, 4};
		}

		@Override
		public String docs() {
			return "void {[player], slot, type, level | player, slot, enchantsArray} Adds enchantments to an item in"
					+ " the player's inventory. A single enchantment type and level can be specified or an"
					+ " enchantment array may be given. If slot is null, the currently selected slot is used."
					+ " If an enchantment cannot be applied to the specified item, an EnchantmentException is thrown."
					+ " The enchantment array must have the enchantment as keys and levels as the values."
					+ " (eg. array('unbreaking': 1)) The minecraft names for enchantments may"
					+ " be used: [http://www.minecraftwiki.net/wiki/Enchanting#Enchantment_Types],"
					+ " and the level parameter may be a roman numeral as well.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREEnchantmentException.class, CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_3;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, GenericParameters generics, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;
			int offset = 0;
			if(args.length == 4 || args.length == 3 && args[2].isInstanceOf(CArray.TYPE, null, env)) {
				p = Static.GetPlayer(args[0].val(), t, env);
				offset = 1;
			} else {
				p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(p, t);
			}
			MCItemStack is = p.getItemAt(args[offset] instanceof CNull ? null : ArgumentValidation.getInt32(args[offset], t, env));
			if(is == null) {
				throw new CRECastException("There is no item at slot " + args[offset], t);
			}

			if(args[args.length - 1].isInstanceOf(CArray.TYPE, null, env)) {
				CArray ca = (CArray) args[args.length - 1];
				Map<MCEnchantment, Integer> enchants = ObjectGenerator.GetGenerator().enchants(ca, t, env);
				for(Map.Entry<MCEnchantment, Integer> en : enchants.entrySet()) {
					is.addUnsafeEnchantment(en.getKey(), en.getValue());
				}
			} else {
				int level = ConvertLevel(env, args[offset + 2]);
				if(level > 0) {
					is.addUnsafeEnchantment(GetEnchantment(args[offset + 1].val(), t), level);
				} else {
					throw new CREEnchantmentException("Invalid enchantment level: " + args[offset + 2].val(), t);
				}
			}
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class remove_item_enchant extends AbstractFunction {

		@Override
		public String getName() {
			return "remove_item_enchant";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {[player], slot, type} Removes an enchantment from an item."
					+ " The type may be a valid enchantment, or an array of enchantment names."
					+ " It can also be null, and all enchantments will be removed. If an enchantment is specified,"
					+ " and the item is not enchanted with that, it is simply ignored.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREEnchantmentException.class, CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_3;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, GenericParameters generics, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;
			int offset = 0;
			if(args.length == 3) {
				p = Static.GetPlayer(args[0].val(), t, env);
				offset = 1;
			} else {
				p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(p, t);
			}

			MCItemStack is = p.getItemAt(args[offset] instanceof CNull ? null : ArgumentValidation.getInt32(args[offset], t, env));
			if(is == null) {
				throw new CRECastException("There is no item at slot " + args[offset], t);
			}

			if(args[offset + 1].isInstanceOf(CArray.TYPE, null, env)) {
				for(String name : ((CArray) args[offset + 1]).stringKeySet()) {
					MCEnchantment e = GetEnchantment(name, t);
					is.removeEnchantment(e);
				}
			} else if(args[offset + 1] instanceof CNull) {
				for(MCEnchantment e : is.getEnchantments().keySet()) {
					is.removeEnchantment(e);
				}
			} else {
				MCEnchantment e = GetEnchantment(args[offset + 1].val(), t);
				is.removeEnchantment(e);
			}
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_item_enchants extends AbstractFunction {

		@Override
		public String getName() {
			return "get_item_enchants";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "array {[player], slot} Returns an array of arrays of the enchantments and their levels on the given"
					+ " item. For example: array('sharpness': 1, 'unbreaking': 3).";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_3;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, GenericParameters generics, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;
			Mixed slot;
			if(args.length == 2) {
				p = Static.GetPlayer(args[0].val(), t, env);
				slot = args[1];
			} else {
				p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(p, t);
				slot = args[0];
			}
			MCItemStack is = p.getItemAt(slot instanceof CNull ? null : ArgumentValidation.getInt32(slot, t, env));
			if(is == null) {
				throw new CRECastException("There is no item at slot " + slot, t);
			}
			return ObjectGenerator.GetGenerator().enchants(is.getEnchantments(), t, env);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class can_enchant_item extends AbstractFunction {

		@Override
		public String getName() {
			return "can_enchant_item";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {name, item} Given an enchantment name and an item array,"
					+ " returns whether or not that item can be enchanted with that enchantment."
					+ " Throws an EnchantmentException if the name is not a valid enchantment type.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREEnchantmentException.class, CREFormatException.class, CRECastException.class,
					CRERangeException.class, CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_3;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, GenericParameters generics, Mixed... args) throws ConfigRuntimeException {
			MCEnchantment e = GetEnchantment(args[0].val(), t);
			MCItemStack is = ObjectGenerator.GetGenerator().item(args[1], t, env);
			return CBoolean.get(e.canEnchantItem(is));
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_enchant_max extends AbstractFunction {

		@Override
		public String getName() {
			return "get_enchant_max";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "int {name} Given an enchantment name, returns the max level it can be."
					+ " If name is not a valid enchantment, an EnchantException is thrown.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREEnchantmentException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, GenericParameters generics, Mixed... args) throws ConfigRuntimeException {
			MCEnchantment e = GetEnchantment(args[0].val().replace(' ', '_'), t);
			return new CInt(e.getMaxLevel(), t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_enchants extends AbstractFunction implements Optimizable {

		private static final Map<String, CArray> CACHE = new HashMap<>();

		@Override
		public String getName() {
			return "get_enchants";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "array {item} Given an item array, returns the enchantments that can"
					+ " be validly added to this item. This may return an empty array.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRECastException.class, CRERangeException.class,
				CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, GenericParameters generics, Mixed... args) throws ConfigRuntimeException {
			MCItemStack is;
			if(args[0].isInstanceOf(CArray.TYPE, null, env)) {
				is = ObjectGenerator.GetGenerator().item(args[0], t, env);
			} else {
				is = Static.ParseItemNotation(null, args[0].val(), 1, t);
			}
			String name = is.getType().getName();
			/*
			 * Because enchantment types won't change from run to run, we can
			 * cache here, and save time on duplicate lookups.
			 */
			if(CACHE.containsKey(name)) {
				return CACHE.get(name).clone();
			}
			CArray ca = new CArray(t, GenericParameters.emptyBuilder(CArray.TYPE)
					.addNativeParameter(CString.TYPE, null).buildNative(), env);
			for(MCEnchantment e : StaticLayer.GetEnchantmentValues()) {
				if(e.canEnchantItem(is)) {
					ca.push(new CString(e.getKey(), t), t, env);
				}
			}
			CACHE.put(name, ca);
			return ca.clone();
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs, List<ParseTree> children,
				FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() == 1
					&& !(children.get(0).getData() instanceof CFunction)
					&& (children.get(0).getData().isInstanceOf(CString.TYPE, null, env)
					|| children.get(0).getData().isInstanceOf(CInt.TYPE, null, env))) {
				env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions,
						new CompilerWarning("The string item format in " + getName() + " is deprecated.", t, null));
			}
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class is_enchantment extends AbstractFunction {

		@Override
		public String getName() {
			return "is_enchantment";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {name} Returns true if this name is a valid enchantment type.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, GenericParameters generics, Mixed... args) throws ConfigRuntimeException {
			try {
				GetEnchantment(args[0].val(), t);
				return CBoolean.TRUE;
			} catch (CREEnchantmentException e) {
				return CBoolean.FALSE;
			}
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class enchantment_list extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, GenericParameters generics, Mixed... args) throws ConfigRuntimeException {
			MCEnchantment[] enchantments = StaticLayer.GetEnchantmentValues();
			CArray ret = new CArray(t, GenericParameters.emptyBuilder(CArray.TYPE)
					.addNativeParameter(CString.TYPE, null).buildNative(), env);
			for(MCEnchantment e : enchantments) {
				ret.push(new CString(e.getKey(), t), t, env);
			}
			return ret;
		}

		@Override
		public String getName() {
			return "enchantment_list";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Returns an informational list of all valid enchantment names. Note that this will"
					+ " simply cover all enchantment types, but may not be a comprehensive list of names that"
					+ " can be accepted, there may be more, however, the list returned here is \"comprehensive\""
					+ " and \"official\". Additionally, this may vary from server type to server type.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}
}
