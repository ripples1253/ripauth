package icu.ripley.fusionauthtest1minecraft.redis;

import icu.ripley.fusionauthtest1minecraft.config.ConfigManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisManager {

    private final JedisPool jedisPool;

    public RedisManager(ConfigManager configManager) {
        this.jedisPool = new JedisPool(configManager.getRedisUrl());
    }

    public Jedis getResource() {
        return jedisPool.getResource();
    }

    public void shutdown() {
        jedisPool.destroy();
    }

}
