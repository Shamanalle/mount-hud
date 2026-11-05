package com.kasper.mounthud.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsFileTest {
    @TempDir
    Path dir;

    @Test
    void missingFileGivesDefaults() {
        assertEquals(new Settings(), new SettingsFile(dir.resolve("none.properties")).load());
    }

    @Test
    void savedSettingsLoadBack() throws Exception {
        SettingsFile file = new SettingsFile(dir.resolve("sub/mounthud.properties"));
        Settings s = new Settings();
        s.enabled = false;
        s.hungerWhileRiding = false;
        s.heartsPosition = HeartsPosition.LEFT;
        s.jumpBar = JumpBarMode.ALWAYS_EXPERIENCE;
        s.mountStats = MountStatsMode.HUD_AND_INVENTORY;
        file.save(s);
        assertEquals(s, file.load());
        String text = new String(Files.readAllBytes(file.path()), StandardCharsets.UTF_8);
        assertTrue(text.contains("jump_bar=always_experience"), text);
        assertTrue(text.contains("mount_stats=hud_and_inventory"), text);
        assertTrue(text.contains("# Where the mount's hearts go"), text);
    }

    @Test
    void brokenValuesFallBackOneByOne() throws Exception {
        Path path = dir.resolve("mounthud.properties");
        Files.write(path, "enabled=maybe\nhearts_position=Left\njump_bar=sideways\nmount_stats=everywhere\nhunger_while_riding=off\n".getBytes(StandardCharsets.UTF_8));
        Settings s = new SettingsFile(path).load();
        assertTrue(s.enabled);
        assertEquals(HeartsPosition.LEFT, s.heartsPosition);
        assertEquals(JumpBarMode.AUTO, s.jumpBar);
        assertEquals(MountStatsMode.INVENTORY, s.mountStats);
        assertEquals(false, s.hungerWhileRiding);
    }
}
