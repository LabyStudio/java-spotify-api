package de.labystudio.spotifyapi.platform.osx.api;


/**
 * Wrapper for the AppleScript parameters.
 *
 * @author LabyStudio
 */
public class Action {

    public static final Action GET = new Action("get", "the");
    public static final Action OF = new Action("of");

    private final String action;

    public Action(String... args) {
        this.action = String.join(" ", args);
    }

    public Action(String argument) {
        this.action = argument;
    }

    public Action(Action... actions) {
        this.action = toString(actions);
    }

    @Override
    public String toString() {
        return this.action;
    }

    /**
     * Convert an array of actions to a string.
     *
     * @param actions The actions to convert
     * @return The string representation of the actions
     */
    public static String toString(Action... actions) {
        String[] args = new String[actions.length];
        for (int i = 0; i < actions.length; i++) {
            args[i] = actions[i].toString();
        }
        return String.join(" ", args);
    }
}
