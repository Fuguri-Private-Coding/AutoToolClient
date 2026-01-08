package fuguriprivatecoding.autotoolrecode.alt;

import fuguriprivatecoding.autotoolrecode.Client;
import lombok.experimental.UtilityClass;

import java.io.*;

@UtilityClass
public class Accounts {
    public final File DIR = new File(Client.INST.CLIENT_DIR, "accounts");
    private final AccountFolder FOLDER = new AccountFolder(null, DIR);

    public void init() {
        if (DIR.mkdirs()) System.out.println("Successful created Accounts Directory.");
        load();
    }

    public void save() {
        FOLDER.save();
    }

    public void load() {
        FOLDER.load();
    }

    public void render(float x, float y) {
        FOLDER.render(x, y);
    }

    public void click(float x, float y, float mouseX, float mouseY, int button) {
        FOLDER.click(x, y, mouseX, mouseY, button);
    }

    public void key(char c, int key) {
        FOLDER.onKey(c, key);
    }
}
