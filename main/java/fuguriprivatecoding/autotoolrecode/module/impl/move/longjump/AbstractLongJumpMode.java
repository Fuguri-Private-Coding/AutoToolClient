package fuguriprivatecoding.autotoolrecode.module.impl.move.longjump;

import fuguriprivatecoding.autotoolrecode.module.impl.move.LongJump;
import fuguriprivatecoding.autotoolrecode.module.impl.move.Speed;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;

public abstract class AbstractLongJumpMode implements LongJumpMode, Imports {
    protected final String name;
    
    public AbstractLongJumpMode(String name) {
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void onEnable(LongJump longJump) {
    }

    @Override
    public void onDisable(LongJump longJump) {
    }
}