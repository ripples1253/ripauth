package icu.ripley.fusionauthtest1minecraft;

import icu.ripley.fusionauthtest1minecraft.config.ConfigManager;
import icu.ripley.fusionauthtest1minecraft.events.AuthenticationEvents;
import icu.ripley.fusionauthtest1minecraft.model.User;
import icu.ripley.fusionauthtest1minecraft.redis.RedisManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class FA_plugin extends JavaPlugin {

    private final Map<UUID, User> uuidToUser = new HashMap<>();
    private RedisManager redisManager;
    private ConfigManager configManager;
    private ExecutorService executorService;

    @Override
    public void onEnable() {
        // set up managers for specific tasks
        configManager = new ConfigManager(this);
        redisManager = new RedisManager(configManager);

        // hehe executorservice go brr :3
        executorService = Executors.newSingleThreadExecutor();

        // register our authentication events, this is the bulk of the plugin.
        getServer().getPluginManager().registerEvents(new AuthenticationEvents(uuidToUser, redisManager, configManager, getServer(), executorService), this);
    }

    @Override
    public void onDisable() {
        // shutdown the executor service.
        redisManager.shutdown();
        executorService.shutdown();
    }
}
