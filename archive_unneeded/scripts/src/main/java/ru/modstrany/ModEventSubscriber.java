package ru.modstrany;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModStranyMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEventSubscriber {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (event.player.level.isClientSide) {
            return;
        }

        if (event.player.getItemBySlot(EquipmentSlot.HEAD).getItem() != ModItems.BORDER_EXPANDER.get()) {
            return;
        }

        CountryManager.INSTANCE.getCountryByPlayer(event.player.getUUID()).ifPresent(country -> {
            if (event.player.tickCount % 20 != 0) {
                return;
            }
            if (!(event.player.level instanceof ServerLevel serverLevel)) {
                return;
            }

            String actionBar = "Страна: " + country.getName() + " | Союзники: " + country.getAllies().size() + " | Баланс: " + country.getBalance();
            event.player.displayClientMessage(Component.literal(actionBar), true);

            int shown = 0;
            for (BlockPos pos : country.getBorderPositions()) {
                if (shown >= 40) {
                    break;
                }
                if (pos.distToCenterSqr(event.player.getX(), event.player.getY(), event.player.getZ()) > 256.0) {
                    continue;
                }
                serverLevel.sendParticles(event.player, ParticleTypes.END_ROD,
                        pos.getX() + 0.5,
                        pos.getY() + 1.0,
                        pos.getZ() + 0.5,
                        1,
                        0.0,
                        0.0,
                        0.0,
                        0.0);
                shown++;
            }
        });
    }
}
