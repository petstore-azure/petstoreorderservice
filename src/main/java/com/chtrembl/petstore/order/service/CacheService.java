package com.chtrembl.petstore.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheService {

    private final CacheManager cacheManager;
    private final OrderCosmosRepository orderCosmosRepository;

    public int getOrdersCacheSize() {
        try {
            long ordersCount = orderCosmosRepository.countOrders();
            return (int) Math.min(Integer.MAX_VALUE, ordersCount);
        } catch (Exception e) {
            log.warn("Could not get orders count from Cosmos DB: {}", e.getMessage());
            return 0;
        }
    }

    // Clear cache every 12 hours (43200000 ms)
    @Scheduled(fixedRate = 43200000)
    public void evictAllCaches() {
        log.info("Evicting all caches on scheduled interval");
        cacheManager.getCacheNames()
                .forEach(cacheName -> {
                    var cache = cacheManager.getCache(cacheName);
                    if (cache != null) {
                        cache.clear();
                    }
                });
    }
}