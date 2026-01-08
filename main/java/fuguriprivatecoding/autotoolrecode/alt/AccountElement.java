package fuguriprivatecoding.autotoolrecode.alt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.joml.Vector2f;

// created by dicves_recode on 04.01.2026
@Getter
@AllArgsConstructor
public abstract class AccountElement {
    protected AccountElement parent;

    public abstract boolean isFile();
    public abstract void save();
    public abstract Vector2f render(float x, float y);
    public abstract Vector2f click(float x, float y, float mouseX, float mouseY, int button);
}
