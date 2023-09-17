package icu.ripley.fusionauthtest1minecraft.events;

import icu.ripley.fusionauthtest1minecraft.config.ConfigManager;
import icu.ripley.fusionauthtest1minecraft.model.User;
import icu.ripley.fusionauthtest1minecraft.redis.RedisManager;
import icu.ripley.fusionauthtest1minecraft.util.Language;
import icu.ripley.fusionauthtest1minecraft.util.UserUtilities;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import redis.clients.jedis.JedisPubSub;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

public class AuthenticationEvents extends JedisPubSub implements Listener {

    private final Map<UUID, User> uuidToUser;
    private final RedisManager redisManager;
    private final ConfigManager configManager;
    private final Server server;
    private final TextComponent unauthenticatedMessage;

    public AuthenticationEvents(Map<UUID, User> uuidToUser, RedisManager redisManager, ConfigManager configManager, Server server, ExecutorService executorService) {
        this.uuidToUser = uuidToUser;
        this.redisManager = redisManager;
        this.configManager = configManager;
        this.server = server;

        this.unauthenticatedMessage = createChatMessage();

        // Jedis hates us, so we have to subscribe in a different thread. ExecutorService is the cleanest way I know of to do it.
        executorService.submit(() -> this.redisManager.getResource().subscribe(this, "oauth2-callback-events"));
    }

    public TextComponent createChatMessage(){
        TextComponent component = new TextComponent(Language.USER_NOT_AUTHENTICATED.getMessage());

        component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, configManager.getSsoUrl()));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Language.USER_NOT_AUTHENTICATED_HOVER.getMessage()).create()));

        return component;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if(configManager.getDisableJoinMessages()){
            event.setJoinMessage("");
        }

        User user = UserUtilities.createFromBukkitPlayer(event.getPlayer());

        uuidToUser.put(user.getUuid(), user);

        if (user.isAuthenticationRequired()) {
            user.setAuthenticated(false); // ensure they are set to NOT authenticated.
            event.getPlayer().sendMessage(unauthenticatedMessage);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        User user = uuidToUser.get(event.getPlayer().getUniqueId());

        if (!user.isAuthenticated()) {
            event.setTo(event.getFrom()); // teleports them back, similar to WorldGuard's way of doing it. Prevents the client from having a panic attack.
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        User user = uuidToUser.get(event.getPlayer().getUniqueId());

        if (event.getMessage().equals("LetMeOutPleaseUwU675&%$(&^48754368)^$%6835HJGfdyurdjhgcTRedt#$#!!")) {
            event.setCancelled(true);

            user.setAuthenticated(true);
            event.getPlayer().sendMessage(Language.USER_AUTHENTICATION_SUCCESS.getMessage());

            return;
        }

        if (!user.isAuthenticated()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(unauthenticatedMessage);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        User user = uuidToUser.get(event.getPlayer().getUniqueId());

        if (!user.isAuthenticated()) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onMessage(String channel, String message) {
        if (!channel.equals("oauth2-callback-events")) {
            return;
        }

        String username = message.split("AUTHENTICATE ")[1].replaceAll("\"", ""); // full message is "AUTHENTICATE <username>" with the quotes.
        System.out.println(message);
        System.out.println(username);

        Player player = server.getPlayer(username);

        if (player == null) {
            return;
        }

        User user = uuidToUser.get(player.getUniqueId());

        user.setAuthenticated(true);
        player.sendMessage(Language.USER_AUTHENTICATION_SUCCESS.getMessage());
    }

}
