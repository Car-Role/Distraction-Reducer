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
}
