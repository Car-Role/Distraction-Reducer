package net.runelite.client.plugins.distractionreducer;

import com.google.inject.Provides;

import javax.inject.Inject;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
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
        description = "Blacks out the screen while woodcutting to reduce distractions",
        tags = {"woodcutting", "skilling", "overlay"}
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

    private static final Set<Integer> WOODCUTTING_ANIMATION_IDS = Set.of(
            AnimationID.WOODCUTTING_BRONZE,
            AnimationID.WOODCUTTING_IRON,
            AnimationID.WOODCUTTING_STEEL,
            AnimationID.WOODCUTTING_BLACK,
            AnimationID.WOODCUTTING_MITHRIL,
            AnimationID.WOODCUTTING_ADAMANT,
            AnimationID.WOODCUTTING_RUNE,
            AnimationID.WOODCUTTING_DRAGON,
            AnimationID.WOODCUTTING_INFERNAL,
            AnimationID.WOODCUTTING_3A_AXE,
            AnimationID.WOODCUTTING_CRYSTAL,
            AnimationID.WOODCUTTING_TRAILBLAZER
    );

    private static final Set<Integer> TREE_OBJECT_IDS = Set.of(
            ObjectID.TREE,
            ObjectID.TREE_1277,
            ObjectID.TREE_1278,
            ObjectID.TREE_1279,
            ObjectID.TREE_1280,
            ObjectID.OAK_TREE,
            ObjectID.WILLOW_TREE,
            ObjectID.MAPLE_TREE,
            ObjectID.YEW_TREE,
            ObjectID.MAGIC_TREE
            // Add more tree object IDs as needed
    );

    @Provides
    DistractionReducerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(DistractionReducerConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        log.info("Distraction Reducer plugin started!");
        overlayManager.add(distractionReducerOverlay);
    }

    @Override
    protected void shutDown() throws Exception {
        log.info("Distraction Reducer plugin stopped!");
        overlayManager.remove(distractionReducerOverlay);
        distractionReducerOverlay.setVisible(false);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
            clientThread.invoke(this::updateOverlayVisibility);
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        clientThread.invoke(this::updateOverlayVisibility);
    }

    private void updateOverlayVisibility() {
        boolean isWoodcutting = isWoodcutting();
        log.debug("Is woodcutting: {}", isWoodcutting);
        distractionReducerOverlay.setVisible(isWoodcutting);
    }

    private boolean isWoodcutting() {
        Player player = client.getLocalPlayer();
        if (player == null) {
            return false;
        }

        int animation = player.getAnimation();
        log.debug("Current animation ID: {}", animation);

        if (WOODCUTTING_ANIMATION_IDS.contains(animation)) {
            log.debug("Woodcutting animation detected!");
            return true;
        }

        WorldPoint playerLocation = player.getWorldLocation();
        Scene scene = client.getScene();
        Tile[][][] tiles = scene.getTiles();

        for (int z = 0; z < Constants.MAX_Z; z++) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    int worldX = playerLocation.getX() + x;
                    int worldY = playerLocation.getY() + y;

                    if (worldX < 0 || worldY < 0 || worldX >= Constants.SCENE_SIZE || worldY >= Constants.SCENE_SIZE) {
                        continue;
                    }

                    Tile tile = tiles[z][worldX][worldY];
                    if (tile == null) {
                        continue;
                    }

                    GameObject[] gameObjects = tile.getGameObjects();
                    if (gameObjects == null) {
                        continue;
                    }

                    for (GameObject gameObject : gameObjects) {
                        if (gameObject == null) {
                            continue;
                        }

                        int objectId = gameObject.getId();
                        log.debug("Nearby object ID: {}", objectId);

                        if (TREE_OBJECT_IDS.contains(objectId)) {
                            log.debug("Tree object detected nearby!");
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public DistractionReducerConfig getConfig() {
        return config;
    }
}