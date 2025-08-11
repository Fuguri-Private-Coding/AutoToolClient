package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Scaffold;
import fuguriprivatecoding.autotoolrecode.utils.animation.Animation2D;
import fuguriprivatecoding.autotoolrecode.utils.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.stencil.StencilUtils;
import net.minecraft.item.ItemBlock;
import org.lwjgl.util.vector.Vector2f;
import java.awt.*;

@ModuleInfo(name = "DynamicIsland", category = Category.VISUAL)
public class DynamicIsland extends Module {

    Animation2D size = new Animation2D();
    Animation2D currentWidth = new Animation2D();
    Animation2D needY = new Animation2D();
    String currentText;

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof Render2DEvent e) {
            ClientFontRenderer font = Client.INST.getFonts().fonts.get("MuseoSans");
            Vector2f screenSize = new Vector2f(e.getWidth(), e.getHeight());

            float width = 20;
            float height = 15;

            String name = Client.INST.getFullName();

            String bps = String.format("%.3f", mc.thePlayer.getBps(false));

            String target;
            String scaffold;

            currentText = name;
            needY.endY = 1;

            if (MoveUtils.isMoving()) {
                currentText = bps;
                needY.endY = -14;
            }

            if (Client.INST.getCombatManager().getTarget() != null) {
                target = "Current Target - " + Client.INST.getCombatManager().getTarget().getName();
                currentText = target;
                needY.endY = -25;
            } else {
                target = "";
            }

            if (Client.INST.getModuleManager().getModule(Scaffold.class).isToggled() && mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.fakeCurrentItem).getItem() instanceof ItemBlock) {
                scaffold = "Blocks Left - " + mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.fakeCurrentItem).stackSize;
                currentText = scaffold;
                needY.endY = -36;
            } else {
                scaffold = "";
            }

            currentWidth.endX = (float) (width + font.getStringWidth(currentText));
            currentWidth.update(15);
            size.update(15);
            needY.update(15);

            StencilUtils.renderStencil(
                    () -> RoundedUtils.drawRect(screenSize.x / 2f - currentWidth.x / 2f, 5, currentWidth.x,height, 7.5f, Color.WHITE),
                    () -> {

                        RoundedUtils.drawRect(screenSize.x / 2f - currentWidth.x / 2f, 5, currentWidth.x,height, 7.5f, Color.WHITE);

                        font.drawString(name, (screenSize.x / 2f - font.getStringWidth(name) / 2f), 6 + 3 + needY.y,Color.BLACK);
                        font.drawString(bps, (screenSize.x / 2f - font.getStringWidth(bps) / 2f), 21 + 3 + needY.y,Color.BLACK);
                        font.drawString(target, (screenSize.x / 2f - font.getStringWidth(target) / 2f), 33 + 3 + needY.y,Color.BLACK);
                        font.drawString(scaffold, (screenSize.x / 2f - font.getStringWidth(scaffold) / 2f), 43 + 3 + needY.y,Color.BLACK);

                    }
            );
        }
    }
}
