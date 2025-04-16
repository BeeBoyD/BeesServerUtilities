package net.beeboyd.beeserverutilities;

import com.mojang.logging.LogUtils;
import net.beeboyd.beeserverutilities.autoexec.AutoExecCommand;
import net.beeboyd.beeserverutilities.autoexec.AutoExecManager;
import net.beeboyd.beeserverutilities.deathmessage.DeathMessageCommand;
import net.beeboyd.beeserverutilities.nametag.NametagCommand;
import net.beeboyd.beeserverutilities.serverlogger.ServerLogger;
import net.beeboyd.beeserverutilities.serverlogger.ServerLoggerBlockCommand;
import net.beeboyd.beeserverutilities.serverlogger.ServerLoggerConfig;
import net.beeboyd.beeserverutilities.serverstats.ServerInfoCommand;
import net.beeboyd.beeserverutilities.serverstats.ServerStatsCommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(BeeServerUtilities.MOD_ID)
public class BeeServerUtilities {
    public static final String MOD_ID = "beeserverutilities";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BeeServerUtilities() {
        // Register the setup method for mod loading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        // Register for forge events
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        AutoExecManager.loadRules();
        ServerLoggerConfig.reload();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        AutoExecCommand.register(event.getDispatcher());
        DeathMessageCommand.register(event.getDispatcher());
        ServerStatsCommand.register(event.getDispatcher());
        ServerLogger.register(event.getDispatcher());
        ServerInfoCommand.register(event.getDispatcher());
        ServerLoggerBlockCommand.register(event.getDispatcher());
        NametagCommand.register(event.getDispatcher());
    }
}
