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
    private Role role;

    @Override
    public String toString() {
        return username + " [" + role.name() + "]";
    }

    /**
     * @return Строка с цветом текста
     */
    public String toColoredString() {
        return username + " §7[" + role.getColorPrefix() + role.name() + "§7]";
    }
}
