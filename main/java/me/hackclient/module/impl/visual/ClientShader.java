package me.hackclient.module.impl.visual;

import lombok.Getter;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.*;

import java.awt.*;

@ModuleInfo(
        name = "ClientShader",
        category = Category.VISUAL
)
@Getter
public class ClientShader extends Module {

    final Color
    orange = new Color(200, 100, 0),
    metallic = new Color(80, 100, 130),
    darkOcean = new Color(0, 0, 230),
    dark = new Color(0, 0, 0),
    pink = new Color(200, 100, 130),
    fuguri = new Color(100, 50, 180),
    bonan  = new Color(255, 160, 50),
    augustus = new Color(0, 0, 120);

    final BooleanSetting customColor = new BooleanSetting("CustomColor", this, false);
    final ModeSetting theme = new ModeSetting(
            "Preset",
            this,
            () -> !customColor.isToggled(),
            "Orange",
            new String[] {
                    "Orange",
                    "Metallic",
                    "Black",
                    "DarkOcean",
                    "Pink",
                    "Fuguri",
                    "Bonan",
                    "Augustus"
            }
    );

    final IntegerSetting
    rOffset = new IntegerSetting("RedOffset", this, customColor::isToggled, 0, 255, 0),
    gOffset = new IntegerSetting("GreenOffset", this, customColor::isToggled, 0, 255, 0),
    bOffset = new IntegerSetting("BlueOffset", this, customColor::isToggled, 0, 255, 0);

    public BooleanSetting clickGui = new BooleanSetting("ClickGui", this, true);
    public BooleanSetting arrayList = new BooleanSetting("ArrayList", this, true);
    public BooleanSetting chat = new BooleanSetting("Chat", this, true);
    public BooleanSetting backtrack = new BooleanSetting("Backtrack", this, true);
    public BooleanSetting ping = new BooleanSetting("Ping", this, true);

    public Color getColor() {
        if (customColor.isToggled()) {
            return new Color(rOffset.getValue(), gOffset.getValue(), bOffset.getValue());
        } else {
            return switch (theme.getMode()) {
                case "Orange" -> orange;
                case "Metallic" -> metallic;
                case "Black" -> dark;
                case "DarkOcean" -> darkOcean;
                case "Pink" -> pink;
                case "Fuguri" -> fuguri;
                case "Bonan" -> bonan;
                case "Augustus" -> augustus;
                default -> throw new IllegalStateException("Unexpected value: " + theme.getMode());
            };
        }
    }
}
