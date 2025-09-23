package fuguriprivatecoding.autotoolrecode.alt;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Account {
    String name;
    String refreshToken, uuid;

    public Account(String name) {
        this.name = name;
    }

    public Account(String name, String refreshToken, String uuid) {
        this.name = name;
        this.refreshToken = refreshToken;
        this.uuid = uuid;
    }
}
