package com.nttdata.bootcamp.service.impl;

import com.google.gson.Gson;
import com.nttdata.bootcamp.entity.Customer;
import com.nttdata.bootcamp.service.RedisCacheService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RedisCacheServiceImpl implements RedisCacheService {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final Gson gson = new Gson();
    private final Logger logger = LogManager.getLogger(RedisCacheServiceImpl.class);

    // TTL en segundos
    @Value("${redis.sessiondata.ttl}")
    private int sessiondataTTL;

    public RedisCacheServiceImpl(ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ------------------------------------------------------------------------
    // REACTIVE METHODS
    // ------------------------------------------------------------------------

    @Override
    public Mono<Void> clearAll() {
        return redisTemplate
                .execute(connection -> connection.serverCommands().flushAll())
                .then()
                .onErrorResume(e -> {
                    logger.error("Error flushing all Redis data: {}", e.getMessage());
                    return Mono.error(new RuntimeException(e));
                });
    }

    @Override
    public Mono<Customer> retrieveCustomer(String customerDni) {

        return redisTemplate.opsForValue()
                .get(customerDni)
                .flatMap(value -> {
                    if (value instanceof String && StringUtils.hasText((String) value)) {
                        return Mono.just(gson.fromJson((String) value, Customer.class));
                    }
                    return Mono.empty();
                })
                .onErrorResume(e -> {
                    logger.error("Error retrieving customer {} from Redis: {}", customerDni, e.getMessage());
                    return Mono.error(new RuntimeException(e));
                });
    }

    @Override
    public Mono<Customer> storeCustomer(String customerDni, Customer customer) {

        String json = gson.toJson(customer);

        return redisTemplate.opsForValue()
                .set(customerDni, json)
                .flatMap(result -> redisTemplate.expire(customerDni, java.time.Duration.ofSeconds(sessiondataTTL)))
                .map(ok -> customer)
                .onErrorResume(e -> {
                    logger.error("Error storing customer {} into Redis: {}", customerDni, e.getMessage());
                    return Mono.error(new RuntimeException(e));
                });
    }

    @Override
    public Mono<Void> flushCustomerCache(String customerId) {

        Flux<String> keysFlux =
                redisTemplate.opsForList()
                        .range(customerId, 0, -1)
                        .map(value -> (String) value);

        return keysFlux
                .collectList()
                .flatMap(keys -> {

                    if (keys.isEmpty()) {
                        return Mono.empty();
                    }

                    keys.add(customerId);

                    return redisTemplate.delete(Flux.fromIterable(keys)).then();
                })
                .onErrorResume(e -> {
                    logger.error("Error flushing cache for customer {}: {}", customerId, e.getMessage());
                    return Mono.error(new RuntimeException(e));
                });
    }

    @Override
    public Mono<Boolean> deleteCustomer(String customerDni) {
        return redisTemplate
                .delete(customerDni)  // Mono<Long>
                .map(count -> count != null && count > 0)  // Long → Boolean
                .onErrorResume(e -> {
                    logger.error("Error deleting customer {} from Redis: {}", customerDni, e.getMessage());
                    return Mono.just(false);
                });
    }



}
