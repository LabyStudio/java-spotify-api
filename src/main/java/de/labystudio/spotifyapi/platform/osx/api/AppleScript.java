package de.labystudio.spotifyapi.platform.osx.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static de.labystudio.spotifyapi.platform.osx.api.Action.GET;
import static de.labystudio.spotifyapi.platform.osx.api.Action.OF;

/**
 * Java wrapper for the AppleScript application
 *
 * @author LabyStudio
 */
public class AppleScript {

    private static final String GRAMMAR_FORMAT = "tell application \"%s\" to %s";

    private final String[] runtimeParameters = new String[]{
            "osascript", "-e", null
    };

    private final String application;
    private final Runtime runtime;

    /**
     * Creates a new AppleScript API for a specific application
     *
     * @param application The application name to talk to
     */
    public AppleScript(String application) {
        this.application = application;
        this.runtime = Runtime.getRuntime();
    }

    /**
     * Request an information from the application
     *
     * @param request The requested type of information
     * @param of      The category where the information belongs to
     * @return The requested information
     * @throws Exception If the request failed
     */
    public String getOf(Action request, Action of) throws Exception {
        return this.execute(GET, request, OF, of);
    }

    /**
     * Request an information from the application without a specific category
     *
     * @param request The requested type of information
     * @return The requested information
     * @throws Exception If the request failed
     */
    public String get(Action request) throws Exception {
        return this.execute(GET, request);
    }

    /**
     * Execute an AppleScript command.
     * <p>
     * It basically calls the AppleScript application with the following command:<br>
     * <code>osascript -e tell application "{@literal <}application{@literal >}" to {@literal <}action{@literal >}</code>
     *
     * @param actions The actions to execute
     * @return The result of the command
     * @throws Exception If the command failed
     */
    public String execute(Action... actions) throws Exception {
        // Update runtime parameters
        String action = Action.toString(actions);
        this.runtimeParameters[2] = String.format(GRAMMAR_FORMAT, this.application, action);

        // Execute AppleScript process
        Process process = this.runtime.exec(this.runtimeParameters);
        int exitCode = process.waitFor();
        if (exitCode == 0) {
            // Read response
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } else {
            // Handle error message
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            throw new Exception("AppleScript execution \"" + action + "\" failed with exit code " + exitCode + ": " + builder);
        }
    }

}
