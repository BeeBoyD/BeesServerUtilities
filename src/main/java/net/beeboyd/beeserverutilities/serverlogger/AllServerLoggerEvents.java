package net.beeboyd.beeserverutilities.serverlogger;

import net.beeboyd.beeserverutilities.BeeServerUtilites;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.level.PistonEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Locale;

@Mod.EventBusSubscriber(modid = BeeServerUtilites.MOD_ID)
public class AllServerLoggerEvents {

    // Player login
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLoggerConfig config = ServerLoggerConfig.get();
        if (config.logLogin) {
            ServerLogger.logEvent("login", player.getScoreboardName(), "");
        }
    }

    // Player logout
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLoggerConfig config = ServerLoggerConfig.get();
        if (config.logLogout) {
            ServerLogger.logEvent("logout", player.getScoreboardName(), "");
        }
    }
    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        // Only log if the event wasn't canceled by another mod/plugin.
        if (event.isCanceled()) return;

        ServerPlayer player = event.getPlayer();
        // Convert the message Component to a String.
        String msg = event.getMessage().getString();

        // Only log non-command chat messages that are not blank.
        if (!msg.startsWith("/") && !msg.trim().isEmpty()) {
            ServerLoggerConfig config = ServerLoggerConfig.get();
            if (config.logChat) {
                ServerLogger.logEvent("chat", player.getScoreboardName(), msg);
            }
        }
    }

    // Command event: logs commands sent by players.
    @SubscribeEvent
    public static void onCommandEvent(CommandEvent event) {
        // Retrieve the command source
        CommandSourceStack source = event.getParseResults().getContext().getSource();
        // Check if the source entity is a ServerPlayer
        if (!(source.getEntity() instanceof ServerPlayer player)) return;

        ServerLoggerConfig config = ServerLoggerConfig.get();
        if (config.logCommandUse) {
            // Get the entire command input.
            String command = event.getParseResults().getReader().getString();
            ServerLogger.logEvent("command_use", player.getScoreboardName(), command);
        }
    }

    // Block break event
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        ServerLoggerConfig config = ServerLoggerConfig.get();
        BlockPos pos = event.getPos();
        String coords = "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
        if (config.logBrokenBlock) {
            ServerLogger.logEvent("broken_block", player.getScoreboardName(), coords);
        }
        Block block = event.getState().getBlock();
        if (config.logCropHarvest &&
                (block == Blocks.WHEAT || block == Blocks.CARROTS || block == Blocks.POTATOES || block == Blocks.BEETROOTS)) {
            ServerLogger.logEvent("crop_harvest", player.getScoreboardName(), coords);
        }
    }

    // Block place event
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLoggerConfig config = ServerLoggerConfig.get();
        BlockPos pos = event.getPos();
        String coords = "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
        if (config.logBlockPlace) {
            ServerLogger.logEvent("block_place", player.getScoreboardName(), coords);
        }
        if (config.logPlacedTNT && event.getState().getBlock() == Blocks.TNT) {
            ServerLogger.logEvent("placed_tnt", player.getScoreboardName(), coords);
        }
    }

    // Crafting event
    @SubscribeEvent
    public static void onItemCrafted(ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLoggerConfig config = ServerLoggerConfig.get();
        if (config.logCrafting) {
            ServerLogger.logEvent("crafting", player.getScoreboardName(), event.getCrafting().getDisplayName().getString());
        }
    }

    // Item pickup event
    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLoggerConfig config = ServerLoggerConfig.get();
        if (config.logItemPickup) {
            ItemStack stack = event.getItem().getItem();
            ServerLogger.logEvent("item_pickup", player.getScoreboardName(), stack.getDisplayName().getString());
        }
    }

    // Item drop event
    @SubscribeEvent
    public static void onItemDrop(ItemTossEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        ServerLoggerConfig config = ServerLoggerConfig.get();
        if (config.logItemDrop) {
            ItemStack stack = event.getEntity().getItem();
            ServerLogger.logEvent("item_drop", player.getScoreboardName(), stack.getDisplayName().getString());
        }
    }

    // Dimension change event
    @SubscribeEvent
    public static void onDimensionChange(PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLoggerConfig config = ServerLoggerConfig.get();
        if (config.logPortalUse) {
            ServerLogger.logEvent("portal_use", player.getScoreboardName(),
                    "From " + event.getFrom() + " to " + player.level.dimension().location());
        }
    }

    // Damage event
    @SubscribeEvent
    public static void onDamage(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLoggerConfig config = ServerLoggerConfig.get();
        if (config.logDamage) {
            String damage = String.format(Locale.ROOT, "%.2f", event.getAmount());
            ServerLogger.logEvent("damage", player.getScoreboardName(), "Damage: " + damage);
        }
    }

    // Explosion event
    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        ServerLoggerConfig config = ServerLoggerConfig.get();
        if (config.logExplosion) {
            String coords = "";
            if (event.getExplosion() != null && event.getExplosion().getPosition() != null) {
                coords = "(" + event.getExplosion().getPosition().x + ", " +
                        event.getExplosion().getPosition().y + ", " +
                        event.getExplosion().getPosition().z + ")";
            }
            ServerLogger.logEvent("explosion", "N/A", coords);
        }
    }

    // Block interaction event
    @SubscribeEvent
    public void onBlockInteraction(PlayerInteractEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLoggerConfig config = ServerLoggerConfig.get();
        try {
            BlockPos pos = event.getPos();
            String coords = "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
            if (config.logBlockInteraction) { // Ensure you have a flag for this event
                ServerLogger.logEvent("block_interaction", player.getScoreboardName(), coords);
            }
        } catch (Exception e) {
            BeeServerUtilites.LOGGER.error("Error in onBlockInteraction: ", e);
        }
    }

    // Entity spawn event
    @SubscribeEvent
    public static void onEntitySpawn(EntityJoinLevelEvent event) {
        ServerLoggerConfig config = ServerLoggerConfig.get();
        if (config.logEntitySpawn) {
            String entityName = event.getEntity().getType().getDescription().getString();
            ServerLogger.logEvent("entity_spawn", "N/A", entityName);
        }
    }

    // Entity kill event (if killed by a player)
    @SubscribeEvent
    public static void onEntityKill(LivingDeathEvent event) {
        ServerLoggerConfig config = ServerLoggerConfig.get();
        if (config.logEntityKill && event.getSource().getEntity() instanceof ServerPlayer player) {
            String entityName = event.getEntity().getType().getDescription().getString();
            ServerLogger.logEvent("entity_kill", player.getScoreboardName(), entityName);
        }
    }

    // Villager trade event (using right-click block with emerald block)
    @SubscribeEvent
    public static void onVillagerTrade(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLoggerConfig config = ServerLoggerConfig.get();
        if (config.logVillagerTrade) {
            Block block = event.getLevel().getBlockState(event.getPos()).getBlock();
            if (block == Blocks.EMERALD_BLOCK) {
                String coords = "(" + event.getPos().getX() + ", " +
                        event.getPos().getY() + ", " + event.getPos().getZ() + ")";
                ServerLogger.logEvent("villager_trade", player.getScoreboardName(), coords);
            }
        }
    }

    // Sleep event
    @SubscribeEvent
    public static void onSleep(PlayerSleepInBedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLoggerConfig config = ServerLoggerConfig.get();
        if (config.logSleep) {
            BlockPos pos = event.getPos();
            String coords = "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
            ServerLogger.logEvent("sleep", player.getScoreboardName(), coords);
        }
    }

    // Piston extend event
    @SubscribeEvent
    public static void onPistonExtend(PistonEvent.Post event) {
        ServerLoggerConfig config = ServerLoggerConfig.get();
        if (config.logPistonExtend) {
            BlockPos pos = event.getPos();
            String coords = "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
            ServerLogger.logEvent("piston_extend", "N/A", coords);
        }
    }

    // Piston retract event
    @SubscribeEvent
    public static void onPistonRetract(PistonEvent.Post event) {
        ServerLoggerConfig config = ServerLoggerConfig.get();
        if (config.logPistonRetract) {
            BlockPos pos = event.getPos();
            String coords = "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
            ServerLogger.logEvent("piston_retract", "N/A", coords);
        }
    }

    // Potion use event
    @SubscribeEvent
    public static void onPotionUse(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLoggerConfig config = ServerLoggerConfig.get();
        if (config.logPotionUse) {
            if (event.getItemStack().getItem().getDescriptionId().contains("potion")) {
                ServerLogger.logEvent("potion_use", player.getScoreboardName(), "");
            }
        }
    }

    // Flower pick event
    @SubscribeEvent
    public static void onFlowerPick(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLoggerConfig config = ServerLoggerConfig.get();
        if (config.logFlowerPick) {
            Block block = event.getLevel().getBlockState(event.getPos()).getBlock();
            if (block == Blocks.DANDELION || block == Blocks.POPPY) {
                String coords = "(" + event.getPos().getX() + ", " +
                        event.getPos().getY() + ", " + event.getPos().getZ() + ")";
                ServerLogger.logEvent("flower_pick", player.getScoreboardName(), coords);
            }
        }
    }

    // Water bucket use event
    @SubscribeEvent
    public static void onWaterBucketUse(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLoggerConfig config = ServerLoggerConfig.get();
        if (config.logWaterBucket) {
            if (event.getItemStack().getItem() == net.minecraft.world.item.Items.WATER_BUCKET) {
                String coords = "(" + event.getPos().getX() + ", " +
                        event.getPos().getY() + ", " + event.getPos().getZ() + ")";
                ServerLogger.logEvent("water_bucket", player.getScoreboardName(), coords);
            }
        }
    }

    // Lava bucket use event
    @SubscribeEvent
    public static void onLavaBucketUse(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLoggerConfig config = ServerLoggerConfig.get();
        if (config.logLavaBucket) {
            if (event.getItemStack().getItem() == net.minecraft.world.item.Items.LAVA_BUCKET) {
                String coords = "(" + event.getPos().getX() + ", " +
                        event.getPos().getY() + ", " + event.getPos().getZ() + ")";
                ServerLogger.logEvent("lava_bucket", player.getScoreboardName(), coords);
            }
        }
    }

    // Trade event (using right-click block event with an emerald block)
    @SubscribeEvent
    public static void onTrade(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLoggerConfig config = ServerLoggerConfig.get();
        if (config.logTrade) {
            if (event.getLevel().getBlockState(event.getPos()).getBlock() == Blocks.EMERALD_BLOCK) {
                String coords = "(" + event.getPos().getX() + ", " +
                        event.getPos().getY() + ", " + event.getPos().getZ() + ")";
                ServerLogger.logEvent("trade", player.getScoreboardName(), coords);
            }
        }
    }
}
