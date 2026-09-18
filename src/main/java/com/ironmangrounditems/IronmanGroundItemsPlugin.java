package com.ironmangrounditems;

import com.google.inject.Provides;
import java.util.*;
import javax.inject.Inject;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.callback.RenderCallback;
import net.runelite.client.callback.RenderCallbackManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(name = "Ironman Ground Items (Local)", description = "Hides ground items an ironman or group ironman cannot pick up", tags = {"ironman", "gim", "ground", "items", "loot", "local"}, enabledByDefault = false)
public class IronmanGroundItemsPlugin extends Plugin
{
    @Inject private Client client;
    @Inject private ClientThread clientThread;
    @Inject private IronmanGroundItemsConfig config;
    @Inject private RenderCallbackManager renderCallbacks;
    /** Layers to skip, keyed on identity: a layer the client replaces drops out and the new one draws. */
    private final Set<ItemLayer> hidden = Collections.newSetFromMap(new IdentityHashMap<>());
    /** Which layer was hidden for each tile, so replacing one leaves nothing behind. */
    private final Map<Tile, ItemLayer> marked = new IdentityHashMap<>();
    private boolean restricted;

    /**
     * Runs for every entity the client adds to the scene, so it stays a type check and a set
     * lookup. Skipping an entity takes its clickbox with it, which is why the menu filtering below
     * only has to cover tiles that are still drawn.
     */
    private final RenderCallback renderCallback = new RenderCallback()
    {
        @Override public boolean addEntity(Renderable renderable, boolean ui)
        { return !(renderable instanceof ItemLayer) || !hidden.contains(renderable); }
    };

    @Provides IronmanGroundItemsConfig provideConfig(ConfigManager configManager)
    { return configManager.getConfig(IronmanGroundItemsConfig.class); }

    @Override protected void startUp()
    {
        renderCallbacks.register(renderCallback);
        clientThread.invoke(this::reload);
    }
    @Override protected void shutDown()
    {
        renderCallbacks.unregister(renderCallback);
        clientThread.invoke(() -> { hidden.clear(); marked.clear(); restricted = false; });
    }

    @Subscribe public void onGameStateChanged(GameStateChanged event)
    {
        GameState state = event.getGameState();
        if (state == GameState.LOGGED_IN) reload();
        // The scene is about to be rebuilt and every Tile with it.
        else if (state == GameState.LOADING || state == GameState.HOPPING || state == GameState.LOGIN_SCREEN)
        { hidden.clear(); marked.clear(); }
    }
    @Subscribe public void onVarbitChanged(VarbitChanged event)
    {
        if (event.getVarbitId() == VarbitID.IRONMAN) reload();
    }
    @Subscribe public void onConfigChanged(ConfigChanged event)
    {
        if (IronmanGroundItemsConfig.GROUP.equals(event.getGroup())) reload();
    }
    @Subscribe public void onItemSpawned(ItemSpawned event)
    {
        refresh(event.getTile(), null);
    }
    @Subscribe public void onItemDespawned(ItemDespawned event)
    {
        // Whether the tile still lists the item at this point is not worth relying on.
        refresh(event.getTile(), event.getItem());
    }
    @Subscribe public void onMenuEntryAdded(MenuEntryAdded event)
    {
        if (!restricted || !config.hideMenuEntries()) return;
        MenuEntry entry = event.getMenuEntry();
        MenuAction type = entry.getType();
        if (!isGroundItemAction(type)) return;
        if (type == MenuAction.EXAMINE_ITEM_GROUND && !config.hideExamine()) return;
        if (isBlocked(entry.getParam0(), entry.getParam1(), entry.getIdentifier())) client.getMenu().removeMenuEntry(entry);
    }

    private void reload()
    {
        int accountType = client.getGameState() == GameState.LOGGED_IN ? client.getVarbitValue(VarbitID.IRONMAN) : Takeable.NORMAL_ACCOUNT;
        restricted = Takeable.isRestricted(accountType) || config.testOnNormalAccount();
        hidden.clear(); marked.clear();
        if (!restricted || !config.hideModels()) return;
        WorldView view = client.getTopLevelWorldView(); if (view == null) return;
        Scene scene = view.getScene(); if (scene == null) return;
        for (Tile[][] plane : scene.getTiles())
            for (Tile[] column : plane)
                for (Tile tile : column)
                    if (tile != null && tile.getItemLayer() != null) refresh(tile, null);
    }

    private void refresh(Tile tile, TileItem leaving)
    {
        if (tile == null) return;
        ItemLayer previous = marked.remove(tile);
        if (previous != null) hidden.remove(previous);
        ItemLayer layer = tile.getItemLayer();
        if (layer == null || !shouldHide(tile, leaving)) return;
        hidden.add(layer); marked.put(tile, layer);
    }

    /**
     * The client draws a tile's items as one object, so a mixed tile cannot be half hidden. Leaving
     * it drawn is the safe answer: hiding it would take the player's own drop with it.
     */
    private boolean shouldHide(Tile tile, TileItem leaving)
    {
        if (!restricted || !config.hideModels()) return false;
        List<TileItem> items = tile.getGroundItems(); if (items == null) return false;
        boolean keepMixed = config.mixedTiles() == MixedTileMode.SHOW, groupTakeable = config.groupDropsTakeable(), blocked = false;
        for (int i = 0; i < items.size(); i++)
        {
            TileItem item = items.get(i);
            if (item == leaving) continue;
            if (Takeable.canTake(true, item.getOwnership(), groupTakeable)) { if (keepMixed) return false; }
            else blocked = true;
        }
        return blocked;
    }

    /**
     * A menu entry carries the item ID but not the stack, so a tile holding two stacks of one ID
     * under different owners cannot be told apart. The entry is kept in that case.
     */
    private boolean isBlocked(int sceneX, int sceneY, int itemId)
    {
        WorldView view = client.getTopLevelWorldView(); if (view == null) return false;
        Scene scene = view.getScene(); if (scene == null) return false;
        Tile[][][] tiles = scene.getTiles();
        int plane = view.getPlane();
        if (plane < 0 || plane >= tiles.length || sceneX < 0 || sceneX >= tiles[plane].length || sceneY < 0 || sceneY >= tiles[plane][sceneX].length) return false;
        Tile tile = tiles[plane][sceneX][sceneY]; if (tile == null) return false;
        List<TileItem> items = tile.getGroundItems(); if (items == null) return false;
        boolean groupTakeable = config.groupDropsTakeable(), blocked = false;
        for (int i = 0; i < items.size(); i++)
        {
            TileItem item = items.get(i);
            if (item.getId() != itemId) continue;
            if (Takeable.canTake(true, item.getOwnership(), groupTakeable)) return false;
            blocked = true;
        }
        return blocked;
    }

    private static boolean isGroundItemAction(MenuAction action)
    {
        switch (action)
        {
            case GROUND_ITEM_FIRST_OPTION:
            case GROUND_ITEM_SECOND_OPTION:
            case GROUND_ITEM_THIRD_OPTION:
            case GROUND_ITEM_FOURTH_OPTION:
            case GROUND_ITEM_FIFTH_OPTION:
            case WIDGET_TARGET_ON_GROUND_ITEM:
            case EXAMINE_ITEM_GROUND:
                return true;
            default:
                return false;
        }
    }
}
