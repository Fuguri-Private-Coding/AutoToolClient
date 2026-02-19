package fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.msdf;

import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class MsdfGlyph {

    private final int code;
    private final float minU, maxU, minV, maxV;
    private final float advance, topPosition, width, height;

    public MsdfGlyph(FontUtils.GlyphData data, float atlasWidth, float atlasHeight) {
        this.code = data.getUnicode();
        this.advance = data.getAdvance();

        FontUtils.BoundsData atlasBounds = data.getAtlasBounds();
        if (atlasBounds != null) {
            this.minU = atlasBounds.getLeft() / atlasWidth;
            this.maxU = atlasBounds.getRight() / atlasWidth;
            this.minV = 1.0F - atlasBounds.getTop() / atlasHeight;
            this.maxV = 1.0F - atlasBounds.getBottom() / atlasHeight;
        } else {
            this.minU = this.maxU = this.minV = this.maxV = 0.0f;
        }

        FontUtils.BoundsData planeBounds = data.getPlaneBounds();
        if (planeBounds != null) {
            this.width = planeBounds.getRight() - planeBounds.getLeft();
            this.height = planeBounds.getTop() - planeBounds.getBottom();
            this.topPosition = planeBounds.getTop();
        } else {
            this.width = this.height = this.topPosition = 0.0f;
        }
    }

    public float apply(float x, float y, float size, Color color) {
        y -= this.topPosition * size;
        float width = this.width * size;
        float height = this.height * size;

        ColorUtils.glColor(color);

        GL11.glTexCoord2f(minU, minV);
        GL11.glVertex2f(x, y);
        GL11.glTexCoord2f(minU, maxV);
        GL11.glVertex2f(x, y + height);
        GL11.glTexCoord2f(maxU, maxV);
        GL11.glVertex2f(x + width, y + height);
        GL11.glTexCoord2f(maxU, minV);
        GL11.glVertex2f(x + width, y);

        return this.advance * size;
    }

    public float getWidth(float size) {
        return this.advance * size;
    }

    public float getHeight(float size) {
        return height * size;
    }

    public int getCharCode() {
        return code;
    }
}
