/**
 * 
 */
package org.morganm.homespawnplus.server.api;

/**
 * @author morganm
 *
 */
public interface CommandSender {
    /**
     * Sends this sender a message
     *
     * @param message Message to be displayed
     */
    public void sendMessage(String message);

    /**
     * Sends this sender multiple messages
     *
     * @param messages An array of messages to be displayed
     */
    public void sendMessage(String[] messages);

    /**
     * Gets the name of this command sender
     *
     * @return Name of the sender
     */
    public String getName();
}
