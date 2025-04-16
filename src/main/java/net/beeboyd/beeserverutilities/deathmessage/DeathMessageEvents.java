package net.beeboyd.beeserverutilities.deathmessage;

import net.beeboyd.beeserverutilities.BeeServerUtilities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = BeeServerUtilities.MOD_ID)
public class DeathMessageEvents {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer)) return;
        ServerPlayer player = (ServerPlayer) event.getEntity();
        MinecraftServer server = player.getServer();
        if (server == null) return;

        if (!DeathMessageConfig.get().enabled) return;

        // Get death position and format coordinates
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        String coordsString = String.format("(%.1f, %.1f, %.1f)", x, y, z);

        // Create clickable, hoverable coordinate component:
        Component coordsComponent = Component.literal(coordsString)
            .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy coordinates")))
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, coordsString))
            );

        // Determine death cause
        DamageSource cause = event.getSource();
        String causeString = cause.getMsgId();

        // Get player's inventory items (names) before death
        String inventoryString = player.getInventory().items.stream()
            .filter(itemStack -> !itemStack.isEmpty())
            .map(ItemStack::getHoverName)
            .map(Component::getString)
            .collect(Collectors.joining(", "));
        if (inventoryString.isEmpty()) {
            inventoryString = "None";
        }

        // Build the death message using the custom format from the config
        String format = DeathMessageConfig.get().messageFormat;
        // Replace placeholders with actual values; note: for coords we insert the clickable component later.
        String plainMessage = format.replace("%player%", player.getName().getString())
                                    .replace("%coords%", coordsString)
                                    .replace("%cause%", causeString)
                                    .replace("%items%", inventoryString);

        // Create the final message component that includes the clickable coordinates.
        Component finalMessage = Component.literal("")
            .append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.RED))
            .append(Component.literal(" died at "))
            .append(coordsComponent)
            .append(Component.literal(" by " + causeString + ". Items: " + inventoryString));

        // Log the message to the server console.
        BeeServerUtilities.LOGGER.info(finalMessage.getString());

        // Send the message to all players with admin permissions.
        for (ServerPlayer admin : server.getPlayerList().getPlayers()) {
            if (admin.hasPermissions(2)) {
                admin.sendSystemMessage(finalMessage);
            }
        }
    }
}
