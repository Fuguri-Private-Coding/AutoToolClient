package fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.msdf;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

public class FontUtils {

    @Getter
    @AllArgsConstructor
    public static class FontData {
        private AtlasData atlas;
        private MetricsData metrics;
        private List<GlyphData> glyphs;
        private @SerializedName("kerning") List<KerningData> kernings;
    }

    @Getter
    @AllArgsConstructor
    public static class AtlasData {
        private @SerializedName("distanceRange") float range;
        private float width;
        private float height;
    }

    @Getter
    @AllArgsConstructor
    public static class MetricsData {
        private float lineHeight;
        private float ascender;
        private float descender;
        public float baselineHeight() {
            return this.lineHeight + this.descender;
        }
    }

    @Getter
    @AllArgsConstructor
    public static class GlyphData {
        private int unicode;
        private float advance;
        private BoundsData planeBounds;
        private BoundsData atlasBounds;
    }

    @Getter
    @AllArgsConstructor
    public static class KerningData {
        private @SerializedName("unicode1") int leftChar;
        private @SerializedName("unicode2") int rightChar;
        private float advance;
    }

    @Getter
    @AllArgsConstructor
    public static class BoundsData {
        private float left;
        private float top;
        private float right;
        private float bottom;
    }
}
