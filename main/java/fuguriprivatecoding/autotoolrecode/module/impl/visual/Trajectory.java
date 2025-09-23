package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import net.minecraft.item.*;
import net.minecraft.util.Vec3;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.opengl.GL11;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@ModuleInfo(name = "Trajectory", category = Category.VISUAL, description = "Показывает траекторию полета бросаемых вещей.")
public class Trajectory extends Module {

    public final ColorSetting color = new ColorSetting("Color", this);

    FloatSetting lineWidth = new FloatSetting("Line Width", this,0.1f, 20, 1, 0.1f);

    List<Vec3> positions = new ArrayList<>();
    Color fadeColor;

    @Override
    public void onDisable() {
        positions.clear();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof Render3DEvent) {
            positions.clear();

            updateColors();

            ItemStack itemStack = mc.thePlayer.getCurrentEquippedItem();
            MovingObjectPosition m = null;
            if (itemStack != null
                    && (
                    itemStack.getItem() instanceof ItemSnowball
                            || itemStack.getItem() instanceof ItemEgg
                            || itemStack.getItem() instanceof ItemBow
                            || itemStack.getItem() instanceof ItemEnderPearl
            )) {
                EntityLivingBase thrower = mc.thePlayer;
                float rotationYaw = thrower.prevRotationYaw + (thrower.rotationYaw - thrower.prevRotationYaw) * mc.timer.renderPartialTicks;
                float rotationPitch = thrower.prevRotationPitch + (thrower.rotationPitch - thrower.prevRotationPitch) * mc.timer.renderPartialTicks;
                double posX = thrower.lastTickPosX + (thrower.posX - thrower.lastTickPosX) * mc.timer.renderPartialTicks;
                double posY = thrower.lastTickPosY + thrower.getEyeHeight() + (thrower.posY - thrower.lastTickPosY) * mc.timer.renderPartialTicks;
                double posZ = thrower.lastTickPosZ + (thrower.posZ - thrower.lastTickPosZ) * mc.timer.renderPartialTicks;
                posX -= MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
                posY -= 0.1F;
                posZ -= MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
                float multipicator = 0.4F;
                if (itemStack.getItem() instanceof ItemBow) {
                    multipicator = 1.0F;
                }

                double motionX = -MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI) * multipicator;
                double motionZ = MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI) * multipicator;
                double motionY = -MathHelper.sin(rotationPitch / 180.0F * (float) Math.PI) * multipicator;
                float inaccuracy = 0.0F;
                float velocity = 1.5F;
                if (itemStack.getItem() instanceof ItemBow) {
                    int i = mc.thePlayer.getItemInUseDuration() - mc.thePlayer.getItemInUseCount();
                    float f = i / 20.0F;
                    f = (f * f + f * 2.0F) / 3.0F;
                    if (f < 0.1) {
                        return;
                    }

                    if (f > 1.0F) {
                        f = 1.0F;
                    }

                    velocity = f * 2.0F * 1.5F;
                }

                Random rand = new Random();
                float ff = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
                double x = motionX / ff;
                double y = motionY / ff;
                double z = motionZ / ff;
                x += rand.nextGaussian() * 0.0075F * inaccuracy;
                y += rand.nextGaussian() * 0.0075F * inaccuracy;
                z += rand.nextGaussian() * 0.0075F * inaccuracy;
                x *= velocity;
                y *= velocity;
                z *= velocity;
                motionX = x;
                motionY = y;
                motionZ = z;
                float prevRotationYaw = (float) (MathHelper.func_181159_b(x, z) * 180.0f / Math.PI);
                float prevRotationPitch = (float) (MathHelper.func_181159_b(y, MathHelper.sqrt_double(x * x + z * z)) * 180.0f / Math.PI);
                boolean b = true;
                int ticksInAir = 0;

