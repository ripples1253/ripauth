package icu.ripley.fusionauthtest1minecraft.util;

import icu.ripley.fusionauthtest1minecraft.model.User;
import org.bukkit.entity.Player;

public class UserUtilities {

    public static User createFromBukkitPlayer(Player player) {
        User user = new User();

        user.setUuid(player.getUniqueId());
        user.setUsername(player.getName());
        user.setAuthenticationRequired(player.hasPermission("ripauth.forcelogin"));
        user.setAuthenticated(true); // we set it to false if auth is required on join.

        return user;
    }

}
