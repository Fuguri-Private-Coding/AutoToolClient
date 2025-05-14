package me.hackclient.utils.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Profile {
    private String username;
    private String role;

    @Override
    public String toString() {
        return "[" + username + "] [" + role + "]";
    }

    public String getColored() {
        String coloredRole = role;

        switch (role) {
            case "dev" -> coloredRole = "§4dev§f";
            case "test" -> coloredRole = "§atest§f";
            case "user" -> coloredRole = "§auser§f";
        }

        return "§f[§6" + username + "§f] [" + coloredRole + "§f]";
    }
}
