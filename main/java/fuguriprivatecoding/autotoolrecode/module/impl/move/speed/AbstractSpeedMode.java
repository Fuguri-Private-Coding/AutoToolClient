package fuguriprivatecoding.autotoolrecode.module.impl.move.speed;

import fuguriprivatecoding.autotoolrecode.module.impl.move.Speed;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;

public abstract class AbstractSpeedMode implements SpeedMode, Imports {
    protected final String name;
    
    public AbstractSpeedMode(String name) {
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void onEnable(Speed speed) {
    }
    
    @Override
    public void onDisable(Speed speed) {
    }
}