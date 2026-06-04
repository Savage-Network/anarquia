package com.minecraft.game.account.context.objects.home;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class HomeMetadata {

    private final Map<String, StoredLocation> homes = new LinkedHashMap<>();
}
