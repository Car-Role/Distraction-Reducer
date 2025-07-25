package com.distractionreducer;

import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import java.awt.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class DistractionReducerOverlay extends Overlay {
    private final DistractionReducerConfig config;
    private final Client client;
    private boolean renderOverlay = false;

    @Inject
    private DistractionReducerOverlay(DistractionReducerConfig config, Client client) {
        this.config = config;
        this.client = client;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGH);
        setLayer(OverlayLayer.ALWAYS_ON_TOP); // Static layer - never changes
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!renderOverlay) {
            return null;
        }

        Color color = config.overlayColor();
        graphics.setColor(color);
        
        // Check if any widgets should be visible through the overlay
        boolean anyWidgetVisible = config.showChat() || config.showInventory();
        
        if (anyWidgetVisible) {
            // Mask out areas for visible widgets
            java.util.List<Rectangle> exclusionAreas = new java.util.ArrayList<>();
            
            if (config.showChat()) {
                Rectangle chatArea = getChatArea();
                if (chatArea != null) {
                    exclusionAreas.add(chatArea);
                }
            }
            
            if (config.showInventory()) {
                Rectangle invArea = getInventoryArea();
                if (invArea != null) {
                    exclusionAreas.add(invArea);
                }
            }
            
            fillScreenExcludingAreas(graphics, exclusionAreas);
        } else {
            // No widgets should be visible, fill entire screen
            graphics.fillRect(0, 0, client.getCanvasWidth(), client.getCanvasHeight());
        }

        return new Dimension(client.getCanvasWidth(), client.getCanvasHeight());
    }

    public void setRenderOverlay(boolean render) {
        this.renderOverlay = render;
        log.debug("Overlay rendering set to: {}", render);
    }

    private Rectangle getChatArea() {
        java.util.List<Rectangle> chatAreas = new java.util.ArrayList<>();
        
        // Get main chatbox parent
        Widget chatboxParent = client.getWidget(WidgetInfo.CHATBOX_PARENT);
        if (chatboxParent != null && !chatboxParent.isHidden()) {
            chatAreas.add(chatboxParent.getBounds());
        }
        
        // Get chatbox messages (the actual text area)
        Widget chatboxMessages = client.getWidget(WidgetInfo.CHATBOX_MESSAGES);
        if (chatboxMessages != null && !chatboxMessages.isHidden()) {
            chatAreas.add(chatboxMessages.getBounds());
        }
        
        // Get chatbox input field
        Widget chatboxInput = client.getWidget(WidgetInfo.CHATBOX_INPUT);
        if (chatboxInput != null && !chatboxInput.isHidden()) {
            chatAreas.add(chatboxInput.getBounds());
        }
        
        // Get chat channel buttons (All, Game, Public, Private, etc.)
        Widget chatboxButtons = client.getWidget(WidgetInfo.CHATBOX_BUTTONS);
        if (chatboxButtons != null && !chatboxButtons.isHidden()) {
            chatAreas.add(chatboxButtons.getBounds());
        }
        
        // Note: Some chat widgets like tabs might not be directly accessible via WidgetInfo
        // We'll rely on the main components with exact bounds (no padding)
        
        if (chatAreas.isEmpty()) {
            return null;
        }
        
        // Combine all chat areas into one bounding rectangle
        Rectangle combinedBounds = new Rectangle(chatAreas.get(0));
        for (int i = 1; i < chatAreas.size(); i++) {
            combinedBounds = combinedBounds.union(chatAreas.get(i));
        }
        
        log.debug("Combined chat area from {} components: {}", chatAreas.size(), combinedBounds);
        return combinedBounds;
    }
    
    private Rectangle getInventoryArea() {
        java.util.List<Rectangle> inventoryAreas = new java.util.ArrayList<>();
        
        // Get inventory widget
        Widget inventory = client.getWidget(WidgetInfo.INVENTORY);
        if (inventory != null && !inventory.isHidden()) {
            inventoryAreas.add(inventory.getBounds());
        }
        
        // Get stats (skills) widget - included with inventory setting
        Widget stats = client.getWidget(WidgetInfo.SKILLS_CONTAINER);
        if (stats != null && !stats.isHidden()) {
            inventoryAreas.add(stats.getBounds());
        }
        
        if (inventoryAreas.isEmpty()) {
            return null;
        }
        
        // If only one area, return it directly
        if (inventoryAreas.size() == 1) {
            Rectangle bounds = inventoryAreas.get(0);
            log.debug("Single inventory/stats area: {}", bounds);
            return bounds;
        }
        
        // Combine inventory and stats areas into one bounding rectangle
        Rectangle combinedBounds = new Rectangle(inventoryAreas.get(0));
        for (int i = 1; i < inventoryAreas.size(); i++) {
            combinedBounds = combinedBounds.union(inventoryAreas.get(i));
        }
        
        log.debug("Combined inventory/stats area from {} components: {}", inventoryAreas.size(), combinedBounds);
        return combinedBounds;
    }
    

    
    private void fillScreenExcludingAreas(Graphics2D graphics, java.util.List<Rectangle> exclusionAreas) {
        int canvasWidth = client.getCanvasWidth();
        int canvasHeight = client.getCanvasHeight();
        
        if (exclusionAreas.isEmpty()) {
            // No exclusions, fill entire screen
            graphics.fillRect(0, 0, canvasWidth, canvasHeight);
            return;
        }
        
        // Create a complex shape that covers the screen but excludes widget areas
        java.awt.geom.Area screenArea = new java.awt.geom.Area(new Rectangle(0, 0, canvasWidth, canvasHeight));
        
        for (Rectangle exclusion : exclusionAreas) {
            // Make sure exclusion area is within screen bounds
            Rectangle clampedExclusion = exclusion.intersection(new Rectangle(0, 0, canvasWidth, canvasHeight));
            if (!clampedExclusion.isEmpty()) {
                screenArea.subtract(new java.awt.geom.Area(clampedExclusion));
            }
        }
        
        graphics.fill(screenArea);
    }
}