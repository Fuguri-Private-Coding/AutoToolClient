package me.hackclient.friend;

import java.util.ArrayList;
import java.util.List;

public class FriendManager {
    List<String> friendsNames;

    public FriendManager() {
        friendsNames = new ArrayList<>();
    }

    public List<String> getFriendNames() {
        return friendsNames;
    }

    public void onClick(String name) {
        if (friendsNames.contains(name)) {
            friendsNames.remove(name);
        } else {
            friendsNames.add(name);
        }
    }

    public boolean isFriend(String name, boolean reverseFriends) {
        if (friendsNames.contains(name)) {
            return !reverseFriends;
        } else {
            return reverseFriends;
        }
    }
}
