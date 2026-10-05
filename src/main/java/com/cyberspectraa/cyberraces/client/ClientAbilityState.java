package com.cyberspectraa.cyberraces.client;

public final class ClientAbilityState {
    private static boolean fairyHoverEnabled;

    private ClientAbilityState() {
    }

    public static boolean isFairyHoverEnabled() {
        return fairyHoverEnabled;
    }

    public static boolean toggleFairyHover() {
        fairyHoverEnabled = !fairyHoverEnabled;
        return fairyHoverEnabled;
    }

    public static void disableFairyHover() {
        fairyHoverEnabled = false;
    }
}
