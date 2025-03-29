package net.beeboyd.beeserverutilities;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(BeeServerUtilites.MOD_ID)
public class BeeServerUtilites {
    public static final String MOD_ID = "beeserverutilities";
    private static final Logger LOGGER = LogUtils.getLogger();

    public BeeServerUtilites() {
        // Register the setup method for mod loading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        // Register for forge events
        MinecraftForge.EVENT_BUS.register(this);
        // Load persisted autoexec rules from file
        AutoExecManager.loadRules();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Common setup code (if needed)
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        AutoExecCommand.register(event.getDispatcher());
    }
}
