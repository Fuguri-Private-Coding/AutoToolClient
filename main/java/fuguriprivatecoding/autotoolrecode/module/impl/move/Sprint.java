package fuguriprivatecoding.autotoolrecode.module.impl.move;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import net.minecraft.client.entity.EntityPlayerSP;

@ModuleInfo(name = "Sprint", category = Category.MOVE)
public class Sprint extends Module {
	@Override
	public void toggle() {
		super.toggle();
		EntityPlayerSP.forceSprint = isToggled();
	}
}
