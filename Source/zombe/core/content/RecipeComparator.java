package zombe.core.content;

import net.minecraft.item.crafting.*;
import java.lang.*;
import java.util.*;

public class RecipeComparator implements Comparator<IRecipe> {

    public int compare(IRecipe p1, IRecipe p2) {
        return p1 instanceof ShapelessRecipes && p2 instanceof ShapedRecipes
        ? 1 : (p2 instanceof ShapelessRecipes && p1 instanceof ShapedRecipes
            ? -1 : (p2.getRecipeSize() < p1.getRecipeSize() 
                 ? -1 : (p2.getRecipeSize() > p1.getRecipeSize() 
                      ? 1 : 0)));
    }

    /*public int compare(Object p1, Object p2) {
        return this.compare((IRecipe)p1, (IRecipe)p2);
    }*/

}
