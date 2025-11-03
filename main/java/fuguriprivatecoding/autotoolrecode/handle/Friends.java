package fuguriprivatecoding.autotoolrecode.handle;

import java.util.ArrayList;
import java.util.List;

public class Friends {
    static List<String> friendsNames;

    public Friends() {
        friendsNames = new ArrayList<>();
    }

    public List<String> getFriendNames() {
        return friendsNames;
    }

    public static void onClick(String name) {
        if (friendsNames.contains(name)) {
            friendsNames.remove(name);
        } else {
            friendsNames.add(name);
        }
    }

    public static boolean isFriend(String name, boolean reverseFriends) {
        if (friendsNames.contains(name)) {
            return !reverseFriends;
        } else {
            return reverseFriends;
        }
    }
}
