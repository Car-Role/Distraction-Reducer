package net.runelite.client.plugins.distractionreducer;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import java.awt.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class DistractionReducerOverlay extends Overlay {
    private final DistractionReducerPlugin plugin;
    private final Client client;
    private boolean visible = false;

    @Inject
    DistractionReducerOverlay(DistractionReducerPlugin plugin, Client client) {
        this.plugin = plugin;
        this.client = client;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGHEST);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!visible || client == null) {
            return null;
        }

        try {
            // Get the size of the client canvas
            final int width = client.getCanvasWidth();
            final int height = client.getCanvasHeight();

            // Create a rectangle covering the entire client canvas
            Rectangle clientRect = new Rectangle(0, 0, width, height);

            // Use the color from the config, which now includes opacity
            graphics.setColor(plugin.getConfig().overlayColor());
            graphics.fill(clientRect);
            log.debug("Rendering overlay with color: {}", plugin.getConfig().overlayColor());
        } catch (Exception e) {
            log.error("Error rendering overlay", e);
        }

        return null;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        log.debug("Overlay visibility set to: {}", visible);
    }
}