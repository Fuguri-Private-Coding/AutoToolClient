package fuguriprivatecoding.autotoolrecode.guis.altmanager;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.alt.Account;
import fuguriprivatecoding.autotoolrecode.alt.Auth;
import fuguriprivatecoding.autotoolrecode.alt.MicrosoftAuthCallback;
import fuguriprivatecoding.autotoolrecode.guis.main.GuiClientButton;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Shadows;
import fuguriprivatecoding.autotoolrecode.utils.animation.Animation2D;
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
import org.lwjgl.input.Mouse;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class AltManagerGuiScreen extends GuiScreen {

    AltManagerGuiText altManagerGuiText;

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final Random random = new Random();

    public ArrayList<Account> accounts = new ArrayList<>();

    private long lastClickTime = 0;
    private Account lastClickedAccount = null;

    Account selectedAccount;
    int scroll, scrollTotalHeight;
    Animation2D scrolls;

    Shadows shadows;

    public AltManagerGuiScreen() {
        mc = Minecraft.getMinecraft();
        scrolls = new Animation2D();
    }

    @Override
    public void initGui() {
        super.initGui();
        ScaledResolution sc = new ScaledResolution(mc);
        Client.INST.getConfigManager().loadAccounts();
        altManagerGuiText = new AltManagerGuiText(0, mc.fontRendererObj, 100, sc.getScaledHeight() - 100, 100, 20);
        buttonList.add(new GuiClientButton(1, 100,  sc.getScaledHeight() - 75, 100, 20, "Login"));
        buttonList.add(new GuiClientButton(2, 100, sc.getScaledHeight() - 50, 100, 20, "Delete"));
        buttonList.add(new GuiClientButton(3, 100, sc.getScaledHeight() - 25, 100, 20, "Microsoft"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Shadows.class);
        ScaledResolution sc = new ScaledResolution(mc);
        int currentScroll = Mouse.getDWheel();

        scroll -= currentScroll / 120 * 15;

        float altVisibleHeight = sc.getScaledHeight() - 25;
        float maxScroll = Math.max(scrollTotalHeight - altVisibleHeight,0);

        if (scroll > 0) scroll = 0;
        if (scroll < -maxScroll) scroll = (int) -maxScroll;

        scrolls.endY = scroll;
        scrolls.update(15f);

        mc.getFramebuffer().framebufferClear();
        BackgroundUtils.run();
        mc.getFramebuffer().bindFramebuffer(true);

        RoundedUtils.drawRect(sc.getScaledWidth() - 265, 10, 250, sc.getScaledHeight() - 20, 5f, new Color(15, 15, 15, 150));

        float offset = scrolls.y;

        scrollTotalHeight = 0;

        ScissorUtils.enableScissor();
        ScissorUtils.scissor(new ScaledResolution(mc), sc.getScaledWidth() - 260, 10, 250, sc.getScaledHeight() - 20);

        for (Account account : accounts) {
            RoundedUtils.drawRect(sc.getScaledWidth() - 260, 10 + 5 + offset, 250 - 10, 20, 4f, selectedAccount != null && account.getName().equals(selectedAccount.getName()) ? new Color(75,75,75,150) : new Color(15,15,15,150));
            fontRendererObj.drawString(account.getName() + ((account.getUuid() != null) ? " | Microsoft." : " | Offline"), sc.getScaledWidth() - 250, 10 + 11f + offset, account.getName().equals(mc.getSession().getUsername()) ? Color.green.getRGB() : -1);
            offset += 25;
            scrollTotalHeight += 25;
        }

        ScissorUtils.disableScissor();

        altManagerGuiText.drawTextBox();
        altManagerGuiText.setMaxStringLength(16);

        String currentUser = "Account: " + mc.getSession().getUsername();

        mc.fontRendererObj.drawString(currentUser, 2.5f, 2.5f, new Color(255, 255, 255, 150).getRGB());
        super.drawScreen(mouseX,mouseY,partialTicks);
    }

    public static String generateRandomNick() {
        int length = random.nextInt(16) + 1;
        StringBuilder sb = new StringBuilder(length);
        sb.append(CHARACTERS.charAt(random.nextInt(26)));
        for (int i = 1; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }

        return sb.toString();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        switch (button.id) {
            case 1 -> {
                if (!altManagerGuiText.getText().isEmpty()) {
                    String accountName = altManagerGuiText.getText();
                    boolean accountExists = accounts.stream().anyMatch(acc -> acc.getName().equals(accountName));

                    if (!accountExists) {
                        mc.getSession().setUsername(accountName);

                        Account newAccount = new Account(accountName);
                        accounts.add(newAccount);
                        selectedAccount = newAccount;
                    }
                    altManagerGuiText.setText("");
                } else if (selectedAccount != null) {
                    if (selectedAccount.getUuid() != null) {
                        try {
                            Account microsoftAltCredential = selectedAccount;
                            Map.Entry<String, String> authRefreshTokens = Auth.refreshToken(microsoftAltCredential.getRefreshToken());
                            String xblToken = Auth.authXBL(authRefreshTokens.getKey());
                            Map.Entry<String, String> xstsTokenUserhash = Auth.authXSTS(xblToken);
                            String accessToken = Auth.authMinecraft(xstsTokenUserhash.getValue(), xstsTokenUserhash.getKey());

                            mc.setSession(new Session(microsoftAltCredential.getName(),
                                    microsoftAltCredential.getUuid(),
                                    accessToken, "msa"));
                            System.out.println("Successful login.");
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    } else {
                        mc.getSession().setUsername(selectedAccount.getName());
                    }
                } else {
                    mc.getSession().setUsername(generateRandomNick());
                }
            }

            case 2 -> {
                if (selectedAccount != null) {
                    accounts.removeIf(acc -> acc == selectedAccount);
                    selectedAccount = null;
                }
            }

            case 3 -> {
                final MicrosoftAuthCallback callback = new MicrosoftAuthCallback();

                CompletableFuture<Account> future = callback.start((s, o) -> System.out.println(s));

                Sys.openURL(MicrosoftAuthCallback.url);

                future.whenCompleteAsync((account, error) -> {
                    if (error != null) {
                        System.out.println("Failed to login Account: " + error);
                    } else {
                        System.out.println("Successful to login Account: " + account.getName());
                        accounts.add(new Account(account.getName(), account.getRefreshToken(), account.getUuid()));
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
                        mouseY > 10 + 5 + offset &&
                        mouseY < 10 + 5 + offset + 20) {

                    long currentTime = System.currentTimeMillis();

                    if (lastClickedAccount == account && (currentTime - lastClickTime) < 250) {
                        if (account.getUuid() != null) {
                            try {
                                Map.Entry<String, String> authRefreshTokens = Auth.refreshToken(account.getRefreshToken());
                                String xblToken = Auth.authXBL(authRefreshTokens.getKey());
                                Map.Entry<String, String> xstsTokenUserhash = Auth.authXSTS(xblToken);
                                String accessToken = Auth.authMinecraft(xstsTokenUserhash.getValue(), xstsTokenUserhash.getKey());

                                mc.setSession(new Session(account.getName(),
                                        account.getUuid(),
                                        accessToken, "msa"));
                                System.out.println("Successful login.");
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                        } else {
                            mc.getSession().setUsername(account.getName());
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

    public String getClipBoard() {
        try {
            return (String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (HeadlessException | UnsupportedFlavorException | IOException e) {
            System.out.println(e.getMessage());
        }
        return "";
    }
}
