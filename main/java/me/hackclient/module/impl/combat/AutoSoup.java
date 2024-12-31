package me.hackclient.module.impl.combat;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.RunGameLoopEvent;
import me.hackclient.event.events.TickEvent;
import me.hackclient.event.events.UpdateEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.utils.client.ClientUtils;
import me.hackclient.utils.timer.StopWatch;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.concurrent.ThreadLocalRandom;

@ModuleInfo(name = "AutoSoup", category = Category.COMBAT)
public class AutoSoup extends Module {

    final StopWatch timer;
    IntegerSetting health = new IntegerSetting("Health", this, 4, 12, 10);

//    boolean used, dropped, test1, test2;
//
//    int prevSlot;

//    int slot;
//    boolean used;
    private int slotOnLastTick = 0;
    private int previousSlot;
    private boolean clicked;
    private boolean dropped;

    public AutoSoup() {
        timer = new StopWatch();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof UpdateEvent) {
            int slot;
            if (mc.currentScreen == null) {
                if ((mc.thePlayer.getCurrentEquippedItem() != null && mc.thePlayer.getCurrentEquippedItem().getItem() == Items.bowl || this.clicked) && mc.thePlayer.inventory.currentItem == this.slotOnLastTick) {
                    this.dropped = true;
                    this.clicked = false;
                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ALL_ITEMS, BlockPos.ORIGIN, EnumFacing.DOWN));
                    return;
                }

                if (this.dropped) {
                    this.dropped = false;
                    if (this.previousSlot != -1) {
                        mc.thePlayer.inventory.currentItem = this.previousSlot;
                    }

                    this.previousSlot = -1;
                }

                slot = this.getSoup();
                if (slot != -1) {
                    if ((double)mc.thePlayer.getHealth() <= this.health.getValue() && !this.clicked) {
                        if (mc.thePlayer.getCurrentEquippedItem() != null && mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemSoup) {
                            mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem)));
                            this.clicked = true;
                        } else {
                            if (this.previousSlot == -1) {
                                this.previousSlot = mc.thePlayer.inventory.currentItem;
                            }

                            mc.thePlayer.inventory.currentItem = slot;
                        }
                    }
                } else {
                    slot = this.getSoupInWholeInventory();
                    if (slot != -1) {
                        this.openInventory();
                    }
                }
            } else if (mc.currentScreen instanceof GuiInventory) {
                slot = this.getEmptySoup();
                if (slot != -1) {
                    if (Math.sin(ThreadLocalRandom.current().nextDouble(0.0, 6.283185307179586)) <= 0.5) {
                        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, 1, 4, mc.thePlayer);
                    }
                } else {
                    slot = this.getSoupExceptHotbar();
                    boolean full = true;
                    int i = 0;

                    while(true) {
                        if (i < 9) {
                            ItemStack item = mc.thePlayer.inventory.mainInventory[i];
                            if (item != null) {
                                ++i;
                                continue;
                            }

                            full = false;
                        }

                        //if (this.autoClose.getBoolean() && (slot == -1 || full)) {
                            //    mc.thePlayer.closeScreen();
                            //    mc.displayGuiScreen((GuiScreen)null);
                            //    mc.setIngameFocus();
                            //    return;
                            //}

                        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, 0, 1, mc.thePlayer);
                        break;
                    }
                }
            }

            this.slotOnLastTick = mc.thePlayer.inventory.currentItem;
