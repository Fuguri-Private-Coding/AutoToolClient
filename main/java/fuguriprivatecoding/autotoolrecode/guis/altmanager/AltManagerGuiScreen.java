package fuguriprivatecoding.autotoolrecode.guis.altmanager;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.alt.Account;
import fuguriprivatecoding.autotoolrecode.alt.Auth;
import fuguriprivatecoding.autotoolrecode.alt.MicrosoftAuthCallback;
import fuguriprivatecoding.autotoolrecode.guis.main.GuiClientButton;
import fuguriprivatecoding.autotoolrecode.utils.animation.Animation2D;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.interpolation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.scissor.ScissorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.AlphaUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BackgroundUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
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

    ResourceLocation removeLogo = new ResourceLocation("minecraft", "autotool/mainmenu/exit.png");

    Account selectedAccount;
    int scroll, scrollTotalHeight;
    Animation2D scrolls;

    private boolean isScrolling = false;
    private float scrollOffsetOnClick = 0;

    static String updatedText;

    EasingAnimation alphaAnim = new EasingAnimation();

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
        alphaAnim.setValue(0);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sc = new ScaledResolution(mc);
        int currentScroll = Mouse.getDWheel();

        scroll -= currentScroll / 120 * 50;

        ClientFontRenderer font = Client.INST.getFonts().fonts.get("SFProRounded");

        float altVisibleHeight = sc.getScaledHeight() - 35;
        float maxScroll = Math.max(scrollTotalHeight - altVisibleHeight, 0);

        if (scroll > 0) scroll = 0;
        if (scroll < -maxScroll) scroll = (int) -maxScroll;

        scrolls.endY = scroll;
        scrolls.update(15f);

        alphaAnim.update(3, Easing.IN_OUT_QUAD);
        alphaAnim.setEnd(1f);

        BackgroundUtils.run();

        AlphaUtils.startWrite();

        RoundedUtils.drawRect(sc.getScaledWidth() - 285, 15, 250, sc.getScaledHeight() - 30, 7.5f, new Color(0, 0, 0, 0.7f));
        if (updatedText != null) font.drawCenteredString(updatedText, sc.getScaledWidth() - 285 + 125, 5, Color.WHITE);

        ScissorUtils.enableScissor();
        ScissorUtils.scissor(new ScaledResolution(mc), sc.getScaledWidth() - 285, 15, 250, sc.getScaledHeight() - 30);

        float offset = scrolls.y;
        scrollTotalHeight = 0;
        for (Account account : accounts) {
            boolean clickedAccount = selectedAccount != null && account.getName().equals(selectedAccount.getName());
            boolean equalsAccount = account.getName().equals(mc.getSession().getUsername());
            boolean removeHover = mouseX > sc.getScaledWidth() - 60 && mouseX < sc.getScaledWidth() - 60 + 15 && mouseY > 20 + 2.5f + offset && mouseY < 20 + 2.5f + offset + 15;
            boolean isMicrosoftAccount = account.getUuid() != null;

            RoundedUtils.drawRect(sc.getScaledWidth() - 280, 10 + 10 + offset, 250 - 10, 20, 10, clickedAccount ? new Color(0.2f, 0.2f, 0.2f, 0.7f) : new Color(0, 0, 0, 0.7f));
            font.drawString(account.getName() + (isMicrosoftAccount ? " | Microsoft." : " | Offline."), sc.getScaledWidth() - 270, 10 + 5 + 2 + 11f + offset, equalsAccount ? Color.green : Color.WHITE);

            ColorUtils.glColor(removeHover ? Color.RED : Color.WHITE);
            RenderUtils.drawImage(removeLogo, sc.getScaledWidth() - 60, 20 + 2.5f + offset, 15, 15, true);
            offset += 25;
            scrollTotalHeight += 25;
        }

        ScissorUtils.disableScissor();

        float scrollbarWidth = 5;
        float scrollbarX = sc.getScaledWidth() - 25;
        float scrollbarTrackHeight = altVisibleHeight - 15;
        float scrollbarY = 20;

        float thumbHeight = Math.max((altVisibleHeight / scrollTotalHeight) * scrollbarTrackHeight, 10f);

        float scrollProgress = maxScroll > 0 ? (-scrolls.y) / maxScroll : 0;
        float thumbY = scrollbarY + (scrollbarTrackHeight - thumbHeight) * scrollProgress;

        if (Mouse.isButtonDown(0) && scrollTotalHeight >= altVisibleHeight) {
            boolean clickedOnThumb = mouseX >= scrollbarX && mouseX <= scrollbarX + scrollbarWidth &&
                mouseY >= thumbY && mouseY <= thumbY + thumbHeight;

            boolean clickedOnTrack = mouseX >= scrollbarX && mouseX <= scrollbarX + scrollbarWidth &&
                mouseY >= scrollbarY && mouseY <= scrollbarY + scrollbarTrackHeight;

            if (clickedOnThumb) {
                if (!isScrolling) {
                    scrollOffsetOnClick = mouseY - thumbY;
                    isScrolling = true;
                }
            }

            if (isScrolling) {
                float newThumbY = mouseY - scrollOffsetOnClick;
                newThumbY = Math.max(scrollbarY, newThumbY);
                newThumbY = Math.min(scrollbarY + scrollbarTrackHeight - thumbHeight, newThumbY);

                float trackScrollableHeight = scrollbarTrackHeight - thumbHeight;
                if (trackScrollableHeight > 0) {
                    float newScrollProgress = (newThumbY - scrollbarY) / trackScrollableHeight;
                    scroll = (int) (-newScrollProgress * maxScroll);
                }
            }

            if (clickedOnTrack && !isScrolling) {
                if (mouseY < thumbY) {
                    scroll += (int) (altVisibleHeight * 2f);
                } else if (mouseY > thumbY + thumbHeight) {
                    scroll -= (int) (altVisibleHeight * 2f);
                }
            }
        } else {
            isScrolling = false;
        }

        scroll = Math.max(scroll, (int)-maxScroll);
        scroll = Math.min(scroll, 0);

        if (scrollTotalHeight >= altVisibleHeight) {
            RoundedUtils.drawRect(sc.getScaledWidth() - 30, 15, 15, sc.getScaledHeight() - 30, 7.5f, new Color(0, 0, 0, 0.7f));
            RoundedUtils.drawRect(scrollbarX, scrollbarY, scrollbarWidth, scrollbarTrackHeight, 2, new Color(0f, 0f, 0f, 0.7f));
            RoundedUtils.drawRect(scrollbarX, thumbY, scrollbarWidth, thumbHeight, 2, Color.WHITE);
        }

        altManagerGuiText.drawTextBox();
        altManagerGuiText.setMaxStringLength(16);

        String currentUser = "Account: " + mc.getSession().getUsername();

        font.drawString(currentUser, 2.5f, 2.5f + 1, Color.WHITE);
        super.drawScreen(mouseX, mouseY, partialTicks);

        AlphaUtils.endWrite();
        AlphaUtils.draw(alphaAnim.getValue());
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        alphaAnim.setEnd(0);
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
            boolean selectAccount =
                mouseX > sc.getScaledWidth() - 280 && mouseX < sc.getScaledWidth() - 280 + (250 - 10) &&
                mouseY > 10 + 10 + offset && mouseY < 10 + 10 + offset + 20;

            boolean removeAccount =
                mouseX > sc.getScaledWidth() - 60 && mouseX < sc.getScaledWidth() - 60 + 15 &&
                mouseY > 20 + 2.5f + offset && mouseY < 20 + 2.5f + offset + 15;

            if (mouseButton == 0) {
                if (removeAccount) {
                    if (account != null) {
                        accounts.remove(account);
                        updateStatus("Successful deleted account: " + account.getName() + ".");
                        if (account == selectedAccount) selectedAccount = null;
                    }
                    return;
                }

                if (selectAccount) {
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
