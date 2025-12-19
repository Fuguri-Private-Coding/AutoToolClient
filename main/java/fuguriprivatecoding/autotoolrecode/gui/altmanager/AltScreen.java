package fuguriprivatecoding.autotoolrecode.gui.altmanager;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.alt.Account;
import fuguriprivatecoding.autotoolrecode.alt.Accounts;
import fuguriprivatecoding.autotoolrecode.alt.microsoft.Auth;
import fuguriprivatecoding.autotoolrecode.alt.microsoft.MicrosoftAuthCallback;
import fuguriprivatecoding.autotoolrecode.gui.buttons.Button;
import fuguriprivatecoding.autotoolrecode.gui.buttons.TextButton;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.generate.NameGenerator;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.gui.Scroll;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFont;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BackgroundUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.stencil.StencilUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class AltScreen extends GuiScreen {

    public static List<Account> accounts = new CopyOnWriteArrayList<>();

    ResourceLocation removeLogo = Client.INST.of("mainmenu/exit.png");

    public static AltScreen INST;

    public static void init() {
        INST = new AltScreen();
    }

    private AltScreen() {}

    Scroll scroll = new Scroll(30);
    int scrollTotal;

    Account selectedAccount, lastClickedAccount;
    long lastClickedTime;

    static String statusText = "";

    TextButton textButton;

    @Override
    public void initGui() {
        super.initGui();
        ScaledResolution sc = new ScaledResolution(mc);

        Accounts.loadAccounts();
        textButton = new TextButton(0, 50, sc.getScaledHeight() - 10 - 75, 100, 20);
        buttonList.add(new Button(1,"Login", 50, sc.getScaledHeight() - 10 - 50, 100, 20));
        buttonList.add(new Button(2,"Microsoft", 50, sc.getScaledHeight() - 10 - 25, 100, 20));

        textButton.setFocused(true);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ClientFont fontRenderer = Fonts.fonts.get("SFPro");

        ScaledResolution sc = new ScaledResolution(mc);

        BackgroundUtils.run();

        float rectX = (sc.getScaledWidth() - 200 - 10);
        float rectY = 20;
        float rectWidth = 200;
        float rectHeight = (sc.getScaledHeight() - 30);

        float buttonsX = 45;
        float buttonsY = sc.getScaledHeight() - 5 - 75 - 10;
        float buttonsWidth = 110;
        float buttonsHeight = 80;

        Color rectsColor = new Color(0,0,0,0.5f);

        scroll.update(scrollTotal, sc.getScaledHeight() - 35);
        scroll.handleScrollInput(GuiUtils.isHovered(mouseX, mouseY, rectX, rectY, rectWidth, rectHeight));

        scroll.getScrollAnim().update(3, Easing.OUT_CUBIC);
        scroll.getScrollAnim().setEnd(scroll.getScroll());

        RoundedUtils.drawRect(rectX, rectY, rectWidth, rectHeight, 10, rectsColor);
        RoundedUtils.drawRect(buttonsX, buttonsY, buttonsWidth, buttonsHeight, 15, rectsColor);

        String currentSession = "Current Session: " + mc.getSession().getUsername();
        String currentType = "Type: " + mc.getSession().getSessionType();
        fontRenderer.drawString(currentSession, 5,5, Color.WHITE);
        fontRenderer.drawString(currentType, 5,5 + 10 + 1, Color.WHITE);

        updateDeleting();

        float statusX = rectX + rectWidth / 2f;
        float statusY = 7.5f;

        fontRenderer.drawCenteredString(statusText, statusX, statusY, Color.WHITE);

        StencilUtils.setUpTexture(rectX, rectY, rectWidth, rectHeight, 10);
        StencilUtils.writeTexture();

        scrollTotal = 0;
        float offset = scroll.getScrollAnim().getValue();
        for (Account account : accounts) {
            EasingAnimation hover = account.getHoverAnim();
            EasingAnimation select = account.getSelectAnim();
            EasingAnimation delete = account.getDeleteAnim();

            account.updateAnimations();

            float accountX = (sc.getScaledWidth() - 200 - 10) + 5 - hover.getValue();
            float accountY = offset + 25 - hover.getValue();
            float accountWidth = 190 + hover.getValue() * 2;
            float accountHeight = 25 + hover.getValue() * 2;

            float deleteX = (sc.getScaledWidth() - 200 - 10 + 175) + 5 - hover.getValue();
            float deleteY = offset + 25 + 5f - hover.getValue();
            float deleteWidth = 15 + hover.getValue() * 2;
            float deleteHeight = 15 + hover.getValue() * 2;

            boolean hoveredAccount = GuiUtils.isHovered(mouseX, mouseY, accountX, accountY, accountWidth, accountHeight);
            boolean hoveredRemove = GuiUtils.isHovered(mouseX, mouseY, deleteX, deleteY, deleteWidth, deleteHeight);

            hover.setEnd(hoveredAccount || isSelected(account));
            select.setEnd(isSelected(account) || account.isSession());
            if (!account.isDeleting()) delete.setEnd(1);

            Color accountRectColor = ColorUtils.interpolateColor(Colors.BLACK.withAlpha(0.5f * delete.getValue()), Colors.BLACK.withAlpha(0.6f * delete.getValue()), hover.getValue());

            Color textColor = ColorUtils.interpolateColor(Colors.WHITE.withAlpha(delete.getValue()), Colors.GRAY.withAlpha(delete.getValue()), select.getValue());

            if (account.isSession()) {
                textColor = Colors.GREEN.withAlpha(delete.getValue());
            }

            Color removeColor = hoveredRemove ? Colors.RED.withAlpha(delete.getValue()) : Colors.WHITE.withAlpha(delete.getValue());

            RoundedUtils.drawRect(accountX, accountY, accountWidth, accountHeight, 7.5f, accountRectColor);
            fontRenderer.drawString(account.getName() + " " + account.getType(), accountX + 5, accountY + accountHeight / 2f - 2.5f, textColor);

            ColorUtils.glColor(removeColor);
            RenderUtils.drawImage(removeLogo, deleteX, deleteY, deleteWidth, deleteHeight, true);

            scrollTotal += 30;
            offset += 30 * account.getDeleteAnim().getValue();
        }

        StencilUtils.endWriteTexture();

        super.drawScreen(mouseX, mouseY, partialTicks);

        textButton.drawTextBox();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        ScaledResolution sc = new ScaledResolution(mc);

        float rectX = (sc.getScaledWidth() - 200 - 10);
        float rectY = 20;
        float rectWidth = 200;
        float rectHeight = (sc.getScaledHeight() - 30);

        boolean hoveredRect = GuiUtils.isHovered(mouseX, mouseY, rectX, rectY, rectWidth, rectHeight);

        if (hoveredRect) {
            float offset = scroll.getScrollAnim().getValue();
            for (Account account : accounts) {
                EasingAnimation hover = account.getHoverAnim();

                float accountX = (sc.getScaledWidth() - 200 - 10) + 5 - hover.getValue();
                float accountY = offset + 25 - hover.getValue();
                float accountWidth = 190 + hover.getValue() * 2;
                float accountHeight = 25 + hover.getValue() * 2;

                boolean hoveredAccount = GuiUtils.isHovered(mouseX, mouseY, accountX, accountY, accountWidth, accountHeight);

                if (hoveredAccount) {
                    long currentTime = System.currentTimeMillis();

                    float deleteX = (sc.getScaledWidth() - 200 - 10 + 175) + 5 - hover.getValue();
                    float deleteY = offset + 25 + 5f - hover.getValue();
                    float deleteWidth = 15 + hover.getValue() * 2;
                    float deleteHeight = 15 + hover.getValue() * 2;

                    boolean hoveredRemove = GuiUtils.isHovered(mouseX, mouseY, deleteX, deleteY, deleteWidth, deleteHeight);

                    if (hoveredRemove) {
                        startDeleting(account);
                        return;
                    }

                    if (getLoginCondition(account, currentTime)) {
                        if (account.getUuid() != null) {
                            loginMicrosoft(account);
                        } else {
                            loginOffline(account);
                        }
                    } else {
                        toggleAccount(account);
                    }

                    lastClickedTime = currentTime;
                    lastClickedAccount = account;
                }

                offset += 30;
            }
        }

    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        switch (button.id) {
            case 1 -> {
                if (!textButton.getText().isEmpty()) {
                    String accountName = textButton.getText();
                    boolean accountExists = accounts.stream().anyMatch(acc -> acc.getName().equals(accountName));

                    if (!accountExists) {
                        Account account = new Account(accountName);
                        addAccount(account);
                        loginOffline(account);
                        selectedAccount = account;
                    }
                    textButton.setText("");
                } else if (selectedAccount != null) {
                    if (selectedAccount.getUuid() != null) {
                        loginMicrosoft(selectedAccount);
                    } else {
                        loginOffline(selectedAccount);
                    }
                } else {
                    Account account = new Account(NameGenerator.generate(16));
                    loginOffline(account);
                }
            }

            case 2 -> {
                AtomicBoolean isFinished = new AtomicBoolean(false);

                final MicrosoftAuthCallback callback = new MicrosoftAuthCallback();

                CompletableFuture<Account> future = callback.start((s, _) -> updateStatus(s));

                Sys.openURL(MicrosoftAuthCallback.url);

                future.whenCompleteAsync((account, error) -> {
                    if (error != null) {
                        updateStatus("Failed added account: " + error + ".");
                        isFinished.set(true);
                    } else {
                        addAccount(new Account(account.getName(), account.getRefreshToken(), account.getUuid()));
                        selectedAccount = account;
                        isFinished.set(true);
                    }
                });

                if (isFinished.get()) callback.close();
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        textButton.setFocused(true);
        textButton.textboxKeyTyped(typedChar, keyCode);

        if (keyCode == Keyboard.KEY_ESCAPE) {
            Accounts.saveAccounts();
            selectedAccount = null;
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Accounts.saveAccounts();
        accounts.removeIf(account -> account.isDeleting() && account.getDeleteAnim().isAnimating());
    }

    public static void updateStatus(String status) {
        statusText = status;
    }

    private void updateDeleting() {
        accounts.removeIf(Account::isDelete);
    }

    private boolean isSelected(Account account) {
        return selectedAccount != null && selectedAccount.equals(account);
    }

    private boolean getLoginCondition(Account account, long currentTime) {
        return lastClickedAccount == account && currentTime - lastClickedTime < 300;
    }

    private void addAccount(Account account) {
        accounts.add(account);
        account.getDeleteAnim().setEnd(1);
        selectedAccount = account;
        updateStatus("Successful added account: " + account.getName() + ".");
    }

    private void startDeleting(Account account) {
        account.getDeleteAnim().setEnd(0);
        account.setDeleting(true);
        selectedAccount = null;
        updateStatus("Successful removed account: " + account.getName() + ".");
    }

    private void loginOffline(Account account) {
        mc.getSession().setUsername(account.getName());
        mc.getSession().setSessionType(Session.Type.LEGACY);
        updateStatus("Successful login account: " + account.getName() + ".");
    }

    private void loginMicrosoft(Account account) {
        new Thread(() -> {
            try {
                Map.Entry<String, String> authRefreshTokens = Auth.refreshToken(account.getRefreshToken());
                String xblToken = Auth.authXBL(authRefreshTokens.getKey());
                Map.Entry<String, String> xstsTokenUserhash = Auth.authXSTS(xblToken);
                String accessToken = Auth.authMinecraft(xstsTokenUserhash.getValue(), xstsTokenUserhash.getKey());

                mc.setSession(new Session(account.getName(),
                    account.getUuid(),
                    accessToken, "msa"));
                selectedAccount = account;
                mc.getSession().setSessionType(Session.Type.MOJANG);
                updateStatus("Successful login account: " + account.getName() + ".");
            } catch (Exception e) {
                System.out.println(e.getMessage());
                updateStatus("Failed login account: " + e.getMessage() + ".");
            }
        }).start();
    }

    private void toggleAccount(Account account) {
        selectedAccount = Objects.equals(selectedAccount, account) ? null : account;
    }
}
