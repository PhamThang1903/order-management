package com.example.ordermanagement.cache;

import com.example.ordermanagement.domain.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderCacheService {

    public void cacheOrder(Order order) {
        log.info("Cached order stub: {}", order.getId());
    }
}
