package me.hackclient.utils.inventory;

import me.hackclient.utils.interfaces.Imports;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.*;

public class InventoryUtils implements Imports {

    public static boolean needDropGOVNO(ItemStack itemStack) {
        ItemStack bestSword = getBestWeapon();

        if (bestSword == null) return false;

        if (itemStack.getItem() instanceof ItemSword && itemStack != bestSword) {
            return true;
        }

        return false;
    }

    public static ItemStack getBestWeapon() {
        ItemStack bestSword = null;
        float damage = -1;

        for (int i = 0; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                final ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

                if (is.getItem() instanceof ItemSword) {
                    float swordDamage = getItemDamage(is);

                    if (swordDamage > damage)
                    {
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

    public static float getItemDamage(ItemStack itemStack)
    {
        float damage = getToolMaterialRating(itemStack, true);
        damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, itemStack) * 1.25F;
        damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, itemStack) * 0.50F;
        damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, itemStack) * 0.01F;
        damage += (itemStack.getMaxDamage() - itemStack.getItemDamage()) * 0.000000000001F;

        if (itemStack.getItem() instanceof ItemSword)
        {
            damage += 0.2f;
        }

        return damage;
    }

    public static float getToolMaterialRating(ItemStack itemStack, boolean checkForDamage)
    {
        final Item is = itemStack.getItem();
        float rating = 0;

        if (is instanceof ItemSword)
        {
            switch (((ItemSword) is).getToolMaterialName())
            {
                case "WOOD":
                    rating = 4;
                    break;

                case "GOLD":
                    rating = 4;
                    break;

                case "STONE":
                    rating = 5;
                    break;

                case "IRON":
                    rating = 6;
                    break;

                case "EMERALD":
                    rating = 7;
                    break;
            }
        }
        else if (is instanceof ItemPickaxe)
        {
            switch (((ItemPickaxe) is).getToolMaterialName())
            {
                case "WOOD":
                    rating = 2;
                    break;

                case "GOLD":
                    rating = 2;
                    break;

                case "STONE":
                    rating = 3;
                    break;

                case "IRON":
                    rating = checkForDamage ? 4 : 40;
                    break;

                case "EMERALD":
                    rating = checkForDamage ? 5 : 50;
                    break;
            };
        }
        else if (is instanceof ItemAxe)
        {
            switch (((ItemAxe) is).getToolMaterialName())
            {
                case "WOOD":
                    rating = 3;
                    break;

                case "GOLD":
                    rating = 3;
                    break;

                case "STONE":
                    rating = 4;
                    break;

                case "IRON":
                    rating = 5;
                    break;

                case "EMERALD":
                    rating = 6;
                    break;
            };
        }
        else if (is instanceof ItemSpade)
        {
            switch (((ItemSpade) is).getToolMaterialName())
            {
                case "WOOD":
                    rating = 1;
                    break;

                case "GOLD":
                    rating = 1;
                    break;

                case "STONE":
                    rating = 2;
                    break;

                case "IRON":
                    rating = 3;
                    break;

                case "EMERALD":
                    rating = 4;
                    break;
            }
        }

        return rating;
    }
}
