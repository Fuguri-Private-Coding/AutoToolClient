package fuguriprivatecoding.autotoolrecode.handle;

import java.util.ArrayList;
import java.util.List;

public class Friends {
    private static final List<String> FRIENDS_NAMES = new ArrayList<>();

    public static void onClick(String name) {
        if (FRIENDS_NAMES.contains(name)) {
            FRIENDS_NAMES.remove(name);
            return;
        }

        FRIENDS_NAMES.add(name);
    }

    public static boolean isFriend(String name, boolean reverseFriends) {
        return FRIENDS_NAMES.contains(name) != reverseFriends;
    }
}
