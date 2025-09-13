package fuguriprivatecoding.autotoolrecode.guis.altmanager;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.alt.Account;
import fuguriprivatecoding.autotoolrecode.alt.Auth;
import fuguriprivatecoding.autotoolrecode.alt.MicrosoftAuthCallback;
import fuguriprivatecoding.autotoolrecode.guis.main.GuiClientButton;
import fuguriprivatecoding.autotoolrecode.module.impl.client.ClientSettings;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Glow;
import fuguriprivatecoding.autotoolrecode.utils.animation.Animation2D;
import fuguriprivatecoding.autotoolrecode.utils.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.scissor.ScissorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BackgroundUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.Session;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class AltManagerGuiScreen extends GuiScreen {

    AltManagerGuiText altManagerGuiText;

    public List<Account> accounts = new CopyOnWriteArrayList<>();

    private long lastClickTime = 0;
    private Account lastClickedAccount = null;

    Account selectedAccount;
    int scroll, scrollTotalHeight;
    Animation2D scrolls;

    static String updatedText;

    Glow shadows;

    public AltManagerGuiScreen() {
        mc = Minecraft.getMinecraft();
        scrolls = new Animation2D();
    }

    @Override
    public void initGui() {
        super.initGui();
        ScaledResolution sc = new ScaledResolution(mc);
        Client.INST.getConfigManager().loadAccounts();
        altManagerGuiText = new AltManagerGuiText(0, mc.fontRendererObj, 75, sc.getScaledHeight() - 100, 100, 20);
        buttonList.add(new GuiClientButton(1, 75,  sc.getScaledHeight() - 75, 100, 20, "Login"));
        buttonList.add(new GuiClientButton(2, 75, sc.getScaledHeight() - 50, 100, 20, "Delete"));
        buttonList.add(new GuiClientButton(3, 75, sc.getScaledHeight() - 25, 100, 20, "Microsoft"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Glow.class);
        ScaledResolution sc = new ScaledResolution(mc);

        scroll -= ClientSettings.getScroll();

        ClientFontRenderer font = Client.INST.getFonts().fonts.get("MuseoSans");

        float altVisibleHeight = sc.getScaledHeight() - 25;
        float maxScroll = Math.max(scrollTotalHeight - altVisibleHeight,0);

        if (scroll > 0) scroll = 0;
        if (scroll < -maxScroll) scroll = (int) -maxScroll;

        scrolls.endY = scroll;
        scrolls.update(15f);

        mc.getFramebuffer().framebufferClear();
        BackgroundUtils.run();
        mc.getFramebuffer().bindFramebuffer(true);

        RenderUtils.drawRoundedOutLineRectangle(sc.getScaledWidth() - 265, 15, 250, sc.getScaledHeight() - 25, 5f, new Color(0, 0, 0, 150).getRGB(), Color.BLACK.getRGB(), Color.BLACK.getRGB());

        if (updatedText != null) font.drawCenteredString(updatedText, sc.getScaledWidth() - 265 + 125, 5, Color.WHITE);

        float offset = scrolls.y;

        scrollTotalHeight = 0;

        ScissorUtils.enableScissor();
        ScissorUtils.scissor(new ScaledResolution(mc), sc.getScaledWidth() - 260, 15, 250, sc.getScaledHeight() - 25);

        for (Account account : accounts) {
            RoundedUtils.drawRect(sc.getScaledWidth() - 260, 10 + 10 + offset, 250 - 10, 20, 4f, selectedAccount != null && account.getName().equals(selectedAccount.getName()) ? new Color(75,75,75,150) : new Color(0, 0, 0,150));
            font.drawString(account.getName() + ((account.getUuid() != null) ? " | Microsoft." : " | Offline."), sc.getScaledWidth() - 250, 10 + 5 + 2 + 11f + offset, account.getName().equals(mc.getSession().getUsername()) ? Color.green : Color.WHITE);
            offset += 25;
            scrollTotalHeight += 25;
        }

        ScissorUtils.disableScissor();

        altManagerGuiText.drawTextBox();
        altManagerGuiText.setMaxStringLength(16);

        String currentUser = "Account: " + mc.getSession().getUsername();

        font.drawString(currentUser, 2.5f, 2.5f + 1, Color.WHITE);
        super.drawScreen(mouseX,mouseY,partialTicks);
    }

    public static void updateStatus(String status) {
        updatedText = status;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        switch (button.id) {
            case 1 -> {
                if (!altManagerGuiText.getText().isEmpty()) {
                    updateStatus("Adding account...");
                    String accountName = altManagerGuiText.getText();
                    boolean accountExists = accounts.stream().anyMatch(acc -> acc.getName().equals(accountName));

                    if (!accountExists) {
                        updateStatus("Account exists logging account...");
                        mc.getSession().setUsername(accountName);
                        Account newAccount = new Account(accountName);
                        accounts.add(newAccount);
                        selectedAccount = newAccount;
                        updateStatus("Successful added account: " + accountName + ".");
                    }
                    altManagerGuiText.setText("");
                } else if (selectedAccount != null) {
                    if (selectedAccount.getUuid() != null) {
                        new Thread(() -> {
                            try {
                                updateStatus("Logging in...");
                                Account account = selectedAccount;
                                Map.Entry<String, String> authRefreshTokens = Auth.refreshToken(account.getRefreshToken());
                                String xblToken = Auth.authXBL(authRefreshTokens.getKey());
                                Map.Entry<String, String> xstsTokenUserhash = Auth.authXSTS(xblToken);
                                String accessToken = Auth.authMinecraft(xstsTokenUserhash.getValue(), xstsTokenUserhash.getKey());

                                mc.setSession(new Session(account.getName(),
                                        account.getUuid(),
                                        accessToken, "msa"));
                                updateStatus("Successful login.");
                            } catch (Exception e) {
                                updateStatus(e.getMessage());
                            }
                        }).start();
                    } else {
                        mc.getSession().setUsername(selectedAccount.getName());
                        updateStatus("Successful logged account: " + selectedAccount.getName() + ".");
                    }
                } else {
                    mc.getSession().setUsername(Client.INST.getGenerator().generateRealisticNick(16));
                    updateStatus("Successful generated name..");
                }
            }

            case 2 -> {
                if (selectedAccount != null) {
                    accounts.removeIf(acc -> acc == selectedAccount);
                    updateStatus("Successful deleted account: " + selectedAccount.getName() + ".");
                    selectedAccount = null;
                }
            }

            case 3 -> {
                final MicrosoftAuthCallback callback = new MicrosoftAuthCallback();

                CompletableFuture<Account> future = callback.start((s, _) -> updateStatus(s));

                Sys.openURL(MicrosoftAuthCallback.url);

                future.whenCompleteAsync((account, error) -> {
                    if (error != null) {
                        updateStatus("Failed added account: " + error + ".");
                    } else {
                        accounts.add(new Account(account.getName(), account.getRefreshToken(), account.getUuid()));
                        updateStatus("Successful added account: " + account.getName() + ".");
                        selectedAccount = account;
                    }
                });
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        ScaledResolution sc = new ScaledResolution(mc);
        float offset = scrolls.y;

        for (Account account : accounts) {
            if (mouseButton == 0) {
                if (mouseX > sc.getScaledWidth() - 260 &&
                        mouseX < sc.getScaledWidth() - 260 + (250 - 10) &&
                        mouseY > 10 + 10 + offset &&
                        mouseY < 10 + 10 + offset + 20) {

                    long currentTime = System.currentTimeMillis();

                    if (lastClickedAccount == account && (currentTime - lastClickTime) < 250) {
                        if (account.getUuid() != null) {
                            new Thread(() -> {
                                try {
                                    updateStatus("Logging in...");
                                    Map.Entry<String, String> authRefreshTokens = Auth.refreshToken(account.getRefreshToken());
                                    String xblToken = Auth.authXBL(authRefreshTokens.getKey());
                                    Map.Entry<String, String> xstsTokenUserhash = Auth.authXSTS(xblToken);
                                    String accessToken = Auth.authMinecraft(xstsTokenUserhash.getValue(), xstsTokenUserhash.getKey());

                                    mc.setSession(new Session(account.getName(),
                                            account.getUuid(),
                                            accessToken, "msa"));
                                    updateStatus("Successful login.");
                                } catch (Exception e) {
                                    System.out.println(e.getMessage());
                                    updateStatus(e.getMessage());
                                }
                            }).start();
                        } else {
                            mc.getSession().setUsername(account.getName());
                            updateStatus("Successful logged account: " + account.getName() + ".");
                        }
                    } else {
                        toggleAccount(account);
                    }

                    lastClickTime = currentTime;
                    lastClickedAccount = account;
                    break;
                }
            }
            offset += 25;
        }
    }

    public void toggleAccount(Account account) {
        if (selectedAccount == account) selectedAccount = null; else selectedAccount = account;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        altManagerGuiText.setFocused(true);
        altManagerGuiText.textboxKeyTyped(typedChar, keyCode);

        if (keyCode == Keyboard.KEY_ESCAPE) {
            Client.INST.getConfigManager().saveAccounts();
            selectedAccount = null;
        }
    }
}
