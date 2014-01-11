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
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.util.List;

/**
 *
 * @author cgallarno
 */
public class Recipes {
	
	public static String docs(){
        return "This class of functions allows recipes to be managed.";
    }
	
	public static abstract class recipeFunction extends AbstractFunction {

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}
	}
	
	@api
	public static class add_recipe extends recipeFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			
			MCRecipe recipe = ObjectGenerator.GetGenerator().recipe(args[0], t);
			
			return new CBoolean(Static.getServer().addRecipe(recipe), t);
		}

		@Override
		public String getName() {
			return "add_recipe";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[] {1};
		}

		@Override
		public String docs() {
			return "boolean {RecipeArray} Adds a recipe to the server and returns whether it was added or not. Please read http://wiki.sk89q.com/wiki/CommandHelper/Array_Formatting to see how the recipe array is formatted.";
		}
	}
	
	@api
	public static class get_recipes_for extends recipeFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CArray ret = new CArray(t);
			MCItemStack item = ObjectGenerator.GetGenerator().item(args[0], t);
			List<MCRecipe> recipes = Static.getServer().getRecipesFor(item);
			for(MCRecipe recipe : recipes) {
				ret.push(ObjectGenerator.GetGenerator().recipe(recipe, t));
			}
			
			return ret;
		}

		@Override
		public String getName() {
			return "get_recipe_for";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[] {1};
		}

		@Override
		public String docs() {
			return "array {itemArray} Gets all recipes that have a result of the given item. " + 
					"NOTE: Gets all recipes for result item regardless of meta and enchants, althogh the array has correct data.";
		}
		
	}
	
	@api
	public static class get_all_recipes extends recipeFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CArray ret = new CArray(t);
			List<MCRecipe> recipes = Static.getServer().allRecipes();
			for(MCRecipe recipe : recipes) {
				ret.push(ObjectGenerator.GetGenerator().recipe(recipe, t));
			}
			
			return ret;
		}

		@Override
		public String getName() {
			return "get_all_recipes";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[] {0};
		}

		@Override
		public String docs() {
			return "array {} Gets all recipes on the server.";
		}
		
	}
	
	@api
	public static class clear_recipes extends recipeFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			Static.getServer().clearRecipes();
			
			return new CVoid(t);
		}

		@Override
		public String getName() {
			return "clear_recipes";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[] {0};
		}

		@Override
		public String docs() {
			return "Void {} Clears all recipes.";
		}
		
	}
	
	@api
	public static class reset_recipes extends recipeFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			Static.getServer().resetRecipes();
			
			return new CVoid(t);
		}

		@Override
		public String getName() {
			return "reset_recipes";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[] {0};
		}

		@Override
		public String docs() {
			return "Void {} Resets all recipes to the default recipes.";
		}
		
	}
}
