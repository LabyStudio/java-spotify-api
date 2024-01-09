package de.labystudio.spotifyapi.platform.linux.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Java wrapper for the dbus-send application
 * <p>
 * The dbus-send command is used to send a message to a D-Bus message bus.
 * There are two well-known message buses:
 * - the systemwide message bus (installed on many systems as the "messagebus" service)
 * - the per-user-login-session message bus (started each time a user logs in).
 * <p>
 * The "system" parameter and "session" parameter options direct dbus-send to send messages to the system or session buses respectively.
 * If neither is specified, dbus-send sends to the session bus.
 * <p>
 * Nearly all uses of dbus-send must provide the "dest" parameter which is the name of
 * a connection on the bus to send the message to. If the "dest" parameter is omitted, no destination is set.
 * <p>
 * The object path and the name of the message to send must always be specified.
 * Following arguments, if any, are the message contents (message arguments).
 * These are given as type-specified values and may include containers (arrays, dicts, and variants).
 *
 * @author LabyStudio
 */
public class DBusSend {

    private static final Parameter PARAM_PRINT_REPLY = new Parameter("print-reply");
    private static final InterfaceMember INTERFACE_GET = new InterfaceMember("org.freedesktop.DBus.Properties.Get");

    private final Parameter[] parameters;
    private final String objectPath;
    private final Runtime runtime;

    /**
     * Creates a new DBusSend API for a specific application
     *
     * @param parameters The parameters to use
     * @param objectPath The object path to use
     */
    public DBusSend(Parameter[] parameters, String objectPath) {
        this.parameters = parameters;
        this.objectPath = objectPath;
        this.runtime = Runtime.getRuntime();
    }

    /**
     * Request an information from the application
     *
     * @param keys The requested type of information
     * @return The requested information
     * @throws Exception If the request failed
     */
    public Variant get(String... keys) throws Exception {
        String[] contents = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            contents[i] = String.format("string:%s", keys[i]);
        }
        return this.send(INTERFACE_GET, contents);
    }

    /**
     * Execute an DBusSend command.
     *
     * @param interfaceMember The interface member to execute
     * @param contents        The contents to send
     * @return The result of the command
     * @throws Exception If the command failed
     */
    public Variant send(InterfaceMember interfaceMember, String... contents) throws Exception {
        // Build arguments
        String[] arguments = new String[2 + this.parameters.length + 2 + contents.length];
        arguments[0] = "dbus-send";
        arguments[1] = PARAM_PRINT_REPLY.toString();
        for (int i = 0; i < this.parameters.length; i++) {
            arguments[2 + i] = this.parameters[i].toString();
        }
        arguments[2 + this.parameters.length] = this.objectPath;
        arguments[2 + this.parameters.length + 1] = interfaceMember.toString();
        for (int i = 0; i < contents.length; i++) {
            arguments[2 + this.parameters.length + 2 + i] = contents[i];
        }

        // Execute dbus-send process
        Process process = this.runtime.exec(arguments);
        int exitCode = process.waitFor();
        if (exitCode == 0) {
            // Read response
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String response;
            while ((response = reader.readLine()) != null) {
                if (response.startsWith("method ")) {
                    continue;
                }
                builder.append(response).append("\n");
            }
            if (builder.toString().isEmpty()) {
                return new Variant("success", true);
            }
            return Variant.parse(builder.toString());
        } else {
            // Handle error message
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            throw new Exception("dbus-send execution \"" + String.join(" ", arguments) + "\" failed with exit code " + exitCode + ": " + builder);
        }
    }

}
