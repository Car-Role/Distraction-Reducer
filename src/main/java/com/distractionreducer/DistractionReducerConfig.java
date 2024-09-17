package net.runelite.client.plugins.distractionreducer;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.Color;

@ConfigGroup("distractionreducer")
public interface DistractionReducerConfig extends Config {
    @Alpha
    @ConfigItem(
            keyName = "overlayColor",
            name = "Overlay Color",
            description = "Configures the color of the overlay, including opacity"
    )
    default Color overlayColor() {
        return new Color(0, 0, 0, 200); // Default to black with some transparency
    }

    @ConfigItem(
            keyName = "coverEntireWindow",
            name = "Cover Entire Window",
            description = "If checked, the overlay will cover the entire RuneLite window"
    )
    default boolean coverEntireWindow() {
        return true;
    }
}