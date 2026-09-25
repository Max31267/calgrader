package com.lucly.calgrader;

import com.lucly.calgrader.client.UpgraderScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = Calgrader.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Calgrader.MODID, value = Dist.CLIENT)
public class CalgraderClient {
    @SubscribeEvent
    static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(Calgrader.UPGRADER_MENU.get(), UpgraderScreen::new);
    }
}
