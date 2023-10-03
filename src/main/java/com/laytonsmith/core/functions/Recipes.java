package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCRecipe;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.List;

/**
 *
 * @author cgallarno
 */
public class Recipes {

	public static String docs() {
		return "This class of functions allows recipes to be managed.";
	}

	public abstract static class recipeFunction extends AbstractFunction {

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class add_recipe extends recipeFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class, CREIllegalArgumentException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, GenericParameters generics, Mixed... args) throws ConfigRuntimeException {
			try {
				return CBoolean.get(Static.getServer().addRecipe(ObjectGenerator.GetGenerator().recipe(args[0], t, env)));
			} catch (IllegalStateException ex) {
				// recipe with the given key probably already exists
				return CBoolean.FALSE;
			}
		}

		@Override
		public String getName() {
			return "add_recipe";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {recipeArray} Adds a recipe to the server and returns whether it was added or not. ----"
					+ " The recipeArray can contain the following keys:"
					+ " <pre>"
					+ "   type: (required) The type of recipe. Expected to be 'BLASTING', 'CAMPFIRE', 'FURNACE', 'SHAPED',"
					+ " 'SHAPELESS', 'SMOKING', or 'STONECUTTING'.\n"
					+ "   key: (required) A unique string id for this recipe.\n"
					+ "   result: (required) The result item array of the recipe.\n"
					+ "   group: (optional) A group id for grouping recipes in the recipe book.\n"
					+ "   shape: (shaped) The shape of the recipe. Represented as a 3 index normal array.\n"
					+ "   ingredients: (shaped and shapeless recipes) Ingredients array of the recipe. See examples.\n"
					+ "   input: (cooking and stonecutting) The input item type or array of types for this recipe.\n"
					+ "   cookingtime: (cooking) The amount of time in ticks for the input item to cook.\n"
					+ "   experience: (cooking) The amount of experience generated by this recipe."
					+ " </pre>"
					+ " Shaped Recipe Data. Turns 9 stone into obsidian."
					+ " <pre>{\n"
					+ "    key: 'stone_to_obsidian',\n"
					+ "    type: 'SHAPED',\n"
					+ "    result: {name: 'OBSIDIAN'},\n"
					+ "    shape: {'AAA', 'AAA', 'AAA'},\n"
					+ "    ingredients: {A: 'STONE'}\n"
					+ "}"
					+ "</pre>"
					+ " Shapeless Recipe Data. Combines tall grass and dirt to make grass block."
					+ "<pre>"
					+ "{\n"
					+ "    key: 'dirt_to_grass',\n"
					+ "    type: 'SHAPELESS',\n"
					+ "    result: {name: 'GRASS_BLOCK'},\n"
					+ "    ingredients: {'DIRT', 'GRASS'}\n"
					+ "}"
					+ "</pre>"
					+ " Furnace Recipe Data. Turn grass or mycelium into dirt through smelting. (multi-item type input)"
					+ "<pre>"
					+ "{\n"
					+ "    key: 'cook_grass_to_dirt'\n"
					+ "    type: 'FURNACE',\n"
					+ "    result: {name: 'DIRT'},\n"
					+ "    input: {'MYCELIUM', 'GRASS_BLOCK'},\n"
					+ "    experience: 0.001,\n"
					+ "    cookingtime: 200\n"
					+ "}</pre>"
					+ " StoneCutting Recipe Data. Turns diamond hoe into diamond. (single item type input)"
					+ "<pre>"
					+ "{\n"
					+ "    key: 'diamond_hoe_to_diamond'\n"
					+ "    type: 'STONECUTTING',\n"
					+ "    result: {name: 'DIAMOND'},\n"
					+ "    input: 'DIAMOND_HOE'\n"
					+ "}</pre>";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Demonstrates adding a shaped recipe.",
				"add_recipe(array(\n"
				+ "\t'key': 'player_head',\n"
				+ "\t'type': 'SHAPED',\n"
				+ "\t'result': array('name': 'PLAYER_HEAD'),\n"
				+ "\t'shape': array(\n"
				+ "\t\t'AAA',\n"
				+ "\t\t'ABA',\n"
				+ "\t\t'AAA'),\n"
				+ "\t'ingredients': array(\n"
				+ "\t\t'A': 'CLAY_BALL',\n"
				+ "\t\t'B': 'SKELETON_SKULL')));",
				"Creates recipe where a skeleton skull is surrounded by clay balls to creates a player head."),

