package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.inventory.InventoryUtil;
import fuguriprivatecoding.autotoolrecode.utils.inventory.ItemUtil;
import fuguriprivatecoding.autotoolrecode.utils.inventory.PlayerUtil;
import fuguriprivatecoding.autotoolrecode.utils.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.*;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;

@ModuleInfo(name = "InvManager", category = Category.PLAYER, description = "Автоматически сортирует ваш инвентарь и выкидывает мусор.")
public class InvManager extends Module {

    DoubleSlider startDelay = new DoubleSlider("StartDelay", this, 0,500,200,1);

    DoubleSlider delay = new DoubleSlider("Delay", this, 0,500,200,1);

    private final CheckBox spoof = new CheckBox("Spoof", this);
    private final CheckBox stopWalkingIfSpoof = new CheckBox("StopSpoofIfWalking", this);

    private final IntegerSetting swordSlot = new IntegerSetting("Sword Slot", this, 1, 9, 1);
    private final IntegerSetting pickaxeSlot = new IntegerSetting("Pickaxe Slot", this,1, 9, 1);
    private final IntegerSetting axeSlot = new IntegerSetting("Axe Slot", this,1,9, 1);
    private final IntegerSetting shovelSlot = new IntegerSetting("Shovel Slot", this,1, 9, 1);
    private final IntegerSetting blockSlot = new IntegerSetting("Block Slot", this,1, 9, 1);
    private final IntegerSetting potionSlot = new IntegerSetting("Potion Slot", this,1, 9, 1);
    private final IntegerSetting foodSlot = new IntegerSetting("Food Slot", this,1, 9, 1);
    private final IntegerSetting pearlSlot = new IntegerSetting("Pearl Slot", this,1, 9, 1);

    private final StopWatch stopwatch = new StopWatch();
    private final StopWatch startTimer = new StopWatch();
    private boolean moved;
    private long nextClick;

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            if (mc.thePlayer.ticksExisted <= 40) {
                return;
            }

            if (!spoof.isToggled() && !(mc.currentScreen instanceof GuiInventory)) {
                this.stopwatch.reset();
                this.startTimer.reset();
                return;
            } else if (spoof.isToggled() && stopWalkingIfSpoof.isToggled()) {
                if (MoveUtils.isMoving()) return;
            }

            if (!startTimer.reachedMS(startDelay.getRandomizedIntValue()) || !stopwatch.reachedMS(delay.getRandomizedIntValue())) return;

            this.moved = false;

            int helmet = -1;
            int chestplate = -1;
            int leggings = -1;
            int boots = -1;

            int sword = -1;
            int pickaxe = -1;
            int axe = -1;
            int shovel = -1;
            int block = -1;
            int potion = -1;
            int food = -1;
            int pearl = -1;

            int INVENTORY_ROWS = 4;
            int INVENTORY_COLUMNS = 9;
            int ARMOR_SLOTS = 4;
            int INVENTORY_SLOTS = (INVENTORY_ROWS * INVENTORY_COLUMNS) + ARMOR_SLOTS;
            for (int i = 0; i < INVENTORY_SLOTS; i++) {
                final ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);

                if (stack == null) {
                    continue;
                }

                final Item item = stack.getItem();

                if (!ItemUtil.useful(stack)) {
                    this.throwItem(i);
                }

                if (item instanceof ItemArmor armor) {
                    final int reduction = this.armorReduction(stack);

                    switch (armor.armorType) {
                        case 0:
                            if (helmet == -1 || reduction > armorReduction(mc.thePlayer.inventory.getStackInSlot(helmet))) {
                                helmet = i;
                            }
                            break;

                        case 1:
                            if (chestplate == -1 || reduction > armorReduction(mc.thePlayer.inventory.getStackInSlot(chestplate))) {
                                chestplate = i;
                            }
                            break;

                        case 2:
                            if (leggings == -1 || reduction > armorReduction(mc.thePlayer.inventory.getStackInSlot(leggings))) {
                                leggings = i;
                            }
                            break;

                        case 3:
                            if (boots == -1 || reduction > armorReduction(mc.thePlayer.inventory.getStackInSlot(boots))) {
                                boots = i;
                            }
                            break;
                    }
                }

