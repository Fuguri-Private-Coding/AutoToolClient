package fuguriprivatecoding.autotoolrecode.managers;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.HUD;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.hud.HUDElement;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.hud.impl.BPSCounter;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.hud.impl.WaterMark;

import java.util.ArrayList;
import java.util.List;

public class HUDManager {

    public List<HUDElement> hudElements = new ArrayList<>();

    public HUDManager() {
        hudElements.add(new WaterMark());
        hudElements.add(new BPSCounter());

        for (HUDElement hudElement : hudElements) {
            hudElement.addSettings(Client.INST.getModuleManager().getModule(HUD.class));
        }
    }
}
