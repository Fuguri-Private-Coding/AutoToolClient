package fuguriprivatecoding.autotool.utils.inventory;

import fuguriprivatecoding.autotool.Client;
import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.UpdateEvent;
import fuguriprivatecoding.autotool.utils.interfaces.Imports;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.*;
import net.minecraft.util.StatCollector;

public class InventoryUtils implements Imports {

    private static boolean selector;

    public static boolean selector() {
        return selector;
    }

    public InventoryUtils() {
        Client.INST.getEventManager().register(this);
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof UpdateEvent) {
            if (getItemStack() != null) {
                ItemStack itemStack = getItemStack();

                selector = !trueName(itemStack).contains(itemStack.getDisplayName());
            } else {
                selector = false;
            }
        }
    }

    public static boolean selector(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        } else if (itemStack == getItemStack()) {
            return selector();
        } else {
            return !trueName(itemStack).contains(itemStack.getDisplayName());
        }
    }

    public static ItemStack getItemStack() {
        return (mc.thePlayer == null || mc.thePlayer.inventoryContainer == null ? null : mc.thePlayer.inventoryContainer.getSlot(getItemIndex() + 36).getStack());
    }

    public static int getItemIndex() {
        final InventoryPlayer inventoryPlayer = mc.thePlayer.inventory;
        return inventoryPlayer.currentItem;
    }

    public static String trueName(ItemStack itemStack) {
        String name = (StatCollector.translateToLocal(itemStack.getUnlocalizedName() + ".name")).trim();
        final String s1 = EntityList.getStringFromID(itemStack.getMetadata());

        if (s1 != null) {
            name = name + " " + StatCollector.translateToLocal("entity." + s1 + ".name");
        }

        return name;
    }

    public static boolean selector(int itemSlot) {
        return selector(mc.thePlayer.inventory.getStackInSlot(itemSlot));
    }


    public static boolean needDropGOVNO(ItemStack itemStack) {
        ItemStack bestSword = getBestWeapon();

        if (bestSword == null) return false;

        return itemStack.getItem() instanceof ItemSword && itemStack != bestSword;
    }

    public static ItemStack getBestWeapon() {
        ItemStack bestSword = null;
        float damage = -1;

        for (int i = 0; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                final ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

                if (is.getItem() instanceof ItemSword) {
                    float swordDamage = getItemDamage(is);

                    if (swordDamage > damage) {
                        damage = getItemDamage(is);
                        bestSword = is;
                    }
                }
            }
        }

        return bestSword;
    }

    public static void drop(int slot) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, 1, 4, mc.thePlayer);
    }

    public static float getItemDamage(ItemStack itemStack) {
        float damage = getToolMaterialRating(itemStack, true);
        damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, itemStack) * 1.25F;
        damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, itemStack) * 0.50F;
        damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, itemStack) * 0.01F;
        damage += (itemStack.getMaxDamage() - itemStack.getItemDamage()) * 0.000000000001F;

        if (itemStack.getItem() instanceof ItemSword) {
            damage += 0.2f;
        }

        return damage;
    }

    public static float getToolMaterialRating(ItemStack itemStack, boolean checkForDamage) {
        final Item is = itemStack.getItem();
        float rating = 0;

        if (is instanceof ItemSword) {
            rating = switch (((ItemSword) is).getToolMaterialName()) {
                case "WOOD", "GOLD" -> 4;
                case "STONE" -> 5;
                case "IRON" -> 6;
                case "EMERALD" -> 7;
                default -> rating;
            };
        } else if (is instanceof ItemPickaxe) {
            rating = switch (((ItemPickaxe) is).getToolMaterialName()) {
                case "WOOD", "GOLD" -> 2;
                case "STONE" -> 3;
                case "IRON" -> checkForDamage ? 4 : 40;
                case "EMERALD" -> checkForDamage ? 5 : 50;
                default -> rating;
            };
        } else if (is instanceof ItemAxe) {
            rating = switch (((ItemAxe) is).getToolMaterialName()) {
                case "WOOD", "GOLD" -> 3;
                case "STONE" -> 4;
                case "IRON" -> 5;
                case "EMERALD" -> 6;
                default -> rating;
            };
        } else if (is instanceof ItemSpade) {
            rating = switch (((ItemSpade) is).getToolMaterialName()) {
                case "WOOD", "GOLD" -> 1;
                case "STONE" -> 2;
                case "IRON" -> 3;
                case "EMERALD" -> 4;
                default -> rating;
            };
        }

        return rating;
    }
}