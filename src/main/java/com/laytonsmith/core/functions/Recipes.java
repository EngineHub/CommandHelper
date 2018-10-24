package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCRecipe;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;

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

	@api
	public static class add_recipe extends recipeFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			try {
				return CBoolean.get(Static.getServer().addRecipe(ObjectGenerator.GetGenerator().recipe(args[0], t)));
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
			return "boolean {RecipeArray} Adds a recipe to the server and returns whether it was added or not. ----"
					+ " The RecipeArray can contain the following keys:"
					+ " <pre>"
					+ "   type: The type of recipe. Expected to be 'SHAPED', 'SHAPELESS', or 'FURNACE'.\n"
					+ "   key: A unique string for this recipe.\n"
					+ "   result: The result item array of the recipe.\n"
					+ "   shape: The shape of the recipe. Represented as a 3 index normal array. (shaped recipes)\n"
					+ "   ingredients: Ingredients array of the recipe. See examples. (shaped and shapeless recipes)\n"
					+ "   input: The input item array of the recipe. (furnace recipes)"
					+ " </pre>"
					+ " Shaped Recipe. Turns 9 stone into obsidian."
					+ " <pre>"
					+ "{\n"
					+ "    key: 'stone_to_obsidian',"
					+ "    type: 'SHAPED',\n"
					+ "    result: {type: '49:0'},\n"
					+ "    shape: {'AAA', 'AAA', 'AAA'},\n"
					+ "    ingredients: {A: {name: STONE}}\n"
					+ "}"
					+ "</pre>"
					+ " Shapeless Recipe. Combines tall grass and dirt to make grass."
					+ "<pre>"
					+ "{\n"
					+ "    key: 'dirt_to_grass',"
					+ "    type: 'SHAPELESS',\n"
					+ "    result: {name: 'GRASS'},\n"
					+ "    ingredients: {{name: 'DIRT'}, {name: 'LONG_GRASS', data: 1}}\n"
					+ "}"
					+ "</pre>"
					+ " Furnace Recipe. Turns grass into dirt through smelting."
					+ " <pre>"
					+ "{\n"
					+ "    key: 'cook_grass_to_dirt'"
					+ "    type: 'FURNACE',\n"
					+ "    result: {name: 'DIRT'},\n"
					+ "    input: {name: 'GRASS'}\n"
					+ "}"
					+ "</pre>";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class get_recipes_for extends recipeFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRECastException.class};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CArray ret = new CArray(t);
			MCItemStack item = ObjectGenerator.GetGenerator().item(args[0], t);
			List<MCRecipe> recipes = Static.getServer().getRecipesFor(item);
			for(MCRecipe recipe : recipes) {
				ret.push(ObjectGenerator.GetGenerator().recipe(recipe, t), t);
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
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class get_all_recipes extends recipeFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CArray ret = new CArray(t);
			List<MCRecipe> recipes = Static.getServer().allRecipes();
			for(MCRecipe recipe : recipes) {
				ret.push(ObjectGenerator.GetGenerator().recipe(recipe, t), t);
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
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class clear_recipes extends recipeFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
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
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class reset_recipes extends recipeFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
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
			return CHVersion.V3_3_1;
		}

	}
}
