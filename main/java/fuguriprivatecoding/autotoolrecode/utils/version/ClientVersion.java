package fuguriprivatecoding.autotoolrecode.utils.version;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
public class ClientVersion {
    private int globalVersion, version, microUpdate;

    @Override
    public String toString() {
        return globalVersion + "." + version + "." + microUpdate;
    }
}
