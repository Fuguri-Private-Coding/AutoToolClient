package fuguriprivatecoding.autotoolrecode.alt;

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import lombok.NonNull;
import net.minecraft.util.Session;

import java.net.Proxy;

public final class AltLoginThread {

    @NonNull
    public final Account credentials;

    private String caller;

    public AltLoginThread(@NonNull Account credential, String caller) {
        this.credentials = credential;
        this.caller = caller;
    }

    public Session run() {
        final String password = this.credentials.getPassword();
        return createSession(this.credentials.getLogin(), password);
    }

    public static Session createSession(String username, String password) {
        final YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "");
        final YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) service
                .createUserAuthentication(Agent.MINECRAFT);

        auth.setUsername(username);
        auth.setPassword(password);

        try {
            auth.logIn();
            final GameProfile selectedProfile = auth.getSelectedProfile();
            return new Session(selectedProfile.getName(), selectedProfile.getId().toString(),
                    auth.getAuthenticatedToken(), "crack cocaine");
        } catch (AuthenticationException e) {
            e.printStackTrace();
            return null;
        }
    }
}