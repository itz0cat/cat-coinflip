package com.itz0cat.catcoinflip.command;

import com.itz0cat.catcoinflip.compat.PlatformCompat;
import com.itz0cat.catcoinflip.profile.Profile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class QuickCreateHandlerTest {

    private QuickCreateHandler handler;
    private Profile profile;
    private TestCompat compat;

    static class TestCompat implements PlatformCompat {
        final List<String> messages = new ArrayList<>();
        @Override public void sendClientChatMessage(String message) { messages.add(message); }
        @Override public String getCurrentServerAddress() { return "play.bananasmp.net"; }
        @Override public Path getConfigDirectory() { return Path.of("build/tmp/test-config"); }
        @Override public void openConfigScreen(Object parentScreen) {}
        @Override public String getMinecraftVersion() { return "1.21.11"; }
    }

    @BeforeEach
    void setUp() {
        handler = new QuickCreateHandler();
        profile = new Profile("BananaSMP");
        profile.setMinBet(100);
        profile.setQuickCreate(true);
        profile.setEchoExpansion(true);
        profile.setConfirmAbove(0); // Off by default
        compat = new TestCompat();
    }

    @Test
    @DisplayName("/cf 28m rewrites to /cf create 28000000")
    void testQuickCreateExpansion() {
        CommandRewriteResult result = handler.processCommand("/cf 28m", profile, compat);
        assertEquals(CommandRewriteResult.Action.REWRITTEN, result.getAction());
        assertEquals("/cf create 28000000", result.getCommand());
        assertTrue(compat.messages.stream().anyMatch(m -> m.contains("-> /cf create 28000000")));
    }

    @Test
    @DisplayName("/cf 1.5b rewrites to /cf create 1500000000")
    void testBillionExpansion() {
        CommandRewriteResult result = handler.processCommand("/cf 1.5b", profile, compat);
        assertEquals(CommandRewriteResult.Action.REWRITTEN, result.getAction());
        assertEquals("/cf create 1500000000", result.getCommand());
    }

    @Test
    @DisplayName("/cf create 2.5m rewrites inside normal command to 2500000")
    void testExplicitCreateExpansion() {
        CommandRewriteResult result = handler.processCommand("/cf create 2.5m", profile, compat);
        assertEquals(CommandRewriteResult.Action.REWRITTEN, result.getAction());
        assertEquals("/cf create 2500000", result.getCommand());
    }

    @Test
    @DisplayName("/cf 28m shards preserves extra argument")
    void testPassThroughExtraArgs() {
        CommandRewriteResult result = handler.processCommand("/cf 28m shards", profile, compat);
        assertEquals(CommandRewriteResult.Action.REWRITTEN, result.getAction());
        assertEquals("/cf create 28000000 shards", result.getCommand());
    }

    @Test
    @DisplayName("Subcommands like /cf, /cf delete, /cf toggle, /cf help remain unmodified")
    void testSubcommandsUnmodified() {
        assertEquals(CommandRewriteResult.Action.UNMODIFIED, handler.processCommand("/cf", profile, compat).getAction());
        assertEquals(CommandRewriteResult.Action.UNMODIFIED, handler.processCommand("/cf delete", profile, compat).getAction());
        assertEquals(CommandRewriteResult.Action.UNMODIFIED, handler.processCommand("/cf toggle", profile, compat).getAction());
        assertEquals(CommandRewriteResult.Action.UNMODIFIED, handler.processCommand("/cf help", profile, compat).getAction());
    }

    @Test
    @DisplayName("Large confirmation threshold requires second confirmation typing")
    void testConfirmationSafety() {
        profile.setConfirmAbove(1_000_000_000L); // 1B confirmation safety

        // First attempt should be blocked with prompt to retype
        CommandRewriteResult first = handler.processCommand("/cf 2b", profile, compat);
        assertEquals(CommandRewriteResult.Action.BLOCKED, first.getAction());
        assertTrue(compat.messages.stream().anyMatch(m -> m.contains("Large bet warning")));

        // Second attempt with exact same command within 10s should succeed
        CommandRewriteResult second = handler.processCommand("/cf 2b", profile, compat);
        assertEquals(CommandRewriteResult.Action.REWRITTEN, second.getAction());
        assertEquals("/cf create 2000000000", second.getCommand());
    }
}
