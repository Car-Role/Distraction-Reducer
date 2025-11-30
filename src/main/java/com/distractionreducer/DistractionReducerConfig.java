package com.distractionreducer;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.Color;

@ConfigGroup("distractionreducer")
public interface DistractionReducerConfig extends Config {
    @ConfigSection(
            name = "Skilling Toggles",
            description = "Toggle overlay for different skilling activities",
            position = 0
    )
    String skillingToggles = "skillingToggles";

    @ConfigSection(
            name = "Combat",
            description = "Configure combat-based overlay activation",
            position = 1
    )
    String combatIntegration = "combatIntegration";

    @ConfigSection(
            name = "Color Picker",
            description = "Customize the overlay color",
            position = 2
    )
    String colorPicker = "colorPicker";

    @ConfigSection(
            name = "Timing",
            description = "Configure timing-related settings",
            position = 3
    )
    String timing = "timing";

    // Existing skilling toggles
    @ConfigItem(
            keyName = "woodcutting",
            name = "Woodcutting",
            description = "Display overlay while woodcutting",
            section = skillingToggles
    )
    default boolean woodcutting() {
        return true;
    }

    @ConfigItem(
            keyName = "fishing",
            name = "Fishing",
            description = "Display overlay while fishing",
            section = skillingToggles
    )
    default boolean fishing() {
        return true;
    }

    @ConfigItem(
            keyName = "mining",
            name = "Mining",
            description = "Display overlay while mining",
            section = skillingToggles
    )
    default boolean mining() {
        return true;
    }

    @ConfigItem(
            keyName = "cooking",
            name = "Cooking",
            description = "Display overlay while cooking",
            section = skillingToggles
    )
    default boolean cooking() {
        return true;
    }

    @ConfigItem(
            keyName = "herblore",
            name = "Herblore",
            description = "Display overlay while doing herblore",
            section = skillingToggles
    )
    default boolean herblore() {
        return true;
    }

    @ConfigItem(
            keyName = "crafting",
            name = "Crafting",
            description = "Display overlay while crafting",
            section = skillingToggles
    )
    default boolean crafting() {
        return true;
    }

    @ConfigItem(
            keyName = "fletching",
            name = "Fletching",
            description = "Display overlay while fletching",
            section = skillingToggles
    )
    default boolean fletching() {
        return true;
    }

    @ConfigItem(
            keyName = "smithing",
            name = "Smithing",
            description = "Display overlay while smithing",
            section = skillingToggles
    )
    default boolean smithing() {
        return true;
    }

    @ConfigItem(
            keyName = "magic",
            name = "Magic",
            description = "Display overlay while performing magic activities",
            section = skillingToggles
    )
    default boolean magic() {
        return true;
    }

    @ConfigItem(
            keyName = "firemaking",
            name = "Firemaking",
            description = "Display overlay while adding logs to fires",
            section = skillingToggles
    )
    default boolean firemaking() {
        return true;
    }

    @ConfigItem(
            keyName = "sailing",
            name = "Sailing (Salvaging)",
            description = "Display overlay while salvaging",
            section = skillingToggles
    )
    default boolean sailing() {
        return true;
    }

    @Alpha
    @ConfigItem(
            keyName = "overlayColor",
            name = "Overlay Color",
            description = "Configures the color of the overlay, including opacity",
            section = colorPicker
    )
    default Color overlayColor() {
        return new Color(0, 0, 0, 180);
    }

    @ConfigItem(
            keyName = "restoreDelay",
            name = "Restore Delay",
            description = "Number of ticks to wait before removing the overlay after stopping skilling",
            section = timing
    )
    default int restoreDelay() {
        return 3;
    }

    // Add this new section after the existing sections
    @ConfigSection(
            name = "Overlay Passthrough",
            description = "Configure which UI elements can pass through the overlay",
            position = 4
    )
    String overlaySettings = "overlaySettings";

    @ConfigSection(
            name = "Miscellaneous",
            description = "Toggle overlay for miscellaneous activities",
            position = 5
    )
    String miscellaneous = "miscellaneous";



