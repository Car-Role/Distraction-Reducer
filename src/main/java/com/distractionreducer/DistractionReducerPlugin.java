package com.distractionreducer;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;  // Add this import
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import java.util.Set;
import net.runelite.client.callback.ClientThread;
import lombok.extern.slf4j.Slf4j;

@PluginDescriptor(
        name = "Distraction Reducer",
        description = "Blacks out the screen while skilling to reduce distractions",
        tags = {"woodcutting", "fishing", "mining", "cooking", "herblore", "crafting", "fletching", "smithing", "magic", "skilling", "overlay"}
)
@Slf4j
public class DistractionReducerPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private DistractionReducerConfig config;

    @Inject
    private DistractionReducerOverlay distractionReducerOverlay;

    private int restoreDelayTicks = 0;
    private boolean wasSkilling = false;

    private static final int WALKING_POSE = 1205;
    private static final int RUNNING_POSE = 1210;
    private static final Set<Integer> TURNING_POSES = Set.of(1206, 1208);

    // TOA Region IDs
    private static final int TOA_LOBBY = 14160;
    private static final Set<Integer> TOA_REGIONS = Set.of(
            14162, // Croc
            14164, // Scarab
            14166, // Het
            14168, // Baba
            14170, // Zebak
            14172, // Kephri
            14160, // Lobby
            14674  // Wardens
    );

    // Duke Sucellus Region ID
    private static final int DUKE_SUCELLUS_REGION = 12132;

    // Updated Magic Animation IDs
    private static final Set<Integer> PLANK_MAKE_ANIMATION_IDS = Set.of(6298);
    private static final Set<Integer> ENCHANT_JEWELRY_ANIMATION_IDS = Set.of(
            619,  // Sapphire
            721,  // Emerald
            724,  // Ruby
            727,  // Diamond
            730,  // Dragonstone

            // Bulk enchantment animations
            719,  // Sapphire bulk
            722,  // Emerald bulk
            725,  // Ruby bulk
            728,  // Diamond bulk
            731,  // Dragonstone bulk

            // Special item-specific animations
            720,  // Games necklace (Sapphire)
            723,  // Ring of dueling (Emerald)
            726,  // Binding necklace (Ruby)
            729,  // Ring of life (Diamond)
            732,  // Combat bracelet (Dragonstone)

            // Modern universal animations
            7531, // Modern universal
            931   // Modern alternative
    );
    private static final Set<Integer> CHARGE_ORB_ANIMATION_IDS = Set.of(726);
    private static final Set<Integer> BAKE_PIE_ANIMATION_IDS = Set.of(4413);
    private static final Set<Integer> STRING_JEWELRY_ANIMATION_IDS = Set.of(4412);

    private static final Set<Integer> WOODCUTTING_ANIMATION_IDS = Set.of(
            AnimationID.WOODCUTTING_BRONZE, AnimationID.WOODCUTTING_IRON, AnimationID.WOODCUTTING_STEEL,
            AnimationID.WOODCUTTING_BLACK, AnimationID.WOODCUTTING_MITHRIL, AnimationID.WOODCUTTING_ADAMANT,
            AnimationID.WOODCUTTING_RUNE, AnimationID.WOODCUTTING_DRAGON, AnimationID.WOODCUTTING_INFERNAL,
            AnimationID.WOODCUTTING_3A_AXE, AnimationID.WOODCUTTING_CRYSTAL, AnimationID.WOODCUTTING_TRAILBLAZER,
            AnimationID.WOODCUTTING_2H_BRONZE, AnimationID.WOODCUTTING_2H_IRON, AnimationID.WOODCUTTING_2H_STEEL,
            AnimationID.WOODCUTTING_2H_BLACK, AnimationID.WOODCUTTING_2H_MITHRIL, AnimationID.WOODCUTTING_2H_ADAMANT,
            AnimationID.WOODCUTTING_2H_RUNE, AnimationID.WOODCUTTING_2H_DRAGON, AnimationID.WOODCUTTING_2H_CRYSTAL,
            AnimationID.WOODCUTTING_2H_CRYSTAL_INACTIVE, AnimationID.WOODCUTTING_2H_3A
    );

    private static final Set<Integer> SMITHING_ANIMATION_IDS = Set.of(
            AnimationID.SMITHING_ANVIL, AnimationID.SMITHING_SMELTING
    );

    private static final Set<Integer> FISHING_ANIMATION_IDS = Set.of(
            AnimationID.FISHING_BARBARIAN_ROD, AnimationID.FISHING_BARBTAIL_HARPOON, AnimationID.FISHING_BAREHAND,
            AnimationID.FISHING_BIG_NET, AnimationID.FISHING_CAGE, AnimationID.FISHING_CRYSTAL_HARPOON,
            AnimationID.FISHING_DRAGON_HARPOON, AnimationID.FISHING_HARPOON, AnimationID.FISHING_INFERNAL_HARPOON,
            AnimationID.FISHING_KARAMBWAN, AnimationID.FISHING_NET, AnimationID.FISHING_OILY_ROD,
            AnimationID.FISHING_POLE_CAST, AnimationID.FISHING_PEARL_ROD, AnimationID.FISHING_PEARL_FLY_ROD,
            AnimationID.FISHING_PEARL_BARBARIAN_ROD, AnimationID.FISHING_PEARL_ROD_2,
            AnimationID.FISHING_PEARL_FLY_ROD_2, AnimationID.FISHING_PEARL_BARBARIAN_ROD_2,
            AnimationID.FISHING_TRAILBLAZER_HARPOON
    );

    private static final Set<Integer> COOKING_ANIMATION_IDS = Set.of(
            AnimationID.COOKING_FIRE, AnimationID.COOKING_RANGE, AnimationID.COOKING_WINE
    );

    private static final Set<Integer> HERBLORE_ANIMATION_IDS = Set.of(
            AnimationID.HERBLORE_POTIONMAKING, AnimationID.HERBLORE_MAKE_TAR
    );

    private static final Set<Integer> CRAFTING_ANIMATION_IDS = Set.of(
            AnimationID.CRAFTING_LEATHER, AnimationID.CRAFTING_GLASSBLOWING, AnimationID.CRAFTING_SPINNING,
            AnimationID.CRAFTING_POTTERS_WHEEL, AnimationID.CRAFTING_POTTERY_OVEN,
            // Gem cutting animations - verified unique IDs
            892,  // Sapphire
            891,  // Emerald
            890,  // Ruby
            889,  // Diamond
            888,  // Dragonstone
            887,  // Opal
            886,  // Jade
            885,  // Red topaz
            7531, // Battlestaff crafting
            7202  // Zeah: Chiseling dark essence blocks into fragments
    );

    private static final Set<Integer> FLETCHING_ANIMATION_IDS = Set.of(
            AnimationID.FLETCHING_BOW_CUTTING, AnimationID.FLETCHING_STRING_NORMAL_SHORTBOW,
            AnimationID.FLETCHING_STRING_NORMAL_LONGBOW, AnimationID.FLETCHING_STRING_OAK_SHORTBOW,
            AnimationID.FLETCHING_STRING_OAK_LONGBOW, AnimationID.FLETCHING_STRING_WILLOW_SHORTBOW,
            AnimationID.FLETCHING_STRING_WILLOW_LONGBOW, AnimationID.FLETCHING_STRING_MAPLE_SHORTBOW,
            AnimationID.FLETCHING_STRING_MAPLE_LONGBOW, AnimationID.FLETCHING_STRING_YEW_SHORTBOW,
            AnimationID.FLETCHING_STRING_YEW_LONGBOW, AnimationID.FLETCHING_STRING_MAGIC_SHORTBOW,
            AnimationID.FLETCHING_STRING_MAGIC_LONGBOW
    );

    private static final Set<Integer> MINING_ANIMATION_IDS = Set.of(
            AnimationID.MINING_BRONZE_PICKAXE, AnimationID.MINING_IRON_PICKAXE, AnimationID.MINING_STEEL_PICKAXE,
            AnimationID.MINING_BLACK_PICKAXE, AnimationID.MINING_MITHRIL_PICKAXE, AnimationID.MINING_ADAMANT_PICKAXE,
            AnimationID.MINING_RUNE_PICKAXE, AnimationID.MINING_DRAGON_PICKAXE, AnimationID.MINING_DRAGON_PICKAXE_UPGRADED,
            AnimationID.MINING_DRAGON_PICKAXE_OR, AnimationID.MINING_INFERNAL_PICKAXE, AnimationID.MINING_3A_PICKAXE,
            AnimationID.MINING_CRYSTAL_PICKAXE, AnimationID.MINING_TRAILBLAZER_PICKAXE, AnimationID.MINING_GILDED_PICKAXE,
            AnimationID.MINING_MOTHERLODE_BRONZE, AnimationID.MINING_MOTHERLODE_IRON, AnimationID.MINING_MOTHERLODE_STEEL,
            AnimationID.MINING_MOTHERLODE_BLACK, AnimationID.MINING_MOTHERLODE_MITHRIL, AnimationID.MINING_MOTHERLODE_ADAMANT,
            AnimationID.MINING_MOTHERLODE_RUNE, AnimationID.MINING_MOTHERLODE_DRAGON, AnimationID.MINING_MOTHERLODE_DRAGON_UPGRADED,
            AnimationID.MINING_MOTHERLODE_DRAGON_OR, AnimationID.MINING_MOTHERLODE_INFERNAL, AnimationID.MINING_MOTHERLODE_3A,
            AnimationID.MINING_MOTHERLODE_CRYSTAL, AnimationID.MINING_MOTHERLODE_TRAILBLAZER,
            6747, 6748, 6749, 6108, 6751, 6750, 6746, 8314, 7140, 643, 8349, 4483, 7284, 8350,
            7201
    );

    // Update the FIREMAKING_ANIMATION_IDS set with correct bonfire animations
    private static final Set<Integer> FIREMAKING_ANIMATION_IDS = Set.of(
            10565,  // Regular logs
            10569,  // Oak logs
            10572,  // Willow logs
            10568,  // Maple logs
            10573,  // Yew logs
            10566,  // Magic logs
            10570   // Redwood logs
    );

    @Provides
    DistractionReducerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(DistractionReducerConfig.class);
    }

    @Override
    protected void startUp() {
        overlayManager.add(distractionReducerOverlay);
        clientThread.invoke(this::updateOverlayVisibility);
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(distractionReducerOverlay);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
            clientThread.invoke(this::updateOverlayVisibility);
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        Player player = client.getLocalPlayer();
        if (player == null) return;

        boolean currentlySkilling = isSkilling();
        boolean isMoving = isPlayerMoving(player);

        // Immediately clear overlay if moving
        if (isMoving) {
            wasSkilling = false;
            restoreDelayTicks = 0;
            distractionReducerOverlay.setRenderOverlay(false);  // Add immediate overlay update
            return;
        }

        if (currentlySkilling) {
            wasSkilling = true;
            restoreDelayTicks = 0;
        } else if (wasSkilling) {
            restoreDelayTicks++;
            if (restoreDelayTicks >= config.restoreDelay()) {
                wasSkilling = false;
                restoreDelayTicks = 0;
            }
        }

        updateOverlayVisibility();
    }

    private boolean isPlayerMoving(Player player) {
        int poseAnimation = player.getPoseAnimation();

        // Store the current position
        if (lastPlayerPosition == null) {
            lastPlayerPosition = player.getWorldLocation();
            return false;
        }

        // Check if position changed since last tick
        WorldPoint currentPosition = player.getWorldLocation();
        boolean moved = !currentPosition.equals(lastPlayerPosition);
        lastPlayerPosition = currentPosition;

        return poseAnimation == WALKING_POSE ||
                poseAnimation == RUNNING_POSE ||
                TURNING_POSES.contains(poseAnimation) ||
                moved;
    }

    // Add this field at the class level (with other private fields)
    private WorldPoint lastPlayerPosition = null;

    private void updateOverlayVisibility() {
        Player player = client.getLocalPlayer();
        if (player == null) return;

        boolean isMoving = isPlayerMoving(player);
        boolean shouldRenderOverlay = (isSkilling() || wasSkilling) && !isMoving;

        distractionReducerOverlay.setRenderOverlay(shouldRenderOverlay);
        log.debug("Overlay visibility updated. Rendering: {}, Delay Ticks: {}, Is Moving: {}, Was Skilling: {}",
                shouldRenderOverlay, restoreDelayTicks, isMoving, wasSkilling);
    }

    private boolean isSkilling() {
        Player player = client.getLocalPlayer();
        if (player == null) return false;

        int animation = player.getAnimation();

        // Failsafe for Chambers of Xeric
        if (client.getVarbitValue(Varbits.IN_RAID) > 0) {
            return false;
        }

        // Failsafe for The Gauntlet & The Corrupted Gauntlet
        // Varbit 9178 is for being inside The Gauntlet
        if (client.getVarbitValue(9178) > 0) {
            return false;
        }


        // Failsafe for various regions
        WorldPoint playerLocation = player.getWorldLocation();

        // Check for Duke Sucellus (non-instanced)
        if (playerLocation != null && playerLocation.getRegionID() == DUKE_SUCELLUS_REGION) {
            return false;
        }

        // Check for instanced regions (TOA and Duke Sucellus)
        if (client.isInInstancedRegion()) {
            WorldPoint instancePoint = WorldPoint.fromLocalInstance(client, player.getLocalLocation());
            if (instancePoint != null) {
                int regionID = instancePoint.getRegionID();
                // TOA puzzle rooms
                if (TOA_REGIONS.contains(regionID) && !isInToaBank()) {
                    return false;
                }
                // Duke Sucellus instanced area
                if (regionID == DUKE_SUCELLUS_REGION) {
                    return false;
                }
            }
        }

        return (WOODCUTTING_ANIMATION_IDS.contains(animation) && config.woodcutting()) ||
                (FISHING_ANIMATION_IDS.contains(animation) && config.fishing()) ||
                (MINING_ANIMATION_IDS.contains(animation) && config.mining()) ||
                (COOKING_ANIMATION_IDS.contains(animation) && config.cooking()) ||
                (HERBLORE_ANIMATION_IDS.contains(animation) && config.herblore()) ||
                (CRAFTING_ANIMATION_IDS.contains(animation) && config.crafting()) ||
                (FLETCHING_ANIMATION_IDS.contains(animation) && config.fletching()) ||
                (FIREMAKING_ANIMATION_IDS.contains(animation) && config.firemaking()) ||
                (isSmithing(animation) && config.smithing()) ||
                (isMagic(animation) && config.magic());
    }

    private boolean isInToaBank() {
        return client.getLocalPlayer().getWorldLocation().getRegionID() == TOA_LOBBY &&
                client.getVarbitValue(Varbits.TOA_RAID_LEVEL) > 0; // Check if in an active raid
    }

    private boolean isSmithing(int animation) {
        if (SMITHING_ANIMATION_IDS.contains(animation)) {
            return true;
        }

        if (animation == AnimationID.SMITHING_CANNONBALL) {
            ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
            if (inventory == null) {
                return false;
            }
            return inventory.contains(ItemID.AMMO_MOULD) || inventory.contains(ItemID.DOUBLE_AMMO_MOULD);
        }

        return false;
    }

    private boolean isMagic(int animation) {
        return PLANK_MAKE_ANIMATION_IDS.contains(animation) ||
                isEnchantingJewelry(animation) ||
                CHARGE_ORB_ANIMATION_IDS.contains(animation) ||
                (BAKE_PIE_ANIMATION_IDS.contains(animation) && config.bakePie());
    }

    private boolean isEnchantingJewelry(int animation) {
        if (!ENCHANT_JEWELRY_ANIMATION_IDS.contains(animation)) {
            return false;
        }

        // Check if the player is using the standard spellbook
        return client.getVarbitValue(Varbits.SPELLBOOK) == 0;
    }

    private boolean isNPCContact() {
        Player player = client.getLocalPlayer();
        if (player == null) return false;

        int animation = player.getAnimation();
        if (animation != NPC_CONTACT_ANIMATION_ID) return false;

        // Check if the player has the Lunar spellbook active
        return client.getVarbitValue(Varbits.SPELLBOOK) == 2;
    }

    // Add this constant with the other animation ID constants
    private static final int NPC_CONTACT_ANIMATION_ID = 4413;

    // Add this constant for the standard spellbook ID
    private static final int STANDARD_SPELLBOOK_ID = 0;
}
