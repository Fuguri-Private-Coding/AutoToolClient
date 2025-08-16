package fuguriprivatecoding.autotoolrecode.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.awt.*;
import java.io.InputStream;
import java.net.URL;

@Getter
@Setter
@AllArgsConstructor
public class DiscordProfile {
    String id, avatarUrl, bannerUrl, userName, tag;
    Color profileColor;
    Color serverRoleColor;

    public DiscordProfile() {}

    public InputStream getAvatar() {
        String avatarUrl = getAvatarUrl() + "?size=512";
        try {
            return new URL(avatarUrl).openStream();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public InputStream getBanner() {
        String bannerUrl = getBannerUrl() + "?size=1024";
        try {
            return new URL(bannerUrl).openStream();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