				new ExampleScript("Demonstrates adding a shaped recipe with exact ingredients using item arrays."
				+ " The item ingredient must match exactly, including unspecified meta. (except 'qty')"
				+ " In this example, the regen potion ingredient must NOT be extended or upgraded for it to match,"
				+ " because those are the default values.",
				"add_recipe(array(\n"
				+ "\t'key': 'regen_extended',\n"
				+ "\t'type': 'SHAPED',\n"
				+ "\t'result': array('name': 'POTION', 'meta': array('base': array('type': 'REGEN', 'extended': true))),\n"
				+ "\t'shape': array(\n"
				+ "\t\t'RRR',\n"
				+ "\t\t'RPR',\n"
				+ "\t\t'RRR'),\n"
				+ "\t'ingredients': array(\n"
				+ "\t\t'R': 'REDSTONE',\n"
				+ "\t\t'P': array('name': 'POTION', 'meta': array('base': array('type': 'REGEN'))))));",
				"Turns a normal regen potion surrounded by redstone into an extended regen potion."),

				new ExampleScript("Demonstrates a shaped recipe with multiple ingredient choices."
				+ " If just one of the ingredient choices is an item array (i.e. associative array instead of string),"
				+ " all other ingredient choices for that key are also treated as exact ingredients.",
				"add_recipe(array(\n"
				+ "\t'key': 'mushroom_stem',\n"
				+ "\t'type': 'SHAPED',\n"
				+ "\t'result': array('name': 'MUSHROOM_STEM'),\n"
				+ "\t'shape': array('X', 'X', 'X'),\n"
				+ "\t'ingredients': array('X': array('RED_MUSHROOM', 'BROWN_MUSHROOM'))));",
				"Crafts a mushroom stem block."),
			};
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class remove_recipe extends recipeFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[0];
		}

		@Override
		public Mixed exec(Target t, Environment env, GenericParameters generics, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(Static.getServer().removeRecipe(args[0].val()));
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public String getName() {
			return "remove_recipe";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {recipe_key} Remove a certain recipe by its registered key."
					+ " Returns whether the recipe was removed successfully or not.";
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_recipes_for extends recipeFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRECastException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, GenericParameters generics, Mixed... args) throws ConfigRuntimeException {
			CArray ret = new CArray(t, null, env);
			MCItemStack item = ObjectGenerator.GetGenerator().item(args[0], t, env);
			List<MCRecipe> recipes = Static.getServer().getRecipesFor(item);
			for(MCRecipe recipe : recipes) {
				ret.push(ObjectGenerator.GetGenerator().recipe(recipe, t, env), t, env);
			}

			return ret;
		}

		@Override
		public String getName() {
			return "get_recipes_for";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "array {itemArray} Gets all recipes that have a result of the given item."
					+ " NOTE: Gets all recipes for result item regardless of meta and enchants,"
					+ " although the array has correct data.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_all_recipes extends recipeFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public Mixed exec(Target t, Environment env, GenericParameters generics, Mixed... args) throws ConfigRuntimeException {
			CArray ret = new CArray(t, null, env);
			List<MCRecipe> recipes = Static.getServer().allRecipes();
			for(MCRecipe recipe : recipes) {
				ret.push(ObjectGenerator.GetGenerator().recipe(recipe, t, env), t, env);
			}

			return ret;
		}

		@Override
		public String getName() {
			return "get_all_recipes";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Gets all recipes on the server.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class clear_recipes extends recipeFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public Mixed exec(Target t, Environment env, GenericParameters generics, Mixed... args) throws ConfigRuntimeException {
			Static.getServer().clearRecipes();
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "clear_recipes";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "void {} Clears all recipes.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class reset_recipes extends recipeFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public Mixed exec(Target t, Environment env, GenericParameters generics, Mixed... args) throws ConfigRuntimeException {
			Static.getServer().resetRecipes();
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "reset_recipes";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "void {} Resets all recipes to the default recipes.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}
}
