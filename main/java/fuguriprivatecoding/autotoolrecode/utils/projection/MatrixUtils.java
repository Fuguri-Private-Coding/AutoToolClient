package fuguriprivatecoding.autotoolrecode.utils.projection;

import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.Arrays;

public class MatrixUtils implements Imports {
    private static final FloatBuffer CAMERA_TRANSFORM_BUFFER = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer PROJECTION_BUFFER = BufferUtils.createFloatBuffer(16);

    public static void getCameraAndProjectionMatrices(float partialTicks, float[] cameraTransformOut, float[] projectionOut) {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        double cameraX = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
        double cameraY = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
        double cameraZ = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;

        PROJECTION_BUFFER.clear();
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, PROJECTION_BUFFER);
        transposeMatrix(PROJECTION_BUFFER);
        PROJECTION_BUFFER.get(projectionOut);

        GL11.glPushMatrix();
        GL11.glTranslated(-cameraX, -cameraY, -cameraZ);
        CAMERA_TRANSFORM_BUFFER.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, CAMERA_TRANSFORM_BUFFER);
        transposeMatrix(CAMERA_TRANSFORM_BUFFER);
        CAMERA_TRANSFORM_BUFFER.get(cameraTransformOut);
        GL11.glPopMatrix();

        float frontX = cameraTransformOut[8];
        float frontY = cameraTransformOut[9];
        float frontZ = cameraTransformOut[10];
        float length = (float) Math.sqrt(frontX * frontX + frontY * frontY + frontZ * frontZ);

        if (length < 0.0001f) {
            Arrays.fill(cameraTransformOut, 0.0f);
            for (int i = 0; i < 16; i += 5) {
                cameraTransformOut[i] = 1.0f;
            }
        }
    }

    private static void transposeMatrix(FloatBuffer m) {
        float m00 = m.get(0), m01 = m.get(1), m02 = m.get(2), m03 = m.get(3);
        float m10 = m.get(4), m11 = m.get(5), m12 = m.get(6), m13 = m.get(7);
        float m20 = m.get(8), m21 = m.get(9), m22 = m.get(10), m23 = m.get(11);
        float m30 = m.get(12), m31 = m.get(13), m32 = m.get(14), m33 = m.get(15);

        m.put(0, m00); m.put(1, m10); m.put(2, m20); m.put(3, m30);
        m.put(4, m01); m.put(5, m11); m.put(6, m21); m.put(7, m31);
        m.put(8, m02); m.put(9, m12); m.put(10, m22); m.put(11, m32);
        m.put(12, m03); m.put(13, m13); m.put(14, m23); m.put(15, m33);
    }
}