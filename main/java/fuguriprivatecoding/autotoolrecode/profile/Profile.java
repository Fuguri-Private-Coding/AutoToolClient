package fuguriprivatecoding.autotoolrecode.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class Profile implements Serializable {
    private String username;
    private String role;

    @Override
    public String toString() {
        return "[" + username + "] [" + role + "]";
    }

    public String getColored() {
        String coloredRole = role;

        switch (role) {
            case "Owner" -> coloredRole = "§4Owner§f";
            case "test" -> coloredRole = "§atest§f";
            case "User" -> coloredRole = "§1User§f";
        }

        return "§f[§6" + username + "§f] [" + coloredRole + "§f]";
    }
}