package com.itz0cat.catcoinflip.command;

public class CommandRewriteResult {

    public enum Action {
        UNMODIFIED,
        REWRITTEN,
        BLOCKED
    }

    private final Action action;
    private final String command;

    private CommandRewriteResult(Action action, String command) {
        this.action = action;
        this.command = command;
    }

    public static CommandRewriteResult unmodified(String original) {
        return new CommandRewriteResult(Action.UNMODIFIED, original);
    }

    public static CommandRewriteResult rewritten(String newCommand) {
        return new CommandRewriteResult(Action.REWRITTEN, newCommand);
    }

    public static CommandRewriteResult blocked() {
        return new CommandRewriteResult(Action.BLOCKED, null);
    }

    public Action getAction() { return action; }
    public String getCommand() { return command; }
}
