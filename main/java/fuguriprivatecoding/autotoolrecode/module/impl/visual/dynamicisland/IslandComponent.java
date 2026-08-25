package fuguriprivatecoding.autotoolrecode.module.impl.visual.dynamicisland;

import fuguriprivatecoding.autotoolrecode.module.impl.visual.dynamicisland.impl.IslandContext;

public abstract class IslandComponent {
    public abstract String getKey(IslandContext ctx);

    public abstract float getWidth(IslandContext ctx);

    public abstract float getHeight();

    public abstract void draw(IslandContext ctx);
}