                while (b) {
                    if (ticksInAir > 300) {
                        b = false;
                    }

                    ++ticksInAir;
                    Vec3 vec3 = new Vec3(posX, posY, posZ);
                    Vec3 vec31 = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
                    MovingObjectPosition movingobjectposition = mc.theWorld.rayTraceBlocks(vec3, vec31);
                    vec3 = new Vec3(posX, posY, posZ);
                    vec31 = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
                    if (movingobjectposition != null) {
                        vec31 = new Vec3(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
                    }

                    for (Entity entity : mc.theWorld.loadedEntityList) {
                        if (entity != mc.thePlayer && entity instanceof EntityLivingBase) {
                            float f = 0.3F;
                            AxisAlignedBB localAxisAlignedBB = entity.getEntityBoundingBox().expand(f, f, f);
                            MovingObjectPosition localMovingObjectPosition = localAxisAlignedBB.calculateIntercept(vec3, vec31);
                            if (localMovingObjectPosition != null) {
                                movingobjectposition = localMovingObjectPosition;
                                break;
                            }
                        }
                    }

                    if (movingobjectposition != null) {
                        b = false;
                    }

                    m = movingobjectposition;
                    posX += motionX;
                    posY += motionY;
                    posZ += motionZ;
                    float f1 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
                    rotationYaw = (float) (MathHelper.func_181159_b(motionX, motionZ) * 180.0 / Math.PI);
                    rotationPitch = (float) (MathHelper.func_181159_b(motionY, f1) * 180.0 / Math.PI);

                    while (rotationPitch - prevRotationPitch < -180.0F) {
                        prevRotationPitch -= 360.0F;
                    }

                    while (rotationPitch - prevRotationPitch >= 180.0F) {
                        prevRotationPitch += 360.0F;
                    }

                    while (rotationYaw - prevRotationYaw < -180.0F) {
                        prevRotationYaw -= 360.0F;
                    }

                    while (rotationYaw - prevRotationYaw >= 180.0F) {
                        prevRotationYaw += 360.0F;
                    }

                    float f2 = 0.99F;
                    float f3 = 0.03F;
                    if (itemStack.getItem() instanceof ItemBow) {
                        f3 = 0.05F;
                    }

                    motionX *= f2;
                    motionY *= f2;
                    motionZ *= f2;
                    motionY -= f3;
                    positions.add(new Vec3(posX, posY, posZ));
                }

                if (positions.size() > 1) {
                    GL11.glEnable(3042);
                    GL11.glBlendFunc(770, 771);
                    GL11.glEnable(2848);
                    GL11.glDisable(3553);
                    GlStateManager.disableCull();
                    GL11.glDepthMask(false);
                    GL11.glColor4f(
                            fadeColor.getRed() / 255.0F,
                            fadeColor.getGreen() / 255.0F,
                            fadeColor.getBlue() / 255.0F,
                            fadeColor.getAlpha() / 255f
                    );
                    GL11.glLineWidth(lineWidth.getValue() / 2.0F);
                    Tessellator tessellator = Tessellator.getInstance();
                    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                    worldrenderer.begin(3, DefaultVertexFormats.POSITION);

                    for (Vec3 vec3 : positions) {
                        worldrenderer.pos(
                                        vec3.xCoord - mc.getRenderManager().viewerPosX,
                                        vec3.yCoord - mc.getRenderManager().viewerPosY,
                                        vec3.zCoord - mc.getRenderManager().viewerPosZ
                                )
                                .endVertex();
                    }

                    tessellator.draw();
                    if (m != null) {
                        GL11.glColor4f(
                                fadeColor.getRed() / 255.0F,
                                fadeColor.getGreen() / 255.0F,
                                fadeColor.getBlue() / 255.0F,
                                fadeColor.getAlpha() / 255f
                        );
                        Vec3 hitVec = m.hitVec;
                        EnumFacing enumFacing1 = m.sideHit;
                        double minX = hitVec.xCoord - mc.getRenderManager().viewerPosX;
                        double maxX = hitVec.xCoord - mc.getRenderManager().viewerPosX;
                        double minY = hitVec.yCoord - mc.getRenderManager().viewerPosY;
                        double maxY = hitVec.yCoord - mc.getRenderManager().viewerPosY;
                        double minZ = hitVec.zCoord - mc.getRenderManager().viewerPosZ;
                        double maxZ = hitVec.zCoord - mc.getRenderManager().viewerPosZ;

                        switch (enumFacing1) {
                            case SOUTH -> {
                                minX = (minX - 0.4);
                                maxX = (maxX + 0.4);
                                minY = (minY - 0.4);
                                maxY = (maxY + 0.4);
                                maxZ = (maxZ + 0.02);
                                minZ = (minZ + 0.05);
                            }
                            case NORTH -> {
                                minX = (minX - 0.4);
                                maxX = (maxX + 0.4);
                                minY = (minY - 0.4);
                                maxY = (maxY + 0.4);
                                maxZ = (maxZ - 0.02);
                                minZ = (minZ - 0.05);
                            }

                            case EAST -> {
                                maxX = (maxX + 0.02);
                                minX = (minX + 0.05);
                                minY = (minY - 0.4);
                                maxY = (maxY + 0.4);
                                minZ = (minZ - 0.4);
                                maxZ = (maxZ + 0.4);
                            }

                            case WEST -> {
                                maxX = (maxX - 0.02);
                                minX = (minX - 0.05);
                                minY = (minY - 0.4);
                                maxY = (maxY + 0.4);
                                minZ = (minZ - 0.4);
                                maxZ = (maxZ + 0.4);
                            }

                            case UP -> {
                                minX = (minX - 0.4);
                                maxX = (maxX + 0.4);
                                maxY = (maxY + 0.02);
                                minY = (minY + 0.05);
                                minZ = (minZ - 0.4);
                                maxZ = (maxZ + 0.4);
                            }

                            case DOWN -> {
                                minX = (minX - 0.4);
                                maxX = (maxX + 0.4);
                                maxY = (maxY - 0.02);
                                minY = (minY - 0.05);
                                minZ = (minZ - 0.4);
                                maxZ = (maxZ + 0.4);
                            }
                        }

                        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
                        worldrenderer.pos(minX, minY, minZ).endVertex();
                        worldrenderer.pos(minX, minY, maxZ).endVertex();
                        worldrenderer.pos(minX, maxY, maxZ).endVertex();
                        worldrenderer.pos(minX, maxY, minZ).endVertex();
                        worldrenderer.pos(minX, minY, maxZ).endVertex();
                        worldrenderer.pos(maxX, minY, maxZ).endVertex();
                        worldrenderer.pos(maxX, maxY, maxZ).endVertex();
                        worldrenderer.pos(minX, maxY, maxZ).endVertex();
                        worldrenderer.pos(maxX, minY, maxZ).endVertex();
                        worldrenderer.pos(maxX, minY, minZ).endVertex();
                        worldrenderer.pos(maxX, maxY, minZ).endVertex();
                        worldrenderer.pos(maxX, maxY, maxZ).endVertex();
                        worldrenderer.pos(maxX, minY, minZ).endVertex();
                        worldrenderer.pos(minX, minY, minZ).endVertex();
                        worldrenderer.pos(minX, maxY, minZ).endVertex();
                        worldrenderer.pos(maxX, maxY, minZ).endVertex();
                        worldrenderer.pos(minX, minY, minZ).endVertex();
                        worldrenderer.pos(minX, minY, maxZ).endVertex();
                        worldrenderer.pos(maxX, minY, maxZ).endVertex();
                        worldrenderer.pos(maxX, minY, minZ).endVertex();
                        worldrenderer.pos(minX, maxY, minZ).endVertex();
                        worldrenderer.pos(minX, maxY, maxZ).endVertex();
                        worldrenderer.pos(maxX, maxY, maxZ).endVertex();
                        worldrenderer.pos(maxX, maxY, minZ).endVertex();
                        worldrenderer.endVertex();
                        tessellator.draw();
                        GL11.glLineWidth(2.0F);
                        worldrenderer.begin(3, DefaultVertexFormats.POSITION);
                        worldrenderer.pos(minX, minY, minZ).endVertex();
                        worldrenderer.pos(minX, minY, maxZ).endVertex();
                        worldrenderer.pos(minX, maxY, maxZ).endVertex();
                        worldrenderer.pos(minX, maxY, minZ).endVertex();
                        worldrenderer.pos(minX, minY, minZ).endVertex();
                        worldrenderer.pos(maxX, minY, minZ).endVertex();
                        worldrenderer.pos(maxX, maxY, minZ).endVertex();
                        worldrenderer.pos(maxX, maxY, maxZ).endVertex();
                        worldrenderer.pos(maxX, minY, maxZ).endVertex();
                        worldrenderer.pos(maxX, minY, minZ).endVertex();
                        worldrenderer.pos(maxX, minY, maxZ).endVertex();
                        worldrenderer.pos(minX, minY, maxZ).endVertex();
                        worldrenderer.pos(minX, maxY, maxZ).endVertex();
                        worldrenderer.pos(maxX, maxY, maxZ).endVertex();
                        worldrenderer.pos(maxX, maxY, minZ).endVertex();
                        worldrenderer.pos(minX, maxY, minZ).endVertex();
                        worldrenderer.endVertex();
                        tessellator.draw();
                    }

                    GL11.glLineWidth(1.0F);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    GL11.glDepthMask(true);
                    GlStateManager.enableCull();
                    GL11.glEnable(3553);
                    GL11.glEnable(2929);
                    GL11.glDisable(3042);
                    GL11.glBlendFunc(770, 771);
                    GL11.glDisable(2848);
                }
            }
        }
    }

    private void updateColors() {
        fadeColor = color.isFade()
                ? ColorUtils.fadeColor(
                color.getColor(), color.getFadeColor(),
                color.getSpeed()) : color.getColor();
    }
}
