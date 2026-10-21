package com.kasper.mounthud.hud;

import org.junit.jupiter.api.Test;

import static com.kasper.mounthud.hud.MountHearts.Heart.EMPTY;
import static com.kasper.mounthud.hud.MountHearts.Heart.FULL;
import static com.kasper.mounthud.hud.MountHearts.Heart.HALF;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MountHeartsTest {
    @Test
    void heartsAreHalfTheHealthRoundedAndAtMostThirty() {
        assertEquals(0, MountHearts.hearts(0));
        assertEquals(5, MountHearts.hearts(10));    // pig
        assertEquals(10, MountHearts.hearts(20));   // strider
        assertEquals(16, MountHearts.hearts(32));   // camel
        assertEquals(15, MountHearts.hearts(30));   // horse with 30 health
        assertEquals(15, MountHearts.hearts(29.5f)); // rounded
        assertEquals(30, MountHearts.hearts(100));  // capped as in vanilla
    }

    @Test
    void rowsHoldTenHearts() {
        assertEquals(0, MountHearts.rows(0));
        assertEquals(1, MountHearts.rows(5));
        assertEquals(1, MountHearts.rows(10));
        assertEquals(2, MountHearts.rows(11));
        assertEquals(3, MountHearts.rows(30));
    }

    @Test
    void heartStateFollowsHealthRoundedUp() {
        assertEquals(FULL, MountHearts.heart(0, 2));
        assertEquals(HALF, MountHearts.heart(1, 3));
        assertEquals(EMPTY, MountHearts.heart(2, 3));
        assertEquals(HALF, MountHearts.heart(0, 0.2f));
        assertEquals(FULL, MountHearts.heart(14, 30));
        assertEquals(EMPTY, MountHearts.heart(0, 0));
    }
}
