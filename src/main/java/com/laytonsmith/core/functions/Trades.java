package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCMerchant;
import com.laytonsmith.abstraction.MCMerchantRecipe;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.entities.MCTrader;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREBadEntityException;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CRELengthException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CREPlayerOfflineException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trades {

	public static String docs() {
		return "Functions related to the management of trades and merchants. A trade is a special kind of recipe"
				+ " accessed through the merchant interface. A merchant is a provider of trades,"
				+ " which may or may not be attached to a Villager or Wandering Trader.";
	}

	@api
	public static class get_merchant_trades extends Recipes.recipeFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIllegalArgumentException.class, CREFormatException.class, CREBadEntityException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CArray ret = new CArray(t);
			for(MCMerchantRecipe mr : GetMerchant(args[0], t).getRecipes()) {
				ret.push(trade(mr, t), t);
			}
			return ret;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_3;
		}

		@Override
		public String getName() {
			return "get_merchant_trades";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "array {specifier} Returns a list of trades used by the specified merchant."
					+ " Specifier can be the UUID of an entity or a virtual merchant ID.";
		}
	}

	@api
	public static class set_merchant_trades extends Recipes.recipeFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIllegalArgumentException.class, CREFormatException.class, CRECastException.class,
					CREBadEntityException.class, CRENotFoundException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCMerchant merchant = GetMerchant(args[0], t);
			CArray trades = Static.getArray(args[1], t);
			List<MCMerchantRecipe> recipes = new ArrayList<>();
			if(trades.isAssociative()) {
				throw new CRECastException("Expected non-associative array for list of trade arrays.", t);
			}
			for(Mixed trade : trades.asList()) {
				recipes.add(trade(trade, t));
			}
			merchant.setRecipes(recipes);
			return CVoid.VOID;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_3;
		}

		@Override
		public String getName() {
			return "set_merchant_trades";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {specifier, array} Sets the list of trades the specified merchant can use to the provided"
					+ " array of TradeArrays. The specifier can be the UUID of a physical entity or the ID"
					+ " of a user-created virtual merchant. ----"
					+ " TradeArrays are similar to RecipeArray format and contain the following keys:"
					+ " <pre>"
					+ "   result: The result item array of the trade.\n"
					+ "   ingredients: Items the player must provide. Must be 1 or 2 itemstacks.\n"
					+ "   uses: (Optional) The number of times the recipe has been used. Defaults to 0."
					+ " Note: this number is not kept in sync between merchants and the master list.\n"
					+ "   maxuses: (Optional) The maximum number of times this trade can be made before it is disabled."
					+ " Defaults to " + Integer.MAX_VALUE + ".\n"
					+ "   hasxpreward: (Optional) Whether xp is given to the player for making this trade."
					+ " Defaults to true."
					+ " </pre>"
					+ " Example 1: Turns 9 stone into obsidian."
					+ " <pre>"
					+ "{\n"
					+ "    result: {name: OBSIDIAN},\n"
					+ "    ingredients: {{name: STONE, qty: 9}}\n"
					+ "}"
					+ "</pre>"
					+ " Example 2: Combines a diamond and dirt to make grass, but only once."
					+ "<pre>"
					+ "{\n"
					+ "    result: {name: 'GRASS'},\n"
					+ "    ingredients: {{name: 'DIRT'}, {name: 'DIAMOND'}}\n"
					+ "    maxuses: 1\n"
					+ "}"
					+ "</pre>";
		}
	}

	@api
	public static class get_virtual_merchants extends Recipes.recipeFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[0];
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CArray ret = CArray.GetAssociativeArray(t);
			for(Map.Entry<String, MCMerchant> entry : VIRTUAL_MERCHANTS.entrySet()) {
				if(entry.getValue() == null) {
					VIRTUAL_MERCHANTS.remove(entry.getKey());
					continue;
				}
				ret.set(entry.getKey(), entry.getValue().getTitle(), t);
			}
			return ret;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_3;
		}

		@Override
		public String getName() {
			return "get_virtual_merchants";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Returns an array where the keys are currently registered merchant IDs and the values are"
					+ " the corresponding window titles of those merchants.";
		}
	}

	@api
	public static class create_virtual_merchant extends Recipes.recipeFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[] {CREIllegalArgumentException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			if(VIRTUAL_MERCHANTS.containsKey(args[0].val())) {
				throw new CREIllegalArgumentException("There is already a merchant with id " + args[0].val(), t);
			} else {
				VIRTUAL_MERCHANTS.put(args[0].val(), Static.getServer().createMerchant(args[1].val()));
				return CVoid.VOID;
			}
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_3;
		}

		@Override
		public String getName() {
			return "create_virtual_merchant";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {ID, title} Creates a merchant that can be traded with by players but is not attached to"
					+ " a physical entity. The ID given should not be a UUID. The title is the text that will display"
					+ " at the top of the window while a player is trading with it. Created merchants will persist"
					+ " across recompiles, but not across server restarts. An exception will be thrown if a merchant"
					+ " already exists using the given ID.";
		}
	}

	@api
	public static class delete_virtual_merchant extends Recipes.recipeFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[0];
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(VIRTUAL_MERCHANTS.remove(args[0].val()) != null);
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_3;
		}

		@Override
		public String getName() {
			return "delete_virtual_merchant";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {string} Deletes a virtual merchant if one by the given ID exists. Returns true if"
					+ " one was removed, or false if there was no match for the ID.";
		}
	}

	@api
	public static class popen_trading extends Recipes.recipeFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRELengthException.class,
					CREIllegalArgumentException.class, CREBadEntityException.class, CREFormatException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer player;
			boolean force = false;
			if(args.length > 1) {
				player = Static.GetPlayer(args[1], t);
			} else {
				player = Static.getPlayer(env, t);
			}
			if(args.length == 3) {
				force = ArgumentValidation.getBooleanish(args[2], t);
			}
			MCMerchant merchant = GetMerchant(args[0], t);
			if(!force && merchant.isTrading()) {
				return CBoolean.FALSE;
			}
			return CBoolean.get(player.openMerchant(merchant, force) != null);
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_3;
		}

		@Override
		public String getName() {
			return "popen_trading";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		@Override
		public String docs() {
			return "boolean {specifier, [player], [force]} Opens a trading interface for the current player,"
					+ " or the one specified. Only one player can trade with a merchant at a time."
					+ " If the merchant is already being traded with, the function will do nothing."
					+ " When true, force will make the merchant trade with the player, closing the trade with"
					+ " the previous player if there was one. Function returns true if trading was successfully"
					+ " opened, and false if not.";
		}
	}

	@api
	public static class merchant_trader extends Recipes.recipeFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBadEntityException.class, CREFormatException.class, CREFormatException.class,
					CREIllegalArgumentException.class, CRELengthException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCMerchant merchant = GetMerchant(args[0], t);
			return merchant.isTrading() ? new CString(merchant.getTrader().getUniqueId().toString(), t) : CNull.NULL;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_3;
		}

		@Override
		public String getName() {
			return "merchant_trader";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "UUID {specifier} Returns the UUID of the user trading with the merchant, or null if no one is.";
		}
	}

	private static final HashMap<String, MCMerchant> VIRTUAL_MERCHANTS = new HashMap<>();

	/**
	 * Returns the merchant specified.
	 * @param specifier The string representing the merchant, whether entity UUID or virtual id.
	 * @param t
	 * @return abstracted merchant
	 */
	private static MCMerchant GetMerchant(Mixed specifier, Target t) {
		MCMerchant merchant;
		if(specifier.val().length() == 36 || specifier.val().length() == 32) {
			try {
				MCEntity entity = Static.getEntity(specifier, t);
				if(!(entity instanceof MCTrader)) {
					throw new CREIllegalArgumentException("The entity specified is not capable of being an merchant.", t);
				}
				return ((MCTrader) entity).asMerchant();
			} catch (CREFormatException iae) {
				// not a UUID
			}
		}
		merchant = VIRTUAL_MERCHANTS.get(specifier.val());
		if(merchant == null) {
			throw new CREIllegalArgumentException("A merchant named \"" + specifier.val() + "\" does not exist.", t);
		}
		return merchant;
	}

	private static MCMerchantRecipe trade(Mixed c, Target t) {

		CArray recipe = Static.getArray(c, t);

		MCItemStack result = ObjectGenerator.GetGenerator().item(recipe.get("result", t), t);

		MCMerchantRecipe mer = (MCMerchantRecipe) StaticLayer.GetNewRecipe(null, MCRecipeType.MERCHANT, result);

		if(recipe.containsKey("maxuses")) {
			mer.setMaxUses(Static.getInt32(recipe.get("maxuses", t), t));
		}
		if(recipe.containsKey("uses")) {
			mer.setUses(Static.getInt32(recipe.get("uses", t), t));
		}
		if(recipe.containsKey("hasxpreward")) {
			mer.setHasExperienceReward(ArgumentValidation.getBoolean(recipe.get("hasxpreward", t), t));
		}

		CArray ingredients = Static.getArray(recipe.get("ingredients", t), t);
		if(ingredients.inAssociativeMode()) {
			throw new CREFormatException("Ingredients array is invalid.", t);
		}
		if(ingredients.size() < 1 || ingredients.size() > 2) {
			throw new CRERangeException("Ingredients for merchants must contain 1 or 2 items, found "
					+ ingredients.size(), t);
		}
		List<MCItemStack> mcIngredients = new ArrayList<>();
		for(Mixed ingredient : ingredients.asList()) {
			mcIngredients.add(ObjectGenerator.GetGenerator().item(ingredient, t));
		}
		mer.setIngredients(mcIngredients);

		return mer;
	}

	private static Mixed trade(MCMerchantRecipe r, Target t) {
		if(r == null) {
			return CNull.NULL;
		}
		CArray ret = CArray.GetAssociativeArray(t);
		ret.set("result", ObjectGenerator.GetGenerator().item(r.getResult(), t), t);
		CArray il = new CArray(t);
		for(MCItemStack i : r.getIngredients()) {
			il.push(ObjectGenerator.GetGenerator().item(i, t), t);
		}
		ret.set("ingredients", il, t);
		ret.set("maxuses", new CInt(r.getMaxUses(), t), t);
		ret.set("uses", new CInt(r.getUses(), t), t);
		ret.set("hasxpreward", CBoolean.get(r.hasExperienceReward()), t);

		return ret;
	}
}
