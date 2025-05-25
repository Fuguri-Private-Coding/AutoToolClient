package fuguriprivatecoding.autotool.profile;

import java.io.Serializable;
import java.util.Arrays;

public enum Role implements Serializable {
    OWNER(4),
    DEV(3),
    TESTER(2),
    USER(1),
    FREE(0);

    private final int level;

    Role(int level) {
        this.level = level;
    }

    public boolean isHigher(Role role) {
        return this.level > role.level;
    }

    public boolean isHigherOrEquals(Role role) {
        return role == this || isHigher(role);
    }

    public boolean isLower(Role role) {
        return this.level < role.level;
    }

    public boolean isLowerOrEquals(Role role) {
        return role == this || isLower(role);
    }

    public boolean isHigher(int level) {
        return this.level > level;
    }

    public boolean isHigherOrEquals(int level) {
        return level == this.level || isHigher(level);
    }

    public boolean isLower(int level) {
        return this.level < level;
    }

    public boolean isLowerOrEquals(int level) {
        return level == this.level || isLower(level);
    }

    public static Role getRole(int level) {
        return Arrays.stream(values()).filter(role -> role.level == level).findFirst().orElse(null);
    }

    public static Role getRole(String name) {
        return Arrays.stream(values()).filter(role -> role.toString().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}