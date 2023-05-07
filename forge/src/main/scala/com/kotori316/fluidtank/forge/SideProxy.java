package com.kotori316.fluidtank.forge;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

import com.kotori316.fluidtank.forge.render.RenderTank;

public abstract class SideProxy {

    public static SideProxy get() {
        return switch (FMLEnvironment.dist) {
            case CLIENT -> ClientProxy.client();
            case DEDICATED_SERVER -> ServerProxy.server();
        };
    }

    @OnlyIn(Dist.CLIENT)
    private static class ClientProxy extends SideProxy {
        private static SideProxy client() {
            return new ClientProxy();
        }

        @SubscribeEvent
        public void registerTESR(FMLClientSetupEvent event) {
            BlockEntityRenderers.register(FluidTank.TILE_TANK_TYPE.get(), RenderTank::new);
        }
    }

    private static class ServerProxy extends SideProxy {
        private static SideProxy server() {
            return new ServerProxy();
        }
    }
}
