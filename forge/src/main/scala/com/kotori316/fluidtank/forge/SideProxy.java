package com.kotori316.fluidtank.forge;

import java.util.Optional;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import com.kotori316.fluidtank.forge.render.RenderTank;

public abstract class SideProxy {

    public abstract Optional<Level> getLevel(NetworkEvent.Context context);

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
            BlockEntityRenderers.register(FluidTank.TILE_CREATIVE_TANK_TYPE.get(), RenderTank::new);
        }

        @Override
        public Optional<Level> getLevel(NetworkEvent.Context context) {
            var serverWorld = Optional.ofNullable(context.getSender()).map(ServerPlayer::getCommandSenderWorld);
            return serverWorld.or(() -> LogicalSidedProvider.CLIENTWORLD.get(context.getDirection().getReceptionSide()));
        }
    }

    private static class ServerProxy extends SideProxy {
        private static SideProxy server() {
            return new ServerProxy();
        }

        @Override
        public Optional<Level> getLevel(NetworkEvent.Context context) {
            return Optional.ofNullable(context.getSender()).map(ServerPlayer::getCommandSenderWorld);
        }
    }
}
