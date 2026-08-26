package io.github.miklires.mcraft.model;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RecipeIngredientTest {
 @Test void customIngredientKeepsStableIdAndBaseMaterial(){ var value=RecipeIngredient.custom("ruby", Material.REDSTONE); assertTrue(value.isCustom()); assertEquals("ruby",value.getCustomId()); assertEquals(Material.REDSTONE,value.getMaterial()); }
 @Test void itemVersionNeverDropsBelowOne(){ var item=new CustomItem("ruby",Material.REDSTONE); item.setVersion(-2); assertEquals(1,item.getVersion()); }
}
