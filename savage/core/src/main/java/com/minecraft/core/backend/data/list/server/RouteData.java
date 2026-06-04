package com.minecraft.core.backend.data.list.server;

import com.minecraft.core.account.context.objects.route.RouteContext;
import com.minecraft.core.backend.database.redis.RedisDatabase;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class RouteData {

    private final RedisDatabase redis;

    private final String ROUTE_KEY = "route:";

    public void save(RouteContext route) {
        redis.save(ROUTE_KEY + route.getSenderId(), route, 5);
    }

    public void delete(UUID sender) {
        redis.delete(ROUTE_KEY + sender);
    }

    public RouteContext read(UUID sender) {
        return redis.load(ROUTE_KEY + sender, RouteContext.class);
    }

    public boolean has(UUID sender) {
        return redis.exists(ROUTE_KEY + sender);
    }
}