package com.example.ordermanagement.cache;

import com.example.ordermanagement.domain.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

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
        String key = buildOrderKey(orderId);

        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof Order order) {
            log.info("cache hit for order={}", orderId);
            return Optional.of(order);
        }
        log.info("cache miss for orderId={}", orderId);
        return Optional.empty();
    }

    public void evictOrder(Long orderId) {
        String key = buildOrderKey(orderId);
        Boolean deleted = redisTemplate.delete(key);
        log.info("Evicted order cache: orderid={}, key={}, deleted={}", orderId, key, deleted);
    }

    public void incrementTodayOrderCount() {
        String key = "order:count:today";
        Long count = redisTemplate.opsForValue().increment(key);

        redisTemplate.expire(key, Duration.ofDays(1));
        log.info("increment today order count: key={}, count={}", key, count);
    }

    public Long getTodayOrderCount() {
        String key = "order:count:today";
        Object value = redisTemplate.opsForValue().get(key);
        return switch (value) {
            case null -> 0L;
            case Integer integerValue -> integerValue.longValue();
            case Long longValue -> longValue;
            default -> Long.parseLong(value.toString());
        };

    }

    public Set<Object> getRecentOrders(int limit) {
        String key = "recent:orders";
        return redisTemplate.opsForZSet().reverseRange(key, 0, limit - 1);
    }

    private String buildOrderKey(Long orderId) {
        return "order:" + orderId;
    }
}
