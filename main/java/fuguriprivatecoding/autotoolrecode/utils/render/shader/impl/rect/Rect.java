package fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.rect;

public record Rect(float x, float y, float width, float height) {
    public Rect interpolate(Rect toRect, float progress) {
        float x = x() + (toRect.x - x()) * progress;
        float y = y() + (toRect.y - y()) * progress;
        float width = width() + (toRect.width - width()) * progress;
        float height = height() + (toRect.height - height()) * progress;
        return new Rect(x, y, width, height);
    }
}