                if (item instanceof ItemSword) {
                    if (sword == -1 || damage(stack) > damage(mc.thePlayer.inventory.getStackInSlot(sword))) {
                        sword = i;
                    }

                    if (i != sword) {
                        this.throwItem(i);
                    }
                }

                if (item instanceof ItemPickaxe) {
                    if (pickaxe == -1 || mineSpeed(stack) > mineSpeed(mc.thePlayer.inventory.getStackInSlot(pickaxe))) {
                        pickaxe = i;
                    }

                    if (i != pickaxe) {
                        this.throwItem(i);
                    }
                }

                if (item instanceof ItemAxe) {
                    if (axe == -1 || mineSpeed(stack) > mineSpeed(mc.thePlayer.inventory.getStackInSlot(axe))) {
                        axe = i;
                    }

                    if (i != axe) {
                        this.throwItem(i);
                    }
                }

                if (item instanceof ItemSpade) {
                    if (shovel == -1 || mineSpeed(stack) > mineSpeed(mc.thePlayer.inventory.getStackInSlot(shovel))) {
                        shovel = i;
                    }

                    if (i != shovel) {
                        this.throwItem(i);
                    }
                }

                if (item instanceof ItemBlock) {
                    if (block == -1) {
                        block = i;
                    } else {
                        final ItemStack currentStack = mc.thePlayer.inventory.getStackInSlot(block);

                        if (currentStack != null && stack.stackSize > currentStack.stackSize) {
                            block = i;
                        }
                    }
                }

                if (item instanceof ItemPotion itemPotion) {
                    if (potion == -1) {
                        potion = i;
                    } else {
                        final ItemStack currentStack = mc.thePlayer.inventory.getStackInSlot(potion);

                        if (currentStack == null) {
                            continue;
                        }

                        final ItemPotion currentItemPotion = (ItemPotion) currentStack.getItem();

                        boolean foundCurrent = false;

                        for (final PotionEffect e : mc.thePlayer.getActivePotionEffects()) {
                            if (e.getPotionID() == currentItemPotion.getEffects(currentStack).getFirst().getPotionID() && e.getDuration() > 0) {
                                foundCurrent = true;
                                break;
                            }
                        }

                        boolean found = false;

                        for (final PotionEffect e : mc.thePlayer.getActivePotionEffects()) {
                            if (e.getPotionID() == itemPotion.getEffects(stack).getFirst().getPotionID() && e.getDuration() > 0) {
                                found = true;
                                break;
                            }
                        }

                        if (itemPotion.getEffects(stack) != null && currentItemPotion.getEffects(currentStack) != null) {
                            if ((PlayerUtil.potionRanking(itemPotion.getEffects(stack).getFirst().getPotionID()) > PlayerUtil.potionRanking(currentItemPotion.getEffects(currentStack).getFirst().getPotionID()) || foundCurrent) && !found) {
                                potion = i;
                            }
                        }
                    }
                }

                if (item instanceof ItemFood itemFood) {
                    if (food == -1) {
                        food = i;
                    } else {
                        final ItemStack currentStack = mc.thePlayer.inventory.getStackInSlot(food);

                        if (currentStack == null) {
                            continue;
                        }

                        final ItemFood currentItemFood = (ItemFood) currentStack.getItem();

                        if (itemFood.getSaturationModifier(stack) > currentItemFood.getSaturationModifier(currentStack)) {
                            food = i;
                        }
                    }
                }

