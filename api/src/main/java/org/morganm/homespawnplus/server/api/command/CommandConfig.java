package org.morganm.homespawnplus.server.api.command;

import java.util.Map;
import java.util.Set;

public interface CommandConfig {
    /**
     *  Check if a command is disabled.
     * 
     * @param command the command name to check
     * @return true if the command is disabled
     */
    public boolean isDisabledCommand(String command);

    /** Return a list of all commands that have been defined and have
     * command parameters.
     * 
     * @return
     */
    public Set<String> getDefinedCommands();

    /** Return command parameters for a specific command.
     * 
     * @return guaranteed to not return null
     */
    public Map<String, Object> getCommandParameters(String command);

    /** This method does the heavy lifting of processing a configuration to load
     * the configuration state. An example config state:
     * 
     * commands:
     *   disabledCommands: [home, sethome]
     *   randomspawn:
     *     class: CustomEventCommand
     *     event: randomspawn
     * 
     * @param section
     */
    public void loadConfig();
}