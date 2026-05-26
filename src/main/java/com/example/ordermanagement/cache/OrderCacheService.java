package com.example.ordermanagement.cache;

import com.example.ordermanagement.domain.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.order.cache-ttl-seconds:600}")
    private long cacheTtlSeconds;

    public void cacheOrder(Order order) {
        String key = buildOrderKey(order.getId());
        redisTemplate.opsForValue().set(key, order, Duration.ofSeconds(cacheTtlSeconds));
        log.info("Cached order: key={}, ttl={}s", order.getId(), cacheTtlSeconds);
    }

    public Optional<Order> getFromCache(Long orderId) {
        log.info("Get order from cache stub: {}", orderId);
        return Optional.empty();
    }

    private String buildOrderKey(Long orderId) {
        return "order:" + orderId;
    }
}
