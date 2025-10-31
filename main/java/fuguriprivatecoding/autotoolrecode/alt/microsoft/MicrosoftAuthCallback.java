package fuguriprivatecoding.autotoolrecode.alt.microsoft;

import com.sun.net.httpserver.HttpServer;
import fuguriprivatecoding.autotoolrecode.alt.Account;
import fuguriprivatecoding.autotoolrecode.gui.altmanager.AltScreen;
import java.io.Closeable;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class MicrosoftAuthCallback implements Closeable {
    public static final String url = "https://login.live.com/oauth20_authorize.srf?client_id=54fd49e4-2103-4044-9603-2b028c814ec3&response_type=code&scope=XboxLive.signin%20XboxLive.offline_access&redirect_uri=http://localhost:59125&prompt=select_account";
    private static HttpServer server;

    public CompletableFuture<Account> start(BiConsumer<String, Object[]> progressHandler) {
        CompletableFuture<Account> cf = new CompletableFuture<>();
        try {
            if (server != null) {
                server.stop(0);
                server = null;
            }

            server = HttpServer.create(new InetSocketAddress("localhost", 59125), 0);
            server.createContext("/", ex -> {
                AltScreen.updateStatus("Microsoft authentication callback request: " + ex.getRemoteAddress());
                try {
                    final byte[] messageToHTML = "Закрывай страницу братишка все сработало можешь не ссать!!".getBytes(StandardCharsets.UTF_8);

                    progressHandler.accept("Authentication... (%s)", new Object[]{"preparing"});
                    ex.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                    ex.sendResponseHeaders(307, messageToHTML.length);
                    try (OutputStream os = ex.getResponseBody()) {
                        os.write(messageToHTML);
                    }
                    close();

                    final Thread thread = new Thread(() -> {
                        try {
                            cf.complete(auth(progressHandler, ex.getRequestURI().getQuery()));
                        } catch (Throwable t) {
                            AltScreen.updateStatus("Unable to authenticate via Microsoft.");
                            cf.completeExceptionally(t);
                        }
                    }, "MicrosoftAuthThread");

                    thread.setDaemon(true);
                    thread.start();
                } catch (Throwable t) {
                    AltScreen.updateStatus("Unable to process request on Microsoft authentication callback server.");
                    close();
                    cf.completeExceptionally(t);
                }
            });
            server.start();
            AltScreen.updateStatus("Started Microsoft authentication callback server.");
        } catch (Throwable t) {
            AltScreen.updateStatus("Unable to run the Microsoft authentication callback server.");
            close();
            cf.completeExceptionally(t);
        }
        return cf;
    }

    private Account auth(BiConsumer<String, Object[]> progressHandler, String query) throws Exception {
        if (query == null) throw new NullPointerException("query=null");
        if (query.equals("error=access_denied&error_description=The user has denied access to the scope requested by the client application.")) return null;
        if (!query.startsWith("code=")) throw new IllegalStateException("query=" + query);
        progressHandler.accept("Authentication... (%s)", new Object[]{"CodeToToken"});
        Map.Entry<String, String> authRefreshTokens = Auth.codeToToken(query.replace("code=", ""));
        String refreshToken = authRefreshTokens.getValue();
        progressHandler.accept("Authentication... (%s)", new Object[]{"AuthXBL"});
        String xblToken = Auth.authXBL(authRefreshTokens.getKey());
        progressHandler.accept("Authentication... (%s)", new Object[]{"AuthXSTS"});
        Map.Entry<String, String> xstsTokenUserhash = Auth.authXSTS(xblToken);
        progressHandler.accept("Authentication... (%s)", new Object[]{"AuthMinecraft"});
        String accessToken = Auth.authMinecraft(xstsTokenUserhash.getValue(), xstsTokenUserhash.getKey());
        progressHandler.accept("Authentication... (%s)", new Object[]{"GetProfile"});
        Map.Entry<UUID, String> profile = Auth.getProfile(accessToken);
        return new Account(profile.getValue(), refreshToken, profile.getKey().toString());
    }

    @Override
    public void close() {
        try {
            if (server != null) {
                server.stop(0);
                AltScreen.updateStatus("Stopped Microsoft authentication callback server.");
            }
        } catch (Throwable t) {
            AltScreen.updateStatus("Unable to stop the Microsoft authentication callback server.");
        }
    }
}