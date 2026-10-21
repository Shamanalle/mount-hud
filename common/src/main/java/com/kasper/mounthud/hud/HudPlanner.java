package com.kasper.mounthud.hud;

import com.kasper.mounthud.config.Settings;

/** Decides which bars the HUD shows while riding, and where; the loaders' hooks only carry it out. */
public final class HudPlanner {
    private HudPlanner() {
    }

    public static HudPlan plan(Settings settings, HudState state) {
        int heartRows = MountHearts.rows(MountHearts.hearts(state.mountMaxHealth));
        HudPlan.Bar vanillaBar = vanillaBar(state);
        boolean riding = heartRows > 0 || state.jumpingMount;
        if (!settings.enabled || !riding) {
            return vanilla(state, heartRows, vanillaBar);
        }

        // Hunger and hearts
        boolean hunger;
        HudPlan.Hearts hearts;
        if (heartRows == 0) {
            hunger = state.survivalBars;
            hearts = HudPlan.Hearts.NONE;
        } else if (!state.survivalBars) {
            // Creative and spectator: vanilla shows the mount's hearts alone, and so do we
            hunger = false;
            hearts = HudPlan.Hearts.VANILLA;
        } else if (settings.hungerWhileRiding) {
            hunger = true;
            hearts = settings.heartsPosition == com.kasper.mounthud.config.HeartsPosition.LEFT
                    ? HudPlan.Hearts.LEFT : HudPlan.Hearts.ABOVE_HUNGER;
        } else {
            hunger = false;
            hearts = HudPlan.Hearts.VANILLA;
        }

        // Experience or jump bar
        HudPlan.Bar bar = vanillaBar;
        if (state.jumpingMount) {
            HudPlan.Bar experience = state.experience ? HudPlan.Bar.EXPERIENCE : HudPlan.Bar.NONE;
            switch (settings.jumpBar) {
                case ALWAYS_JUMP:
                    bar = HudPlan.Bar.JUMP;
                    break;
                case ALWAYS_EXPERIENCE:
                    bar = experience;
                    break;
                case AUTO:
                default:
                    bar = state.jumpCharge > 0 || state.jumpCooldown ? HudPlan.Bar.JUMP : experience;
                    break;
            }
        }
        boolean vanilla = hunger == (heartRows == 0 && state.survivalBars)
                && hearts == (heartRows == 0 ? HudPlan.Hearts.NONE : HudPlan.Hearts.VANILLA)
                && bar == vanillaBar;
        return new HudPlan(vanilla, hunger, hearts, heartRows, bar);
    }

    private static HudPlan vanilla(HudState state, int heartRows, HudPlan.Bar bar) {
        boolean hunger = heartRows == 0 && state.survivalBars;
        HudPlan.Hearts hearts = heartRows == 0 ? HudPlan.Hearts.NONE : HudPlan.Hearts.VANILLA;
        return new HudPlan(true, hunger, hearts, heartRows, bar);
    }

    /** The bar vanilla shows: the jump bar on a jumping mount, else experience in survival. */
    private static HudPlan.Bar vanillaBar(HudState state) {
        if (state.jumpingMount) {
            return HudPlan.Bar.JUMP;
        }
        return state.experience ? HudPlan.Bar.EXPERIENCE : HudPlan.Bar.NONE;
    }
}
