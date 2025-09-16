package fuguriprivatecoding.autotoolrecode.alt;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class Account {
    String name;
    String refreshToken, uuid;
    String login, password;

    public Account(String name) {
        this.name = name;
    }

    public Account(String login, String password) {
        this.login = login.trim();
        this.password = StringUtils.isNotBlank(password) ? password : null;
    }

    public Account(String name, String refreshToken, String uuid) {
        this.name = name;
        this.refreshToken = refreshToken;
        this.uuid = uuid;
    }
}
