package icu.ripley.fusionauthtest1minecraft.config;

import icu.ripley.fusionauthtest1minecraft.FA_plugin;
import lombok.Getter;
import org.bukkit.configuration.Configuration;

@Getter
public class ConfigManager {

    private final String ssoUrl;
    private final String redisUrl;
    private final Boolean disableJoinMessages;

    public ConfigManager(FA_plugin plugin) {

        plugin.saveDefaultConfig(); // save config from jar
        Configuration config = plugin.getConfig();

        this.ssoUrl = config.getString("sso-signin-url");
        this.redisUrl = config.getString("redis-url");
        this.disableJoinMessages = config.getBoolean("disable-join-messages");
    }

}
