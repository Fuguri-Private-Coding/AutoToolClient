package fuguriprivatecoding.autotoolrecode.module.impl.visual.dynamicisland;

public abstract class IslandComponent {
    public abstract String getKey(IslandContext ctx);

    public abstract float getWidth(IslandContext ctx);

    public abstract float getHeight();

    public abstract void draw(IslandContext ctx);
}
