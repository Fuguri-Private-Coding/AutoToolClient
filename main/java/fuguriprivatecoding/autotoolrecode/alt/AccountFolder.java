package fuguriprivatecoding.autotoolrecode.alt;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFont;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import lombok.Getter;
import net.minecraft.client.gui.GuiTextField;
import org.joml.Vector2f;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// created by dicves_recode on 04.01.2026
@Getter
public class AccountFolder extends AccountElement {
    private static final JsonParser PARSER = new JsonParser();
    private static final Color COLOR = new Color(25, 25, 25, 120);

    private File file;
    private final List<AccountElement> elements = new CopyOnWriteArrayList<>();

    public AccountFolder(AccountElement parent, File file) {
        super(parent);

        if (file.isFile())
            throw new IllegalStateException("File must be not file.");

        this.file = file;
    }

    @Override
    public void save() {
        for (AccountElement element : elements) {
            element.save();
        }
    }

    public void load() {
        elements.clear();
        File[] files = file.listFiles();

        if (files == null) {
            System.out.println("Error while loading account elements: Files list is null.");
            return;
        }

        for (File file : files) {
            if (file.isFile()) {
                readFile(file);
            } else {
                AccountFolder folder = new AccountFolder(this, file);

                elements.add(folder);
                folder.load();
            }
        }
    }

    private void readFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            JsonObject json = PARSER.parse(reader).getAsJsonObject();

            Account.Type type = Account.Type.valueOf(json.get("type").getAsString());
            String ign = file.getName();

            List<String> notes = new ArrayList<>();

            if (json.has("notes")) {
                for (JsonElement jsonElement : json.get("notes").getAsJsonArray()) {
                    notes.add(jsonElement.getAsString());
                }
            }

            String token = null;
            String uuid = null;

            if (type == Account.Type.MICROSOFT && json.has("token") && json.has("uuid")) {
                token = json.get("token").getAsString();
                uuid = json.get("uuid").getAsString();
            }

            Account account = null;

            if (type == Account.Type.OFFLINE) {
                account = new Account(this, file, notes, ign);
            } else if (token != null && uuid != null) {
                account = new Account(this, file, notes, ign, token, uuid);
            }

            if (account != null) {
                elements.add(account);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    private boolean opened;
    private float lastWidth;
    private float lastHeight;

    private final float[] clickedPos = new float[] { 0f, 0f };
    private boolean miniWindowOpened;

    private final GuiTextField input = new GuiTextField(0, null, 0, 0, 0, 0);
    private boolean inputOffline, inputRename;

    @Override
    public Vector2f render(float x, float y) {
        ClientFont font = Fonts.fonts.get("SFPro");

        float height = 0;

        RoundedUtils.drawRect(x, y, lastWidth, 25 + lastHeight, 5f, COLOR);
//        RenderUtils.drawRoundedOutLineRectangle(x, y, lastWidth, 25 + lastHeight, 5f, 0, -1, -1);

        font.drawString(file.getName() + (opened ? " +" : " -"), x + 5, y + 5, Color.WHITE);

        float largestElementWidth = 0;

        if (opened) {
            for (AccountElement element : elements) {
                Vector2f size = element.render(x + 15, y + 15 + height);

                if (size.x > largestElementWidth) {
                    largestElementWidth = size.x;
                }

                height += size.y + 5;
            }
        }

        lastWidth = Math.max(font.getStringWidth(file.getName() + " +") + 5 + 5, largestElementWidth + 15 + 5);
        lastHeight = height - 10;

        if (miniWindowOpened) {
            RoundedUtils.drawRect(clickedPos[0], clickedPos[1], 100, 15, 0, 5, 5, 0, COLOR);
            font.drawString(input.getText(), clickedPos[0] + 5, clickedPos[1] + 2, Color.WHITE);

            RoundedUtils.drawRect(clickedPos[0], clickedPos[1] + 15, 100, 15, 0, 0, 0, 0, COLOR);
            RoundedUtils.drawRect(clickedPos[0], clickedPos[1] + 15 + 15, 100, 15, 5, 0, 0, 5, COLOR);
        }

        return new Vector2f(lastWidth, 25 + lastHeight);
    }

    @Override
    public Vector2f click(float x, float y, float mouseX, float mouseY, int button) {
        ClientFont font = Fonts.fonts.get("SFPro");

        if (mouseX >= x + 5 + font.getStringWidth(file.getName()) && mouseX <= x + lastWidth
            && mouseY >= y + 5 && mouseY <= y + 15) {
            opened = !opened;
        }

        boolean nameClicked = mouseX >= x + 5 && mouseX <= x + 5 + font.getStringWidth(file.getName()) && mouseY >= y + 5 && mouseY <= y + 15 && button == 1;

        if (!miniWindowOpened && nameClicked) {
            miniWindowOpened = true;

            clickedPos[0] = mouseX;
            clickedPos[1] = mouseY;
        }

        if (miniWindowOpened) {
            boolean hovered = mouseX >= clickedPos[0] && mouseX <= clickedPos[0] + 100 && mouseY >= clickedPos[1] && mouseY <= clickedPos[1] + 45;

            if (!hovered) {
                miniWindowOpened = false;
            } else if (button == 0) {
                if (mouseY >= clickedPos[1] && mouseY <= clickedPos[1] + 15) {
                    input.setText("");
                    inputOffline = true;
                }

                if (mouseY >= clickedPos[1] + 15 && mouseY <= clickedPos[1] + 30) {

                }

                if (mouseY >= clickedPos[1] + 30 && mouseY <= clickedPos[1] + 45) {
                    input.setText("");
                }
            }
        }

        if (opened) {
            float height = 0;

            for (AccountElement element : elements) {
                Vector2f size = element.click(x + 15, y + 15 + height, mouseX, mouseY, button);
                height += size.y + 5;
            }
        }

        return new Vector2f(lastWidth, 25 + lastHeight);
    }

    public void onKey(char c, int key) {
        if (inputOffline) {
            input.textboxKeyTyped(c, key);

            if (key == Keyboard.KEY_RETURN) {
                elements.add(new Account(this, new File(file, input.getText()), new ArrayList<>(), input.getText()));
                miniWindowOpened = false;
                inputOffline = false;
                input.setText("");
                return;
            }
        }
    }

    @Override
    public boolean isFile() {
        return false;
    }
}
