package org.morganm.homespawnplus.server.api.command;


public interface CommandRegister {
    /** Register all known HSP commands. This includes those defined by
     * the admin in the config file as well as all commands found
     * automatically on the command path.
     * 
     */
    public void registerAllCommands();

}