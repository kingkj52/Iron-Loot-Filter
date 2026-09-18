package com.ironlootfilter;

import com.google.inject.Provides;
import java.util.List;
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

@PluginDescriptor(name = "Iron Loot Filter", description = "Hides ground loot your account cannot pick up. Covers every ironman type; group ironmen keep seeing their group's drops", tags = {"ironman", "iron", "uim", "hcim", "gim", "group", "ground", "items", "loot", "hide", "clutter"})
public class IronLootFilterPlugin extends Plugin
{
    private static final int ABSENT = 0, TAKEABLE = 1, BLOCKED = 2;

    @Inject private Client client;
    @Inject private ClientThread clientThread;
    @Inject private IronLootFilterConfig config;
    @Inject private RenderCallbackManager renderCallbacks;
    /** Read from the render path, replaced wholesale on the client thread. */
    private volatile Rules rules = Rules.INACTIVE;
    private volatile boolean stale = true;

    /**
     * An ItemLayer is a TileObject rather than a Renderable, so it arrives through drawObject and
     * never through addEntity. It carries the items it is about to draw, so the decision is made
     * here from live state: a setting change or a pile changing hands shows up on the next frame
     * with nothing to recalculate or invalidate.
     */
    private final RenderCallback renderCallback = new RenderCallback()
    {
        @Override public boolean drawObject(Scene scene, TileObject object)
        { return !(object instanceof ItemLayer) || drawLayer((ItemLayer) object); }
    };

    @Provides IronLootFilterConfig provideConfig(ConfigManager configManager)
    { return configManager.getConfig(IronLootFilterConfig.class); }

    @Override protected void startUp()
    {
        stale = true;
        renderCallbacks.register(renderCallback);
        clientThread.invoke(this::apply);
    }
    @Override protected void shutDown()
    {
        renderCallbacks.unregister(renderCallback);
        rules = Rules.INACTIVE;
    }

    // Settings arrive on the Swing thread and a slider or a spammed checkbox can post several in a
    // row, so the change is only flagged here and picked up once on the next client tick.
    @Subscribe public void onConfigChanged(ConfigChanged event)
    {
        if (IronLootFilterConfig.GROUP.equals(event.getGroup())) stale = true;
    }
    @Subscribe public void onVarbitChanged(VarbitChanged event)
    {
        if (event.getVarbitId() == VarbitID.IRONMAN) stale = true;
    }
    @Subscribe public void onGameStateChanged(GameStateChanged event)
    {
        stale = true;
    }
    @Subscribe public void onClientTick(ClientTick event)
    {
        if (stale) apply();
    }
    @Subscribe public void onMenuEntryAdded(MenuEntryAdded event)
    {
        Rules current = rules;
        if (!current.active || !current.hideMenuEntries) return;
        MenuEntry entry = event.getMenuEntry();
        MenuAction type = entry.getType();
        if (!isGroundItemAction(type)) return;
        if (type == MenuAction.EXAMINE_ITEM_GROUND && !current.hideExamine) return;
        if (isBlocked(entry.getParam0(), entry.getParam1(), entry.getIdentifier(), current)) client.getMenu().removeMenuEntry(entry);
    }

    /** Client thread only. */
    private void apply()
    {
        stale = false;
        int accountType = client.getGameState() == GameState.LOGGED_IN ? client.getVarbitValue(VarbitID.IRONMAN) : Takeable.NORMAL_ACCOUNT;
        boolean active = Takeable.isRestricted(accountType) || config.testOnNormalAccount();
        rules = active ? new Rules(true, config.hideModels(), config.hideMenuEntries(), config.hideExamine(), config.groupDropsTakeable()) : Rules.INACTIVE;
    }

    /**
     * A pile draws at most three items, and those are the only ones on screen to hide. A tile
     * holding anything still takeable is left alone; once that item goes, the next frame hides
     * what remains.
     */
    private boolean drawLayer(ItemLayer layer)
    {
        Rules current = rules;
        if (!current.active || !current.hideModels) return true;
        int bottom = verdict(layer.getBottom(), current), middle = verdict(layer.getMiddle(), current), top = verdict(layer.getTop(), current);
        if (bottom == TAKEABLE || middle == TAKEABLE || top == TAKEABLE) return true;
        return bottom != BLOCKED && middle != BLOCKED && top != BLOCKED;
    }

    private static int verdict(Renderable renderable, Rules rules)
    {
        if (!(renderable instanceof TileItem)) return ABSENT;
        return Takeable.canTake(true, ((TileItem) renderable).getOwnership(), rules.groupDropsTakeable) ? TAKEABLE : BLOCKED;
    }

    /**
     * A menu entry carries the item ID but not the stack, so a tile holding two stacks of one ID
     * under different owners cannot be told apart. The entry is kept in that case.
     */
    private boolean isBlocked(int sceneX, int sceneY, int itemId, Rules current)
    {
        WorldView view = client.getTopLevelWorldView(); if (view == null) return false;
        Scene scene = view.getScene(); if (scene == null) return false;
        Tile[][][] tiles = scene.getTiles();
        int plane = view.getPlane();
        if (plane < 0 || plane >= tiles.length || sceneX < 0 || sceneX >= tiles[plane].length || sceneY < 0 || sceneY >= tiles[plane][sceneX].length) return false;
        Tile tile = tiles[plane][sceneX][sceneY]; if (tile == null) return false;
        List<TileItem> items = tile.getGroundItems(); if (items == null) return false;
        boolean blocked = false;
        for (int i = 0; i < items.size(); i++)
        {
            TileItem item = items.get(i);
            if (item.getId() != itemId) continue;
            if (Takeable.canTake(true, item.getOwnership(), current.groupDropsTakeable)) return false;
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

    /** One snapshot so the render path reads a consistent set rather than five separate fields. */
    private static final class Rules
    {
        static final Rules INACTIVE = new Rules(false, false, false, false, false);
        final boolean active, hideModels, hideMenuEntries, hideExamine, groupDropsTakeable;
        Rules(boolean active, boolean hideModels, boolean hideMenuEntries, boolean hideExamine, boolean groupDropsTakeable)
        { this.active = active; this.hideModels = hideModels; this.hideMenuEntries = hideMenuEntries; this.hideExamine = hideExamine; this.groupDropsTakeable = groupDropsTakeable; }
    }
}
