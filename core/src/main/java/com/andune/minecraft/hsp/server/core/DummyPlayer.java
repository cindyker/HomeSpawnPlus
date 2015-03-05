package com.andune.minecraft.hsp.server.core;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.*;
import com.andune.minecraft.hsp.Permissions;
import com.andune.minecraft.hsp.server.api.Server;

/**
 * Dummy Player object that allows us to simulate a player for purposes of
 * strategy testing.
 *
 * @author andune
 */
public class DummyPlayer implements Player {
    private static final Logger log = LoggerFactory.getLogger(DummyPlayer.class);

    // For any messages to sent, they go to proxyCommandSender instead
    private final CommandSender proxyCommandSender;
    private final Server server;
    private boolean isNewPlayer = false;
    private String name="dummy";
    private Location location;
    private Location bedSpawnLocation;
    private Permissions perm;
    private boolean locked = false;
    private final long lastPlayed = System.currentTimeMillis();

    public DummyPlayer(CommandSender proxyCommandSender, Server server,
                       Permissions perm) {
        this.proxyCommandSender = proxyCommandSender;
        this.server = server;
        this.perm = perm;
    }

    /**
     * If set to true, playerState will be locked. This means changed to
     * location, name, isNewPlayer will all be refused.
     *
     * @param locked
     */
    public void setLockState(boolean locked) {
        this.locked = locked;
    }
    public boolean isLocked() { return locked; }

    @Override
    public void sendMessage(String message) {
        proxyCommandSender.sendMessage("%green%[proxyMsg "+getName()+"] " +
                server.getDefaultColor() + message);
    }

    @Override
    public void sendMessage(String[] messages) {
        if (messages != null && messages.length > 0)
            messages[0] = server.getDefaultColor() + messages[0];
        this.sendMessage(messages);
    }

    public void setIsNewPlayer(boolean isNewPlayer) {
        if (!locked)
            this.isNewPlayer = isNewPlayer;
    }
    @Override
    public boolean isNewPlayer() {
        return isNewPlayer;
    }

    public void setName(String name) {
        if (!locked)
            this.name = name;
    }
    @Override
    public String getName() {
        return name;
    }

    /**
     * Purposely not implemented for Dummy Players - this method should
     * never be used in strategy processing.
     *
     * @return
     */
    @Override
    public java.util.UUID getUUID() {
        return null;
    }

    public void setLocation(Location location) {
        if (!locked)
            this.location = location;
    }
    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public boolean hasPermission(String permission) {
        // we always return true for "hsp.admin.verbose" so Dummy Players
        // will get all verbose Admin log messages
        if (permission.equalsIgnoreCase("hsp.admin.verbose")) {
            return true;
        }
        else {
            return perm.hasPermission(this, permission);
        }
    }

    @Override
    public void setBedSpawnLocation(Location bedSpawnLocation) {
        if (!locked)
            this.bedSpawnLocation = bedSpawnLocation;
    }

    @Override
    public Location getBedSpawnLocation() {
        return bedSpawnLocation;
    }

    @Override
    public World getWorld() {
        if( getLocation() != null )
            return getLocation().getWorld();
        else
            return null;
    }

    @Override
    public void teleport(Location location) {
        if (!locked)
            this.location = location;
    }

    @Override
    public void setVelocity(Vector vector) {
        // do nothing
    }

    public boolean equals(Object o) {
        if( o == null )
            return false;
        if( !(o instanceof DummyPlayer) )
            return false;
        String name = ((DummyPlayer) o).getName();
        return getName().equals(name);
    }

    @Override
    public boolean isSneaking() {
        return false;
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public boolean hasPlayedBefore() {
        return true;
    }

    @Override
    public long getLastPlayed() {
        return lastPlayed;
    }

    @Override
    public String toString() {
        return "{DummyPlayer:"+getName()+"}";
    }
}
