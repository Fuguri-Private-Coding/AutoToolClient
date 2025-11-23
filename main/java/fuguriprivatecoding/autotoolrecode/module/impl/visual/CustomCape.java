package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.WorldChangeEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.List;

@ModuleInfo(name = "CustomCape", category = Category.VISUAL, description = "Изменяет вам плащ.")
public class CustomCape extends Module {

    public Mode capeMode = new Mode("CapeMode", this)
        .addMode("None")
        .setMode("None")
        ;

    @Getter
    File capeDirectory = new File(Client.INST.getName() + "/capes");

    DynamicTexture dynamicTexture;
    BufferedImage capeImage;
    public String selectedCape = "none";
    File capeFile;

    public CustomCape() {
        if (!capeDirectory.exists()) capeDirectory.mkdirs();

        if (capeDirectory.listFiles() != null && Arrays.stream(capeDirectory.listFiles()).toList().isEmpty()) {
            downloadCapes();
        }

        updateCape();
    }

    @Override
    public void onEnable() {
        selectedCape = "";
        updateCape();
    }

    public void updateCape() {
        capeMode.getModes().clear();
        for (File cape : capeDirectory.listFiles()) {
            capeMode.addMode(cape.getName().replaceAll(".png", ""));
        }
    }

    private void downloadCapes() {
        try {
            MessageChannel capesChannel = Client.INST.getIrc().getClientCapesChannel();

            List<Message> messages = capesChannel.getIterableHistory().stream().toList();

            for (Message message : messages) {
                if (message.getAttachments().isEmpty()) continue;

                message.getAttachments().forEach(attachment -> {
                    try {
                        if (attachment.getFileName().endsWith(".png")) {
                            attachment.getProxy().downloadToFile(new File(capeDirectory + "/" + attachment.getFileName()))
                                    .thenAccept(_ -> updateCape());
                        }
                    } catch (Exception _) { }
                });
            }
        } catch (Exception e) {
            ClientUtils.chatLog("У ВАС ИНТЕРНЕТ ХУЕТА ПОЛНАЯ ИЛИ ЗАПРЕТ ПОЙДИ СКАЧАЙ ТУПОЙ УЕБАН!");
        }
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof WorldChangeEvent) selectedCape = "";
    }

    public ResourceLocation getCape() {
        if (!selectedCape.equalsIgnoreCase(capeMode.getMode())) {
            capeFile = new File(capeDirectory, capeMode.getMode() + ".png");
            if (!capeFile.exists()) return null;

            try (InputStream inputStream = new FileInputStream(capeFile)) {
                capeImage = ImageIO.read(inputStream);

                if (capeImage == null) {
                    System.err.println("Failed to load cape: " + capeMode.getMode());
                    return null;
                }

                dynamicTexture = new DynamicTexture(capeImage);
                return mc.getTextureManager().getDynamicTextureLocation(
                        "custom_cape_" + capeMode.getMode().toLowerCase(),
                        dynamicTexture
                );
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return null;
            } finally {
                selectedCape = capeMode.getMode();
            }
        }
        return null;
    }
}