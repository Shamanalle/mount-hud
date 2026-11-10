package com.kasper.mounthud.stats;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MountStatsTest {
    @Test
    void nonPositiveAndInvalidInputsGiveZero() {
        assertEquals(0, MountStats.blocksPerSecond(0));
        assertEquals(0, MountStats.blocksPerSecond(-0.2));
        assertEquals(0, MountStats.blocksPerSecond(Double.NaN));
        assertEquals(0, MountStats.jumpHeight(0));
        assertEquals(0, MountStats.jumpHeight(-0.5));
        assertEquals(0, MountStats.jumpHeight(Double.NaN));
        assertEquals(0, MountStats.hearts(0));
        assertEquals(0, MountStats.hearts(-10));
    }

    @Test
    void horseSpeedRangeMatchesBlocksPerSecond() {
        // Vanilla horses range from 0.1125 (slowest) to 0.3375 (fastest), with 0.225 average
        assertEquals(4.856625, MountStats.blocksPerSecond(0.1125), 1e-6);
        assertEquals(9.71325, MountStats.blocksPerSecond(0.225), 1e-6);
        assertEquals(14.569875, MountStats.blocksPerSecond(0.3375), 1e-6);
    }

    @Test
    void horseJumpRangeMatchesVanillaTrajectory() {
        // Vanilla horses range from 0.4 (~1.11 blocks) to 1.0 (~5.30 blocks), donkeys are 0.5 (~1.62 blocks)
        assertEquals(1.109, MountStats.jumpHeight(0.4), 0.005);
        assertEquals(1.625, MountStats.jumpHeight(0.5), 0.005);
        assertEquals(2.893, MountStats.jumpHeight(0.7), 0.005);
        assertEquals(5.300, MountStats.jumpHeight(1.0), 0.005);
        assertTrue(MountStats.jumpHeight(0.8) > MountStats.jumpHeight(0.7));
    }

    @Test
    void formatsHealthSpeedAndJumpForDisplay() {
        MountStats hurtHorse = new MountStats(23f, 30f, 0.225, 0.7);
        assertEquals("23/30", hurtHorse.healthText());
        assertEquals("9.7", hurtHorse.speedText());
        assertEquals("2.9", hurtHorse.jumpText());
        assertTrue(hurtHorse.hasJump());

        MountStats fullPig = new MountStats(10f, 10f, 0.175, 0);
        assertEquals("10", fullPig.healthText());
        assertEquals("7.6", fullPig.speedText());
        assertEquals("0.0", fullPig.jumpText());
        assertFalse(fullPig.hasJump());
        assertEquals(5.0, MountStats.hearts(10), 1e-6);
    }

    @Test
    void normalizesStatRatiosIntoUnitInterval() {
        MountStats hurtHorse = new MountStats(23f, 30f, 0.225, 0.7);
        assertEquals(23.0 / 30.0, hurtHorse.healthRatio(), 1e-6);
        assertEquals(1.0, hurtHorse.maxHealthRatio(), 1e-6);
        assertEquals(0.225 / 0.3375, hurtHorse.speedRatio(), 1e-6);
        assertTrue(hurtHorse.jumpRatio() > 0.5 && hurtHorse.jumpRatio() < 0.6);

        MountStats weakHorse = new MountStats(15f, 15f, 0.1125, 0.4);
        assertEquals(0.5, weakHorse.healthRatio(), 1e-6);
        assertEquals(0.5, weakHorse.maxHealthRatio(), 1e-6);

        MountStats boostedCamel = new MountStats(32f, 32f, 0.5, 1.4);
        assertEquals(1.0, boostedCamel.healthRatio(), 1e-6);
        assertEquals(1.0, boostedCamel.maxHealthRatio(), 1e-6);
        assertEquals(1.0, boostedCamel.speedRatio(), 1e-6);
        assertEquals(1.0, boostedCamel.jumpRatio(), 1e-6);

        MountStats invalid = new MountStats(-5f, Float.NaN, Double.NaN, -1.0);
        assertEquals(0.0, invalid.healthRatio());
        assertEquals(0.0, invalid.maxHealthRatio());
        assertEquals(0.0, invalid.speedRatio());
        assertEquals(0.0, invalid.jumpRatio());
    }
}
