package net.unethicalite.api.game;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.gameval.SpotanimID;
import net.runelite.api.AnimationID;
import net.unethicalite.client.Static;

public class Interaction {
    public static boolean isFishing() {
        Client client = Static.getClient();
        Player localPlayer = client.getLocalPlayer();

        if (localPlayer == null || localPlayer.getInteracting() == null) {
            return false;
        }

        String interactingName = localPlayer.getInteracting().getName();
        if (interactingName == null || !interactingName.contains("Fishing spot")) {
            return false;
        }

        // Check if it's not the minnow fishing flying fish spot
        if (localPlayer.getInteracting().getGraphic() == SpotanimID.MINNOW_FISHING_FLYINGFISH) {
            return false;
        }

        // Check if player is performing fishing animation
        return Animations.FISHING_ANIMATIONS.contains(localPlayer.getAnimation());
    }

    public static boolean isMining() {
        Client client = Static.getClient();
        Player localPlayer = client.getLocalPlayer();

        if (localPlayer == null) {
            return false;
        }

        int currentAnim = localPlayer.getAnimation();

        // Check if player is performing mining animation
        if (Animations.MINING_ANIMATIONS.contains(currentAnim)) {
            return true;
        }

        // Check for Arceuus chisel essence animation
        if (currentAnim == AnimationID.ARCEUUS_CHISEL_ESSENCE) {
            return true;
        }

        // Check for wall mining animations with timing consideration
        // when receiving ore from a wall the animation sets to -1 before starting up
        // again
        if (Animations.WAll_ANIMATIONS.contains(currentAnim)) {
            return true;
        }

        return false;
    }
}
