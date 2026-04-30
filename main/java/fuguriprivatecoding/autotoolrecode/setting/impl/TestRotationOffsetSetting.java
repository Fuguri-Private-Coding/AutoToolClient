package fuguriprivatecoding.autotoolrecode.setting.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fuguriprivatecoding.autotoolrecode.setting.Setting;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFont;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;

public class TestRotationOffsetSetting extends Setting {
    public final List<Rot> offsets = new CopyOnWriteArrayList<>();
    private final Color BACKGROUND_COLOR = new Color(0, 0, 0, 120);

    public boolean recording;

    public TestRotationOffsetSetting(String name, SettingAble parent) {
        super(name, parent);
    }

    public TestRotationOffsetSetting(String name, SettingAble parent, BooleanSupplier visible) {
        super(name, parent, visible);
    }

    @Override
    public void render() {

    }

    @Override
    public JsonObject getObject() {
        JsonObject object = new JsonObject();

        JsonArray offsetArray = new JsonArray();

        for (Rot offset : offsets) {
            offsetArray.add(offset.toJsonObject());
        }

        object.add("Offsets", offsetArray);

        return object;
    }

    @Override
    public void setObject(JsonObject object) {
        if (!object.has("Offsets")) {
            ClientUtils.chatLog("missing the \"Offsets\" array");
        }

        JsonArray offsetArray = object.get("Offsets").getAsJsonArray();

        clear();
        for (JsonElement jsonElement : offsetArray) {
            offsets.add(Rot.fromJsonObject(jsonElement.getAsJsonObject()));
        }
    }

    private void clear() {
        offsets.clear();
    }

    public Rot getByIndex(int index) {
        return offsets.get(index % offsets.size());
    }
}
