package fuguriprivatecoding.autotool.module.impl.move;

import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import net.minecraft.client.entity.EntityPlayerSP;

@ModuleInfo(name = "Sprint", category = Category.MOVE)
public class Sprint extends Module {
	@Override
	public void toggle() {
		super.toggle();
		EntityPlayerSP.forceSprint = isToggled();
	}
}
