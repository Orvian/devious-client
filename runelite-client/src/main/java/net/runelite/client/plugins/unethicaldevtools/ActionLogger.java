package net.runelite.client.plugins.unethicaldevtools;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.Projectile;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.api.Prayer;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;

import java.util.HashSet;
import java.util.EnumSet;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
public class ActionLogger {
    private final Set<NPC> loggedNpcs = new HashSet<>();
    private final Set<Projectile> loggedProjectiles = new HashSet<>();
    private Set<Prayer> lastActivePrayers = EnumSet.noneOf(Prayer.class);
    private final Map<Prayer, Integer> prayerLastLogTick = new EnumMap<>(Prayer.class);
    private final UnethicalDevToolsConfig config;
    private final Client client;

    @Inject
    ActionLogger(UnethicalDevToolsConfig config, Client client) {
        this.config = config;
        this.client = client;
    }

    private void log(String action, String details) {
        log.info("[Action Logger] {}: {}", action, details);
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (!config.actionLogger()) {
            return;
        }

        MenuAction menuAction = event.getMenuAction();
        String menuTarget = event.getMenuTarget();
        int id = event.getId();
        String action = "Unknown";
        String details = "";

        switch (menuAction) {
            case WIDGET_TARGET_ON_NPC:
            case NPC_FIRST_OPTION:
            case NPC_SECOND_OPTION:
            case NPC_THIRD_OPTION:
            case NPC_FOURTH_OPTION:
            case NPC_FIFTH_OPTION: {
                action = "NPC Interaction";
                LocalPoint localPoint = LocalPoint.fromScene(event.getMenuEntry().getParam0(),
                        event.getMenuEntry().getParam1());
                WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, localPoint);
                details = String.format("NPC: %s, ID: %d, Location: %s", menuTarget, id, worldPoint);
                break;
            }
            case WIDGET_TARGET_ON_GAME_OBJECT:
            case GAME_OBJECT_FIRST_OPTION:
            case GAME_OBJECT_SECOND_OPTION:
            case GAME_OBJECT_THIRD_OPTION:
            case GAME_OBJECT_FOURTH_OPTION:
            case GAME_OBJECT_FIFTH_OPTION: {
                action = "Object Interaction";
                LocalPoint localPoint = LocalPoint.fromScene(event.getMenuEntry().getParam0(),
                        event.getMenuEntry().getParam1());
                WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, localPoint);
                details = String.format("Object: %s, ID: %d, Location: %s", menuTarget, id, worldPoint);
                break;
            }
            case WIDGET_TARGET_ON_GROUND_ITEM:
            case GROUND_ITEM_FIRST_OPTION:
            case GROUND_ITEM_SECOND_OPTION:
            case GROUND_ITEM_THIRD_OPTION:
            case GROUND_ITEM_FOURTH_OPTION:
            case GROUND_ITEM_FIFTH_OPTION: {
                action = "Item Pickup";
                LocalPoint localPoint = LocalPoint.fromScene(event.getMenuEntry().getParam0(),
                        event.getMenuEntry().getParam1());
                WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, localPoint);
                details = String.format("Item: %s, ID: %d, Location: %s", menuTarget, id, worldPoint);
                break;
            }
            case PLAYER_FIRST_OPTION:
            case PLAYER_SECOND_OPTION:
            case PLAYER_THIRD_OPTION:
            case PLAYER_FOURTH_OPTION:
            case PLAYER_FIFTH_OPTION:
            case WIDGET_TARGET_ON_PLAYER: {
                action = "Player Interaction";
                details = String.format("Option: %s, Target: %s, ID: %d", event.getMenuOption(), menuTarget, id);
                break;
            }
            case ITEM_FIRST_OPTION:
            case ITEM_SECOND_OPTION:
            case ITEM_THIRD_OPTION:
            case ITEM_FOURTH_OPTION: {
                int slot = event.getMenuEntry().getParam0();
                int widgetId = event.getMenuEntry().getParam1();
                int group = WidgetInfo.TO_GROUP(widgetId);
                int child = WidgetInfo.TO_CHILD(widgetId);
                action = "Inventory Item";
                details = String.format(
                        "Option: %s, Item: %s, ID: %d, Slot: %d, WidgetId: %d (group=%d, child=%d)",
                        event.getMenuOption(), menuTarget, id, slot, widgetId, group, child);
                break;
            }
            case ITEM_FIFTH_OPTION: // This is often used for item options in inventory
                if ("Drop".equals(event.getMenuOption())) {
                    action = "Item Drop";
                    details = String.format("Item: %s, ID: %d", menuTarget, id);
                }
                break;
            case WIDGET_TARGET: {
                int slot = event.getMenuEntry().getParam0();
                int widgetId = event.getMenuEntry().getParam1();
                int group = WidgetInfo.TO_GROUP(widgetId);
                int child = WidgetInfo.TO_CHILD(widgetId);
                action = "Widget Target";
                details = String.format(
                        "Option: %s, Target: %s, WidgetId: %d (group=%d, child=%d), Slot: %d",
                        event.getMenuOption(), menuTarget, widgetId, group, child, slot);
                break;
            }
            case CC_OP:
            case CC_OP_LOW_PRIORITY: {
                String optLower = event.getMenuOption().toLowerCase();
                if (optLower.contains("prayer")) {
                    action = "Prayer";
                    details = String.format("Prayer: %s", menuTarget);
                } else {
                    int widgetId = event.getMenuEntry().getParam1();
                    int group = WidgetInfo.TO_GROUP(widgetId);
                    int child = WidgetInfo.TO_CHILD(widgetId);
                    action = "Widget Action";
                    details = String.format(
                            "Option: %s, Target: %s, WidgetId: %d (group=%d, child=%d), Param0: %d",
                            event.getMenuOption(), menuTarget, widgetId, group, child,
                            event.getMenuEntry().getParam0());
                }
                break;
            }
            case WIDGET_CONTINUE: {
                int widgetId = event.getMenuEntry().getParam1();
                int group = WidgetInfo.TO_GROUP(widgetId);
                int child = WidgetInfo.TO_CHILD(widgetId);
                action = "Widget Continue";
                details = String.format("WidgetId: %d (group=%d, child=%d)", widgetId, group, child);
                break;
            }
            case WALK: {
                action = "Walk Here";
                LocalPoint localPoint = LocalPoint.fromScene(event.getMenuEntry().getParam0(),
                        event.getMenuEntry().getParam1());
                if (localPoint == null) {
                    // Fallback (handles minimap clicks etc.)
                    localPoint = client.getLocalDestinationLocation();
                }
                WorldPoint worldPoint = localPoint != null ? WorldPoint.fromLocalInstance(client, localPoint) : null;
                details = String.format("Location: %s", worldPoint);
                break;
            }
            default:
                action = "Menu Click";
                details = String.format("MenuOption: %s, Target: %s, ID: %d, MenuAction: %s",
                        event.getMenuOption(), menuTarget, id, menuAction);
                break;
        }

        if (!"Unknown".equals(action)) {
            log(action, details);
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (!config.actionLogger() || client.getLocalPlayer() == null) {
            return;
        }

        // Prayer state changes (debounced once per tick, with 3-tick suppression)
        final int tick = client.getTickCount();
        Set<Prayer> now = EnumSet.noneOf(Prayer.class);
        for (Prayer p : Prayer.values()) {
            if (client.isPrayerActive(p)) {
                now.add(p);
            }
        }
        // Enabled
        for (Prayer p : now) {
            if (!lastActivePrayers.contains(p)) {
                Integer suppressUntil = prayerLastLogTick.get(p);
                if (suppressUntil == null || tick >= suppressUntil) {
                    log("Prayer Enabled", p.name());
                    prayerLastLogTick.put(p, tick + 3);
                }
            }
        }
        // Disabled
        for (Prayer p : lastActivePrayers) {
            if (!now.contains(p)) {
                Integer suppressUntil = prayerLastLogTick.get(p);
                if (suppressUntil == null || tick >= suppressUntil) {
                    log("Prayer Disabled", p.name());
                    prayerLastLogTick.put(p, tick + 3);
                }
            }
        }
        lastActivePrayers = now;

        for (NPC npc : client.getNpcs()) {
            if (npc != null && !loggedNpcs.contains(npc)) {
                WorldPoint npcLocation = npc.getWorldLocation();
                WorldArea playerArea = new WorldArea(client.getLocalPlayer().getWorldLocation(), 1, 1);
                boolean hasLineOfSight = playerArea.hasLineOfSightTo(client.getTopLevelWorldView(), npcLocation);
                String details = String.format("NPC: %s, ID: %d, Location: %s, Line of Sight: %b",
                        npc.getName(), npc.getId(), npcLocation, hasLineOfSight);
                log("NPC Detected", details);
                loggedNpcs.add(npc);
            }
        }
    }

    @Subscribe
    public void onProjectileMoved(ProjectileMoved event) {
        if (!config.actionLogger() || !config.projectiles()) {
            return;
        }

        Projectile projectile = event.getProjectile();
        if (projectile == null || loggedProjectiles.contains(projectile)) {
            return;
        }
        LocalPoint lp = event.getPosition();
        WorldPoint worldPoint = lp != null ? WorldPoint.fromLocalInstance(client, lp) : null;

        String details = String.format(
                "Projectile ID: %d, Location: %s, StartCycle: %d, EndCycle: %d",
                projectile.getId(),
                worldPoint,
                projectile.getStartCycle(),
                projectile.getEndCycle());

        log("Projectile", details);
        loggedProjectiles.add(projectile);
    }
}
