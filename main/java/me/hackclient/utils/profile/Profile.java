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
        return username + " [" + role + "]";
    }
}
