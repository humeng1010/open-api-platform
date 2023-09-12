package com.panda.springbootinit.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 配置
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    private String host;

    private String port;

    // private String password;

    @Bean
    public RedissonClient redissonClient() {
        // 1.创建配置
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s", host, port);
        config.useSingleServer()
                .setAddress(redisAddress)
                // .setPassword(password)
                .setDatabase(0);
        // 2.创建redis实例
        return Redisson.create(config);
    }

    @Autowired
    public void testLock(RedissonClient redissonClient) throws InterruptedException {
        RLock lock = redissonClient.getLock("lock");
        try {
            boolean isLock = lock.tryLock();
            if (!isLock) {
                while (true) {
                    Thread.sleep(500);
                    boolean b = lock.tryLock();
                    if (b) {
                        break;
                    }
                }
            }
            //    加锁后的操作

        } finally {
            lock.unlock();
        }

    }
}