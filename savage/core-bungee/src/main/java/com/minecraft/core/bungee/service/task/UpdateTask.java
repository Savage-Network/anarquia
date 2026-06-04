package com.minecraft.core.bungee.service.task;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.bungee.event.list.update.UpdateEvent;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.Util;

import java.util.Arrays;
import java.util.List;

public class UpdateTask implements Runnable {

    private final List<String> customMessages = Arrays.asList(
            "&fDeseja participar de nossa equipe? Faça sua aplicação em §bstaff." + Constant.SERVER_DOMAIN,
            "&fAltere suas preferências usando §a/prefs§f!",
            "§fParticipe de nossas redes sociais: §bmidia." + Constant.SERVER_DOMAIN,
            "§fAdquira VIPs, tags e medalhas! §6" + Constant.SERVER_STORE,
            "§fCansado da mesma skin de sempre? Use §c/skin §fe use skins ilimitadas §a§lDE GRAÇA!"
    );

    private final int messageSize = customMessages.size();
    private final int delay = 120;
    private int cleanupCounter = 0;
    int time = delay, index = 0;

    @Override
    public void run() {
        new UpdateEvent().call();
        cleanupCounter++;
        int cleanupInterval = 10;
        if (cleanupCounter >= cleanupInterval) {
            cleanupCounter = 0;
            Core.getServerData().cleanupDeadServers();
        }

        time--;

        if (time == 0) {
            Core.getAccountController()
                    .filter(account -> !account.inServer(ServerType.AUTH))
                    .forEach(account -> account.send(Constant.SERVER_PREFIX + Util.color(customMessages.get(index))));

            time = delay;
            index++;

            if (index >= messageSize)
                index = 0;
        }
    }
}
