package zombe.core.content;


import net.minecraft.item.crafting.*;

import java.util.Comparator;

public class RecipeComparator implements Comparator<IRecipe> {

    public int compare(IRecipe p1, IRecipe p2) {
        if (p1 instanceof ShapelessRecipes && p2 instanceof ShapedRecipes) {
            return 1;
        } else if (p2 instanceof ShapelessRecipes && p1 instanceof ShapedRecipes) {
            return -1;
        } else if (p2.getRecipeSize() < p1.getRecipeSize()) {
            return -1;
        } else {
            return (p2.getRecipeSize() > p1.getRecipeSize() ? 1 : 0);
        }
    }
}
