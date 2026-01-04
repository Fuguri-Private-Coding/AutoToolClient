package fuguriprivatecoding.autotoolrecode.alt;

import com.google.gson.*;
import fuguriprivatecoding.autotoolrecode.alt.microsoft.Auth;
import fuguriprivatecoding.autotoolrecode.gui.altmanager.AltScreen;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFont;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.util.Session;
import org.joml.Vector2f;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import static fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports.mc;

@Getter
public class Account extends AccountElement {
    private static final Gson GSON = new GsonBuilder().create();
    private static final String BANNED_CHARS = "./\\";
    private static final Color COLOR = new Color(25, 25, 25, 120);

    private File file;
    private String ign; // in game name
    private Type type;
    private String token, uuid;

    private final List<String> notes;

    private long lastClickTime;

    public Account(AccountElement parent, File file, List<String> notes, String ign) {
        super(parent);
        for (Character c : ign.toCharArray()) {
            if (BANNED_CHARS.contains(c.toString())) {
                throw new IllegalArgumentException("Ign contains ./\\");
            }
        }

        this.file = file;
        this.ign = ign;
        this.notes = notes;

        type = Type.OFFLINE;
    }

    public Account(AccountElement parent, File file, List<String> notes, String ign, String token, String uuid) {
        this(parent, file, notes, ign);
        this.token = token;
        this.uuid = uuid;

        type = Type.MICROSOFT;
    }

    public boolean isCurrentUsing() {
        Session session = mc.getSession();

        if (type == Type.OFFLINE) {
            return session.getUsername().equals(ign);
        } else {
            return session.getUsername().equals(ign)
                && token.equals(session.getToken())
                && uuid.equals(session.getPlayerID());
        }
    }

    public boolean isPremium() {
        return type == Type.MICROSOFT && token != null && uuid != null;
    }

    private void login() {
        switch (type) {
            case OFFLINE -> {
                Session session = mc.getSession();

                session.setUsername(ign);
                session.setSessionType(Session.Type.LEGACY);
                AltScreen.INST.updateStatus("Successful login to: " + ign);
            }
            case MICROSOFT -> new Thread(() -> {
                try {
                    Map.Entry<String, String> authRefreshTokens = Auth.refreshToken(token);
                    String xblToken = Auth.authXBL(authRefreshTokens.getKey());
                    Map.Entry<String, String> xstsTokenUserhash = Auth.authXSTS(xblToken);
                    String accessToken = Auth.authMinecraft(xstsTokenUserhash.getValue(), xstsTokenUserhash.getKey());

                    mc.setSession(new Session(ign, uuid, accessToken, "msa"));
                    mc.getSession().setSessionType(Session.Type.MOJANG);

                    AltScreen.INST.updateStatus("Successful login to: " + ign);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    AltScreen.INST.updateStatus("Failed login account: " + e.getMessage() + ".");
                }
            }).start();
        }
    }

    @Override
    @SneakyThrows
    public void save() {
        file.createNewFile();

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("type", type.name());

        if (!notes.isEmpty()) {
            JsonArray notesArray = new JsonArray();

            for (String note : notes) {
                notesArray.add(new JsonPrimitive(note));
            }

            jsonObject.add("notes", notesArray);
        }

        if (type == Type.MICROSOFT) {
            jsonObject.addProperty("token", token);
            jsonObject.addProperty("uuid", uuid);
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println(GSON.toJson(jsonObject));
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public Vector2f render(float x, float y) {
        ClientFont font = Fonts.fonts.get("SFPro");

        float width = Math.max(50, font.getStringWidth(file.getName().toLowerCase()) + 10);

        RoundedUtils.drawRect(x, y, width, 25, 5f, COLOR);

        font.drawString(ign, x + 5, y + 5, Color.WHITE);
        font.drawString(type.name().toLowerCase(), x + 5, y + 15, switch (type) {
            case OFFLINE -> Color.RED;
            case MICROSOFT -> Color.GREEN;
        });

        font.drawString("note + -", x + 5 + font.getStringWidth(type.name().toLowerCase()) + 5, y + 15, Color.WHITE);

        return new Vector2f(width, 25);
    }

    @Override
    public Vector2f click(float x, float y, float mouseX, float mouseY, int button) {
        ClientFont font = Fonts.fonts.get("SFPro");

        float width = Math.max(50, font.getStringWidth(file.getName().toLowerCase()) + 10);

        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 25 && button == 0) {
            long reached = System.currentTimeMillis() - lastClickTime;

            if (reached <= 500) {
                login();
            } else {
                lastClickTime = System.currentTimeMillis();
            }
        }

        return new Vector2f(width, 25);
    }

    public enum Type {
        OFFLINE, MICROSOFT
    }
}
