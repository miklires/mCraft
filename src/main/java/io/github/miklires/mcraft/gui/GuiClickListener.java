package io.github.miklires.mcraft.gui;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import io.github.miklires.mcraft.MCraft;
import io.github.miklires.mcraft.model.CustomItem;
import io.github.miklires.mcraft.model.CustomRecipe;
import io.github.miklires.mcraft.model.RecipeIngredient;
import io.github.miklires.mcraft.model.RecipeType;
import io.github.miklires.mcraft.registry.ItemBuilder;
import io.github.miklires.mcraft.util.InventoryUtil;

import java.util.ArrayList;
import java.util.List;

public class GuiClickListener implements Listener {

    private final MCraft plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public GuiClickListener(MCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof GuiHolder holder)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory top = event.getView().getTopInventory();
        boolean clickedTop = event.getClickedInventory() == top;

        if (clickedTop) {
            int slot = event.getRawSlot();
            if (holder.isEditable(slot)) {
                // Recipe slots are virtual: copy a single cursor item or clear the slot.
                // No player-owned stack enters the GUI, so closing/editing cannot duplicate items.
                event.setCancelled(true);
                ItemStack cursor = event.getCursor();
                if (cursor == null || cursor.getType().isAir() || event.isRightClick()) {
                    top.setItem(slot, null);
                } else {
                    ItemStack ghost = cursor.clone();
                    ghost.setAmount(slot == RecipeEditGui.RESULT_SLOT ? cursor.getAmount() : 1);
                    top.setItem(slot, ghost);
                }
                return;
            }
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;
            String action = GuiButton.readAction(clicked);
            if (action == null) return;
            String payload = GuiButton.readPayload(clicked);
            handle(player, holder, action, payload, event.getClick(), clicked, top);
            return;
        }

        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof GuiHolder holder)) return;
        for (int slot : event.getRawSlots()) {
            if (slot < event.getView().getTopInventory().getSize()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private void handle(Player player, GuiHolder holder, String action, String payload,
                        ClickType clickType, ItemStack clicked, Inventory top) {
        switch (action) {
            case GuiAction.NOOP -> {}
            case GuiAction.OPEN_ITEMS -> ItemsListGui.open(plugin, player, 0);
            case GuiAction.OPEN_RECIPES -> RecipesListGui.open(plugin, player, 0);
            case GuiAction.BACK -> handleBack(player, holder);
            case GuiAction.PAGE_NEXT, GuiAction.PAGE_PREV -> handlePage(player, holder.getScreen(), payload);
            case GuiAction.VIEW_ITEM -> handleViewItem(player, payload, clickType);
            case GuiAction.VIEW_RECIPE -> RecipeViewGui.open(plugin, player, payload);
            case GuiAction.GIVE_ITEM -> handleGive(player, payload);
            case GuiAction.RECIPE_CREATE -> RecipeEditGui.openNew(plugin, player);
            case GuiAction.RECIPE_EDIT -> RecipeEditGui.open(plugin, player, payload);
            case GuiAction.RECIPE_DELETE -> handleDelete(player, payload);
            case GuiAction.RECIPE_TOGGLE_TYPE -> handleToggleType(top);
            case GuiAction.RECIPE_TOGGLE_PRIORITY -> handleTogglePriority(top);
            case GuiAction.RECIPE_SAVE -> handleSave(player, holder, top);
            default -> {}
        }
    }

    private void handleBack(Player player, GuiHolder holder) {
        switch (holder.getScreen()) {
            case ITEMS_LIST, RECIPES_LIST -> MainMenuGui.open(plugin, player);
            case ITEM_VIEW -> ItemsListGui.open(plugin, player, 0);
            case RECIPE_VIEW, RECIPE_EDIT -> RecipesListGui.open(plugin, player, 0);
            default -> player.closeInventory();
        }
    }

    private void handlePage(Player player, GuiHolder.GuiScreen screen, String payload) {
        int page = 0;
        if (payload != null) {
            try { page = Integer.parseInt(payload); } catch (NumberFormatException ignored) {}
        }
        switch (screen) {
            case ITEMS_LIST -> ItemsListGui.open(plugin, player, page);
            case RECIPES_LIST -> RecipesListGui.open(plugin, player, page);
            default -> {}
        }
    }

    private void handleViewItem(Player player, String itemId, ClickType clickType) {
        if (clickType == ClickType.RIGHT) { giveDirect(player, itemId, 1); return; }
        if (clickType == ClickType.SHIFT_RIGHT) { giveDirect(player, itemId, 64); return; }
        ItemViewGui.open(plugin, player, itemId);
    }

    private void handleGive(Player player, String payload) {
        if (payload == null) return;
        String[] parts = payload.split(":");
        int amount = 1;
        if (parts.length >= 2) {
            try { amount = Math.max(1, Integer.parseInt(parts[1])); } catch (NumberFormatException ignored) {}
        }
        giveDirect(player, parts[0], amount);
    }

    private void giveDirect(Player player, String itemId, int amount) {
        CustomItem ci = plugin.getItemRegistry().get(itemId);
        String prefix = plugin.getMessageUtil().prefix();
        if (ci == null) {
            player.sendMessage(mm.deserialize(prefix + plugin.getMessageUtil().get("item.not-found")));
            return;
        }
        InventoryUtil.giveOrDrop(player, ci.buildItemStack(amount));
        player.sendMessage(mm.deserialize(prefix + plugin.getMessageUtil().get("item.given",
                "id", ci.getId(),
                "amount", String.valueOf(amount))));
    }

    private void handleDelete(Player player, String recipeId) {
        if (recipeId == null) return;
        plugin.getRecipeRegistry().delete(recipeId);
        player.sendMessage(mm.deserialize(plugin.getMessageUtil().prefix()
                + plugin.getMessageUtil().get("recipe.deleted", "id", recipeId)));
        RecipesListGui.open(plugin, player, 0);
    }

    private void handleToggleType(Inventory top) {
        RecipeType current = RecipeEditGui.readType(top);
        RecipeType next = current == RecipeType.SHAPED ? RecipeType.SHAPELESS : RecipeType.SHAPED;
        top.setItem(RecipeEditGui.TYPE_BUTTON_SLOT, RecipeEditGui.buildTypeButton(plugin, next));
    }

    private void handleTogglePriority(Inventory top) {
        boolean current = RecipeEditGui.readPriority(top);
        top.setItem(RecipeEditGui.PRIORITY_BUTTON_SLOT, RecipeEditGui.buildPriorityButton(plugin, !current));
    }

    private void handleSave(Player player, GuiHolder holder, Inventory top) {
        RecipeType type = RecipeEditGui.readType(top);
        boolean override = RecipeEditGui.readPriority(top);
        String prefix = plugin.getMessageUtil().prefix();

        ItemStack resultStack = top.getItem(RecipeEditGui.RESULT_SLOT);
        if (resultStack == null || resultStack.getType().isAir()) {
            player.sendMessage(mm.deserialize(prefix + plugin.getMessageUtil().get("recipe.need-result")));
            return;
        }

        boolean anyIngredient = false;
        for (int s : RecipeEditGui.CRAFT_SLOTS) {
            ItemStack stack = top.getItem(s);
            if (stack != null && !stack.getType().isAir()) {
                anyIngredient = true;
                break;
            }
        }
        if (!anyIngredient) {
            player.sendMessage(mm.deserialize(prefix + plugin.getMessageUtil().get("recipe.need-ingredient")));
            return;
        }

        String editingId = holder.getEditingRecipeId();
        String id = editingId != null ? editingId : plugin.getRecipeRegistry().allocateId();
        CustomRecipe r = new CustomRecipe(id, type);
        r.setResultAmount(resultStack.getAmount());
        r.setOverrideVanilla(override);

        String customResultId = ItemBuilder.readCustomId(resultStack);
        if (customResultId != null) {
            r.setResultRefCustomId(customResultId);
        } else {
            r.setResultVanilla(resultStack.getType());
        }

        if (type == RecipeType.SHAPED) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = top.getItem(RecipeEditGui.CRAFT_SLOTS[i]);
                r.setSlot(i, RecipeEditGui.buildIngredient(stack));
            }
        } else {
            List<RecipeIngredient> ingredients = new ArrayList<>();
            for (int s : RecipeEditGui.CRAFT_SLOTS) {
                ItemStack stack = top.getItem(s);
                RecipeIngredient ing = RecipeEditGui.buildIngredient(stack);
                if (ing != null) ingredients.add(ing);
            }
            r.setIngredients(ingredients);
        }

        plugin.getRecipeRegistry().save(r);
        String savedKey = override ? "recipe.saved-with-priority" : "recipe.saved";
        player.sendMessage(mm.deserialize(prefix + plugin.getMessageUtil().get(savedKey, "id", id)));
        RecipesListGui.open(plugin, player, 0);
    }
}

