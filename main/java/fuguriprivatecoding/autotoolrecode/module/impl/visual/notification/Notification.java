package fuguriprivatecoding.autotoolrecode.module.impl.visual.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Notification {
    String moduleName;
    boolean toggle;
    long lastMS;
}
