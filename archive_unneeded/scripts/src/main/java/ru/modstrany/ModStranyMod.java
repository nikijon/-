package ru.modstrany;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ModStranyMod.MODID)
public class ModStranyMod {
    public static final String MODID = "modstrany";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ModStranyMod() {
        LOGGER.info("ModStrany запущен!");
        ModItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
