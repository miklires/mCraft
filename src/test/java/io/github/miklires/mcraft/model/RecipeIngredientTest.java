package io.github.miklires.mcraft.model;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RecipeIngredientTest {
 @Test void customIngredientKeepsStableIdAndBaseMaterial(){ var value=RecipeIngredient.custom("ruby", Material.REDSTONE); assertTrue(value.isCustom()); assertEquals("ruby",value.getCustomId()); assertEquals(Material.REDSTONE,value.getMaterial()); }
 @Test void itemVersionNeverDropsBelowOne(){ var item=new CustomItem("ruby",Material.REDSTONE); item.setVersion(-2); assertEquals(1,item.getVersion()); }
 @Test void itemTagsAreNormalizedAndDeduplicated(){ var item=new CustomItem("ruby",Material.REDSTONE); item.setTags(java.util.List.of(" Weapon ","weapon","RUBY")); assertEquals(java.util.Set.of("weapon","ruby"),item.getTags()); }
 @Test void recipeConditionsAreNormalized(){ var recipe=new CustomRecipe("ruby_sword",RecipeType.SHAPED); recipe.setPermission(" mcraft.craft.ruby "); recipe.setMinimumLevel(-5); recipe.setWorlds(java.util.List.of(" World ","WORLD_nether")); assertEquals("mcraft.craft.ruby",recipe.getPermission()); assertEquals(0,recipe.getMinimumLevel()); assertEquals(java.util.Set.of("world","world_nether"),recipe.getWorlds()); }
 @Test void returnedConditionCollectionsAreImmutable(){ var recipe=new CustomRecipe("ruby",RecipeType.SHAPELESS); recipe.setWorlds(java.util.List.of("world")); assertThrows(UnsupportedOperationException.class,()->recipe.getWorlds().add("other")); }
}
