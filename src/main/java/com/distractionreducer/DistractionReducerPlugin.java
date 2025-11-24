package com.distractionreducer;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;  // Add this import
import net.runelite.api.coords.WorldArea;
import net.runelite.api.widgets.Widget;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.events.ConfigChanged;
import java.util.Set;
import java.util.HashSet;
import net.runelite.client.callback.ClientThread;
import lombok.extern.slf4j.Slf4j;

@PluginDescriptor(
        name = "Distraction Reducer",
        description = "Blacks out the screen while skilling to reduce distractions",
        tags = {"woodcutting", "fishing", "mining", "cooking", "herblore", "crafting", "fletching", "smithing", "magic", "sailing", "salvaging", "skilling", "overlay"}
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

    // Combat integration fields
    private int combatRestoreDelayTicks = 0;
    private boolean wasCombating = false;
    private Set<Integer> targetMonsterIds = new HashSet<>();
    private Set<String> targetMonsterNames = new HashSet<>();
    
    // Interface auto-exit cooldown
    private int interfaceExitCooldownTicks = 0;

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

    // POH Region IDs - Player-Owned Houses are instanced and use specific region ranges
    private static final Set<Integer> POH_REGIONS = Set.of(
            7513,  // Rimmington POH
            7769,  // Taverley POH
            7257,  // Pollnivneach POH
            6457,  // Hosidius POH
            10553, // Rellekka POH
            7499,  // Brimhaven POH
            8013,  // Yanille POH
            4919   // Prifddinas POH
    );

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
            AnimationID.SMITHING_ANVIL, AnimationID.SMITHING_SMELTING, AnimationID.SMITHING_IMCANDO_HAMMER
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

    private static final Set<Integer> SAILING_SALVAGING_ANIMATION_IDS = Set.of(
            net.runelite.api.gameval.AnimationID.SAILING_HUMAN_SALVAGE_HOOK_KANDARIN_3X8_DROP01, // Salvaging is beginning
            net.runelite.api.gameval.AnimationID.SAILING_HUMAN_SALVAGE_HOOK_KANDARIN_3X8_IDLE01, // We are salvaging
            net.runelite.api.gameval.AnimationID.SAILING_HUMAN_SALVAGE_HOOK_KANDARIN_1X3_INTERACT01 // Processing salvages
    );

    @Provides
    DistractionReducerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(DistractionReducerConfig.class);
    }

    @Override
    protected void startUp() {
        overlayManager.add(distractionReducerOverlay);
        clientThread.invoke(this::updateOverlayVisibility);
        updateTargetMonsterIds();
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(distractionReducerOverlay);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged configChanged) {
        if ("distractionreducer".equals(configChanged.getGroup())) {
            if ("monsterIds".equals(configChanged.getKey()) || 
                "monsterNames".equals(configChanged.getKey()) ||
                "enableCombatBlackout".equals(configChanged.getKey())) {
                updateTargetMonsterIds();
            }
        }
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
        boolean currentlyCombating = isCombatingTargetMonster();
        boolean isMoving = isPlayerMoving(player);
        boolean shouldExitForInterface = shouldExitForInterface();

        // Immediately clear overlay if moving or interface conditions are met
        if (isMoving || shouldExitForInterface) {
            wasSkilling = false;
            wasCombating = false;
            restoreDelayTicks = 0;
            combatRestoreDelayTicks = 0;
            
            // Set cooldown if interface auto-exit was triggered
            if (shouldExitForInterface) {
                interfaceExitCooldownTicks = 3;
            }
            
            distractionReducerOverlay.setRenderOverlay(false);
            return;
        }

        // Decrement interface exit cooldown
        if (interfaceExitCooldownTicks > 0) {
            interfaceExitCooldownTicks--;
        }

        // Handle skilling logic
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

        // Handle combat logic
        if (currentlyCombating) {
            wasCombating = true;
            combatRestoreDelayTicks = 0;
        } else if (wasCombating) {
            combatRestoreDelayTicks++;
            if (combatRestoreDelayTicks >= config.combatRestoreDelay()) {
                wasCombating = false;
                combatRestoreDelayTicks = 0;
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
        boolean shouldRenderFromSkilling = (isSkilling() || wasSkilling) && !isMoving;
        boolean shouldRenderFromCombat = (isCombatingTargetMonster() || wasCombating) && !isMoving && !isHealthOrPrayerOverrideActive() && !isWildernessOverrideActive();
        
        // Prevent overlay from showing during interface exit cooldown
        boolean shouldRenderOverlay = (shouldRenderFromSkilling || shouldRenderFromCombat) && interfaceExitCooldownTicks == 0;

        distractionReducerOverlay.setRenderOverlay(shouldRenderOverlay);
        log.debug("Overlay visibility updated. Rendering: {}, Skilling: {}, Combat: {}, Is Moving: {}, HP/Prayer Override: {}, Wilderness Override: {}, Interface Cooldown: {}",
                shouldRenderOverlay, shouldRenderFromSkilling, shouldRenderFromCombat, isMoving, isHealthOrPrayerOverrideActive(), isWildernessOverrideActive(), interfaceExitCooldownTicks);
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

        // Check for POH (Player-Owned House) - disable overlay if not enabled in config
        if (isInPOH() && !config.enableInPOH()) {
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
                (SAILING_SALVAGING_ANIMATION_IDS.contains(animation) && config.sailing()) ||
                (isSmithing(animation) && config.smithing()) ||
                (isMagic(animation) && config.magic());
    }

    private boolean isInToaBank() {
        return client.getLocalPlayer().getWorldLocation().getRegionID() == TOA_LOBBY &&
                client.getVarbitValue(Varbits.TOA_RAID_LEVEL) > 0; // Check if in an active raid
    }

    private boolean isInPOH() {
        Player player = client.getLocalPlayer();
        if (player == null) {
            return false;
        }

        WorldPoint playerLocation = player.getWorldLocation();
        if (playerLocation == null) {
            return false;
        }

        int regionID = playerLocation.getRegionID();
        
        // Check if player is in any POH region
        if (POH_REGIONS.contains(regionID)) {
            return true;
        }

        // Check for instanced POH (when visiting other players' houses)
        if (client.isInInstancedRegion()) {
            WorldPoint instancePoint = WorldPoint.fromLocalInstance(client, player.getLocalLocation());
            if (instancePoint != null) {
                int instanceRegionID = instancePoint.getRegionID();
                return POH_REGIONS.contains(instanceRegionID);
            }
        }

        return false;
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
                (BAKE_PIE_ANIMATION_IDS.contains(animation) && config.bakePie()) ||
                (animation == STRING_JEWELRY_ANIMATION_ID && config.stringJewelry()) ||
                (animation == PLANK_MAKE_ANIMATION_ID && config.plankMake());
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

    private boolean isInWilderness() {
        Player player = client.getLocalPlayer();
        if (player == null) return false;
        
        WorldPoint location = player.getWorldLocation();
        return location.isInArea2D(WILDERNESS_ABOVE_GROUND, WILDERNESS_UNDERGROUND);
    }

    private boolean isWildernessOverrideActive() {
        // If in wilderness and wilderness is NOT enabled, then override (disable) combat blackout
        return isInWilderness() && !config.enableWildernessOverride();
    }

    // Add this constant with the other animation ID constants
    private static final int NPC_CONTACT_ANIMATION_ID = 4413;
    private static final int STRING_JEWELRY_ANIMATION_ID = 4412;
    private static final int PLANK_MAKE_ANIMATION_ID = 6298;

    // Add this constant for the standard spellbook ID
    private static final int STANDARD_SPELLBOOK_ID = 0;

    // Wilderness area constants for robust detection (from NPC Aggro Area plugin)
    private static final WorldArea WILDERNESS_ABOVE_GROUND = new WorldArea(2944, 3523, 448, 448, 0);
    private static final WorldArea WILDERNESS_UNDERGROUND = new WorldArea(2944, 9918, 320, 442, 0);

    private void updateTargetMonsterIds() {
        targetMonsterIds.clear();
        targetMonsterNames.clear();
        
        if (config.enableCombatBlackout()) {
            // Parse monster IDs
            if (!config.monsterIds().trim().isEmpty()) {
                String[] ids = config.monsterIds().split(",");
                for (String id : ids) {
                    try {
                        targetMonsterIds.add(Integer.parseInt(id.trim()));
                    } catch (NumberFormatException e) {
                        log.warn("Invalid monster ID: {}", id.trim());
                    }
                }
            }
            
            // Parse monster names
            if (!config.monsterNames().trim().isEmpty()) {
                String[] names = config.monsterNames().split(",");
                for (String name : names) {
                    targetMonsterNames.add(name.trim().toLowerCase());
                }
            }
        }
        
        log.debug("Updated target monster IDs: {}", targetMonsterIds);
        log.debug("Updated target monster names: {}", targetMonsterNames);
    }

    private boolean isCombatingTargetMonster() {
        if (!config.enableCombatBlackout() || (targetMonsterIds.isEmpty() && targetMonsterNames.isEmpty())) {
            return false;
        }

        Player player = client.getLocalPlayer();
        if (player == null) {
            return false;
        }

        Actor interacting = player.getInteracting();
        if (!(interacting instanceof NPC)) {
            return false;
        }

        NPC npc = (NPC) interacting;
        int npcId = npc.getId();
        String npcName = npc.getName();
        
        // Check by ID
        boolean isTargetById = targetMonsterIds.contains(npcId);
        
        // Check by name
        boolean isTargetByName = false;
        if (npcName != null && !targetMonsterNames.isEmpty()) {
            isTargetByName = targetMonsterNames.contains(npcName.toLowerCase());
        }
        
        boolean isTargetMonster = isTargetById || isTargetByName;
        if (isTargetMonster) {
            if (isTargetById) {
                log.debug("Player is combating target monster with ID: {}", npcId);
            }
            if (isTargetByName) {
                log.debug("Player is combating target monster with name: {}", npcName);
            }
        }
        
        return isTargetMonster;
    }

    private boolean isHealthOrPrayerOverrideActive() {
        if (!config.enableCombatBlackout()) {
            return false;
        }

        // Check health override
        if (config.enableHealthOverride()) {
            int currentHp = client.getBoostedSkillLevel(Skill.HITPOINTS);
            
            if (currentHp <= config.healthThreshold()) {
                log.debug("Health override active: {} HP <= {} HP", currentHp, config.healthThreshold());
                return true;
            }
        }

        // Check prayer override
        if (config.enablePrayerOverride()) {
            int currentPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
            
            if (currentPrayer <= config.prayerThreshold()) {
                log.debug("Prayer override active: {} Prayer <= {} Prayer", currentPrayer, config.prayerThreshold());
                return true;
            }
        }

        return false;
    }

    private boolean shouldExitForInterface() {
        // Check if bank interface is open
        if (isBankInterfaceOpen()) {
            log.debug("Bank interface detected, exiting overlay");
            return true;
        }

        // Check if Grand Exchange interface is open
        if (isGrandExchangeInterfaceOpen()) {
            log.debug("Grand Exchange interface detected, exiting overlay");
            return true;
        }

        return false;
    }

    private boolean isBankInterfaceOpen() {
        // Check for various bank interface widget IDs
        // Bank interface widget ID is 12 (main bank interface)
        Widget bankWidget = client.getWidget(12, 0);
        if (bankWidget != null && !bankWidget.isHidden()) {
            return true;
        }

        // Check for deposit box interface (widget ID 192)
        Widget depositBoxWidget = client.getWidget(192, 0);
        if (depositBoxWidget != null && !depositBoxWidget.isHidden()) {
            return true;
        }

        // Check for bank pin interface (widget ID 213)
        Widget bankPinWidget = client.getWidget(213, 0);
        if (bankPinWidget != null && !bankPinWidget.isHidden()) {
            return true;
        }

        return false;
    }

    private boolean isGrandExchangeInterfaceOpen() {
        // Grand Exchange interface widget ID is 465
        Widget geWidget = client.getWidget(465, 0);
        if (geWidget != null && !geWidget.isHidden()) {
            return true;
        }

        // Check for Grand Exchange collection box (widget ID 402)
        Widget geCollectionWidget = client.getWidget(402, 0);
        if (geCollectionWidget != null && !geCollectionWidget.isHidden()) {
            return true;
        }

        return false;
    }
}
