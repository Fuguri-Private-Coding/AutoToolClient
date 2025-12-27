package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.*;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.utils.packet.PacketUtils;
import fuguriprivatecoding.autotoolrecode.utils.player.ItemUtils;
import fuguriprivatecoding.autotoolrecode.utils.player.PlayerUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.player.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.CameraRot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.raytrace.RayCastUtils;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockBed;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.*;

import java.awt.*;

@ModuleInfo(name = "Fucker", category = Category.PLAYER, description = "Автоматически ломает кровать через стены.")
public class Fucker extends Module {

    CheckBox instantBreak = new CheckBox("InstantBreak", this);

    CheckBox whiteListOwnBed = new CheckBox("WhiteListOwnBed", this);
    CheckBox emptySurrounding = new CheckBox("EmptySurrounding", this);

    CheckBox renderBreaking = new CheckBox("RenderBreaking", this, true);
    ColorSetting color = new ColorSetting("Color", this, renderBreaking::isToggled);

    CheckBox glow = new CheckBox("Glow", this, renderBreaking::isToggled, true);
    ColorSetting glowColor = new ColorSetting("GlowColor", this, () -> renderBreaking.isToggled() && glow.isToggled());

    private Vec3 block;
    private Vec3 home;
    private double damage;

    private int delay = 0;

    @Override
    public void onDisable() {
        super.onDisable();
        delay = 5;
        damage = 0;
        CameraRot.INST.setWillChange(false);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof TeleportEvent e) {
            if (whiteListOwnBed.isToggled()) {
                final double distance = mc.thePlayer.getDistance(e.getX(), e.getY(), e.getZ());

                if (distance > 40) {
                    home = new Vec3(e.getX(), e.getY(), e.getZ());
                }
            }
        }

        if (Modules.getModule(Scaffold.class).isToggled() || TargetStorage.getTarget() != null) return;

        if (event instanceof TickEvent) {
            if (delay > 0) {
                delay--;
                return;
            }

            block = this.findBlock();

            if (block == null) return;

            Rot needRot = RotUtils.getRotationToPoint(block);

            Rot delta = RotUtils.getDelta(mc.thePlayer.getRotation(), needRot);

            CameraRot.INST.setUnlocked(true);
            mc.thePlayer.moveRotation(delta.fix());
        }

        if (event instanceof Render3DEvent && block != null && renderBreaking.isToggled()) {
            Color bedColor = new Colors(color.getFadedColor());
            Color bedGlowColor = new Colors(glowColor.getFadedColor());

            RenderUtils.start3D();

            BlockPos bedPos = new BlockPos(block);

            if (glow.isToggled()) BloomUtils.addToDraw(() -> RenderUtils.drawBlockESP(bedPos, bedGlowColor));
            RenderUtils.drawBlockESP(bedPos, bedColor);

            ColorUtils.resetColor();
            RenderUtils.stop3D();
        }

        if (event instanceof LegitClickTimingEvent && block != null && CameraRot.INST.isUnlocked() && delay == 0) {
            mine();
        }

        if (block != null) {
            if (event instanceof MoveEvent e) {
                MoveUtils.moveFix(e, MoveUtils.getDirection(CameraRot.INST.getYaw(), e.getForward(), e.getStrafe()));
            }
        }
    }

    public void updateDamage(final BlockPos blockPos, final double hardness) {
        damage += hardness;
        mc.theWorld.sendBlockBreakProgress(mc.thePlayer.getEntityId(), blockPos, (int) (damage * 10 - 1));
    }

    public void mine() {
        final BlockPos blockPos = new BlockPos(block.xCoord, block.yCoord, block.zCoord);
        final double hardness = ItemUtils.getPlayerRelativeBlockHardness(mc.theWorld, blockPos, mc.thePlayer.inventory.fakeCurrentItem);

        if (instantBreak.isToggled()) {
            PacketUtils.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, blockPos, EnumFacing.UP));
            PacketUtils.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos, EnumFacing.UP));
            mc.playerController.onPlayerDestroyBlock(blockPos, EnumFacing.DOWN);
        } else {
            if (damage <= 0) {
                PacketUtils.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, blockPos, EnumFacing.UP));

                if (hardness >= 1) {
                    mc.playerController.onPlayerDestroyBlock(blockPos, EnumFacing.DOWN);
                    delay = 5;
                    damage = 0;
                }

                this.updateDamage(blockPos, hardness);
            } else if (damage > 1) {
                PacketUtils.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos, EnumFacing.UP));
                mc.playerController.onPlayerDestroyBlock(blockPos, EnumFacing.DOWN);
                damage = 0;
                delay = 5;
                this.updateDamage(blockPos, hardness);
            } else {
                this.updateDamage(blockPos, hardness);
            }

            mc.thePlayer.swingItem();
        }
    }

    public Vec3 findBlock() {
        if (home != null && mc.thePlayer.getDistanceSq(home.xCoord, home.yCoord, home.zCoord) < 35 * 35 && whiteListOwnBed.isToggled()) {
            return null;
        }

        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {

                    final Block block = PlayerUtils.blockRelativeToPlayer(x, y, z);
                    final Vec3 position = new Vec3(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z);

                    if (!(block instanceof BlockBed)) {
                        continue;
                    }

                    final RayTrace movingObjectPosition = RayCastUtils.rayCast(RotUtils.getRotationToPoint(position), 4.5f);
                    if (movingObjectPosition == null || movingObjectPosition.hitVec.distanceTo(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)) > 4.5) {
                        continue;
                    }

                    if (emptySurrounding.isToggled()) {
                        Vec3 addVec = position;
                        double hardness = Double.MAX_VALUE;
                        boolean empty = false;

                        for (int addX = -1; addX <= 1; addX++) {
                            for (int addY = 0; addY <= 1; addY++) {
                                for (int addZ = -1; addZ <= 1; addZ++) {
                                    if (empty || (mc.thePlayer.getDistanceSq(position.xCoord + addX, position.yCoord + addY, position.zCoord + addZ) + 4 > 4.5 * 4.5))
                                        continue;

                                    if (Math.abs(addX) + Math.abs(addY) + Math.abs(addZ) != 1) {
                                        continue;
                                    }

                                    Block possibleBlock = PlayerUtils.block(position.xCoord + addX, position.yCoord + addY, position.zCoord + addZ);

                                    if (possibleBlock instanceof BlockBed) {
                                        continue;
                                    } else if (possibleBlock instanceof BlockAir) {
                                        empty = true;
                                        continue;
                                    }

                                    double possibleHardness = possibleBlock.getBlockHardness();

                                    if (possibleHardness < hardness) {
                                        hardness = possibleHardness;
                                        addVec = position.add(new Vec3(addX, addY, addZ));
                                    }
                                }
                            }
                        }

                        if (!empty) {
                            if (addVec.equals(position)) {
                                return null;
                            } else {
                                return addVec;
                            }
                        }
                    }

                    return position;
                }
            }
        }

        return null;
    }
}
