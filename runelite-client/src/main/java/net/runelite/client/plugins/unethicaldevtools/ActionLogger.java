package net.runelite.client.plugins.unethicaldevtools;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
public class ActionLogger
{
	private final UnethicalDevToolsConfig config;
	private final Client client;

	@Inject
	ActionLogger(UnethicalDevToolsConfig config, Client client)
	{
		this.config = config;
		this.client = client;
	}

	    private void log(String action, String details)
    {
        if (!config.actionLogger())
        {
            return;
        }

        log.info("[Action Logger] {}: {}", action, details);
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        if (!config.actionLogger())
        {
            return;
        }

                                        LocalPoint localPoint = LocalPoint.fromScene(event.getMenuEntry().getParam0(), event.getMenuEntry().getParam1());
        WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, localPoint);

        String action = "Unknown";
        String details = "";

        MenuAction menuAction = event.getMenuAction();
        String menuTarget = event.getMenuTarget();
        int id = event.getId();

        switch (menuAction)
        {
            case NPC_FIRST_OPTION:
            case NPC_SECOND_OPTION:
            case NPC_THIRD_OPTION:
            case NPC_FOURTH_OPTION:
            case NPC_FIFTH_OPTION:
                action = "NPC Interaction";
                details = String.format("NPC: %s, ID: %d, Location: %s", menuTarget, id, worldPoint);
                break;
            case WIDGET_TARGET_ON_GAME_OBJECT:
            case GAME_OBJECT_FIRST_OPTION:
            case GAME_OBJECT_SECOND_OPTION:
            case GAME_OBJECT_THIRD_OPTION:
            case GAME_OBJECT_FOURTH_OPTION:
            case GAME_OBJECT_FIFTH_OPTION:
                action = "Object Interaction";
                details = String.format("Object: %s, ID: %d, Location: %s", menuTarget, id, worldPoint);
                break;
            case ITEM_FIFTH_OPTION:
                if ("Drop".equals(event.getMenuOption()))
                {
                    action = "Item Drop";
                    details = String.format("Item: %s, ID: %d", menuTarget, id);
                    break;
                }
            case GROUND_ITEM_FIRST_OPTION:
            case GROUND_ITEM_SECOND_OPTION:
            case GROUND_ITEM_THIRD_OPTION:
            case GROUND_ITEM_FOURTH_OPTION:
                action = "Item Pickup";
                details = String.format("Item: %s, ID: %d, Location: %s", menuTarget, id, worldPoint);
                break;
            case CC_OP:
                if (event.getMenuOption().toLowerCase().contains("prayer"))
                {
                    action = "Prayer";
                    details = String.format("Prayer: %s", menuTarget);
                }
                break;
        }

        if (!"Unknown".equals(action))
        {
            log(action, details);
        }
        else
        {
            details = String.format("MenuOption: %s, Target: %s, ID: %d, MenuAction: %s, WorldPoint: %s",
                    event.getMenuOption(), menuTarget, id, menuAction, worldPoint);
            log("Menu Click", details);
        }
    }
}
