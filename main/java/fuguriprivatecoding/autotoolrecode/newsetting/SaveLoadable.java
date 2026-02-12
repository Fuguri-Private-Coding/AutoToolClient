package fuguriprivatecoding.autotoolrecode.newsetting;

import com.google.gson.JsonObject;

// created by dicves_recode on 11.02.2026
public interface SaveLoadable {
    JsonObject getJson();
    void setJson(JsonObject object);
}
