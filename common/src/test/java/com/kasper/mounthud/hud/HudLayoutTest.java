package com.kasper.mounthud.hud;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HudLayoutTest {
    @Test
    void heartRowsCountAbsorption() {
        assertEquals(1, HudLayout.playerHeartRows(20, 0));
        assertEquals(2, HudLayout.playerHeartRows(20, 4));
        assertEquals(2, HudLayout.playerHeartRows(40, 0));
        assertEquals(3, HudLayout.playerHeartRows(40, 0.5f));
    }

    @Test
    void rowsSqueezeAfterTwo() {
        assertEquals(10, HudLayout.playerHeartRowHeight(1));
        assertEquals(10, HudLayout.playerHeartRowHeight(2));
        assertEquals(9, HudLayout.playerHeartRowHeight(3));
        assertEquals(3, HudLayout.playerHeartRowHeight(20));
    }

    @Test
    void leftRowIsAboveHeartsAndArmor() {
        // 240 high: hearts at 201, armor at 191
        assertEquals(191, HudLayout.leftFreeRow(240, 20, 0, 0));
        assertEquals(181, HudLayout.leftFreeRow(240, 20, 0, 8));
        // Two rows of hearts (golden apple): the second at 191, armor at 181
        assertEquals(171, HudLayout.leftFreeRow(240, 20, 4, 8));
    }

    @Test
    void aboveHungerIsOneRowUp() {
        assertEquals(191, HudLayout.aboveHungerRow(240));
    }
}
