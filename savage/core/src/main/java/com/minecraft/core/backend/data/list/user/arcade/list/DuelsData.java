package com.minecraft.core.backend.data.list.user.arcade.list;

import com.minecraft.core.backend.data.list.user.arcade.MemberData;
import com.minecraft.core.backend.database.postgresql.PostgresDatabase;
import com.minecraft.core.backend.database.redis.RedisDatabase;
import com.minecraft.core.member.list.duels.DuelMember;

public class DuelsData extends MemberData<DuelMember> {

    public DuelsData(PostgresDatabase postgres, RedisDatabase redis) {
        super(postgres, redis, "members_duels");
    }
}