//            if (slot == -1) {
//                slot = getSoup();
//            }
//
//            if (slot == -1)
//                return;
//
//            if (timer.reachedMS() < 300) {
//                ClientUtils.chatLog("Time reachedMS");
//                return;
//            }
//
//            if (mc.thePlayer.getHealth() <= health.getValue() && !used) {
//                if (mc.thePlayer.serverSlot != slot) {
//                    ClientUtils.chatLog("Switched from " + mc.thePlayer.serverSlot + " to " + slot);
//                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(slot));
//                    mc.thePlayer.serverSlot = slot;
//                }
//                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(slot)));
//                ClientUtils.chatLog("Using " + slot);
//                used = true;
//                return;
//            }
//
//            if (used) {
//                if (mc.thePlayer.serverSlot != slot) {
//                    ClientUtils.chatLog("Switched from " + mc.thePlayer.serverSlot + " to " + slot + "AND Return");
//                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(slot));
//                    mc.thePlayer.serverSlot = slot;
//                    return;
//                }
//                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
//                ClientUtils.chatLog("Drop " + slot);
//                used = false;
//                timer.reset();
//                slot = -1;
//                if (mc.thePlayer.serverSlot != mc.thePlayer.inventory.currentItem) {
//                    ClientUtils.chatLog("Switched from " + mc.thePlayer.serverSlot + " to " + mc.thePlayer.inventory.currentItem);
//                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
//                    mc.thePlayer.serverSlot = mc.thePlayer.inventory.currentItem;
//                }
//            }
//

//            if (mc.thePlayer.getHealth() <= health.getValue() && !test1) {
//                test2 = mc.thePlayer.serverSlot != soup;
//                if (test2) {
//                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(soup));
//                    mc.thePlayer.serverSlot = soup;
//                    return;
//                }
//                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(soup)));
//                test1 = true;
//                return;
//            }
//
//            if (test1) {
//                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
//                if (test2) {
//                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
//                    mc.thePlayer.serverSlot = mc.thePlayer.inventory.currentItem;
//                }
//                test1 = false;
//                test2 = false;
//                timer.reset();
//            }

//            if (mc.thePlayer.getHealth() < health.getValue() && !test1) {
//                test2 = mc.thePlayer.inventory.currentItem != soup;
//                if (test2) {
//                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(soup));
//                }
//                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(soup)));
//                test1 = true;
//                return;
//            }
//
//            if (test1) {
//                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
//                if (test2) mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
//                test1 = false;
//                test2 = false;
//            }

            /*if (mc.thePlayer.inventory.getCurrentItem().getItem() == Items.bowl) {
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                mc.thePlayer.addChatMessage(new ChatComponentText("try drop"));
                return;
            }

            if (mc.thePlayer.inventory.getCurrentItem() == null || mc.thePlayer.inventory.getCurrentItem().getItem() == null) {
                mc.thePlayer.addChatMessage(new ChatComponentText("swith back"));
                mc.thePlayer.inventory.currentItem = prevSlot;
                timer.reset();
            }*/

            //if (mc.thePlayer.inventory.currentItem != soup) {
            //    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(soup));
            //}
            //
            //mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ALL_ITEMS, BlockPos.ORIGIN, EnumFacing.DOWN));
            //if (mc.thePlayer.inventory.currentItem != soup) {
            //    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
            //}
            //timer.reset();
        }
    }

    public void openInventory() {
        mc.getNetHandler().addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
        mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
    }

    public int getEmptySoup() {
        if (mc.currentScreen instanceof GuiInventory) {
            GuiInventory inventory = (GuiInventory)mc.currentScreen;

            for(int i = 36; i < 45; ++i) {
                ItemStack item = (ItemStack)inventory.inventorySlots.getInventory().get(i);
                if (item != null && item.getItem() == Items.bowl) {
                    return i;
                }
            }
        }

        return -1;
    }

    public int getSoupExceptHotbar() {
        for(int i = 9; i < mc.thePlayer.inventory.mainInventory.length; ++i) {
            ItemStack item = mc.thePlayer.inventory.mainInventory[i];
            if (item != null && item.getItem() instanceof ItemSoup) {
                return i;
            }
        }

        return -1;
    }

    public int getSoupInWholeInventory() {
        for(int i = 0; i < mc.thePlayer.inventory.mainInventory.length; ++i) {
            ItemStack item = mc.thePlayer.inventory.mainInventory[i];
            if (item != null && item.getItem() instanceof ItemSoup) {
                return i;
            }
        }

        return -1;
    }

    int getSoup() {
        for (int i = 0; i < 9; ++i) {
            ItemStack item = mc.thePlayer.inventory.mainInventory[i];
            if (item == null || !(item.getItem() instanceof ItemSoup)) continue;
            return i;
        }
        return -1;
    }
}
