package com.kasper.mounthud.hud;

import com.kasper.mounthud.config.HeartsPosition;
import com.kasper.mounthud.config.JumpBarMode;
import com.kasper.mounthud.config.Settings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HudPlannerTest {
    private static HudState horse(float charge) {
        return new HudState().survival(true).mount(30, 30).jumping(charge, false);
    }

    @Test
    void onFootNothingChanges() {
        HudPlan plan = HudPlanner.plan(new Settings(), new HudState().survival(true));
        assertTrue(plan.vanilla);
        assertTrue(plan.hunger);
        assertEquals(HudPlan.Hearts.NONE, plan.hearts);
        assertEquals(HudPlan.Bar.EXPERIENCE, plan.bar);
    }

    @Test
    void horseShowsHungerHeartsAboveAndExperience() {
        HudPlan plan = HudPlanner.plan(new Settings(), horse(0));
        assertFalse(plan.vanilla);
        assertTrue(plan.hunger);
        assertEquals(HudPlan.Hearts.ABOVE_HUNGER, plan.hearts);
        assertEquals(2, plan.heartRows);
        assertEquals(2, plan.airRowsUp);
        assertEquals(HudPlan.Bar.EXPERIENCE, plan.bar);
        assertTrue(plan.experienceLevel());
    }

    @Test
    void chargingAJumpShowsTheJumpBar() {
        assertEquals(HudPlan.Bar.JUMP, HudPlanner.plan(new Settings(), horse(0.3f)).bar);
        HudState camel = new HudState().survival(true).mount(32, 32).jumping(0, true);
        assertEquals(HudPlan.Bar.JUMP, HudPlanner.plan(new Settings(), camel).bar);
    }

    @Test
    void jumpBarModes() {
        Settings s = new Settings();
        s.jumpBar = JumpBarMode.ALWAYS_JUMP;
        assertEquals(HudPlan.Bar.JUMP, HudPlanner.plan(s, horse(0)).bar);
        s.jumpBar = JumpBarMode.ALWAYS_EXPERIENCE;
        assertEquals(HudPlan.Bar.EXPERIENCE, HudPlanner.plan(s, horse(1)).bar);
    }

    @Test
    void pigKeepsItsExperienceBarAndGetsHunger() {
        HudState pig = new HudState().survival(true).mount(10, 10);
        HudPlan plan = HudPlanner.plan(new Settings(), pig);
        assertTrue(plan.hunger);
        assertEquals(HudPlan.Hearts.ABOVE_HUNGER, plan.hearts);
        assertEquals(1, plan.heartRows);
        assertEquals(HudPlan.Bar.EXPERIENCE, plan.bar);
    }

    @Test
    void heartsOnTheLeftLeaveTheAirWhereItIs() {
        Settings s = new Settings();
        s.heartsPosition = HeartsPosition.LEFT;
        HudPlan plan = HudPlanner.plan(s, horse(0));
        assertEquals(HudPlan.Hearts.LEFT, plan.hearts);
        assertEquals(0, plan.airRowsUp);
    }

    @Test
    void creativeShowsTheMountsHeartsAloneAndTheJumpBarOnlyWhileCharging() {
        HudState creative = new HudState().survival(false).mount(30, 30).jumping(0, false);
        HudPlan plan = HudPlanner.plan(new Settings(), creative);
        assertFalse(plan.hunger);
        assertEquals(HudPlan.Hearts.VANILLA, plan.hearts);
        assertEquals(HudPlan.Bar.NONE, plan.bar);
        creative.jumpCharge = 0.5f;
        assertEquals(HudPlan.Bar.JUMP, HudPlanner.plan(new Settings(), creative).bar);
    }

    @Test
    void hungerSwitchedOffGivesTheVanillaHearts() {
        Settings s = new Settings();
        s.hungerWhileRiding = false;
        s.jumpBar = JumpBarMode.ALWAYS_JUMP;
        HudPlan plan = HudPlanner.plan(s, horse(0));
        assertTrue(plan.vanilla);
        assertFalse(plan.hunger);
        assertEquals(HudPlan.Hearts.VANILLA, plan.hearts);
        assertEquals(1, plan.airRowsUp);
    }

    @Test
    void switchedOffIsVanilla() {
        Settings s = new Settings();
        s.enabled = false;
        HudPlan plan = HudPlanner.plan(s, horse(0));
        assertTrue(plan.vanilla);
        assertFalse(plan.hunger);
        assertEquals(HudPlan.Hearts.VANILLA, plan.hearts);
        assertEquals(HudPlan.Bar.JUMP, plan.bar);
    }
}