    // Add this new config item
    @ConfigItem(
            keyName = "bakePie",
            name = "Bake Pie",
            description = "Display overlay while using Bake Pie spell (Warning: Shares animation with NPC Contact)",
            section = miscellaneous
    )
    default boolean bakePie() {
        return false;
    }

    @ConfigItem(
            keyName = "stringJewelry",
            name = "String Jewelry",
            description = "Display overlay while using String Jewelry spell (Warning: Problematic animation ID)",
            section = miscellaneous
    )
    default boolean stringJewelry() {
        return false;
    }

    @ConfigItem(
            keyName = "plankMake",
            name = "Plank Make",
            description = "Display overlay while using Plank Make spell (Warning: Problematic animation ID)",
            section = miscellaneous
    )
    default boolean plankMake() {
        return false;
    }

    @ConfigItem(
            keyName = "enableInPOH",
            name = "Enable in POH",
            description = "Allow overlay to activate while inside Player-Owned House",
            section = miscellaneous
    )
    default boolean enableInPOH() {
        return false;
    }

    @ConfigItem(
            keyName = "showChat",
            name = "Show Chat",
            description = "Show chat window in front of the overlay when active",
            section = overlaySettings
    )
    default boolean showChat() {
        return false;
    }

    @ConfigItem(
            keyName = "showInventory",
            name = "Show Inventory",
            description = "Show inventory in front of the overlay when active",
            section = overlaySettings
    )
    default boolean showInventory() {
        return false;
    }

    // Combat Settings
    @ConfigItem(
            keyName = "enableCombatBlackout",
            name = "Enable Combat Blackout",
            description = "Enable overlay blackout when fighting specific monsters",
            section = combatIntegration,
            position = 1
    )
    default boolean enableCombatBlackout() {
        return true;
    }

    @ConfigItem(
            keyName = "monsterNames",
            name = "Monster Names",
            description = "Comma-separated list of monster names to trigger blackout (e.g., Zulrah, Vorkath)",
            section = combatIntegration,
            position = 2
    )
    default String monsterNames() {
        return "Gemstone Crab";
    }

    @ConfigItem(
            keyName = "monsterIds",
            name = "Monster IDs",
            description = "Comma-separated list of monster IDs to trigger blackout. Use the Identificator plugin to find NPC IDs (e.g., 2042,2043,2044)",
            section = combatIntegration,
            position = 3
    )
    default String monsterIds() {
        return "";
    }

    @ConfigItem(
            keyName = "combatRestoreDelay",
            name = "Combat Restore Delay",
            description = "Duration in ticks to maintain blackout after combat ends",
            section = combatIntegration,
            position = 4
    )
    default int combatRestoreDelay() {
        return 8;
    }

    @ConfigItem(
            keyName = "enableHealthOverride",
            name = "Enable Health Override",
            description = "Disable blackout when health is below threshold",
            section = combatIntegration,
            position = 50
    )
    default boolean enableHealthOverride() {
        return true;
    }

    @ConfigItem(
            keyName = "healthThreshold",
            name = "Health Threshold",
            description = "Health points below which blackout is disabled",
            section = combatIntegration,
            position = 51
    )
    default int healthThreshold() {
        return 30;
    }

    @ConfigItem(
            keyName = "enablePrayerOverride",
            name = "Enable Prayer Override",
            description = "Disable blackout when prayer is below threshold",
            section = combatIntegration,
            position = 52
    )
    default boolean enablePrayerOverride() {
        return true;
    }

    @ConfigItem(
            keyName = "prayerThreshold",
            name = "Prayer Threshold",
            description = "Prayer points below which blackout is disabled",
            section = combatIntegration,
            position = 53
    )
    default int prayerThreshold() {
        return 20;
    }

    @ConfigItem(
            keyName = "enableWildernessOverride",
            name = "Enable in Wilderness",
            description = "Allow combat blackout to work in the Wilderness",
            section = combatIntegration,
            position = 54
    )
    default boolean enableWildernessOverride() {
        return false;
    }

}
