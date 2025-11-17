package com.example.hmrback.utils.test;

import com.example.hmrback.model.request.RecipeFilter;
import com.example.hmrback.persistence.enums.IngredientType;
import com.example.hmrback.persistence.enums.RecipeType;

import java.util.List;

import static com.example.hmrback.utils.test.TestConstants.FAKE;
import static com.example.hmrback.utils.test.TestConstants.PRODUCT_NAME;
import static com.example.hmrback.utils.test.TestConstants.RECIPE_DESCRIPTION;
import static com.example.hmrback.utils.test.TestConstants.RECIPE_TITLE;

public class CommonTestUtils {

    private CommonTestUtils() {
    }

    public static RecipeFilter buildRecipeFilter(RecipeFilterEnum filterEnum, boolean matchingFilters) {
        String title = matchingFilters ? "Recipe" : FAKE;
        String description = matchingFilters ? "Description" : FAKE;
        Integer maxPrepTime = matchingFilters ? 30 : 1;
        List<RecipeType> recipeTypes = matchingFilters ? List.of(RecipeType.APPETIZER) : List.of(RecipeType.SIDE_DISH);
        String userName = matchingFilters ? "otherUser" : FAKE;
        List<String> ingredientNames = matchingFilters ? List.of(PRODUCT_NAME.formatted("1")) : List.of(FAKE);
        List<IngredientType> ingredientTypes = matchingFilters ? List.of(IngredientType.VEGETABLE) : List.of(IngredientType.OTHER);

        return new RecipeFilter(RecipeFilterEnum.TITLE.equals(filterEnum) ? title : null,
            RecipeFilterEnum.DESCRIPTION.equals(filterEnum) ? description : null,
            RecipeFilterEnum.MAXIMUM_PREPARATION_TIME.equals(filterEnum) ? maxPrepTime : null,
            RecipeFilterEnum.RECIPE_TYPE.equals(filterEnum) ? recipeTypes : null,
            RecipeFilterEnum.AUTHOR_USERNAME.equals(filterEnum) ? userName : null,
            RecipeFilterEnum.INGREDIENT_NAME.equals(filterEnum) ? ingredientNames : null,
            RecipeFilterEnum.INGREDIENT_TYPE.equals(filterEnum) ? ingredientTypes : null);
    }
}