                if (item instanceof ItemEnderPearl) {
                    if (pearl == -1) {
                        pearl = i;
                    }
                }
            }

            for (int i = 0; i < INVENTORY_SLOTS; i++) {
                final ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);

                if (stack == null) {
                    continue;
                }

                final Item item = stack.getItem();

                if (item instanceof ItemArmor armor) {
                    switch (armor.armorType) {
                        case 0:
                            if (i != helmet) {
                                this.throwItem(i);
                            }
                            break;

                        case 1:
                            if (i != chestplate) {
                                this.throwItem(i);
                            }
                            break;

                        case 2:
                            if (i != leggings) {
                                this.throwItem(i);
                            }
                            break;

                        case 3:
                            if (i != boots) {
                                this.throwItem(i);
                            }
                            break;
                    }
                }
            }

            if (helmet != -1 && helmet != 39) {
                this.equipItem(helmet);
            }

            if (chestplate != -1 && chestplate != 38) {
                this.equipItem(chestplate);
            }

            if (leggings != -1 && leggings != 37) {
                this.equipItem(leggings);
            }

            if (boots != -1 && boots != 36) {
                this.equipItem(boots);
            }

            if (sword != -1 && sword != this.swordSlot.getValue() - 1) {
                this.moveItem(sword, this.swordSlot.getValue() - 37);
            }

            if (pickaxe != -1 && pickaxe != this.pickaxeSlot.getValue() - 1) {
                this.moveItem(pickaxe, this.pickaxeSlot.getValue() - 37);
            }

            if (axe != -1 && axe != this.axeSlot.getValue() - 1) {
                this.moveItem(axe, this.axeSlot.getValue() - 37);
            }

            if (shovel != -1 && shovel != this.shovelSlot.getValue() - 1) {
                this.moveItem(shovel, this.shovelSlot.getValue() - 37);
            }

            if (block != -1 && block != this.blockSlot.getValue() - 1) {
                this.moveItem(block, this.blockSlot.getValue() - 37);
            }

            if (potion != -1 && potion != this.potionSlot.getValue() - 1) {
                this.moveItem(potion, this.potionSlot.getValue() - 37);
            }

            if (food != -1 && food != this.foodSlot.getValue() - 1) {
                this.moveItem(food, this.foodSlot.getValue() - 37);
            }

            if (pearl != -1 && pearl != this.pearlSlot.getValue() - 1) {
                moveItem(pearl, this.pearlSlot.getValue() - 37);
            }
        }
    }

    private void throwItem(final int slot) {
        if ((!this.moved || this.nextClick <= 0) && !InventoryUtil.selector(slot)) {

            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, this.slot(slot), 1, 4, mc.thePlayer);

            this.nextClick = delay.getRandomizedIntValue();
            this.stopwatch.reset();
            this.moved = true;
        }
    }

    private void moveItem(final int slot, final int destination) {
        if ((!this.moved || this.nextClick <= 0) && !InventoryUtil.selector(slot)) {

            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, this.slot(slot), this.slot(destination), 2, mc.thePlayer);

            this.nextClick = delay.getRandomizedIntValue();
            this.stopwatch.reset();
            this.moved = true;
        }
    }

    private void equipItem(final int slot) {
        if ((!this.moved || this.nextClick <= 0) && !InventoryUtil.selector(slot)) {

            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, this.slot(slot), 0, 1, mc.thePlayer);

            this.nextClick = delay.getRandomizedIntValue();
            this.stopwatch.reset();
            this.moved = true;
        }
    }

    private float damage(final ItemStack stack) {
        final ItemSword sword = (ItemSword) stack.getItem();
        final int level = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack);
        return (float) (sword.getDamageVsEntity() + level * 1.25);
    }

    private float mineSpeed(final ItemStack stack) {
        final Item item = stack.getItem();
        int level = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack);

        level = switch (level) {
            case 1 -> 30;
            case 2 -> 69;
            case 3 -> 120;
            case 4 -> 186;
            case 5 -> 271;
            default -> 0;
        };

        if (item instanceof ItemPickaxe pickaxe) {
            return pickaxe.getToolMaterial().getEfficiencyOnProperMaterial() + level;
        } else if (item instanceof ItemSpade shovel) {
            return shovel.getToolMaterial().getEfficiencyOnProperMaterial() + level;
        } else if (item instanceof ItemAxe axe) {
            return axe.getToolMaterial().getEfficiencyOnProperMaterial() + level;
        }

        return 0;
    }

    private int armorReduction(final ItemStack stack) {
        final ItemArmor armor = (ItemArmor) stack.getItem();
        return armor.damageReduceAmount + EnchantmentHelper.getEnchantmentModifierDamage(new ItemStack[]{stack}, DamageSource.generic);
    }

    private int slot(final int slot) {
        if (slot >= 36) {
            return 8 - (slot - 36);
        }

        if (slot < 9) {
            return slot + 36;
        }

        return slot;
    }
}
