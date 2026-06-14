package ru.modstrany;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class BorderExpanderItem extends ArmorItem {
    public BorderExpanderItem(ArmorMaterial material, EquipmentSlot slot, Properties properties) {
        super(material, slot, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        }

        CountryManager.INSTANCE.getCountryByPlayer(player.getUUID())
                .ifPresentOrElse(country -> {
                    BlockPos pos = context.getClickedPos();
                    boolean added = country.addBorderPosition(pos);
                    if (added) {
                        player.sendMessage(Component.literal("Граница страны '" + country.getName() + "' расширена."), player.getUUID());
                    } else {
                        player.sendMessage(Component.literal("Эта точка уже включена в границу страны."), player.getUUID());
                    }
                }, () -> player.sendMessage(Component.literal("Ты не член ни одной страны, чтобы расширить границу."), player.getUUID()));

        return InteractionResult.CONSUME;
    }
}
