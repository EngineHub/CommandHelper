package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCMerchant;
import com.laytonsmith.abstraction.MCMerchantRecipe;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCRecipe;
import com.laytonsmith.abstraction.entities.MCVillager;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trades {

	public static String docs() {
		return "Functions related to the management of trades and merchants. A trade is a special kind of recipe"
				+ " accessed through the merchant interface. A merchant is a provider of trades,"
				+ " which may or may not be attached to a Villager.";
	}

	@api
	public static class create_trade extends Recipes.recipeFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIllegalArgumentException.class, CRECastException.class, CRERangeException.class};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {

			MCRecipe recipe = ObjectGenerator.GetGenerator().recipe(args[0], t);
			if(recipe.getRecipeType() != MCRecipeType.MERCHANT) {
				throw new CREIllegalArgumentException("Trades must be a recipe of type " + MCRecipeType.MERCHANT.name(), t);
			}
			MCMerchantRecipe merchantRecipe = (MCMerchantRecipe) recipe;
			if(MERCHANT_RECIPES.containsKey(merchantRecipe.getKey())) {
				return CBoolean.FALSE;
			} else {
				MERCHANT_RECIPES.put(merchantRecipe.getKey(), merchantRecipe);
				return CBoolean.TRUE;
			}
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_3;
		}

		@Override
		public String getName() {
			return "create_trade";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {RecipeArray} Adds a trade to an internal list so that it may be added to merchants later."
					+ " This list persists through recompiles, but not through restarts. ===-"
					+ " Trades follow RecipeArray format and contain the following keys:"
					+ " <pre>"
					+ "   type: The type of recipe. Expected to be 'MERCHANT'.\n"
					+ "   key: A unique string for this recipe.\n"
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
					+ "    key: 'stone_to_obsidian',"
					+ "    type: 'MERCHANT',\n"
					+ "    result: {name: OBSIDIAN},\n"
					+ "    ingredients: {{name: STONE, qty: 9}}\n"
					+ "}"
					+ "</pre>"
					+ " Example 2: Combines a diamond and dirt to make grass, but only once."
					+ "<pre>"
					+ "{\n"
					+ "    key: 'diamond_to_grass',"
					+ "    type: 'MERCHANT',\n"
					+ "    result: {name: 'GRASS'},\n"
					+ "    ingredients: {{name: 'DIRT'}, {name: 'DIAMOND'}}\n"
					+ "    maxuses: 1\n"
					+ "}"
					+ "</pre>";
		}
	}

	@api
	public static class delete_trade extends Recipes.recipeFunction {
		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[0];
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return CBoolean.get(MERCHANT_RECIPES.remove(args[0].val()) != null);
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_3;
		}

		@Override
		public String getName() {
			return "delete_trade";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {key} Deletes a trade if one with the given key exists. Returns true if"
					+ " one was removed, or false if there was no match for the key.";
		}
	}

	@api
	public static class get_trades extends Recipes.recipeFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {

			if(args.length == 1) {
				if(MERCHANT_RECIPES.containsKey(args[0].val())) {
					return ObjectGenerator.GetGenerator().recipe(MERCHANT_RECIPES.get(args[0].val()), t);
				} else {
					throw new CRENotFoundException("No trade with key " + args[0].val() + " found.", t);
				}
			} else {
				CArray ret = new CArray(t);
				for(String key : MERCHANT_RECIPES.keySet()) {
					ret.push(new CString(key, t), t);
				}
				return ret;
			}
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_3;
		}

		@Override
		public String getName() {
			return "get_trades";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "array {[key]} Will return a list of trade keys, or if a key is specified,"
					+ " the RecipeArray of that trade object.";
		}
	}

	@api
	public static class get_merchant_trades extends Recipes.recipeFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIllegalArgumentException.class, CREFormatException.class, CREBadEntityException.class};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			CArray ret = new CArray(t);
			for(MCMerchantRecipe mr : GetMerchant(args[0], t).getRecipes()) {
				ret.push(ObjectGenerator.GetGenerator().recipe(mr, t), t);
			}
			return ret;
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_3;
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
					+ " Specifier can be the UUID of an entity or a virtual merchant ID." + note;
		}
	}

	private static String note = " NOTE: The trade a merchant receives is a copy of the one provided to it."
			+ " The usage stat will not be updated in get_trades when the trade is used and vice versa."
			+ " Keys as well are just a copy of what they were when the trade was added to the merchant."
			+ " Furthermore, keys can only be determined on merchants created by create_virtual_merchant.";

	@api
	public static class set_merchant_trades extends Recipes.recipeFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIllegalArgumentException.class, CREFormatException.class, CRECastException.class,
					CREBadEntityException.class, CRENotFoundException.class};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			MCMerchant merchant = GetMerchant(args[0], t);
			CArray trades = Static.getArray(args[1], t);
			List<MCMerchantRecipe> recipes = new ArrayList<>();
			for(Construct trade : trades.asList()) {
				if(MERCHANT_RECIPES.containsKey(trade.val())) {
					recipes.add(MERCHANT_RECIPES.get(trade.val()));
				} else {
					throw new CRENotFoundException("No trade found matching '" + trade.val() + "'", t);
				}
			}
			merchant.setRecipes(recipes);
			return CVoid.VOID;
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_3;
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
					+ " array of trade keys. The specifier can be the UUID of a physical entity or the ID"
					+ " of a user-created virtual merchant." + note;
		}
	}

	@api
	public static class get_virtual_merchants extends Recipes.recipeFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[0];
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
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
			return CHVersion.V3_3_3;
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
			return new Class[0];
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			if(VIRTUAL_MERCHANTS.containsKey(args[0].val())) {
				return CBoolean.FALSE;
			} else {
				VIRTUAL_MERCHANTS.put(args[0].val(), Static.getServer().createMerchant(args[1].val()));
				return CBoolean.TRUE;
			}
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_3;
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
			return "boolean {ID, title} Creates a merchant that can be traded with by players but is not attached to"
					+ " a physical entity. Will return true if the ID was newly added, and false if the ID was already"
					+ " in use. The ID given should not be a UUID. The title is the text that will display at the top"
					+ " of the window while a player is trading with it. This list will persist across recompiles,"
					+ " but not across server restarts.";
		}
	}

	@api
	public static class delete_virtual_merchant extends Recipes.recipeFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[0];
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return CBoolean.get(VIRTUAL_MERCHANTS.remove(args[0].val()) != null);
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_3;
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
					CREIllegalArgumentException.class};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			MCPlayer player;
			boolean force = false;
			if(args.length > 1) {
				player = Static.GetPlayer(args[1], t);
			} else {
				player = Static.getPlayer(env, t);
			}
			if(args.length == 3) {
				force = Static.getBoolean(args[2], t);
			}
			MCMerchant merchant = GetMerchant(args[0], t);
			if(!force && merchant.isTrading()) {
				return CBoolean.FALSE;
			}
			return CBoolean.get(player.openMerchant(merchant, force) != null);
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_3;
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

	public static final HashMap<String, MCMerchantRecipe> MERCHANT_RECIPES = new HashMap<>();
	public static final HashMap<String, MCMerchant> VIRTUAL_MERCHANTS = new HashMap<>();

	/**
	 * Returns the merchant specified.
	 * @param specifier The string representing the merchant, whether entity UUID or virtual id.
	 * @param t
	 * @return abstracted merchant
	 */
	private static MCMerchant GetMerchant(Construct specifier, Target t) {
		MCMerchant merchant;
		if(specifier.val().length() == 36 || specifier.val().length() == 32) {
			try {
				MCEntity entity = Static.getEntity(specifier, t);
				if(!(entity instanceof MCVillager)) {
					throw new CREIllegalArgumentException("The entity specified is not capable of being an merchant.", t);
				}
				return ((MCVillager) entity).asMerchant();
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
}
